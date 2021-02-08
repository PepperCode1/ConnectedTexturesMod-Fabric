package team.chisel.ctm.client.util.connection;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.client.Facade;
import team.chisel.ctm.client.CTMClient;

public class ConnectionLogic {
	public Optional<Boolean> disableObscuredFaceCheck = Optional.empty();
	// Mapping the different corner indeces to their respective dirs
	protected byte connectionMap;
	protected boolean ignoreStates;
	protected StateComparisonCallback stateComparator = StateComparisonCallback.DEFAULT;

	public boolean ignoreStates() {
		return ignoreStates;
	}

	public StateComparisonCallback stateComparator() {
		return stateComparator;
	}

	public ConnectionLogic ignoreStates(final boolean ignoreStates) {
		this.ignoreStates = ignoreStates;
		return this;
	}

	public ConnectionLogic stateComparator(final StateComparisonCallback stateComparator) {
		this.stateComparator = stateComparator;
		return this;
	}

	/**
	 * Builds the connection map and stores it in this instance. The {@link #connected(ConnectionDirection)}, {@link #connectedAnd(ConnectionDirection...)}, and {@link #connectedOr(ConnectionDirection...)} methods can be used to access it.
	 */
	public void buildConnectionMap(@NotNull BlockView world, @NotNull BlockPos pos, @NotNull Direction side) {
		BlockState state = getConnectionState(world, pos, side, pos);
		// TODO this naive check doesn't work for models that have unculled faces.
		// Perhaps a smarter optimization could be done eventually?
		//if (state.shouldDrawSide(world, pos, side)) {
		for (ConnectionDirection direction : ConnectionDirection.VALUES) {
			setConnectedState(direction, direction.isConnected(this, world, pos, side, state));
		}
		//}
	}

	public void buildConnectionMap(long data, Direction side) { // TODO never used. remove?
		connectionMap = 0; // Clear all connections
		List<ConnectionLocation> connections = ConnectionLocation.decode(data);
		for (ConnectionLocation location : connections) {
			if (location.getDirectionForSide(side) != null) {
				ConnectionDirection direction = location.getDirectionForSide(side);
				if (direction != null) {
					setConnectedState(direction, true);
				}
			}
		}
	}

	protected void setConnectedState(ConnectionDirection direction, boolean connected) {
		connectionMap = setConnectedState(connectionMap, direction, connected);
	}

	/**
	 * @param direction The direction to check connection in.
	 * @return True if the cached connectionMap holds a connection in this {@link ConnectionDirection direction}.
	 */
	public boolean connected(ConnectionDirection direction) {
		return ((connectionMap >> direction.ordinal()) & 1) == 1;
	}

	/**
	 * @param directions The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>all</b></i> of the given {@link ConnectionDirection directions}.
	 */
	public boolean connectedAnd(ConnectionDirection... directions) {
		for (ConnectionDirection direction : directions) {
			if (!connected(direction)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param directions The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>at least one</b></i> of the given {@link ConnectionDirection directions}.
	 */
	public boolean connectedOr(ConnectionDirection... directions) {
		for (ConnectionDirection direction : directions) {
			if (connected(direction)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param directions The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>none</b></i> of the given {@link ConnectionDirection directions}.
	 */
	public boolean connectedNone(ConnectionDirection... directions) {
		for (ConnectionDirection direction : directions) {
			if (connected(direction)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param directions The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>only all</b></i> of the given {@link ConnectionDirection directions}.
	 */
	public boolean connectedOnly(ConnectionDirection... directions) {
		byte map = 0;
		for (ConnectionDirection direction : directions) {
			map = setConnectedState(map, direction, true);
		}
		return map == connectionMap;
	}

	public int numConnections() {
		return Integer.bitCount(connectionMap);
	}

	/**
	 * A simple check for if the given block can connect to the given direction on the given side.
	 *
	 * @param world The world.
	 * @param current The position of the block.
	 * @param connection The position of the block to check against.
	 * @param direction The {@link Direction side} of the block to check for connection status. This is <i>not</i> the direction to check in.
	 * @return True if the given block can connect to the given location on the given side.
	 */
	public final boolean isConnected(BlockView world, BlockPos current, BlockPos connection, Direction direction) {
		BlockState state = getConnectionState(world, current, direction, connection);
		return isConnected(world, current, connection, direction, state);
	}

	public BlockState getConnectionState(BlockView world, BlockPos pos, @Nullable Direction side, BlockPos connection) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof Facade) {
			return ((Facade) state.getBlock()).getFacade(world, pos, side, connection);
		}
		return state;
	}

	/**
	 * A simple check for if the given block can connect to the given direction on the given side.
	 *
	 * @param world The world.
	 * @param current The position of the block.
	 * @param connection The position of the block to check against.
	 * @param direction The {@link Direction side} of the block to check for connection status. This is <i>not</i> the direction to check in.
	 * @param state The state to check against for connection.
	 * @return True if the given block can connect to the given location on the given side.
	 */
	public boolean isConnected(BlockView world, BlockPos current, BlockPos connection, Direction direction, BlockState state) {
		//if (CTMLib.chiselLoaded() && connectionBlocked(world, x, y, z, dir.ordinal())) {
		//	return false;
		//}
		BlockPos obscuringPos = connection.offset(direction);
		boolean disableObscured = disableObscuredFaceCheck.orElse(CTMClient.getConfigManager().getConfig().connectInsideCTM);
		BlockState connectionState = getConnectionState(world, connection, direction, current);
		BlockState obscuring = disableObscured ? null : getConnectionState(world, obscuringPos, direction, current);
		// bad API user
		if (connectionState == null) {
			throw new IllegalStateException("Error: received null blockstate as facade from block " + world.getBlockState(connection));
		}
		boolean connected = stateComparator(state, connectionState, direction);
		// no block obscuring this face
		if (obscuring == null) {
			return connected;
		}
		// check that we aren't already connected outwards from this side
		connected &= !stateComparator(state, obscuring, direction);
		return connected;
	}

	protected boolean stateComparator(BlockState from, BlockState to, Direction direction) {
		return stateComparator.connects(this, from, to, direction);
	}

	//private boolean connectionBlocked(BlockView world, int x, int y, int z, int side) {
	//	Block block = world.getBlock(x, y, z);
	//	if (block instanceof IConnectable) {
	//		return !((IConnectable) block).canConnectCTM(world, x, y, z, side);
	//	}
	//	return false;
	//}

	public long serialized() {
		return Byte.toUnsignedLong(connectionMap);
	}

	private static byte setConnectedState(byte map, ConnectionDirection direction, boolean connected) {
		if (connected) {
			return (byte) (map | (1 << direction.ordinal()));
		} else {
			return (byte) (map & ~(1 << direction.ordinal()));
		}
	}

	public interface StateComparisonCallback {
		StateComparisonCallback DEFAULT = (logic, from, to, direction) -> logic.ignoreStates ? from.getBlock() == to.getBlock() : from == to;

		boolean connects(ConnectionLogic logic, BlockState from, BlockState to, Direction direction);
	}
}
