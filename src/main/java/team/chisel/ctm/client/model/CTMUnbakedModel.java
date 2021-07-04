package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.client.util.TextureUtil;

public class CTMUnbakedModel implements UnbakedModel {
	private final UnbakedModel parent;

	// Filled during getTextureDependencies
	private Set<SpriteIdentifier> textureDependencies;

	public CTMUnbakedModel(UnbakedModel parent) {
		this.parent = parent;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return parent.getModelDependencies();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		if (textureDependencies == null) {
			textureDependencies = new HashSet<>();
			textureDependencies.addAll(parent.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
		}
		return textureDependencies;
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		Map<Identifier, CTMTexture<?>> textures = TextureUtil.initializeTextures(textureDependencies, textureGetter);
		return new CTMBakedModel(parent.bake(loader, textureGetter, rotationContainer, modelId), new CTMModelInfoImpl(textures));
	}

	private static class CTMModelInfoImpl implements CTMModelInfo {
		private final Set<CTMTexture<?>> allTextures;
		private final Map<Identifier, CTMTexture<?>> textures;

		private CTMModelInfoImpl(Map<Identifier, CTMTexture<?>> textures) {
			this.textures = textures;
			allTextures = ImmutableSet.copyOf(this.textures.values());
		}

		@Override
		public Collection<CTMTexture<?>> getTextures() {
			return allTextures;
		}

		@Override
		public CTMTexture<?> getTexture(Identifier identifier) {
			return textures.get(identifier);
		}
	}
}
