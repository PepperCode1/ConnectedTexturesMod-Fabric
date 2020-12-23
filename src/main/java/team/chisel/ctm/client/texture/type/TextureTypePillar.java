package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.TexturePillar;
import team.chisel.ctm.client.texture.context.TextureContextPillar;

public class TextureTypePillar implements TextureType {
	@Override
	public CTMTexture<TextureTypePillar> makeTexture(TextureInfo info) {
		return new TexturePillar(this, info);
	}

	@Override
	public TextureContextPillar getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> texture) {
		return new TextureContextPillar(world, pos);
	}

	@Override
	public int requiredTextures() {
		return 2;
	}

	@Override
	public TextureContext getContextFromData(long data) {
		return new TextureContextPillar(data);
	}
}
