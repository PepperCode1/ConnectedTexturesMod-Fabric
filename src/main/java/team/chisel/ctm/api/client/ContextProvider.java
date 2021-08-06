package team.chisel.ctm.api.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public interface ContextProvider {
	/**
	 * Creates the context for an upcoming mesh build. This context will then be passed to
	 * {@link CTMTexture#transformQuad(BakedQuad, TextureContext, Direction)}.
	 *
	 * @param state The state of the block being rendered.
	 * @param world The world.
	 * @param pos The position of the block being rendered.
	 * @param texture The {@link CTMTexture} being rendered.
	 * @return The context which can be used to manipulate quads later in the pipeline.
	 */
	TextureContext getTextureContext(BlockState state, BlockRenderView world, BlockPos pos, CTMTexture<?> texture);

	/**
	 * Recreates a TextureContext from compressed data.
	 *
	 * <p>As of yet, this method is unused.
	 *
	 * @param data The compressed data, which will match what is produced by {@link TextureContext#serialize()}.
	 */
	@Deprecated
	default TextureContext deserializeContext(long data) {
		throw new UnsupportedOperationException();
	}
}
