package team.chisel.ctm.client.util.connection;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Represents the different spots for connection locations for a CTM block model.
 */
public enum ConnectionLocation {
	UP(ConnectionDirection.TOP),
	DOWN(ConnectionDirection.BOTTOM),
	NORTH(Direction.EAST, ConnectionDirection.RIGHT),
	SOUTH(Direction.EAST, ConnectionDirection.LEFT),
	EAST(ConnectionDirection.RIGHT),
	WEST(ConnectionDirection.LEFT),

	NORTH_EAST(Direction.UP, ConnectionDirection.TOP_RIGHT),
	NORTH_WEST(Direction.UP, ConnectionDirection.TOP_LEFT),
	SOUTH_EAST(Direction.UP, ConnectionDirection.BOTTOM_RIGHT),
	SOUTH_WEST(Direction.UP, ConnectionDirection.BOTTOM_LEFT),

	NORTH_UP(Direction.EAST, ConnectionDirection.TOP_RIGHT),
	NORTH_DOWN(Direction.EAST, ConnectionDirection.BOTTOM_RIGHT),
	SOUTH_UP(Direction.EAST, ConnectionDirection.TOP_LEFT),
	SOUTH_DOWN(Direction.EAST, ConnectionDirection.BOTTOM_LEFT),

	EAST_UP(ConnectionDirection.TOP_RIGHT),
	EAST_DOWN(ConnectionDirection.BOTTOM_RIGHT),
	WEST_UP(ConnectionDirection.TOP_LEFT),
	WEST_DOWN(ConnectionDirection.BOTTOM_LEFT),

	NORTH_EAST_UP(Direction.EAST, ConnectionDirection.TOP_RIGHT, true),
	NORTH_EAST_DOWN(Direction.EAST, ConnectionDirection.BOTTOM_RIGHT, true),

	SOUTH_EAST_UP(Direction.EAST, ConnectionDirection.TOP_LEFT, true),
	SOUTH_EAST_DOWN(Direction.EAST, ConnectionDirection.BOTTOM_LEFT, true),

	SOUTH_WEST_UP(Direction.WEST, ConnectionDirection.TOP_LEFT, true),
	SOUTH_WEST_DOWN(Direction.WEST, ConnectionDirection.BOTTOM_LEFT, true),

	NORTH_WEST_UP(Direction.WEST, ConnectionDirection.TOP_RIGHT, true),
	NORTH_WEST_DOWN(Direction.WEST, ConnectionDirection.BOTTOM_RIGHT, true),

	UP_UP(Direction.UP, null, true),
	DOWN_DOWN(Direction.DOWN, null, true),
	NORTH_NORTH(Direction.NORTH, null, true),
	SOUTH_SOUTH(Direction.SOUTH, null, true),
	EAST_EAST(Direction.EAST, null, true),
	WEST_WEST(Direction.WEST, null, true);

	public static final ConnectionLocation[] VALUES = values();

	/**
	 * The enum facing directions needed to get to this connection location.
	 */
	private final Direction normal;
	@Nullable
	private final ConnectionDirection direction;
	private boolean offset;

	ConnectionLocation(@Nullable ConnectionDirection direction) {
		this(Direction.SOUTH, direction);
	}

	ConnectionLocation(@Nullable ConnectionDirection direction, boolean offset) {
		this(Direction.SOUTH, direction, offset);
	}

	ConnectionLocation(Direction normal, @Nullable ConnectionDirection direction) {
		this(normal, direction, false);
	}

	ConnectionLocation(Direction normal, @Nullable ConnectionDirection direction, boolean offset) {
		this.normal = normal;
		this.direction = direction;
		this.offset = offset;
	}

	@Deprecated
	@Nullable
	public ConnectionDirection getDirectionForSide(Direction facing) {
		return direction == null ? null : direction.relativize(facing);
	}

	@Deprecated
	@Nullable
	public Direction clipOrDestroy(Direction direction) {
		throw new UnsupportedOperationException("Deserialization is not yet supported");
		/*
		Direction[] dirs = dir == null ? new Direction[] {normal, normal} : dir.getNormalizedDirs(direction);
		if (dirs[0] == direction) {
			return dirs.length > 1 ? dirs[1] : null;
		} else if (dirs.length > 1 && dirs[1] == direction) {
			return dirs[0];
		} else {
			return null;
		}
		*/
	}

	public BlockPos transform(BlockPos pos) {
		if (direction != null) {
			pos = pos.add(direction.getOffset(normal));
		} else {
			pos = pos.offset(normal);
		}

		if (offset) {
			pos = pos.offset(normal);
		}
		return pos;
	}

	public long getMask() {
		return 1 << ordinal();
	}

	public static ConnectionLocation fromFacing(Direction facing) {
		switch (facing) {
		case NORTH:
			return NORTH;
		case SOUTH:
			return SOUTH;
		case EAST:
			return EAST;
		case WEST:
			return WEST;
		case UP:
			return UP;
		case DOWN:
			return DOWN;
		default:
			return NORTH;
		}
	}

	public static Direction toFacing(ConnectionLocation location) {
		switch (location) {
		case NORTH:
			return Direction.NORTH;
		case SOUTH:
			return Direction.SOUTH;
		case EAST:
			return Direction.EAST;
		case WEST:
			return Direction.WEST;
		case UP:
			return Direction.UP;
		case DOWN:
			return Direction.DOWN;
		default:
			return Direction.NORTH;
		}
	}

	public static List<ConnectionLocation> decode(long data) {
		List<ConnectionLocation> list = new ArrayList<>();
		for (ConnectionLocation location : VALUES) {
			if ((1 & (data >> location.ordinal())) != 0) {
				list.add(location);
			}
		}
		return list;
	}
}
