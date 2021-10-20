package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.texture.TextureEdges;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.context.TextureContextEdges;

public class TextureTypeEdges extends TextureTypeCTM {
	@Override
	public CTMTexture<? extends TextureTypeCTM> makeTexture(TextureInfo info) {
		return new TextureEdges(this, info);
	}

	@Override
	public TextureContextConnecting getTextureContext(BlockState state, BlockRenderView world, BlockPos pos, CTMTexture<?> texture) {
		return new TextureContextEdges(state, world, pos, (AbstractConnectingTexture<?>) texture);
	}

	@Override
	public int requiredTextures() {
		return 3;
	}
}
