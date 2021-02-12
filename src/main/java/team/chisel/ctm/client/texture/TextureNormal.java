package team.chisel.ctm.client.texture;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;
import team.chisel.ctm.client.util.RenderUtil;

/**
 * CTM texture for a normal texture.
 */
public class TextureNormal extends AbstractTexture<TextureTypeNormal> {
	public TextureNormal(TextureTypeNormal type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		return (emitter) -> {
			emitter.fromVanilla(bakedQuad, material, cullFace);
			if (hasLight) {
				int lightmap = RenderUtil.getLightmap(skyLight, blockLight);
				for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
					emitter.lightmap(vertexIndex, lightmap);
				}
			}
			emitter.emit();
		};
	}
}
