package team.chisel.ctm.client.util;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.util.NonnullType;
import team.chisel.ctm.client.render.CTMLogic;

/**
 * Think of this class as a "Two dimensional Direction, with diagonals".
 *
 * <p>It represents the eight different directions a face of a block can connect with CTM, and contains the logic for determining if a block is indeed connected in that direction.
 *
 * <p>Note that, for example, {@link #TOP_RIGHT} does not mean connected to the {@link #TOP} and {@link #RIGHT}, but connected in the diagonal direction represented by {@link #TOP_RIGHT}. This is used
 * for inner corner rendering.
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
	private static final Direction NORMAL = SOUTH;

	static {
		// Run after static init
		for (ConnectionDirection direction : ConnectionDirection.VALUES) {
			direction.buildCaches();
		}
	}

	@NonnullType
	private Direction[] directions;
	@NonnullType
	private BlockPos[] offsets = new BlockPos[6];

	ConnectionDirection(Direction... directions) {
		this.directions = directions;
	}

	private void buildCaches() {
		// Fill normalized dirs
		for (Direction normal : Direction.values()) {
			@NonnullType Direction[] normalized;
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
					ret[i] = rotate(directions[i], axis);
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
	 * Finds if this block is connected for the given side in this ConnectionDirection.
	 * @param logic The CTM instance to use for logic.
	 * @param world The world the block is in.
	 * @param pos The position of the block.
	 * @param side The side of the current face.
	 * @return True if the block is connected in the given ConnectionDirection, false otherwise.
	 */
	public boolean isConnected(CTMLogic logic, BlockView world, BlockPos pos, Direction side) {
		return logic.isConnected(world, pos, applyConnection(pos, side), side);
	}

	/**
	 * Finds if this block is connected for the given side in this ConnectionDirection.
	 * @param logic The CTM instance to use for logic.
	 * @param world The world the block is in.
	 * @param pos The position of the block.
	 * @param side The side of the current face.
	 * @param state The state to check for connection with.
	 * @return True if the block is connected in the given ConnectionDirection, false otherwise.
	 */
	public boolean isConnected(CTMLogic logic, BlockView world, BlockPos pos, Direction side, BlockState state) {
		return logic.isConnected(world, pos, applyConnection(pos, side), side, state);
	}

	/**
	 * Apply this ConnectionDirection to the given BlockPos for the given Direction normal direction.
	 * @return The offset BlockPos.
	 */
	@NotNull
	public BlockPos applyConnection(BlockPos pos, Direction side) {
		return pos.add(getOffset(side));
	}

	public ConnectionDirection relativize(Direction normal) {
		throw new UnsupportedOperationException("Yell at tterrag to finish deserialization");
		/*
		if (normal == NORMAL) {
			return this;
		} else if (normal == NORMAL.getOpposite()) {
			return getDirFor(getNormalizedDirs(normal));
		} else {
			if (dirs.length == 1) {
				if (normal.getAxis() == dirs[0].getAxis()) {
					return null;
				} else {
					return this;
				}
			}
		}
		*/
	}

	@NotNull
	public BlockPos getOffset(Direction normal) {
		return offsets[normal.ordinal()];
	}

	public @Nullable ConnectionDirection getDirFor(Direction[] directions) {
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

	private Direction rotate(Direction facing, Direction axisFacing) {
		Axis axis = axisFacing.getAxis();
		AxisDirection axisDirection = axisFacing.getDirection();

		if (axisDirection == AxisDirection.POSITIVE) {
			return DirectionHelper.rotateAround(facing, axis);
		}

		if (facing.getAxis() != axis) {
			switch (axis) {
			case X:
				// Inverted results from Direction#rotateX
				switch (facing) {
				case NORTH:
					return UP;
				case DOWN:
					return NORTH;
				case SOUTH:
					return DOWN;
				case UP:
					return SOUTH;
				default:
					return facing; // Invalid but ignored
				}
			case Y:
				return facing.rotateYCounterclockwise();
			case Z:
				// Inverted results from Direction#rotateZ
				switch (facing) {
				case EAST:
					return EAST;
				case WEST:
					return WEST;
				case UP:
					return DOWN;
				case DOWN:
					return UP;
				default:
					return facing; // invalid but ignored
				}
			}
		}

		return facing;
	}
}
