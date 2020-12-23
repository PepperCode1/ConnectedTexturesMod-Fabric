package team.chisel.ctm.client.texture.context;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import team.chisel.ctm.api.texture.OffsetProviderRegistry;
import team.chisel.ctm.api.texture.TextureContext;

public class TextureContextPosition implements TextureContext {
	@NotNull
	protected BlockPos position;

	public TextureContextPosition(@NotNull BlockPos pos) {
		this.position = pos;
	}

	public TextureContextPosition(int x, int y, int z) {
		this(new BlockPos(x, y, z));
	}

	@SuppressWarnings("resource")
	public TextureContextPosition applyOffset() {
		this.position = position.add(OffsetProviderRegistry.INSTANCE.getOffset(MinecraftClient.getInstance().world, position));
		return this;
	}

	public @NotNull BlockPos getPosition() {
		return position;
	}

	@Override
	public long getCompressedData() {
		return 0L; // Position data is not useful for serialization (and in fact breaks caching as each location is a new key)
	}
}
