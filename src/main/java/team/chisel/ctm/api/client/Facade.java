package team.chisel.ctm.api.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

/**
 * To be implemented on blocks that "hide" another block inside, so that connected textures can still be accomplished.
 */
public interface Facade {
	/**
	 * Gets the BlockState this facade appears as.
	 *
	 * @param world The world.
	 * @param pos The block's position.
	 * @param connection The position of the block being connected to. If this is null, the default facade state should be returned.
	 * @param side The side being rendered, <b>not</b> the side being connected from. This value can be null if no side is specified. Make sure this is handled appropriately.
	 * @return The BlockState which the block appears as.
	 */
	@NotNull
	BlockState getFacadeState(@NotNull BlockView world, @NotNull BlockPos pos, @Nullable BlockPos connection, @Nullable Direction side);
}
