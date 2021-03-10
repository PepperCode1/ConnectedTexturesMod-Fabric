package team.chisel.ctm.client.texture;

import static team.chisel.ctm.client.util.connection.ConnectionDirection.BOTTOM;
import static team.chisel.ctm.client.util.connection.ConnectionDirection.BOTTOM_LEFT;
import static team.chisel.ctm.client.util.connection.ConnectionDirection.BOTTOM_RIGHT;
import static team.chisel.ctm.client.util.connection.ConnectionDirection.LEFT;
import static team.chisel.ctm.client.util.connection.ConnectionDirection.RIGHT;
import static team.chisel.ctm.client.util.connection.ConnectionDirection.TOP;
import static team.chisel.ctm.client.util.connection.ConnectionDirection.TOP_LEFT;
import static team.chisel.ctm.client.util.connection.ConnectionDirection.TOP_RIGHT;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.RenderableArray;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;

public class TextureCTM extends AbstractConnectingTexture<TextureTypeCTM> {
	protected static final ConnectionDirection[][] DIRECTION_MAP = new ConnectionDirection[][] {
		{TOP, LEFT, TOP_LEFT},
		{BOTTOM, LEFT, BOTTOM_LEFT},
		{BOTTOM, RIGHT, BOTTOM_RIGHT},
		{TOP, RIGHT, TOP_RIGHT}
	};

	public TextureCTM(TextureTypeCTM type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		UnbakedQuad quad = unbake(bakedQuad, cullFace);

		if (CTMClient.getConfigManager().getConfig().disableCTM || !(context instanceof TextureContextConnecting)) {
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		ConnectionLogic logic = ((TextureContextConnecting) context).getLogic(quad.lightFace);
		UnbakedQuad[] quads = quad.toQuadrants();
		for (int i = 0; i < quads.length; i++) {
			if (quads[i] != null) {
				int id = getSubmapId(logic, i);
				if (id != -1) {
					quads[i].setUVBounds(sprites[1]);
					quads[i].applySubmap(getX2Submap(id, quad.areUVsRotatedOnce()));
				} else {
					quads[i].setUVBounds(sprites[0]);
				}
			}
		}

		return new RenderableArray(quads);
	}

	protected int getSubmapId(ConnectionLogic logic, int quadrant) {
		ConnectionDirection[] directions = DIRECTION_MAP[quadrant];
		boolean connected1 = logic.connected(directions[0]);
		boolean connected2 = logic.connected(directions[1]);
		if (connected1 && connected2) {
			if (logic.connected(directions[2])) {
				return 0;
			}
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

	public static Submap getX2Submap(int id, boolean rotate) {
		if (rotate) {
			if (id == 1) {
				id = 2;
			} else if (id == 2) {
				id = 1;
			}
		}
		return SubmapImpl.getX2Submap(id);
	}
}
