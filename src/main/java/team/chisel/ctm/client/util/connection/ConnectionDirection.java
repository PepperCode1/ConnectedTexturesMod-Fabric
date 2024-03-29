package team.chisel.ctm.client.util.connection;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.client.util.DirectionUtil;

/**
 * Think of this class as a "two dimensional Direction, with diagonals".
 *
 * <p>It represents the eight different directions a face of a block can connect with CTM.
 *
 * <p>Note that, for example, {@link #TOP_RIGHT} does not mean connected to the {@link #TOP} and {@link #RIGHT}, but connected in the diagonal direction represented by {@link #TOP_RIGHT}.
 * This is used for inner corner rendering.
 */
public enum ConnectionDirection {
	TOP(UP),
	TOP_RIGHT(UP, EAST),
	RIGHT(EAST),
	BOTTOM_RIGHT(DOWN, EAST),
	BOTTOM(DOWN),
	BOTTOM_LEFT(DOWN, WEST),
	LEFT(WEST),
	TOP_LEFT(UP, WEST);

	/**
	 * All values of this enum, used to prevent unnecessary allocation via {@link #values()}.
	 */
	public static final ConnectionDirection[] VALUES = values();
	public static final ConnectionDirection[] SIDES = new ConnectionDirection[] { TOP, RIGHT, BOTTOM, LEFT };
	public static final ConnectionDirection[] CORNERS = new ConnectionDirection[] { TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT };

	private static final Direction NORMAL = SOUTH;

	static {
		// Run after static init
		for (ConnectionDirection direction : VALUES) {
			direction.buildCaches();
		}
	}

	@NotNull
	private Direction[] directions;
	@NotNull
	private BlockPos[] offsets = new BlockPos[6];

	ConnectionDirection(Direction... directions) {
		this.directions = directions;
	}

	private void buildCaches() {
		// Fill normalized dirs
		for (Direction normal : Direction.values()) {
			Direction[] normalized;
			if (normal == NORMAL) {
				normalized = directions;
			} else if (normal == NORMAL.getOpposite()) {
				// If this is the opposite direction of the default normal, we
				// need to mirror the dirs
				// A mirror version does not affect y+ and y- so we ignore those
				Direction[] ret = new Direction[directions.length];
				for (int i = 0; i < ret.length; i++) {
					ret[i] = directions[i].getOffsetY() != 0 ? directions[i] : directions[i].getOpposite();
				}
				normalized = ret;
			} else {
				Direction axis;
				// Next, we need different a different rotation axis depending
				// on if this is up/down or not
				if (normal.getOffsetY() == 0) {
					// If it is not up/down, pick either the left or right-hand
					// rotation
					axis = normal == NORMAL.rotateYClockwise() ? UP : DOWN;
				} else {
					// If it is up/down, pick either the up or down rotation.
					axis = normal == UP ? NORMAL.rotateYCounterclockwise() : NORMAL.rotateYClockwise();
				}
				Direction[] ret = new Direction[directions.length];
				// Finally apply all the rotations
				for (int i = 0; i < ret.length; i++) {
					ret[i] = DirectionUtil.rotateClockwiseRelative(directions[i], axis);
				}
				normalized = ret;
			}
			BlockPos ret = BlockPos.ORIGIN;
			for (Direction dir : normalized) {
				ret = ret.offset(dir);
			}
			offsets[normal.ordinal()] = ret;
		}
	}

	/**
	 * Apply this ConnectionDirection to the given BlockPos for the given Direction.
	 *
	 * @return The offset BlockPos.
	 */
	@NotNull
	public BlockPos applyOffset(BlockPos pos, Direction side) {
		return pos.add(getOffset(side));
	}

	@NotNull
	public BlockPos getOffset(Direction normal) {
		return offsets[normal.ordinal()];
	}

	@Nullable
	public ConnectionDirection getDirectionFor(Direction[] directions) {
		if (directions == this.directions) { // Short circuit for identical return from getNormalizedDirs
			return this;
		}

		for (ConnectionDirection direction : VALUES) {
			if (Arrays.equals(direction.directions, directions)) {
				return direction;
			}
		}
		return null;
	}
}
