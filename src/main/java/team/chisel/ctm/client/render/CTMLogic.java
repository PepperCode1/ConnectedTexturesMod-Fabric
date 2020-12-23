package team.chisel.ctm.client.render;

import static team.chisel.ctm.client.util.ConnectionDirection.BOTTOM;
import static team.chisel.ctm.client.util.ConnectionDirection.BOTTOM_LEFT;
import static team.chisel.ctm.client.util.ConnectionDirection.BOTTOM_RIGHT;
import static team.chisel.ctm.client.util.ConnectionDirection.LEFT;
import static team.chisel.ctm.client.util.ConnectionDirection.RIGHT;
import static team.chisel.ctm.client.util.ConnectionDirection.TOP;
import static team.chisel.ctm.client.util.ConnectionDirection.TOP_LEFT;
import static team.chisel.ctm.client.util.ConnectionDirection.TOP_RIGHT;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.Facade;
import team.chisel.ctm.api.texture.Submap;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.util.ConnectionDirection;
import team.chisel.ctm.client.util.ConnectionLocation;

/**
 * The CTM renderer will draw the block's FACE using by assembling 4 quadrants from the 5 available block
 * textures. The normal texture.png is the block's "unconnected" texture, and is used when CTM is disabled or the block
 * has nothing to connect to. This texture has all of the outside corner quadrants. The texture_ctm.png contains the
 * rest of the quadrants.
 * <pre>
 * ┌─────────────────┐ ┌────────────────────────────────┐
 * │ texture.png     │ │ texture_ctm.png                │
 * │ ╔══════╤══════╗ │ │  ──────┼────── ║ ─────┼───── ║ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 16   │ 17   ║ │ │ │ 0    │ 1    │║ 2    │ 3    ║ │
 * │ ╟──────┼──────╢ │ │ ┼──────┼──────┼╟──────┼──────╢ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 18   │ 19   ║ │ │ │ 4    │ 5    │║ 6    │ 7    ║ │
 * │ ╚══════╧══════╝ │ │  ──────┼────── ║ ─────┼───── ║ │
 * └─────────────────┘ │ ═══════╤═══════╝ ─────┼───── ╚ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 8    │ 9    ││ 10   │ 11   │ │
 *                     │ ┼──────┼──────┼┼──────┼──────┼ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 12   │ 13   ││ 14   │ 15   │ │
 *                     │ ═══════╧═══════╗ ─────┼───── ╔ │
 *                     └────────────────────────────────┘
 * </pre>
 *
 * <p>Combining { 18, 13,  9, 16 }, we can generate a texture connected to the right!
 * <pre>
 * ╔══════╤═══════
 * ║      │      │
 * ║ 16   │ 9    │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 18   │ 13   │
 * ╚══════╧═══════
 * </pre>
 *
 * <p>Combining { 18, 13, 11,  2 }, we can generate a texture in the shape of an L (connected up and to the right).
 * <pre>
 * ║ ─────┼───── ╚
 * ║      │      │
 * ║ 2    │ 11   │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 18   │ 13   │
 * ╚══════╧═══════
 * </pre>
 *
 * <p>HAVE FUN!
 * -CptRageToaster-
 */
