package team.chisel.ctm.client.model;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.client.CTMTexture;

public interface CTMModelInfo {
	/**
	 * <b>The returned collection's iterator must always return the same amount of elements in the same order.</b>
	 *
	 * @return A collection of all textures this model info contains.
	 */
	Collection<CTMTexture<?>> getTextures();

	CTMTexture<?> getTexture(Identifier identifier);

	@Nullable
	default Sprite getOverrideSprite(int tintIndex) {
		return null;
	}

	@Nullable
	default CTMTexture<?> getOverrideTexture(int tintIndex, Identifier identifier) {
		return null;
	}
}
