package team.chisel.ctm.client.util.connection;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class PillarConnectionLogic extends SpacialConnectionLogic {
	@Override
	public void buildConnectionMap(@NotNull BlockRenderView world, @NotNull BlockPos pos) {
		connectionMap = 0;

		if (!checkY(world, pos, true)) {
			if (!checkX(world, pos, true)) {
				checkZ(world, pos, true);
			}
		}
	}

	private boolean checkY(BlockRenderView world, BlockPos pos, boolean changeMap) {
		boolean connectedUp = isConnected(world, pos, UP);
		boolean connectedDown = isConnected(world, pos, DOWN);
		if (connectedUp || connectedDown) {
			if (changeMap) {
				setConnected(UP, connectedUp);
				setConnected(DOWN, connectedDown);
			}
			return true;
		}
		return false;
	}

	private boolean checkX(BlockRenderView world, BlockPos pos, boolean changeMap) {
		boolean connectedEast = isConnected(world, pos, EAST);
		if (connectedEast) {
			connectedEast = !checkY(world, pos.offset(EAST), false);
		}
		boolean connectedWest = isConnected(world, pos, WEST);
		if (connectedWest) {
			connectedWest = !checkY(world, pos.offset(WEST), false);
		}
		if (connectedEast || connectedWest) {
			if (changeMap) {
				setConnected(EAST, connectedEast);
				setConnected(WEST, connectedWest);
			}
			return true;
		}
		return false;
	}

	private boolean checkZ(BlockRenderView world, BlockPos pos, boolean changeMap) {
		BlockPos offsetPos;
		boolean connectedSouth = isConnected(world, pos, SOUTH);
		if (connectedSouth) {
			offsetPos = pos.offset(SOUTH);
			connectedSouth = !checkX(world, offsetPos, false) && !checkY(world, offsetPos, false);
		}
		boolean connectedNorth = isConnected(world, pos, NORTH);
		if (connectedNorth) {
			offsetPos = pos.offset(NORTH);
			connectedNorth = !checkX(world, offsetPos, false) && !checkY(world, offsetPos, false);
		}
		if (connectedSouth || connectedNorth) {
			if (changeMap) {
				setConnected(SOUTH, connectedSouth);
				setConnected(NORTH, connectedNorth);
			}
			return true;
		}
		return false;
	}
}
