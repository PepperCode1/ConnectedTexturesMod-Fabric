package team.chisel.ctm.api.client;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

// TODO: Remove generic argument?
public interface CTMTexture<T extends TextureType> {
	/**
	 * Transforms a BakedQuad.
	 *
	 * @param bakedQuad The BakedQuad.
	 * @param cullFace The cull face. This is not the same as the BakedQuad's face.
	 * @param context The context. <b>If this is null, the mesh that is currently being built is for an item model.</b>
	 * @return A Renderable.
	 */
	Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, @Nullable TextureContext context);

	Collection<Identifier> getTextures();

	/**
	 * Gets the TextureType of this texture.
	 *
	 * @return The TextureType of this texture.
	 */
	T getType();

	/**
	 * Gets the sprite for the particle.
	 *
	 * @return The sprite for the particle.
	 */
	Sprite getParticle();
}
