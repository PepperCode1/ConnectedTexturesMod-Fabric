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
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;

public class TextureCTM extends AbstractConnectingTexture<TextureTypeCTM> {
	protected static final ConnectionDirection[][] DIRECTION_MAP = new ConnectionDirection[][] {
		{BOTTOM, LEFT, BOTTOM_LEFT},
		{BOTTOM, RIGHT, BOTTOM_RIGHT},
		{TOP, RIGHT, TOP_RIGHT},
		{TOP, LEFT, TOP_LEFT}
	};
	// Some hardcoded offset values for the different corner indeces.
	protected static final int[] QUADRANT_SUBMAP_OFFSETS = {4, 5, 1, 0};
	protected static final int[] DEFAULT_QUADRANT_SUBMAPS = {18, 19, 17, 16};

	public TextureCTM(TextureTypeCTM type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);

		if (CTMClient.getConfigManager().getConfig().disableCTM || !(context instanceof TextureContextConnecting)) {
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		int[] quadrantSubmapIds = getQuadrantSubmapIds(((TextureContextConnecting) context).getLogic(quad.nominalFace));
		SpriteUnbakedQuad[] quads = quad.toQuadrants();
		for (int i = 0; i < quads.length; i++) {
			if (quads[i] != null) {
				int quadrant = (i + 3) % 4;
				int quadrantSubmapId = quadrantSubmapIds[quadrant];

				int submapId = quadrantSubmapId / 2;
				submapId = submapId < 8 ? (((submapId < 4) ? 0 : 2) + submapId % 2) : 4;
				Submap submap;
				if (submapId == 4) {
					submap = SubmapImpl.X1;
				} else {
					submap = getX2Submap(submapId, quad.areUVsRotatedOnce());
				}

				quads[i].setUVBounds(sprites[quadrantSubmapId > 15 ? 0 : 1]);
				quads[i].applySubmap(submap);
			}
		}

		return new RenderableArray(quads);
	}

	protected int[] getQuadrantSubmapIds(ConnectionLogic logic) {
		int[] submapIds = new int[4];
		for (int quadrant = 0; quadrant < 4; quadrant++) {
			submapIds[quadrant] = getQuadrantSubmapId(logic, quadrant);
		}
		return submapIds;
	}

	protected int getQuadrantSubmapId(ConnectionLogic logic, int quadrant) {
		ConnectionDirection[] directions = DIRECTION_MAP[quadrant];
		if (logic.connectedOr(directions[0], directions[1])) {
			if (logic.connectedAnd(directions)) {
				// If all dirs are connected, we use the fully connected face,
				// the base offset value.
				return QUADRANT_SUBMAP_OFFSETS[quadrant];
			} else {
				// This is a bit magic-y, but basically the array is ordered so
				// the first dir requires an offset of 2, and the second dir
				// requires an offset of 8, plus the initial offset for the
				// corner.
				return QUADRANT_SUBMAP_OFFSETS[quadrant] + (logic.connected(directions[0]) ? 2 : 0) + (logic.connected(directions[1]) ? 8 : 0);
			}
		}
		return DEFAULT_QUADRANT_SUBMAPS[quadrant];
	}

	protected Submap getQuadrantSubmap(int id) {
		if (id < 16) {
			return SubmapImpl.X4[id / 4][id % 4];
		} else {
			return SubmapImpl.X2[(id-16) / 2][(id-16) % 2];
		}
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
