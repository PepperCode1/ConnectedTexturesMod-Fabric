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

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.mixin.JsonUnbakedModelAccessor;
import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.resource.CTMMetadataSection;
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

	private final Collection<Identifier> textureDependencies = new HashSet<>();

	private Map<Identifier, CTMTexture<?>> textures = new HashMap<>();

	public CTMUnbakedModelImpl(UnbakedModel parent) {
		this.parent = parent;
		jsonParent = null;
		overrides = new Int2ObjectOpenHashMap<>();
	}

	public CTMUnbakedModelImpl(JsonUnbakedModel parent, Int2ObjectMap<JsonElement> overrides) throws IOException {
		this.parent = parent;
		jsonParent = parent;
		this.overrides = overrides;

		for (Int2ObjectMap.Entry<JsonElement> entry : this.overrides.int2ObjectEntrySet()) {
			CTMMetadataSection metadata = null;
			if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
				Identifier identifier = new Identifier(entry.getValue().getAsString());
				metadata = ResourceUtil.getMetadata(ResourceUtil.toTextureIdentifier(identifier));
				textureDependencies.add(identifier);
			} else if (entry.getValue().isJsonObject()) {
				JsonObject jsonObject = entry.getValue().getAsJsonObject();
				if (!jsonObject.has("ctm_version")) {
					// This model can only be version 1, TODO improve this
					jsonObject.add("ctm_version", new JsonPrimitive(1));
				}
				metadata = CTMMetadataReader.INSTANCE.fromJson(jsonObject);
			}
			if (metadata != null) {
				metaOverrides.put(entry.getIntKey(), metadata);
				textureDependencies.addAll(Arrays.asList(metadata.getAdditionalTextures()));
			}
		}

		textureDependencies.removeIf((identifier) -> identifier.getPath().startsWith("#"));
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> modelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		List<SpriteIdentifier> identifiers = textureDependencies.stream()
				.map((identifier) -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, identifier))
				.collect(Collectors.toList());
		identifiers.addAll(parent.getTextureDependencies(modelGetter, unresolvedTextureReferences));
		return identifiers;
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings settings, Identifier identifier) {
		BakedModel bakedParent;
		//if (jsonParent != null && jsonParent.getRootModel() == ModelLoader.GENERATION_MARKER) { // Apply same special case that ModelLoader does
		//	return ITEM_MODEL_GENERATOR.create(spriteGetter, jsonParent).bake(loader, jsonParent, spriteGetter, settings, identifier, false);
		//} else if (parent instanceof JsonUnbakedModel && ((JsonUnbakedModel) parent).getRootModel() == ModelLoader.GENERATION_MARKER) {
		//	return ITEM_MODEL_GENERATOR.create(spriteGetter, ((JsonUnbakedModel) parent)).bake(loader, ((JsonUnbakedModel) parent), spriteGetter, settings, identifier, false);
		//} else {
		bakedParent = parent.bake(loader, spriteGetter, settings, identifier);
		//}
		initializeTextures(loader, spriteGetter);
		return new CTMBakedModel(this, bakedParent);
	}

	public void initializeTextures(ModelLoader loader, Function<SpriteIdentifier, Sprite> spriteGetter) {
		for (SpriteIdentifier identifier : getTextureDependencies(loader::getOrLoadModel, new HashSet<>())) {
			Sprite sprite = spriteGetter.apply(identifier);
			CTMMetadataSection metadata = null;
			try {
				metadata = ResourceUtil.getMetadata(sprite);
			} catch (IOException e) {
				//
			}
			final CTMMetadataSection metadata1 = metadata;
			textures.computeIfAbsent(sprite.getId(), (id) -> {
				CTMTexture<?> texture;
				if (metadata1 == null) {
					texture = TextureTypeNormal.INSTANCE.makeTexture(new TextureInfo(new Sprite[] { sprite }, null, Optional.empty()));
				} else {
					texture = metadata1.makeTexture(sprite, spriteGetter);
				}
				return texture;
			});
		}
		if (spriteOverrides == null) {
			spriteOverrides = new Int2ObjectArrayMap<>();
			// Convert all primitive values into sprites
			for (Int2ObjectMap.Entry<JsonElement> entry : overrides.int2ObjectEntrySet()) {
				if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
					Sprite sprite = spriteGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(entry.getValue().getAsString())));
					spriteOverrides.put(entry.getIntKey(), sprite);
				}
			}
		}
		if (textureOverrides == null) {
			textureOverrides = new HashMap<>();
			for (Int2ObjectMap.Entry<CTMMetadataSection> entry : metaOverrides.int2ObjectEntrySet()) {
				List<ModelElementFace> matches = jsonParent.getElements().stream().flatMap((element) -> element.faces.values().stream()).filter((face) -> face.tintIndex == entry.getIntKey()).collect(Collectors.toList());
				Multimap<SpriteIdentifier, ModelElementFace> bySprite = HashMultimap.create();
				// TODO 1.15 this isn't right
				matches.forEach((part) -> bySprite.put(((JsonUnbakedModelAccessor) jsonParent).getTextureMap().getOrDefault(part.textureId.substring(1), Either.right(part.textureId)).left().get(), part));
				for (Map.Entry<SpriteIdentifier, Collection<ModelElementFace>> entry1 : bySprite.asMap().entrySet()) {
					Identifier textureId = entry1.getKey().getTextureId();
					Sprite sprite = getOverrideSprite(entry.getIntKey());
					if (sprite == null) {
						sprite = spriteGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, textureId));
					}
					CTMTexture<?> texture = entry.getValue().makeTexture(sprite, spriteGetter);
					textureOverrides.put(Pair.of(entry.getIntKey(), textureId), texture);
				}
			}
		}
	}

	@Override
	public Collection<CTMTexture<?>> getCTMTextures() {
		return ImmutableList.<CTMTexture<?>>builder()
				.addAll(textures.values())
				.addAll(textureOverrides.values())
				.build();
	}

	@Override
	public CTMTexture<?> getTexture(Identifier identifier) {
		return textures.get(identifier);
	}

	@Override
	@Nullable
	public Sprite getOverrideSprite(int colorIndex) {
		return spriteOverrides.get(colorIndex);
	}

	@Override
	@Nullable
	public CTMTexture<?> getOverrideTexture(int colorIndex, Identifier identifier) {
		return textureOverrides.get(Pair.of(colorIndex, identifier));
	}
}
