package team.chisel.ctm.client.util.connection;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.Facade;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.util.BitUtil;

public class SpacialConnectionLogic {
	protected byte connectionMap;
	protected boolean ignoreStates;
	protected StateComparator stateComparator = StateComparator.DEFAULT;

	public boolean ignoreStates() {
		return ignoreStates;
	}

	public StateComparator getStateComparator() {
		return stateComparator;
	}

	public void ignoreStates(boolean ignoreStates) {
		this.ignoreStates = ignoreStates;
	}

	public void setStateComparator(StateComparator stateComparator) {
		this.stateComparator = stateComparator;
	}

	public void buildConnectionMap(@NotNull BlockRenderView world, @NotNull BlockPos pos) {
		connectionMap = 0;
		for (Direction direction : Direction.values()) {
			if (isConnected(world, pos, direction)) {
				setConnected(direction, true);
			}
		}
	}

	protected void setConnected(Direction direction, boolean connected) {
		if (connected) {
			connectionMap = BitUtil.setBit(connectionMap, direction.ordinal());
		} else {
			connectionMap = BitUtil.clearBit(connectionMap, direction.ordinal());
		}
	}

	public boolean isConnected(BlockRenderView world, BlockPos pos, Direction direction) {
		return isConnected(world, pos, pos.offset(direction));
	}

	public boolean isConnected(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
		return isConnected(world, pos, pos.offset(direction), state);
	}

	public boolean isConnected(BlockRenderView world, BlockPos pos, BlockPos connection) {
		BlockState state = getConnectionState(world, pos, connection);
		return isConnected(world, pos, connection, state);
	}

	public boolean isConnected(BlockRenderView world, BlockPos pos, BlockPos connection, BlockState state) {
		BlockState connectionState = getConnectionState(world, connection, pos);
		return compare(state, connectionState);
	}

	public BlockState getConnectionState(BlockRenderView world, BlockPos pos, BlockPos connection) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof Facade) {
			BlockState facadeState = ((Facade) state.getBlock()).getFacadeState(world, pos, connection, null);
			if (facadeState != null) {
				return facadeState;
			} else {
				CTMClient.LOGGER.error("Received null facade blockstate from {} at {}.", state.getBlock(), pos);
			}
		}
		return state;
	}

	protected boolean compare(BlockState from, BlockState to) {
		return stateComparator.connects(this, from, to);
	}

	public boolean connected(Direction direction) {
		return BitUtil.getBit(connectionMap, direction.ordinal());
	}

	public boolean connectedAnd(Direction... directions) {
		for (Direction direction : directions) {
			if (!connected(direction)) {
				return false;
			}
		}
		return true;
	}

	public boolean connectedOr(Direction... directions) {
		for (Direction direction : directions) {
			if (connected(direction)) {
				return true;
			}
		}
		return false;
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
		StateComparator DEFAULT = (logic, from, to) -> logic.ignoreStates() ? from.getBlock() == to.getBlock() : from == to;

		boolean connects(SpacialConnectionLogic logic, BlockState from, BlockState to);
	}
}
