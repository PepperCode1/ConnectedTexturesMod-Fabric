package team.chisel.ctm.client.texture.context;

import static team.chisel.ctm.client.util.ConnectionLocation.DOWN;
import static team.chisel.ctm.client.util.ConnectionLocation.EAST;
import static team.chisel.ctm.client.util.ConnectionLocation.NORTH;
import static team.chisel.ctm.client.util.ConnectionLocation.NORTH_EAST_DOWN;
import static team.chisel.ctm.client.util.ConnectionLocation.NORTH_EAST_UP;
import static team.chisel.ctm.client.util.ConnectionLocation.NORTH_WEST_DOWN;
import static team.chisel.ctm.client.util.ConnectionLocation.NORTH_WEST_UP;
import static team.chisel.ctm.client.util.ConnectionLocation.SOUTH;
import static team.chisel.ctm.client.util.ConnectionLocation.SOUTH_EAST_DOWN;
import static team.chisel.ctm.client.util.ConnectionLocation.SOUTH_EAST_UP;
import static team.chisel.ctm.client.util.ConnectionLocation.SOUTH_WEST_DOWN;
import static team.chisel.ctm.client.util.ConnectionLocation.SOUTH_WEST_UP;
import static team.chisel.ctm.client.util.ConnectionLocation.UP;
import static team.chisel.ctm.client.util.ConnectionLocation.WEST;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.google.common.collect.ObjectArrays;
import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.client.util.ConnectionLocation;

public class TextureContextPillar implements TextureContext {
	private static final ConnectionLocation[] MAIN_VALUES = {UP, DOWN, NORTH, SOUTH, EAST, WEST};
	private static final ConnectionLocation[] OFFSET_VALUES = ArrayUtils.removeElements(ConnectionLocation.VALUES, ObjectArrays.concat(new ConnectionLocation[] {NORTH_EAST_UP, NORTH_EAST_DOWN, NORTH_WEST_UP, NORTH_WEST_DOWN, SOUTH_WEST_UP, SOUTH_WEST_DOWN, SOUTH_EAST_UP, SOUTH_EAST_DOWN}, MAIN_VALUES, ConnectionLocation.class));
	private static final ConnectionLocation[] ALL_VALUES = ObjectArrays.concat(MAIN_VALUES, OFFSET_VALUES, ConnectionLocation.class);

	private ConnectionData data;
	private long compressedData;

	public TextureContextPillar(BlockView world, BlockPos pos) {
		data = new ConnectionData(world, pos);
		BlockState state = world.getBlockState(pos);
		for (ConnectionLocation loc : ALL_VALUES) {
			if (state == world.getBlockState(loc.transform(pos))) {
				compressedData = compressedData | loc.getMask();
			}
		}
	}

	public TextureContextPillar(long data) {
		this.data = new ConnectionData(data);
	}

	@Override
	public long getCompressedData() {
		return this.compressedData;
	}

	public ConnectionData getData() {
		return this.data;
	}

	public static class Connections {
		private EnumSet<Direction> connections;

		public Connections(final EnumSet<Direction> connections) {
			this.connections = connections;
		}

		public EnumSet<Direction> getConnections() {
			return this.connections;
		}

		public boolean connected(Direction facing) {
			return connections.contains(facing);
		}

		public boolean connectedAnd(Direction... facings) {
			for (Direction f : facings) {
				if (!connected(f)) {
					return false;
				}
			}
			return true;
		}

		public boolean connectedOr(Direction... facings) {
			for (Direction f : facings) {
				if (connected(f)) {
					return true;
				}
			}
			return false;
		}

		public static Connections forPos(BlockView world, BlockPos pos) {
			BlockState state = world.getBlockState(pos);
			return forPos(world, state, pos);
		}

		public static Connections forData(long data, Direction offset) {
			EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
			if (offset == null) {
				for (ConnectionLocation loc : MAIN_VALUES) {
					if ((data & loc.getMask()) != 0) {
						connections.add(ConnectionLocation.toFacing(loc));
					}
				}
			} else {
				for (ConnectionLocation loc : OFFSET_VALUES) {
					if ((data & loc.getMask()) != 0) {
						Direction facing = loc.clipOrDestroy(offset);
						if (facing != null) {
							connections.add(facing);
						}
					}
				}
			}
			return new Connections(connections);
		}

		public static Connections forPos(BlockView world, BlockState baseState, BlockPos pos) {
			EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
			BlockState state = world.getBlockState(pos);
			if (state == baseState) {
				for (Direction f : Direction.values()) {
					if (world.getBlockState(pos.offset(f)) == baseState) {
						connections.add(f);
					}
				}
			}
			return new Connections(connections);
		}

		@Override
		public String toString() {
			return "TextureContextPillar.Connections(connections=" + this.getConnections() + ")";
		}
	}

	public static class ConnectionData {
		private Connections connections;
		private Map<Direction, Connections> connectionsMap = new EnumMap<>(Direction.class);

		public ConnectionData(BlockView world, BlockPos pos) {
			connections = Connections.forPos(world, pos);
			BlockState state = world.getBlockState(pos);
			for (Direction facing : Direction.values()) {
				connectionsMap.put(facing, Connections.forPos(world, state, pos.offset(facing)));
			}
		}

		public ConnectionData(long data) {
			connections = Connections.forData(data, null);
			for (Direction facing : Direction.values()) {
				connectionsMap.put(facing, Connections.forData(data, facing));
			}
		}

		public Connections getConnections(Direction facing) {
			return connectionsMap.get(facing);
		}

		@Override
		public String toString() {
			return "TextureContextPillar.ConnectionData(connections=" + this.getConnections() + ", connectionConnections=" + this.connectionsMap + ")";
		}

		public Connections getConnections() {
			return this.connections;
		}
	}
}