public class CTMLogic {
	/**
	 * The submap for the specific "magic number" value.
	 */
	public static final Submap[] QUADRANTS = new Submap[] {
		// CTM texture
		new SubmapImpl(4, 4, 0, 0), // 0
		new SubmapImpl(4, 4, 4, 0), // 1
		new SubmapImpl(4, 4, 8, 0), // 2
		new SubmapImpl(4, 4, 12, 0), // 3
		new SubmapImpl(4, 4, 0, 4), // 4
		new SubmapImpl(4, 4, 4, 4), // 5
		new SubmapImpl(4, 4, 8, 4), // 6
		new SubmapImpl(4, 4, 12, 4), // 7
		new SubmapImpl(4, 4, 0, 8), // 8
		new SubmapImpl(4, 4, 4, 8), // 9
		new SubmapImpl(4, 4, 8, 8), // 10
		new SubmapImpl(4, 4, 12, 8), // 11
		new SubmapImpl(4, 4, 0, 12), // 12
		new SubmapImpl(4, 4, 4, 12), // 13
		new SubmapImpl(4, 4, 8, 12), // 14
		new SubmapImpl(4, 4, 12, 12), // 15
		// Default texture
		new SubmapImpl(8, 8, 0, 0), // 16
		new SubmapImpl(8, 8, 8, 0), // 17
		new SubmapImpl(8, 8, 0, 8), // 18
		new SubmapImpl(8, 8, 8, 8) // 19
	}; // TODO TextureCTM only
	protected static final ConnectionDirection[][] SUBMAP_MAP = new ConnectionDirection[][] {
		{BOTTOM, LEFT, BOTTOM_LEFT}, {BOTTOM, RIGHT, BOTTOM_RIGHT}, {TOP, RIGHT, TOP_RIGHT}, {TOP, LEFT, TOP_LEFT}
	}; // TODO TextureCTM only

	/**
	 * Some hardcoded offset values for the different corner indeces.
	 */
	protected static int[] submapOffsets = {4, 5, 1, 0}; // TODO TextureCTM only

	public Optional<Boolean> disableObscuredFaceCheck = Optional.empty();
	// Mapping the different corner indeces to their respective dirs
	protected byte connectionMap;
	protected int[] submapCache = new int[] {18, 19, 17, 16}; // TODO TextureCTM only
	protected boolean ignoreStates;
	protected StateComparisonCallback stateComparator = StateComparisonCallback.DEFAULT;

	public boolean ignoreStates() {
		return this.ignoreStates;
	}

	public StateComparisonCallback stateComparator() {
		return this.stateComparator;
	}

	public CTMLogic ignoreStates(final boolean ignoreStates) {
		this.ignoreStates = ignoreStates;
		return this;
	}

	public CTMLogic stateComparator(final StateComparisonCallback stateComparator) {
		this.stateComparator = stateComparator;
		return this;
	}

	/**
	 * Builds the connection map and stores it in this CTM instance. The {@link #connected(ConnectionDirection)}, {@link #connectedAnd(ConnectionDirection...)}, and {@link #connectedOr(ConnectionDirection...)} methods can be used to access it.
	 */
	public void buildConnectionMap(BlockView world, BlockPos pos, Direction side) {
		BlockState state = getConnectionState(world, pos, side, pos);
		// TODO this naive check doesn't work for models that have unculled faces.
		// Perhaps a smarter optimization could be done eventually?
		//if (state.shouldSideBeRendered(world, pos, side)) {
		for (ConnectionDirection dir : ConnectionDirection.VALUES) {
			setConnectedState(dir, dir.isConnected(this, world, pos, side, state));
		}
		//}
	}

	public void buildConnectionMap(long data, Direction side) { // TODO never used. remove?
		connectionMap = 0; // Clear all connections
		List<ConnectionLocation> connections = ConnectionLocation.decode(data);
		for (ConnectionLocation loc : connections) {
			if (loc.getDirectionForSide(side) != null) {
				ConnectionDirection dir = loc.getDirectionForSide(side);
				if (dir != null) {
					setConnectedState(dir, true);
				}
			}
		}
	}

	protected void setConnectedState(ConnectionDirection dir, boolean connected) {
		connectionMap = setConnectedState(connectionMap, dir, connected);
	}

	/**
	 * @param dir The direction to check connection in.
	 * @return True if the cached connectionMap holds a connection in this {@link ConnectionDirection direction}.
	 */
	public boolean connected(ConnectionDirection dir) {
		return ((connectionMap >> dir.ordinal()) & 1) == 1;
	}

