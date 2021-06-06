package team.chisel.ctm.client.util.connection;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.client.util.BitUtil;

public class ConnectionLogicObscured extends ConnectionLogic {
	private boolean obscured;

	public boolean isObscured() {
		return obscured;
	}

	public void setObscured(boolean obscured) {
		this.obscured = obscured;
	}

	@Override
	public boolean isConnected(BlockRenderView world, BlockPos pos, BlockPos connection, Direction side, BlockState state) {
		if (isObscured()) {
			return false;
		}
		BlockState obscuring = getConnectionState(world, pos.offset(side), pos, side);
		if (compare(state, obscuring, side)) {
			setObscured(true);
			return false;
		}
		BlockState connectionState = getConnectionState(world, connection, pos, side);
		BlockState obscuringCon = getConnectionState(world, connection.offset(side), pos, side);
		if (compare(state, connectionState, side) || compare(state, obscuringCon, side)) {
			Vec3d difference = Vec3d.of(connection.subtract(pos));
			if (difference.lengthSquared() > 1) {
				difference = difference.normalize();
				if (side.getAxis() == Axis.Z) {
					difference = difference.rotateY((float) (-Math.PI / 2));
				}
				float angle = (float) Math.PI / 4;
				Vec3d vA;
				Vec3d vB;
				if (side.getAxis().isVertical()) {
					vA = difference.rotateY(angle);
					vB = difference.rotateY(-angle);
				} else {
					vA = difference.rotateX(angle);
					vB = difference.rotateX(-angle);
				}
				BlockPos posA = new BlockPos(vA).add(pos);
				BlockPos posB = new BlockPos(vB).add(pos);
				return (getConnectionState(world, posA, pos, side) == state && !compare(state, getConnectionState(world, posA.offset(side), pos, side), side)) || (getConnectionState(world, posB, pos, side) == state && !compare(state, getConnectionState(world, posB.offset(side), pos, side), side));
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public long serialize() {
		long data = super.serialize();
		if (obscured) {
			data = BitUtil.setBit(data, 8);
		}
		return data;
	}

	@Override
	public void deserialize(long data) {
		super.deserialize(data);
		obscured = BitUtil.getBit(data, 8);
	}
}
