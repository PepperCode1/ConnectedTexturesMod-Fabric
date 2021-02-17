package team.chisel.ctm.client.util;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class DirectionUtil {
	/**
	 * Rotates a direction clockwise relative to a side.
	 *
	 * @param direction The direction being rotated.
	 * @param side The side relative to which the rotation is happening.
	 * @return The rotated direction.
	 */
	public static Direction rotateClockwiseRelative(Direction direction, Direction side) {
		Axis axis = side.getAxis();
		AxisDirection axisDirection = side.getDirection();

		if (axisDirection == AxisDirection.POSITIVE) {
			return DirectionUtil.rotateClockwiseAround(direction, axis);
		}
		return DirectionUtil.rotateCounterclockwiseAround(direction, axis);
	}

	public static Direction rotateClockwiseAround(Direction direction, Direction.Axis axis) {
		if (direction.getAxis() == axis) {
			return direction;
		}

		switch (axis) {
		case X:
			return rotateXClockwise(direction);
		case Y:
			return direction.rotateYClockwise();
		case Z:
			return rotateZClockwise(direction);
		default:
			throw new IllegalStateException("Unable to get CW facing for axis " + axis);
		}
	}

	public static Direction rotateCounterclockwiseAround(Direction direction, Direction.Axis axis) {
		if (direction.getAxis() == axis) {
			return direction;
		}

		switch (axis) {
		case X:
			return rotateXCounterclockwise(direction);
		case Y:
			return direction.rotateYCounterclockwise();
		case Z:
			return rotateZCounterclockwise(direction);
		default:
			throw new IllegalStateException("Unable to get CCW facing for axis " + axis);
		}
	}

	public static Direction rotateXClockwise(Direction direction) {
		switch (direction) {
		case DOWN:
			return SOUTH;
		case SOUTH:
			return UP;
		case UP:
			return NORTH;
		case NORTH:
			return DOWN;
		default:
			throw new IllegalStateException("Unable to get CW X-rotated facing of " + direction);
		}
	}

	public static Direction rotateZClockwise(Direction direction) {
		switch (direction) {
		case DOWN:
			return WEST;
		case WEST:
			return UP;
		case UP:
			return EAST;
		case EAST:
			return DOWN;
		default:
			throw new IllegalStateException("Unable to get CW Z-rotated facing of " + direction);
		}
	}

	public static Direction rotateXCounterclockwise(Direction direction) {
		switch (direction) {
		case DOWN:
			return NORTH;
		case NORTH:
			return UP;
		case UP:
			return SOUTH;
		case SOUTH:
			return DOWN;
		default:
			throw new IllegalStateException("Unable to get CCW X-rotated facing of " + direction);
		}
	}

	public static Direction rotateZCounterclockwise(Direction direction) {
		switch (direction) {
		case DOWN:
			return EAST;
		case EAST:
			return UP;
		case UP:
			return WEST;
		case WEST:
			return DOWN;
		default:
			throw new IllegalStateException("Unable to get CCW Z-rotated facing of " + direction);
		}
	}

	public static Vec3i getRelativeOffset(Direction side, int xOffset, int yOffset) {
		switch (side) {
		case DOWN:
			return new Vec3i(xOffset, 0, yOffset);
		case UP:
			return new Vec3i(xOffset, 0, -yOffset);
		case NORTH:
			return new Vec3i(-xOffset, yOffset, 0);
		case SOUTH:
		default:
			return new Vec3i(xOffset, yOffset, 0);
		case WEST:
			return new Vec3i(0, yOffset, xOffset);
		case EAST:
			return new Vec3i(0, yOffset, -xOffset);
		}
	}
}
