package team.chisel.ctm.client.texture;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.RenderableList;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextCTM;
import team.chisel.ctm.client.texture.type.TextureTypeEdges;
import team.chisel.ctm.client.texture.type.TextureTypeEdges.CTMLogicEdges;

public class TextureEdges extends TextureCTM<TextureTypeEdges> {
	public TextureEdges(TextureTypeEdges type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);

		if (context == null || CTMClient.getConfigManager().getConfig().disableCTM) {
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		CTMLogicEdges logic = (CTMLogicEdges) ((TextureContextCTM) context).getCTM(bakedQuad.getFace());
		if (logic.isObscured()) {
			quad.setUVBounds(sprites[2]);
			return new RenderableList(List.of(quad.toQuadrants()));
		}

		return super.transformQuad(bakedQuad, context, quadGoal, cullFace);
	}
}
