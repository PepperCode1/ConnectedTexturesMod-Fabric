package team.chisel.ctm.api.client;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface ContextProvider {
	/**
	 * Called to create the context for an upcoming mesh build. This context will then be passed to
	 * {@link CTMTexture#transformQuad(net.minecraft.client.renderer.block.model.BakedQuad, TextureContext, int, net.minecraft.util.math.Direction)}.
	 * @param state The state of the block being rendered.
	 * @param world The current world.
	 * @param pos The position of the block being rendered.
	 * @param texture The {@link CTMTexture} being rendered.
	 * @return The context which can be used to manipulate quads later in the pipeline.
	 */
	TextureContext getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> texture);

	/**
	 * Recreates a TextureContext from compressed data.<br>
	 * As of yet, this method is unused.
	 * @param data The compressed data, which will match what is produced by {@link TextureContext#getCompressedData()}.
	 */
	@Deprecated
	default TextureContext getContextFromData(long data) {
		throw new UnsupportedOperationException();
	}
}
