package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.texture.TextureEdgesFull;
import team.chisel.ctm.client.texture.context.TextureContextEdges;

public class TextureTypeEdgesFull implements TextureType {
	@Override
	public CTMTexture<TextureTypeEdgesFull> makeTexture(TextureInfo info) {
		return new TextureEdgesFull(this, info);
	}

	@Override
	public TextureContext getTextureContext(BlockState state, BlockRenderView world, BlockPos pos, CTMTexture<?> texture) {
		return new TextureContextEdges(state, world, pos, (AbstractConnectingTexture<?>) texture);
	}

	@Override
	public int requiredTextures() {
		return 2;
	}
}
