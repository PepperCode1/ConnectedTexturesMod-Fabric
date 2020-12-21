package team.chisel.ctm.client.texture;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.RenderableList;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

/**
 * CTM texture for a normal texture
 */
public class TextureNormal extends AbstractTexture<TextureTypeNormal> {
	public TextureNormal(TextureTypeNormal type, TextureInfo info){
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);
		quad.setUVBounds(sprites[0]);
		if (quadGoal == 4) {
			return new RenderableList(List.of(quad.toQuadrants()));
		}
		return quad;
	}
}
