package team.chisel.ctm.client.util.connection;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockView;

public class ConnectionLogicObscured extends ConnectionLogic {
	private boolean obscured;

	public boolean isObscured() {
		return obscured;
	}

	public void setObscured(boolean obscured) {
		this.obscured = obscured;
	}

	@Override
	public boolean isConnected(BlockView world, BlockPos current, BlockPos connection, Direction direction, BlockState state) {
		if (isObscured()) {
			return false;
		}
		BlockState obscuring = getConnectionState(world, current.offset(direction), direction, current);
		if (stateComparator(state, obscuring, direction)) {
			setObscured(true);
			return false;
		}
		BlockState connectionState = getConnectionState(world, connection, direction, current);
		BlockState obscuringcon = getConnectionState(world, connection.offset(direction), direction, current);
		if (stateComparator(state, connectionState, direction) || stateComparator(state, obscuringcon, direction)) {
			Vec3d difference = Vec3d.of(connection.subtract(current));
			if (difference.lengthSquared() > 1) {
				difference = difference.normalize();
				if (direction.getAxis() == Axis.Z) {
					difference = difference.rotateY((float) (-Math.PI / 2));
				}
				float angle = (float) Math.PI / 4;
				Vec3d vA;
				Vec3d vB;
				if (direction.getAxis().isVertical()) {
					vA = difference.rotateY(angle);
					vB = difference.rotateY(-angle);
				} else {
					vA = difference.rotateX(angle);
					vB = difference.rotateX(-angle);
				}
				BlockPos posA = new BlockPos(vA).add(current);
				BlockPos posB = new BlockPos(vB).add(current);
				return (getConnectionState(world, posA, direction, current) == state && !stateComparator(state, getConnectionState(world, posA.offset(direction), direction, current), direction)) || (getConnectionState(world, posB, direction, current) == state && !stateComparator(state, getConnectionState(world, posB.offset(direction), direction, current), direction));
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public long serialized() {
		return isObscured() ? (super.serialized() | (1 << 8)) : super.serialized();
	}
}
