package team.chisel.ctm.impl.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import team.chisel.ctm.api.client.OffsetProvider;
import team.chisel.ctm.api.client.OffsetProviderRegistry;

public class OffsetProviderRegistryImpl implements OffsetProviderRegistry {
	private List<OffsetProvider> providers = new ArrayList<>();

	@Override
	public void register(OffsetProvider provider) {
		providers.add(provider);
	}

	@Override
	public Vec3i getOffset(World world, BlockPos pos) {
		int x = 0;
		int y = 0;
		int z = 0;
		Vec3i offset;
		for (OffsetProvider provider : providers) {
			offset = provider.getOffset(world, pos);
			x += offset.getX();
			y += offset.getY();
			z += offset.getZ();
		}
		return new Vec3i(x, y, z);
	}
}
