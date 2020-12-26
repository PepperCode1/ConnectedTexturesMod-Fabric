package team.chisel.ctm.client.model;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class CTMRenderContext {
	private final BlockView world;
	private final BlockPos pos;
	@Nullable
	private TextureContextList contextCache;

	public CTMRenderContext(BlockView world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	public CTMRenderContext(CTMRenderContext parent) {
		this(parent.world, parent.pos);
	}

	public TextureContextList getContextList(BlockState state, AbstractCTMBakedModel model) {
		if (contextCache == null) {
			contextCache = new TextureContextList(state, model.getCTMTextures(), world, pos);
		}
		return contextCache;
	}

	public BlockView getWorld() {
		return world;
	}

	public BlockPos getPos() {
		return pos;
	}
}
