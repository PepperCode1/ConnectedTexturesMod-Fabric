package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.TextureEldritch;
import team.chisel.ctm.client.texture.context.TextureContextPosition;

public class TextureTypeEldritch implements TextureType {
	@Override
	public TextureContext getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> tex) {
		return new Context(pos);
	}

	@Override
	public TextureContext getContextFromData(long data) {
		return new Context(BlockPos.fromLong(data));
	}

	@Override
	public CTMTexture<TextureTypeEldritch> makeTexture(TextureInfo info) {
		return new TextureEldritch(this, info);
	}
	
	public static class Context extends TextureContextPosition {
		private final BlockPos wrappedPos;

		public Context(BlockPos pos) {
			super(pos);
			wrappedPos = new BlockPos(pos.getX() & 7, pos.getY() & 7, pos.getZ() & 7);
		}

		@Override
		public BlockPos getPosition() {
			return wrappedPos;
		}

		@Override
		public long getCompressedData() {
			return getPosition().asLong();
		}
	}
}
