package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;
import team.chisel.ctm.client.util.TextureUtil;

public class JsonCTMUnbakedModel implements UnbakedModel {
	private static final CTMOverrideReader OVERRIDE_READER = new CTMOverrideReader();

	private final JsonUnbakedModel parent;
	private final Int2ObjectMap<JsonElement> overrides;

	// Filled during constructor
	private Int2ObjectMap<SpriteIdentifier> identifierOverrides = new Int2ObjectArrayMap<>();
	private Int2ObjectMap<CTMMetadataSection> metadataOverrides = new Int2ObjectArrayMap<>();
	private Set<SpriteIdentifier> extraTextureDependencies = new HashSet<>();

	// Filled during getTextureDependencies
	private Set<SpriteIdentifier> textureDependencies;

	public JsonCTMUnbakedModel(JsonUnbakedModel parent, Int2ObjectMap<JsonElement> overrides) {
		this.parent = parent;
		this.overrides = overrides;

		for (Int2ObjectMap.Entry<JsonElement> entry : this.overrides.int2ObjectEntrySet()) {
			int tintIndex = entry.getIntKey();
			JsonElement element = entry.getValue();
			CTMMetadataSection metadata = null;
			try {
				if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
					Identifier identifier = new Identifier(element.getAsString());
					SpriteIdentifier spriteId = TextureUtil.toSpriteIdentifier(identifier);
					identifierOverrides.put(tintIndex, spriteId);
					extraTextureDependencies.add(spriteId);
					metadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(identifier));
				} else if (element.isJsonObject()) {
					JsonObject object = element.getAsJsonObject();
					if (!object.has("ctm_version")) {
						object.add("ctm_version", new JsonPrimitive(1));
					}
					OVERRIDE_READER.jsonModel = parent;
					metadata = OVERRIDE_READER.fromJson(object);

					int required = metadata.getType().requiredTextures();
					int provided = metadata.getAdditionalTextures().length + 1;
					if (required > provided) {
						CTMClient.LOGGER.error("Too few textures provided for override {} in model {}: TextureType {} requires {} textures, but {} were provided.", tintIndex, parent.id, metadata.getType(), required, provided);
					} else if (required < provided) {
						CTMClient.LOGGER.warn("Too many textures provided for override {} in model {}: TextureType {} requires {} textures, but {} were provided.", tintIndex, parent.id, metadata.getType(), required, provided);
					}
				}
			} catch (Exception e) {
				CTMClient.LOGGER.error("Error processing CTM override.", e);
			}
			if (metadata != null) {
				for (Identifier identifier : metadata.getAdditionalTextures()) {
					extraTextureDependencies.add(TextureUtil.toSpriteIdentifier(identifier));
				}
				metadataOverrides.put(tintIndex, metadata);
			}
		}
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return parent.getModelDependencies();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<com.mojang.datafixers.util.Pair<String, String>> unresolvedTextureReferences) {
		if (textureDependencies == null) {
			textureDependencies = new HashSet<>();
			textureDependencies.addAll(parent.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
			textureDependencies.addAll(extraTextureDependencies);
		}
		return textureDependencies;
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		Map<Identifier, CTMTexture<?>> textures = TextureUtil.initializeTextures(textureDependencies, spriteGetter);

		Int2ObjectMap<Sprite> spriteOverrides = new Int2ObjectArrayMap<>();
		for (Int2ObjectMap.Entry<SpriteIdentifier> entry : identifierOverrides.int2ObjectEntrySet()) {
			spriteOverrides.put(entry.getIntKey(), spriteGetter.apply(entry.getValue()));
		}

		Multimap<Integer, SpriteIdentifier> spriteIds = HashMultimap.create();
		for (ModelElement element : parent.getElements()) {
			for (ModelElementFace face : element.faces.values()) {
				spriteIds.put(face.tintIndex, parent.resolveSprite(face.textureId));
			}
		}

		Map<Pair<Integer, Identifier>, CTMTexture<?>> textureOverrides = new HashMap<>();
		for (Int2ObjectMap.Entry<CTMMetadataSection> entry : metadataOverrides.int2ObjectEntrySet()) {
			int tintIndex = entry.getIntKey();
			Sprite sprite = spriteOverrides.get(tintIndex);
			if (sprite == null) {
				for (SpriteIdentifier id : spriteIds.get(tintIndex)) {
					sprite = spriteGetter.apply(id);
					textureOverrides.put(Pair.of(tintIndex, sprite.getId()), TextureUtil.makeTexture(entry.getValue(), sprite, spriteGetter));
				}
			} else {
				textureOverrides.put(Pair.of(tintIndex, sprite.getId()), TextureUtil.makeTexture(entry.getValue(), sprite, spriteGetter));
			}
		}

		return new CTMBakedModel(parent.bake(loader, spriteGetter, rotationContainer, modelId), new JsonCTMModelInfo(textures, spriteOverrides, textureOverrides));
	}

	private static class JsonCTMModelInfo implements CTMModelInfo {
		private final List<CTMTexture<?>> allTextures;
		private final Map<Identifier, CTMTexture<?>> textures;
		private final Int2ObjectMap<Sprite> spriteOverrides;
		private final Map<Pair<Integer, Identifier>, CTMTexture<?>> textureOverrides;

		private JsonCTMModelInfo(Map<Identifier, CTMTexture<?>> textures, Int2ObjectMap<Sprite> spriteOverrides, Map<Pair<Integer, Identifier>, CTMTexture<?>> textureOverrides) {
			this.textures = textures;
			this.spriteOverrides = spriteOverrides;
			this.textureOverrides = textureOverrides;
			allTextures = ImmutableList.<CTMTexture<?>>builder()
					.addAll(this.textures.values())
					.addAll(this.textureOverrides.values())
					.build();
		}

		@Override
		public Collection<CTMTexture<?>> getTextures() {
			return allTextures;
		}

		@Override
		public CTMTexture<?> getTexture(Identifier identifier) {
			return textures.get(identifier);
		}

		@Override
		public @Nullable Sprite getOverrideSprite(int tintIndex) {
			return spriteOverrides.get(tintIndex);
		}

		@Override
		public @Nullable CTMTexture<?> getOverrideTexture(int tintIndex, Identifier identifier) {
			return textureOverrides.get(Pair.of(tintIndex, identifier));
		}
	}

	private static class CTMOverrideReader extends CTMMetadataReader {
		private JsonUnbakedModel jsonModel;

		@Override
		public Identifier makeIdentifier(String string) {
			if (TextureUtil.isTextureReference(string)) {
				return jsonModel.resolveSprite(string).getTextureId();
			}
			return super.makeIdentifier(string);
		}
	}
}
