package team.chisel.ctm.client.texture.type;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.CTMLogic;
import team.chisel.ctm.client.texture.TextureCTM;
import team.chisel.ctm.client.texture.TextureSCTM;
import team.chisel.ctm.client.texture.context.TextureContextCTM;

public class TextureTypeSCTM extends TextureTypeCTM {
	@Override
	public CTMTexture<TextureTypeSCTM> makeTexture(TextureInfo info) {
		return new TextureSCTM(this, info);
	}

	@Override
	public TextureContextCTM getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> texture) {
		return new TextureContextCTM(state, world, pos, (TextureCTM<?>) texture) {
			@Override
			protected CTMLogic createCTM(BlockState state) {
				CTMLogic logic = super.createCTM(state);
				logic.disableObscuredFaceCheck = Optional.of(true);
				return logic;
			}
		};
	}

	@Override
	public int getQuadsPerSide() {
		return 1;
	}

	@Override
	public int requiredTextures() {
		return 1;
	}
}
