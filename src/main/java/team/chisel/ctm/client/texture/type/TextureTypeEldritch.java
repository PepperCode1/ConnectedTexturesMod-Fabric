package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.client.texture.TextureEldritch;
import team.chisel.ctm.client.texture.context.TextureContextEldritch;

public class TextureTypeEldritch implements TextureType {
	@Override
	public CTMTexture<TextureTypeEldritch> makeTexture(TextureInfo info) {
		return new TextureEldritch(this, info);
	}

	@Override
	public TextureContext getTextureContext(BlockState state, BlockRenderView world, BlockPos pos, CTMTexture<?> texture) {
		return new TextureContextEldritch(pos);
	}

	@Override
	public TextureContext getContextFromData(long data) {
		return new TextureContextEldritch(BlockPos.fromLong(data));
	}
}
