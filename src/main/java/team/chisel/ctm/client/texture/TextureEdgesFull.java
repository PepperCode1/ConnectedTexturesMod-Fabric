package team.chisel.ctm.client.texture;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.texture.context.TextureContextConnectingObscured;
import team.chisel.ctm.client.texture.type.TextureTypeEdgesFull;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogicObscured;

public class TextureEdgesFull extends AbstractConnectingTexture<TextureTypeEdgesFull> {
	public TextureEdgesFull(TextureTypeEdgesFull type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);

		if (CTMClient.getConfigManager().getConfig().disableCTM || !(context instanceof TextureContextConnectingObscured)) {
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		ConnectionLogicObscured logic = ((TextureContextConnectingObscured) context).getLogic(quad.nominalFace);
		Sprite sprite;
		Submap submap = null;
		// Short circuit zero connections, as this is almost always the most common case
		if (!logic.isObscured() && !logic.hasConnections()) {
			sprite = sprites[0];
		} else {
			sprite = sprites[1];

			boolean top = logic.connected(ConnectionDirection.TOP) || logic.connectedAnd(ConnectionDirection.TOP_LEFT, ConnectionDirection.TOP_RIGHT);
			boolean right = logic.connected(ConnectionDirection.RIGHT) || logic.connectedAnd(ConnectionDirection.TOP_RIGHT, ConnectionDirection.BOTTOM_RIGHT);
			boolean bottom = logic.connected(ConnectionDirection.BOTTOM) || logic.connectedAnd(ConnectionDirection.BOTTOM_LEFT, ConnectionDirection.BOTTOM_RIGHT);
			boolean left = logic.connected(ConnectionDirection.LEFT) || logic.connectedAnd(ConnectionDirection.TOP_LEFT, ConnectionDirection.BOTTOM_LEFT);

			if (logic.isObscured() || (top && bottom) || (right && left)) {
				submap = SubmapImpl.X4[2][1];
			} else if (!(top || right || bottom || left) && logic.connectedAnd(ConnectionDirection.TOP_LEFT, ConnectionDirection.BOTTOM_RIGHT)) {
				submap = SubmapImpl.X4[0][1];
			} else if (!(top || right || bottom || left) && logic.connectedAnd(ConnectionDirection.TOP_RIGHT, ConnectionDirection.BOTTOM_LEFT)) {
				submap = SubmapImpl.X4[0][2];
			} else if (!(bottom || right) && logic.connectedOr(ConnectionDirection.LEFT, ConnectionDirection.BOTTOM_LEFT) && logic.connectedOr(ConnectionDirection.TOP, ConnectionDirection.TOP_RIGHT)) {
				submap = SubmapImpl.X4[0][3];
			} else if (!(bottom || left) && logic.connectedOr(ConnectionDirection.TOP, ConnectionDirection.TOP_LEFT) && logic.connectedOr(ConnectionDirection.RIGHT, ConnectionDirection.BOTTOM_RIGHT)) {
				submap = SubmapImpl.X4[1][3];
			} else if (!(top || left) && logic.connectedOr(ConnectionDirection.RIGHT, ConnectionDirection.TOP_RIGHT) && logic.connectedOr(ConnectionDirection.BOTTOM, ConnectionDirection.BOTTOM_LEFT)) {
				submap = SubmapImpl.X4[2][3];
			} else if (!(top || right) && logic.connectedOr(ConnectionDirection.BOTTOM, ConnectionDirection.BOTTOM_RIGHT) && logic.connectedOr(ConnectionDirection.LEFT, ConnectionDirection.TOP_LEFT)) {
				submap = SubmapImpl.X4[3][3];
			} else if (bottom) {
				submap = SubmapImpl.X4[1][1];
			} else if (right) {
				submap = SubmapImpl.X4[2][0];
			} else if (left) {
				submap = SubmapImpl.X4[2][2];
			} else if (top) {
				submap = SubmapImpl.X4[3][1];
			} else if (logic.connected(ConnectionDirection.BOTTOM_LEFT)) {
				submap = SubmapImpl.X4[1][2];
			} else if (logic.connected(ConnectionDirection.BOTTOM_RIGHT)) {
				submap = SubmapImpl.X4[1][0];
			} else if (logic.connected(ConnectionDirection.TOP_RIGHT)) {
				submap = SubmapImpl.X4[3][0];
			} else if (logic.connected(ConnectionDirection.TOP_LEFT)) {
				submap = SubmapImpl.X4[3][2];
			}

			if (!untransform) {
				quad.untransformUVs();
			}
		}

		quad.setUVBounds(sprite);
		if (submap != null) {
			quad.applySubmap(submap);
		}
		return quad;
	}
}
