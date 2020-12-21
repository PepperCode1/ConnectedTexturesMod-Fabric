package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.texture.TextureCTM;
import team.chisel.ctm.client.texture.context.TextureContextCTM;

public class TextureTypeCTM implements TextureType {
	@Override
	public CTMTexture<? extends TextureTypeCTM> makeTexture(TextureInfo info) {
	  return new TextureCTM<TextureTypeCTM>(this, info);
	}

	@Override
	public TextureContextCTM getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> tex) {
		return new TextureContextCTM(state, world, pos, (TextureCTM<?>) tex);
	}

	@Override
	public int getQuadsPerSide() {
		return CTMClient.getConfig().disableCTM ? 1 : 4;
	}

	@Override
	public int requiredTextures() {
		return 2;
	}

	@Override
	public TextureContext getContextFromData(long data) {
		throw new UnsupportedOperationException();
	}
}
