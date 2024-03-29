package team.chisel.ctm.api.client;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import team.chisel.ctm.client.texture.TextureMap;

/**
 * Provides texture block offsets for some textures, such as {@link TextureMap}.
 */
public interface OffsetProvider {
	@NotNull
	Vec3i getOffset(@NotNull World world, @NotNull BlockPos pos);
}
