package team.chisel.ctm.client.texture;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.Submap;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
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
	public Renderable transformQuad(BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
		if (context == null) {
			SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);
			if (bakedQuad.getFace() != null && bakedQuad.getFace().getAxis().isVertical()) {
				quad.setUVBounds(sprites[0]);
			} else {
				quad.setUVBounds(sprites[1]);
				quad.applySubmap(SubmapImpl.X2[0][0]);
			}
			return quad;
		}
		return getQuad(bakedQuad, context, cullFace);
	}

	private UnbakedQuad getQuad(BakedQuad bakedQuad, TextureContext context, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);
		Direction nominalFace = bakedQuad.getFace();
		ConnectionData data = ((TextureContextPillar) context).getData();
		Connections cons = data.getConnections();
		
		// This is the order of operations for connections
		EnumSet<Direction> realConnections = EnumSet.copyOf(data.getConnections().getConnections());
		if (cons.connectedOr(UP, DOWN)) {
			// If connected up or down, ignore all other connections
			realConnections.removeIf(f -> f.getAxis().isHorizontal());
		} else if (cons.connectedOr(EAST, WEST)) {
			// If connected east or west, ignore any north/south connections, and any connections that are already connected up or down
			realConnections.removeIf(f -> f == NORTH || f == SOUTH);
			realConnections.removeIf(f -> blockConnectionZ(f, data));
		} else {
			// Otherwise, remove every connection that is already connected to something else
			realConnections.removeIf(f -> blockConnectionY(f, data));
		}

		// Replace our initial connection data with the new info
		cons = new Connections(realConnections);

		int rotation = 0;
		Submap submap = SubmapImpl.X2[0][0];
		if (nominalFace.getAxis().isHorizontal() && cons.connectedOr(UP, DOWN)) {
			submap = getSubmap(UP, DOWN, cons);
		} else if (cons.connectedOr(EAST, WEST)) {
			rotation = 1;
			submap = getSubmap(EAST, WEST, cons);
		} else if (cons.connectedOr(NORTH, SOUTH)) {
			submap = getSubmap(NORTH, SOUTH, cons);
			if (nominalFace == DOWN) {
				rotation += 2;
			}
		}

		boolean connected = !cons.getConnections().isEmpty();

		// Side textures need to be rotated to look correct
		if (connected && !cons.connectedOr(UP, DOWN)) {
			if (nominalFace == EAST) {
				rotation += 1;
			}
			if (nominalFace == NORTH) {
				rotation += 2;
			}
			if (nominalFace == WEST) {
				rotation += 3;
			}
		}

		// If there is a connection opposite this side, it is an end-cap, so render as unconnected
		if (cons.connected(nominalFace.getOpposite())) {
			connected = false;
		}
		// If there are no connections at all, and this is not the top or bottom, render the "short" column texture
		if (cons.getConnections().isEmpty() && nominalFace.getAxis().isHorizontal()) {
			connected = true;
		}
		
		quad.rotateUVs(rotation);
		if (connected) {
			quad.setUVBounds(sprites[1]);
			quad.applySubmap(submap);
		} else {
			quad.setUVBounds(sprites[0]);
		}
		return quad;
	}

	private Submap getSubmap(Direction face1, Direction face2, Connections cons) {
		Submap uvs;
		if (cons.connectedAnd(face1, face2)) {
			uvs = SubmapImpl.X2[1][0];
		} else {
			if (cons.connected(face1)) {
				uvs = SubmapImpl.X2[1][1];
			} else {
				uvs = SubmapImpl.X2[0][1];
			}
		}
		return uvs;
	}

	private boolean blockConnectionY(Direction dir, ConnectionData data) {
		return blockConnection(dir, Axis.Y, data) || blockConnection(dir, dir.rotateYClockwise().getAxis(), data);
	}

	private boolean blockConnectionZ(Direction dir, ConnectionData data) {
		return blockConnection(dir, Axis.Z, data);
	}

	private boolean blockConnection(Direction dir, Axis axis, ConnectionData data) {
		Direction rot = DirectionHelper.rotateAround(dir, axis);
		return data.getConnections(dir).connectedOr(rot, rot.getOpposite());
	}
}
