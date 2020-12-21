package team.chisel.ctm.client.util;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import net.minecraft.util.math.Direction;

/**
 * A bunch of methods that got stripped out of Direction in 1.15
 * 
 * @author Mojang
 */
public class DirectionHelper {
	public static Direction rotateAround(Direction direction, Direction.Axis axis) {
		switch (axis) {
		case X:
			if (direction != WEST && direction != EAST) {
				return rotateX(direction);
			}

			return direction;
		case Y:
			if (direction != UP && direction != DOWN) {
				return direction.rotateYClockwise();
			}

			return direction;
		case Z:
			if (direction != NORTH && direction != SOUTH) {
				return rotateZ(direction);
			}

			return direction;
		default:
			throw new IllegalStateException("Unable to get CW facing for axis " + axis);
		}
	}

	public static Direction rotateX(Direction direction) {
		switch (direction) {
		case NORTH:
			return DOWN;
		case EAST:
		case WEST:
		default:
			throw new IllegalStateException("Unable to get X-rotated facing of " + direction);
		case SOUTH:
			return UP;
		case UP:
			return NORTH;
		case DOWN:
			return SOUTH;
		}
	}

	public static Direction rotateZ(Direction direction) {
		switch (direction) {
		case EAST:
			return DOWN;
		case SOUTH:
		default:
			throw new IllegalStateException("Unable to get Z-rotated facing of " + direction);
		case WEST:
			return UP;
		case UP:
			return EAST;
		case DOWN:
			return WEST;
		}
	}
}
