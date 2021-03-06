package team.chisel.ctm.client.texture;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

/**
 * CTM texture for a normal texture.
 */
public class TextureNormal extends AbstractTexture<TextureTypeNormal> {
	public TextureNormal(TextureTypeNormal type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		UnbakedQuad quad = unbake(bakedQuad, cullFace);
		quad.setUVBounds(sprites[0]);
		return quad;
	}
}
