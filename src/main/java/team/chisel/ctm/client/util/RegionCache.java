package team.chisel.ctm.client.util;

import java.lang.ref.WeakReference;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Used by render state creation to avoid unnecessary block lookups through the world.
 */
public class RegionCache implements BlockView {
	/*
	 * These are required for future use, in case there is ever a need to have this region cache only store a certain area of the world.
	 *
	 * Currently, this class is only used by CTM, which is limited to a very small subsection of the world,
	 * and thus the overhead of distance checking is unnecessary.
	 */
	//private final BlockPos center;
	//private final int radius;

	private WeakReference<BlockView> passthrough;
	private final Long2ObjectMap<BlockState> stateCache = new Long2ObjectOpenHashMap<>();

	public RegionCache(BlockPos center, int radius, @Nullable BlockView passthrough) {
		//this.center = center;
		//this.radius = radius;
		this.passthrough = new WeakReference<>(passthrough);
	}

	private BlockView getPassthrough() {
		BlockView ret = passthrough.get();
		Preconditions.checkNotNull(ret);
		return ret;
	}

	@NotNull
	public RegionCache updateWorld(BlockView passthrough) {
		// We do NOT use getPassthrough() here so as to skip the null-validation - it's obviously valid to be null here
		if (this.passthrough.get() != passthrough) {
			stateCache.clear();
		}
		this.passthrough = new WeakReference<>(passthrough);
		return this;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return getPassthrough().getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		long address = pos.asLong();
		BlockState ret = stateCache.get(address);
		if (ret == null) {
			stateCache.put(address, ret = getPassthrough().getBlockState(pos));
		}
		return ret;
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getPassthrough().getFluidState(pos);
	}
}