	/**
	 * @param dirs The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>all</b></i> the given {@link ConnectionDirection directions}.
	 */
	public boolean connectedAnd(ConnectionDirection... dirs) {
		for (ConnectionDirection dir : dirs) {
			if (!connected(dir)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param dirs The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>one of</b></i> the given {@link ConnectionDirection directions}.
	 */
	public boolean connectedOr(ConnectionDirection... dirs) {
		for (ConnectionDirection dir : dirs) {
			if (connected(dir)) {
				return true;
			}
		}
		return false;
	}

	public boolean connectedNone(ConnectionDirection... dirs) {
		for (ConnectionDirection dir : dirs) {
			if (connected(dir)) {
				return false;
			}
		}
		return true;
	}

	public boolean connectedOnly(ConnectionDirection... dirs) {
		byte map = 0;
		for (ConnectionDirection dir : dirs) {
			map = setConnectedState(map, dir, true);
		}
		return map == this.connectionMap;
	}

	public int numConnections() {
		return Integer.bitCount(connectionMap);
	}

	/**
	 * A simple check for if the given block can connect to the given direction on the given side.
	 * @param world
	 * @param current The position of your block.
	 * @param connection The position of the block to check against.
	 * @param dir The {@link Direction side} of the block to check for connection status. This is <i>not</i> the direction to check in.
	 * @return True if the given block can connect to the given location on the given side.
	 */
	public final boolean isConnected(BlockView world, BlockPos current, BlockPos connection, Direction dir) {
		BlockState state = getConnectionState(world, current, dir, connection);
		return isConnected(world, current, connection, dir, state);
	}

	/**
	 * A simple check for if the given block can connect to the given direction on the given side.
	 * @param world
	 * @param current The position of your block.
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

	public BlockState getConnectionState(BlockView world, BlockPos pos, @Nullable Direction side, BlockPos connection) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof Facade) {
			return ((Facade) state.getBlock()).getFacade(world, pos, side, connection);
		}
		return state;
	}

	public long serialized() {
		return Byte.toUnsignedLong(connectionMap);
	}

	// start TODO move or delete this code as it's only used in TextureCTM (and TextureEdges)
	/**
	 * @return The indeces of the typical 4x4 submap to use for the given face at the given location.
	 * Indeces are in counter-clockwise order starting at bottom left.
	 */
	public int[] createSubmapIndices(@Nullable BlockView world, BlockPos pos, Direction side) {
		if (world == null) {
			return submapCache;
		}
		buildConnectionMap(world, pos, side);
		// Map connections to submap indeces
		for (int i = 0; i < 4; i++) {
			fillSubmaps(i);
		}
		return submapCache;
	}

	public int[] createSubmapIndices(long data, Direction side) { // TODO never used. remove?
		submapCache = new int[] {18, 19, 17, 16};
		buildConnectionMap(data, side);
		// Map connections to submap indeces
		for (int i = 0; i < 4; i++) {
			fillSubmaps(i);
		}
		return submapCache;
	}

	protected void fillSubmaps(int index) {
		ConnectionDirection[] directions = SUBMAP_MAP[index];
		if (connectedOr(directions[0], directions[1])) {
			if (connectedAnd(directions)) {
				// If all dirs are connected, we use the fully connected face,
				// the base offset value.
				submapCache[index] = submapOffsets[index];
			} else {
				// This is a bit magic-y, but basically the array is ordered so
				// the first dir requires an offset of 2, and the second dir
				// requires an offset of 8, plus the initial offset for the
				// corner.
				submapCache[index] = submapOffsets[index] + (connected(directions[0]) ? 2 : 0) + (connected(directions[1]) ? 8 : 0);
			}
		}
	}

	public int[] getSubmapIndices() {
		return submapCache;
	}

	public static boolean isDefaultTexture(int id) {
		return (id == 16 || id == 17 || id == 18 || id == 19);
	}
	// end

	private static byte setConnectedState(byte map, ConnectionDirection direction, boolean connected) {
		if (connected) {
			return (byte) (map | (1 << direction.ordinal()));
		} else {
			return (byte) (map & ~(1 << direction.ordinal()));
		}
	}

	public interface StateComparisonCallback {
		StateComparisonCallback DEFAULT = (logic, from, to, direction) -> logic.ignoreStates ? from.getBlock() == to.getBlock() : from == to;

		boolean connects(CTMLogic logic, BlockState from, BlockState to, Direction direction);
	}
}
