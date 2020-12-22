package team.chisel.ctm.client.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.model.CTMUnbakedModel;
import team.chisel.ctm.api.texture.CTMMetadataSection;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.mixin.JsonUnbakedModelAccessor;
import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.texture.TextureNormal;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;
import team.chisel.ctm.client.util.ResourceUtil;

public class CTMUnbakedModelImpl implements CTMUnbakedModel {
	//private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

	private final UnbakedModel parent;
	private final @Nullable JsonUnbakedModel jsonParent;

	// Populated from overrides data during construction
	private final Int2ObjectMap<JsonElement> overrides;
	protected final Int2ObjectMap<CTMMetadataSection> metaOverrides = new Int2ObjectArrayMap<>();

	// Populated during bake with real texture data
	protected Int2ObjectMap<Sprite> spriteOverrides;
	protected Map<Pair<Integer, Identifier>, CTMTexture<?>> textureOverrides;

	private final Collection<Identifier> textureDependencies;

	private Map<Identifier, CTMTexture<?>> textures = new HashMap<>();

	public CTMUnbakedModelImpl(UnbakedModel parent) {
		this.parent = parent;
		this.jsonParent = null;
		this.overrides = new Int2ObjectOpenHashMap<>();
		this.textureDependencies = new HashSet<>();
	}

	public CTMUnbakedModelImpl(JsonUnbakedModel parent, Int2ObjectMap<JsonElement> overrides) throws IOException {
		this.parent = parent;
		this.jsonParent = parent;
		this.overrides = overrides;
		this.textureDependencies = new HashSet<>();
		for (Int2ObjectMap.Entry<JsonElement> entry : this.overrides.int2ObjectEntrySet()) {
			CTMMetadataSection metadata = null;
			if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
				Identifier rl = new Identifier(entry.getValue().getAsString());
				metadata = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(rl));
				textureDependencies.add(rl);
			} else if (entry.getValue().isJsonObject()) {
				JsonObject obj = entry.getValue().getAsJsonObject();
				if (!obj.has("ctm_version")) {
					// This model can only be version 1, TODO improve this
					obj.add("ctm_version", new JsonPrimitive(1));
				}
				metadata = new CTMMetadataReader().fromJson(obj);
			}
			if (metadata != null) {
				metaOverrides.put(entry.getIntKey(), metadata);
				textureDependencies.addAll(Arrays.asList(metadata.getAdditionalTextures()));
			}
		}

		this.textureDependencies.removeIf(rl -> rl.getPath().startsWith("#"));
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		List<SpriteIdentifier> ret = textureDependencies.stream()
				.map(rl -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, rl))
				.collect(Collectors.toList());
		ret.addAll(parent.getTextureDependencies(modelGetter, missingTextureErrors));
		// Validate all texture metadata
		for (SpriteIdentifier tex : ret) {
			CTMMetadataSection meta;
			try {
				meta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(tex.getTextureId()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (meta != null) {
				if (meta.getType().requiredTextures() != meta.getAdditionalTextures().length + 1) {
					throw new IllegalArgumentException(String.format("Texture type %s requires exactly %d textures. %d were provided.", meta.getType(), meta.getType().requiredTextures(), meta.getAdditionalTextures().length + 1));
				}
			}
		}
		return ret;
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings settings, Identifier modelLocation) {
		BakedModel bakedParent;
		//if (jsonUnbakedModel != null && jsonUnbakedModel.getRootModel() == ModelLoader.GENERATION_MARKER) { // Apply same special case that ModelLoader does
		//	return ITEM_MODEL_GENERATOR.create(spriteGetter, jsonUnbakedModel).bake(loader, jsonUnbakedModel, spriteGetter, settings, modelLocation, false);
		//} else if (unbakedModel instanceof JsonUnbakedModel && ((JsonUnbakedModel) unbakedModel).getRootModel() == ModelLoader.GENERATION_MARKER) {
		//	return ITEM_MODEL_GENERATOR.create(spriteGetter, ((JsonUnbakedModel) unbakedModel)).bake(loader, ((JsonUnbakedModel) unbakedModel), spriteGetter, settings, modelLocation, false);
		//} else {
		bakedParent = parent.bake(loader, spriteGetter, settings, modelLocation);
		//}
		initializeTextures(loader, spriteGetter);
		return new CTMBakedModel(this, bakedParent);
	}

	public void initializeTextures(ModelLoader loader, Function<SpriteIdentifier, Sprite> spriteGetter) {
		for (SpriteIdentifier m : getTextureDependencies(loader::getOrLoadModel, new HashSet<>())) {
			Sprite sprite = spriteGetter.apply(m);
			CTMMetadataSection metadata = null;
			try {
				metadata = ResourceUtil.getMetadata(sprite);
			} catch (IOException e) {
				//
			}
			final CTMMetadataSection metadata1 = metadata;
			textures.computeIfAbsent(sprite.getId(), s -> {
				CTMTexture<?> texture;
				if (metadata1 == null) {
					texture = new TextureNormal(TextureTypeNormal.INSTANCE, new TextureInfo(new Sprite[] { sprite }, Optional.empty(), null));
				} else {
					texture = metadata1.makeTexture(sprite, spriteGetter);
				}
				return texture;
			});
		}
		if (spriteOverrides == null) {
			spriteOverrides = new Int2ObjectArrayMap<>();
			// Convert all primitive values into sprites
			for (Int2ObjectMap.Entry<JsonElement> e : overrides.int2ObjectEntrySet()) {
				if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
					Sprite sprite = spriteGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(e.getValue().getAsString())));
					spriteOverrides.put(e.getIntKey(), sprite);
				}
			}
		}
		if (textureOverrides == null) {
			textureOverrides = new HashMap<>();
			for (Int2ObjectMap.Entry<CTMMetadataSection> e : metaOverrides.int2ObjectEntrySet()) {
				List<ModelElementFace> matches = jsonParent.getElements().stream().flatMap(b -> b.faces.values().stream()).filter(b -> b.tintIndex == e.getIntKey()).collect(Collectors.toList());
				Multimap<SpriteIdentifier, ModelElementFace> bySprite = HashMultimap.create();
				// TODO 1.15 this isn't right
				matches.forEach(part -> bySprite.put(((JsonUnbakedModelAccessor) jsonParent).getTextureMap().getOrDefault(part.textureId.substring(1), Either.right(part.textureId)).left().get(), part));
				for (Map.Entry<SpriteIdentifier, Collection<ModelElementFace>> e2 : bySprite.asMap().entrySet()) {
					Identifier texLoc = e2.getKey().getTextureId();
					Sprite sprite = getOverrideSprite(e.getIntKey());
					if (sprite == null) {
						sprite = spriteGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, texLoc));
					}
					CTMTexture<?> tex = e.getValue().makeTexture(sprite, spriteGetter);
					textureOverrides.put(Pair.of(e.getIntKey(), texLoc), tex);
				}
			}
		}
	}

	@Override
	public Collection<CTMTexture<?>> getCTMTextures() {
		return ImmutableList.<CTMTexture<?>>builder().addAll(textures.values()).addAll(textureOverrides.values()).build();
	}

	@Override
	public CTMTexture<?> getTexture(Identifier id) {
		return textures.get(id);
	}

	@Override
	@Nullable
	public Sprite getOverrideSprite(int tintIndex) {
		return spriteOverrides.get(tintIndex);
	}

	@Override
	@Nullable
	public CTMTexture<?> getOverrideTexture(int tintIndex, Identifier id) {
		return textureOverrides.get(Pair.of(tintIndex, id));
	}
}
