package team.chisel.ctm.client.util.connection;

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
		BlockState state = getConnectionState(world, pos, null, side);
		// TODO this naive check doesn't work for models that have unculled faces.
		// Perhaps a smarter optimization could be done eventually?
		//if (state.shouldDrawSide(world, pos, side)) {
		for (ConnectionDirection direction : ConnectionDirection.VALUES) {
			setConnected(direction, isConnected(world, pos, side, state, direction));
		}
		//}
	}

	/**
	 * <b>Use {@link #connected} if the connection map has already been built.</b>
	 *
	 * <p>Check if the given block position can connect to another position on the given side.
	 *
	 * @param world The world.
	 * @param pos The position of the block.
	 * @param side The {@link Direction side} of the block to check for connection status. This is <i>not</i> the direction to check in.
	 * @param direction The direction in which to check.
	 * @return True if the block can connect in the given ConnectionDirection, false otherwise.
	 */
	public boolean isConnected(BlockView world, BlockPos pos, Direction side, ConnectionDirection direction) {
		return isConnected(world, pos, direction.applyOffset(pos, side), side);
	}

	/**
	 * <b>Use {@link #connected} if the connection map has already been built.</b>
	 *
	 * <p>Check if the given block position can connect to another position on the given side.
	 *
	 * @param world The world.
	 * @param pos The position of the block.
	 * @param side The {@link Direction side} of the block to check for connection status. This is <i>not</i> the direction to check in.
	 * @param state The state of the block with which to check for connection with.
	 * @param direction The direction in which to check.
	 * @return True if the block can connect in the given ConnectionDirection, false otherwise.
	 */
	public boolean isConnected(BlockView world, BlockPos pos, Direction side, BlockState state, ConnectionDirection direction) {
		return isConnected(world, pos, direction.applyOffset(pos, side), side, state);
	}

	/**
	 * <b>Use {@link #connected} if the connection map has already been built.</b>
	 *
	 * <p>Check if the given block position can connect to another position on the given side.
	 *
	 * @param world The world.
	 * @param pos The position of the block.
	 * @param connection The position of the block to check against.
	 * @param side The {@link Direction side} of the block to check for connection status. This is <i>not</i> the direction to check in.
	 * @return True if the positions can connect, false otherwise.
	 */
	public boolean isConnected(BlockView world, BlockPos pos, BlockPos connection, Direction side) {
		BlockState state = getConnectionState(world, pos, connection, side);
		return isConnected(world, pos, connection, side, state);
	}

	/**
	 * <b>Use {@link #connected} if the connection map has already been built.</b>
	 *
	 * <p>Check if the given block position can connect to another position on the given side.
	 *
	 * @param world The world.
	 * @param pos The position of the block.
	 * @param connection The position of the block to check against.
	 * @param side The {@link Direction side} of the block to check for connection status. This is <i>not</i> the direction to check in.
	 * @param state The state of the block with which to check for connection with.
	 * @return True if the positions can connect, false otherwise.
	 */
	public boolean isConnected(BlockView world, BlockPos pos, BlockPos connection, Direction side, BlockState state) {
		BlockState connectionState = getConnectionState(world, connection, pos, side);
		// bad API user
		if (connectionState == null) {
			throw new IllegalStateException("Error: received null facade blockstate from block " + world.getBlockState(connection));
		}
		boolean connected = compare(state, connectionState, side);
		boolean disableObscured = disableObscuredFaceCheck.orElse(CTMClient.getConfigManager().getConfig().connectInsideCTM);
		// no block obscuring this face
		if (disableObscured) {
			return connected;
		}
		BlockState obscuring = getConnectionState(world, connection.offset(side), pos, side);
		// check that we aren't already connected outwards from this side
		connected &= !compare(state, obscuring, side);
		return connected;
	}

	public BlockState getConnectionState(BlockView world, BlockPos pos, @Nullable BlockPos connection, @Nullable Direction side) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof Facade) {
			return ((Facade) state.getBlock()).getFacadeState(world, pos, connection, side);
		}
		return state;
	}

	protected boolean compare(BlockState from, BlockState to, Direction side) {
		return stateComparator.connects(this, from, to, side);
	}

	/**
	 * @param direction The direction to check connection in.
	 * @return True if the cached connectionMap holds a connection in this {@link ConnectionDirection direction}.
	 */
	public boolean connected(ConnectionDirection direction) {
		return getConnected(connectionMap, direction);
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
			map = setConnected(map, direction, true);
		}
		return map == connectionMap;
	}

	public boolean hasConnections() {
		return connectionMap != 0;
	}

	public int numConnections() {
		return Integer.bitCount(connectionMap);
	}

	public long serialized() {
		return Byte.toUnsignedLong(connectionMap);
	}

	protected void setConnected(ConnectionDirection direction, boolean connected) {
		connectionMap = setConnected(connectionMap, direction, connected);
	}

	protected static boolean getConnected(byte map, ConnectionDirection direction) {
		return ((map >> direction.ordinal()) & 1) == 1;
	}

	protected static byte setConnected(byte map, ConnectionDirection direction, boolean connected) {
		if (connected) {
			return (byte) (map | (1 << direction.ordinal()));
		} else {
			return (byte) (map & ~(1 << direction.ordinal()));
		}
	}

	public interface StateComparisonCallback {
		StateComparisonCallback DEFAULT = (logic, from, to, side) -> logic.ignoreStates ? from.getBlock() == to.getBlock() : from == to;

		boolean connects(ConnectionLogic logic, BlockState from, BlockState to, Direction side);
	}
}
