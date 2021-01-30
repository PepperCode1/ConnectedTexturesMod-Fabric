package team.chisel.ctm.client.texture;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import java.util.EnumSet;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.texture.context.TextureContextPillar;
import team.chisel.ctm.client.texture.context.TextureContextPillar.ConnectionData;
import team.chisel.ctm.client.texture.context.TextureContextPillar.Connections;
import team.chisel.ctm.client.texture.type.TextureTypePillar;
import team.chisel.ctm.client.util.DirectionHelper;

public class TexturePillar extends AbstractTexture<TextureTypePillar> {
	public TexturePillar(TextureTypePillar type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);
		if (context instanceof TextureContextPillar) {
			transform(quad, (TextureContextPillar) context);
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

	private void transform(SpriteUnbakedQuad quad, TextureContextPillar context) {
		Direction nominalFace = quad.nominalFace;
		ConnectionData data = context.getData();
		Connections connections = data.getConnections();

		// This is the order of operations for connections
		EnumSet<Direction> realConnections = EnumSet.copyOf(connections.getConnections());
		if (connections.connectedOr(UP, DOWN)) {
			// If connected up or down, ignore all other connections
			realConnections.removeIf((direction) -> direction.getAxis().isHorizontal());
		} else if (connections.connectedOr(EAST, WEST)) {
			// If connected east or west, ignore any north/south connections, and any connections that are already connected up or down
			realConnections.removeIf((direction) -> direction == NORTH || direction == SOUTH);
			realConnections.removeIf((direction) -> blockConnectionZ(direction, data));
		} else {
			// Otherwise, remove every connection that is already connected to something else
			realConnections.removeIf((direction) -> blockConnectionY(direction, data));
		}

		// Replace our initial connection data with the new info
		connections = new Connections(realConnections);

		int rotation = 0;
		Submap submap = SubmapImpl.X2[0][0];
		if (nominalFace.getAxis().isHorizontal() && connections.connectedOr(UP, DOWN)) {
			submap = getSubmap(UP, DOWN, connections);
		} else if (connections.connectedOr(EAST, WEST)) {
			rotation = 3;
			submap = getSubmap(EAST, WEST, connections);
		} else if (connections.connectedOr(NORTH, SOUTH)) {
			submap = getSubmap(NORTH, SOUTH, connections);
			if (nominalFace == DOWN) {
				rotation += 2;
			}
		}

		boolean connected = !connections.getConnections().isEmpty();

		// Side textures need to be rotated to look correct
		if (connected && !connections.connectedOr(UP, DOWN)) {
			if (nominalFace == WEST) {
				rotation += 1;
			} else if (nominalFace == NORTH) {
				rotation += 2;
			} else if (nominalFace == EAST) {
				rotation += 3;
			}
		}

		// If there is a connection opposite this side, it is an end-cap, so render as unconnected
		if (connections.connected(nominalFace.getOpposite())) {
			connected = false;
		}
		// If there are no connections at all, and this is not the top or bottom, render the "short" column texture
		if (connections.getConnections().isEmpty() && nominalFace.getAxis().isHorizontal()) {
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

	private Submap getSubmap(Direction face1, Direction face2, Connections connections) {
		Submap submap;
		if (connections.connectedAnd(face1, face2)) {
			submap = SubmapImpl.X2[1][0];
		} else {
			if (connections.connected(face1)) {
				submap = SubmapImpl.X2[1][1];
			} else {
				submap = SubmapImpl.X2[0][1];
			}
		}
		return submap;
	}

	private boolean blockConnectionY(Direction direction, ConnectionData data) {
		return blockConnection(direction, Axis.Y, data) || blockConnection(direction, direction.rotateYClockwise().getAxis(), data);
	}

	private boolean blockConnectionZ(Direction direction, ConnectionData data) {
		return blockConnection(direction, Axis.Z, data);
	}

	private boolean blockConnection(Direction direction, Axis axis, ConnectionData data) {
		Direction rotated = DirectionHelper.rotateAround(direction, axis);
		return data.getConnections(direction).connectedOr(rotated, rotated.getOpposite());
	}
}
