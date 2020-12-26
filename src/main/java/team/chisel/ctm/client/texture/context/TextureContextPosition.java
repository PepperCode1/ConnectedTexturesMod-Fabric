package team.chisel.ctm.client.texture.context;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import team.chisel.ctm.api.client.OffsetProviderRegistry;
import team.chisel.ctm.api.client.TextureContext;

public class TextureContextPosition implements TextureContext {
	@NotNull
	protected BlockPos pos;

	public TextureContextPosition(@NotNull BlockPos pos) {
		this.pos = pos;
	}

	public TextureContextPosition(int x, int y, int z) {
		this(new BlockPos(x, y, z));
	}

	@SuppressWarnings("resource")
	public TextureContextPosition applyOffset() {
		pos = pos.add(OffsetProviderRegistry.INSTANCE.getOffset(MinecraftClient.getInstance().world, pos));
		return this;
	}

	@NotNull
	public BlockPos getPosition() {
		return pos;
	}

	@Override
	public long getCompressedData() {
		return 0L; // Position data is not useful for serialization (and in fact breaks caching as each location is a new key)
	}
}
