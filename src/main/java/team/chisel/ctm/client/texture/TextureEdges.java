package team.chisel.ctm.client.texture;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextConnectingObscured;
import team.chisel.ctm.client.texture.type.TextureTypeEdges;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.client.util.connection.ConnectionLogicObscured;

public class TextureEdges extends TextureCTM {
	public TextureEdges(TextureTypeEdges type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, TextureContext context, int quadGoal, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);

		if (CTMClient.getConfigManager().getConfig().disableCTM || !(context instanceof TextureContextConnectingObscured)) {
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		ConnectionLogicObscured logic = ((TextureContextConnectingObscured) context).getLogic(bakedQuad.getFace());
		if (logic.isObscured()) {
			quad.setUVBounds(sprites[2]);
			return quad;
		}

		return super.transformQuad(bakedQuad, context, quadGoal, cullFace);
	}

	@Override
	protected int getQuadrantSubmapId(ConnectionLogic logic, int quadrant) {
		ConnectionDirection[] directions = DIRECTION_MAP[quadrant];
		if (!logic.connectedOr(directions[0], directions[1]) && logic.connected(directions[2])) {
			return QUADRANT_SUBMAP_OFFSETS[quadrant];
		}
		return super.getQuadrantSubmapId(logic, quadrant);
	}
}
