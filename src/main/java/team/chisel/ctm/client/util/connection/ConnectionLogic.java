package team.chisel.ctm.client.util.connection;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.Facade;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.util.BitUtil;

public class ConnectionLogic {
	protected byte connectionMap;
	public Optional<Boolean> disableObscuredFaceCheck = Optional.empty();
	protected boolean ignoreStates;
	protected StateComparator stateComparator = StateComparator.DEFAULT;

	public boolean ignoreStates() {
		return ignoreStates;
	}

	public StateComparator getStateComparator() {
		return stateComparator;
	}

	public ConnectionLogic ignoreStates(boolean ignoreStates) {
		this.ignoreStates = ignoreStates;
		return this;
	}

	public ConnectionLogic setStateComparator(StateComparator stateComparator) {
		this.stateComparator = stateComparator;
		return this;
	}

	/**
	 * Builds the connection map and stores it in this instance.
	 * The {@link #connected(ConnectionDirection)}, {@link #connectedAnd(ConnectionDirection...)}, {@link #connectedOr(ConnectionDirection...)}, {@link #connectedNone(ConnectionDirection...)}, and {@link #connectedOnly(ConnectionDirection...)} methods can be used to access it.
	 */
	public void buildConnectionMap(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull Direction side) {
		connectionMap = 0;
		for (ConnectionDirection direction : ConnectionDirection.VALUES) {
			if (isConnected(world, pos, side, direction)) {
				setConnected(direction, true);
			}
		}
	}

	protected void setConnected(ConnectionDirection direction, boolean connected) {
		if (connected) {
			connectionMap = BitUtil.setBit(connectionMap, direction.ordinal());
		} else {
			connectionMap = BitUtil.clearBit(connectionMap, direction.ordinal());
		}
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
	protected boolean isConnected(BlockRenderView world, BlockPos pos, Direction side, ConnectionDirection direction) {
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
	protected boolean isConnected(BlockRenderView world, BlockPos pos, Direction side, BlockState state, ConnectionDirection direction) {
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
	protected boolean isConnected(BlockRenderView world, BlockPos pos, BlockPos connection, Direction side) {
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
	protected boolean isConnected(BlockRenderView world, BlockPos pos, BlockPos connection, Direction side, BlockState state) {
		BlockState connectionState = getConnectionState(world, connection, pos, side);
		boolean connected = compare(state, connectionState, side);
		if (!connected) {
			return false;
		}
		// Check if obscuring check should be applied
		if (disableObscuredFaceCheck.orElse(CTMClient.getConfigManager().getConfig().connectInsideCTM)) {
			return true;
		}
		BlockState obscuring = getConnectionState(world, connection.offset(side), connection, side);
		// Check if obscuring state should prevent connection
		return !compare(connectionState, obscuring, side);
	}

	protected BlockState getConnectionState(BlockRenderView world, BlockPos pos, BlockPos connection, @Nullable Direction side) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof Facade) {
			BlockState facadeState = ((Facade) state.getBlock()).getFacadeState(world, pos, connection, side);
			if (facadeState != null) {
				return facadeState;
			} else {
				CTMClient.LOGGER.error("Received null facade blockstate from {} at {}.", state.getBlock(), pos);
			}
		}
		return state;
	}

	protected boolean compare(BlockState from, BlockState to, Direction side) {
		return stateComparator.connects(this, from, to, side);
	}

	/**
	 * @param direction The direction to check connection in.
	 * @return True if the connection map holds a connection in this {@link ConnectionDirection direction}.
	 */
	public boolean connected(ConnectionDirection direction) {
		return BitUtil.getBit(connectionMap, direction.ordinal());
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
			map = BitUtil.setBit(map, direction.ordinal());
		}
		return map == connectionMap;
	}

	public boolean hasConnections() {
		return connectionMap != 0;
	}

	public int numConnections() {
		return Integer.bitCount(connectionMap);
	}

	public long serialize() {
		return Byte.toUnsignedLong(connectionMap);
	}

	public void deserialize(long data) {
		connectionMap = (byte) data;
	}

	public interface StateComparator {
		StateComparator DEFAULT = (logic, from, to, side) -> logic.ignoreStates() ? from.getBlock() == to.getBlock() : from == to;

		boolean connects(ConnectionLogic logic, BlockState from, BlockState to, Direction side);
	}
}
