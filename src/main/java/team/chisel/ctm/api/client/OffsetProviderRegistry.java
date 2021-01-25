package team.chisel.ctm.api.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import team.chisel.ctm.impl.client.OffsetProviderRegistryImpl;

/**
 * Registry for {@link OffsetProvider}. Use {@link OffsetProvider#INSTANCE} to obtain an instance of this class.
 */
public interface OffsetProviderRegistry {
	OffsetProviderRegistry INSTANCE = OffsetProviderRegistryImpl.INSTANCE;

	void register(OffsetProvider provider);

	Vec3i getOffset(World world, BlockPos pos);
}
