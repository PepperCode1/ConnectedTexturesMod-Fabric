package team.chisel.ctm.api.texture;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface CTMTexture<T extends TextureType> {
	/**
	 * Transforms a BakedQuad.
	 * @param bakedQuad The BakedQuad.
	 * @param context The Context. <b>If this is null, the model which is currently being built is an item model.</b>
	 * @param quadGoal The amount of quads that should be rendered by the returned Renderable.
	 * @param cullFace The cull face. This is not the same as the BakedQuad's face.
	 * @return A Renderable.
	 */
	Renderable transformQuad(BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace);

	Collection<Identifier> getTextures();
	
	/**
	 * Gets the TextureType of this texture.
	 * @return The TextureType of this texture.
	 */
	T getType();

	/**
	 * Gets the sprite for the particle
	 * @return The sprite for the particle.
	 */
	Sprite getParticle();

	/**
	 * The BlendMode with which this texture will be rendered.
	 * @return The BlendMode of this texture.
	 */
	@Nullable
	BlendMode getBlendMode();
}
