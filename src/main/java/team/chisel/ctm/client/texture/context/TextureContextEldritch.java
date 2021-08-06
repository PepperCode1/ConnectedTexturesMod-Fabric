package team.chisel.ctm.client.texture.context;

import net.minecraft.util.math.BlockPos;

public class TextureContextEldritch extends TextureContextPosition {
	private final BlockPos wrappedPos;

	public TextureContextEldritch(BlockPos pos) {
		super(pos);
		wrappedPos = new BlockPos(pos.getX() & 7, pos.getY() & 7, pos.getZ() & 7);
	}

	@Override
	public BlockPos getPosition() {
		return wrappedPos;
	}

	@Override
	public long serialize() {
		return getPosition().asLong();
	}
}
