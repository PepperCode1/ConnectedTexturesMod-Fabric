package team.chisel.ctm.client.texture;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.UnbakedQuad;
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
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		if (CTMClient.getConfigManager().getConfig().disableCTM || !(context instanceof TextureContextConnectingObscured)) {
			UnbakedQuad quad = unbake(bakedQuad, cullFace);
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		ConnectionLogicObscured logic = ((TextureContextConnectingObscured) context).getLogic(bakedQuad.getFace());
		if (logic.isObscured()) {
			UnbakedQuad quad = unbake(bakedQuad, cullFace);
			quad.setUVBounds(sprites[2]);
			return quad;
		}

		return super.transformQuad(bakedQuad, cullFace, context);
	}

	@Override
	protected int getSubmapId(ConnectionLogic logic, int quadrant) {
		ConnectionDirection[] directions = DIRECTION_MAP[quadrant];
		boolean connected1 = logic.connected(directions[0]);
		boolean connected2 = logic.connected(directions[1]);
		if (logic.connected(directions[2]) && ((connected1 && connected2) || (!connected1 && !connected2))) {
			return 0;
		}
		if (connected1 && connected2) {
			return 3;
		}
		if (connected1) {
			return 1;
		}
		if (connected2) {
			return 2;
		}
		return -1;
	}
}
