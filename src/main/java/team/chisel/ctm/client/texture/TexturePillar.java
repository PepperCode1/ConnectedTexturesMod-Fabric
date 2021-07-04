package team.chisel.ctm.client.texture;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;
import static net.minecraft.util.math.Direction.Axis.X;
import static net.minecraft.util.math.Direction.Axis.Y;
import static net.minecraft.util.math.Direction.Axis.Z;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextPillar;
import team.chisel.ctm.client.texture.type.TextureTypePillar;
import team.chisel.ctm.client.util.connection.SpacialConnectionLogic;

public class TexturePillar extends AbstractTexture<TextureTypePillar> {
	public TexturePillar(TextureTypePillar type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		UnbakedQuad quad = unbake(bakedQuad, cullFace);
		Direction lightFace = quad.lightFace;

		if (CTMClient.getConfigManager().getConfig().disableCTM || !(context instanceof TextureContextPillar) || !((TextureContextPillar) context).getLogic().hasConnections()) {
			if (lightFace.getAxis().isVertical()) {
				quad.setUVBounds(sprites[0]);
			} else {
				quad.setUVBounds(sprites[1]);
				quad.applySubmap(SubmapImpl.X2[0][0]);
			}
			return quad;
		}

		SpacialConnectionLogic logic = ((TextureContextPillar) context).getLogic();
		Axis connectionAxis = getConnectionAxis(logic);

		quad.rotateUVs(getRotation(connectionAxis, lightFace));

		if (lightFace.getAxis() == connectionAxis) {
			quad.setUVBounds(sprites[0]);
		} else {
			Submap submap = SubmapImpl.X2[0][0];
			if (connectionAxis == Y) {
				submap = getSubmap(logic, UP, DOWN);
			} else if (connectionAxis == X) {
				submap = getSubmap(logic, EAST, WEST);
			} else if (connectionAxis == Z) {
				submap = getSubmap(logic, NORTH, SOUTH); // Flipped on purpose
			}

			quad.setUVBounds(sprites[1]);
			quad.applySubmap(submap);
		}
		return quad;
	}

	private Axis getConnectionAxis(SpacialConnectionLogic logic) {
		if (logic.connectedOr(UP, DOWN)) {
			return Y;
		}
		if (logic.connectedOr(EAST, WEST)) {
			return X;
		}
		if (logic.connectedOr(SOUTH, NORTH)) {
			return Z;
		}
		return null;
	}

	private int getRotation(Axis connectionAxis, Direction lightFace) {
		int rotation = 0;
		if (connectionAxis != Y) {
			if (connectionAxis == X) {
				rotation = 3;
			} else if (connectionAxis == Z && lightFace == DOWN) {
				rotation = 2;
			}
			if (lightFace == WEST) {
				rotation += 1;
			} else if (lightFace == NORTH) {
				rotation += 2;
			} else if (lightFace == EAST) {
				rotation += 3;
			}
		}
		return rotation;
	}

	private Submap getSubmap(SpacialConnectionLogic logic, Direction direction1, Direction direction2) {
		boolean connected1 = logic.connected(direction1);
		boolean connected2 = logic.connected(direction2);
		if (connected1 && connected2) {
			return SubmapImpl.X2[1][0];
		}
		if (connected1) {
			return SubmapImpl.X2[1][1];
		}
		if (connected2) {
			return SubmapImpl.X2[0][1];
		}
		return SubmapImpl.X2[0][0];
	}
}
