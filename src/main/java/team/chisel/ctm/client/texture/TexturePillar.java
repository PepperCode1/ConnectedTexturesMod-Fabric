package team.chisel.ctm.client.texture;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.texture.context.TextureContextPillar;
import team.chisel.ctm.client.texture.type.TextureTypePillar;
import team.chisel.ctm.client.util.connection.SpacialConnectionLogic;

public class TexturePillar extends AbstractTexture<TextureTypePillar> {
	public TexturePillar(TextureTypePillar type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);
		if (context instanceof TextureContextPillar && !CTMClient.getConfigManager().getConfig().disableCTM) {
			transform(quad, ((TextureContextPillar) context).getLogic());
		} else {
			if (quad.nominalFace.getAxis().isVertical()) {
				quad.setUVBounds(sprites[0]);
			} else {
				quad.setUVBounds(sprites[1]);
				quad.applySubmap(SubmapImpl.X2[0][0]);
			}
		}
		return quad;
	}

	private void transform(SpriteUnbakedQuad quad, SpacialConnectionLogic logic) {
		Direction nominalFace = quad.nominalFace;

		int rotation = 0;
		Submap submap = SubmapImpl.X2[0][0];
		if (nominalFace.getAxis().isHorizontal() && logic.connectedOr(UP, DOWN)) {
			submap = getSubmap(UP, DOWN, logic);
		} else if (logic.connectedOr(EAST, WEST)) {
			submap = getSubmap(EAST, WEST, logic);
			rotation = 3;
		} else if (logic.connectedOr(NORTH, SOUTH)) {
			submap = getSubmap(NORTH, SOUTH, logic);
			if (nominalFace == DOWN) {
				rotation += 2;
			}
		}

		boolean connected = logic.hasConnections();

		// Side textures need to be rotated to look correct
		if (connected && !logic.connectedOr(UP, DOWN)) {
			if (nominalFace == WEST) {
				rotation += 1;
			} else if (nominalFace == NORTH) {
				rotation += 2;
			} else if (nominalFace == EAST) {
				rotation += 3;
			}
		}

		// If there is a connection opposite this side, it is an end-cap, so render as unconnected
		if (logic.connected(nominalFace.getOpposite())) {
			connected = false;
		}
		// If there are no connections at all, and this is not the top or bottom, render the "short" column texture
		if (!logic.hasConnections() && nominalFace.getAxis().isHorizontal()) {
			connected = true;
		}

		quad.rotateUVs(rotation);
		if (connected) {
			quad.setUVBounds(sprites[1]);
			quad.applySubmap(submap);
		} else {
			quad.setUVBounds(sprites[0]);
		}
	}

	private Submap getSubmap(Direction direction1, Direction direction2, SpacialConnectionLogic logic) {
		Submap submap;
		if (logic.connectedAnd(direction1, direction2)) {
			submap = SubmapImpl.X2[1][0];
		} else {
			if (logic.connected(direction1)) {
				submap = SubmapImpl.X2[1][1];
			} else {
				submap = SubmapImpl.X2[0][1];
			}
		}
		return submap;
	}
}
