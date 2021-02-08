package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
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
import net.minecraft.util.InvalidIdentifierException;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;
import team.chisel.ctm.client.util.TextureUtil;

public class JsonCTMUnbakedModel implements UnbakedModel {
	private final JsonUnbakedModel parent;
	private final Int2ObjectMap<JsonElement> overrides;

	// filled during constructor
	private Int2ObjectMap<SpriteIdentifier> identifierOverrides = new Int2ObjectArrayMap<>();
	private Set<SpriteIdentifier> extraTextureDependencies = new HashSet<>();
	private Int2ObjectMap<CTMMetadataSection> metadataOverrides = new Int2ObjectArrayMap<>();

	// filled during getTextureDependencies
	private Set<SpriteIdentifier> textureDependencies;

	// filled during bake
	private Map<Identifier, CTMTexture<?>> textures = new HashMap<>();
	private Int2ObjectMap<Sprite> spriteOverrides = new Int2ObjectArrayMap<>();
	private Map<Pair<Integer, Identifier>, CTMTexture<?>> textureOverrides = new HashMap<>();

	public JsonCTMUnbakedModel(JsonUnbakedModel parent, Int2ObjectMap<JsonElement> overrides) {
		this.parent = parent;
		this.overrides = overrides;

		// TODO: add support for references
		// if a texture location in the json is a reference (starts with a "#"), the Identifier constructor will throw an exception
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
					metadata = CTMMetadataReader.INSTANCE.fromJson(element.getAsJsonObject());
				}
			} catch (InvalidIdentifierException e) {
				CTMClient.LOGGER.error("Error processing CTM override: invalid identifier.", e);
			} catch (JsonParseException e) {
				CTMClient.LOGGER.error("Error processing CTM override: invalid JSON.", e);
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
		return Collections.emptyList();
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
		TextureUtil.initializeTextures(textureDependencies, textures, spriteGetter);

		for (Int2ObjectMap.Entry<SpriteIdentifier> entry : identifierOverrides.int2ObjectEntrySet()) {
			spriteOverrides.put(entry.getIntKey(), spriteGetter.apply(entry.getValue()));
		}

		Multimap<Integer, SpriteIdentifier> spriteIds = HashMultimap.create();
		for (ModelElement element : parent.getElements()) {
			for (ModelElementFace face : element.faces.values()) {
				spriteIds.put(face.tintIndex, parent.resolveSprite(face.textureId));
			}
		}
		for (Int2ObjectMap.Entry<CTMMetadataSection> entry : metadataOverrides.int2ObjectEntrySet()) {
			int tintIndex = entry.getIntKey();
			for (SpriteIdentifier id : spriteIds.get(tintIndex)) {
				Sprite sprite = spriteOverrides.get(tintIndex);
				if (sprite == null) {
					sprite = spriteGetter.apply(id);
				}
				textureOverrides.put(Pair.of(tintIndex, sprite.getId()), TextureUtil.makeTexture(entry.getValue(), sprite, spriteGetter));
			}
		}

		return new CTMBakedModel(parent.bake(loader, spriteGetter, rotationContainer, modelId), new JsonCTMModelInfo(this));
	}

	private static class JsonCTMModelInfo implements CTMModelInfo {
		private final Collection<CTMTexture<?>> allTextures;
		private final Map<Identifier, CTMTexture<?>> textures;
		private final Int2ObjectMap<Sprite> spriteOverrides;
		private final Map<Pair<Integer, Identifier>, CTMTexture<?>> textureOverrides;

		JsonCTMModelInfo(JsonCTMUnbakedModel unbakedModel) {
			textures = unbakedModel.textures;
			spriteOverrides = unbakedModel.spriteOverrides;
			textureOverrides = unbakedModel.textureOverrides;
			allTextures = ImmutableList.<CTMTexture<?>>builder()
					.addAll(textures.values())
					.addAll(textureOverrides.values())
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
}
