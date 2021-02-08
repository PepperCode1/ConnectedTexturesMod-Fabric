package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.texture.TexturePlane;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;

public class TextureTypePlane implements TextureType {
	public static final TextureTypePlane HORIZONRAL = new TextureTypePlane(Direction.Type.HORIZONTAL);
	public static final TextureTypePlane VERTICAL = new TextureTypePlane(Direction.Type.VERTICAL);

	private final Direction.Type plane;

	public TextureTypePlane(Direction.Type plane) {
		this.plane = plane;
	}

	@Override
	public CTMTexture<TextureTypePlane> makeTexture(TextureInfo info) {
		return new TexturePlane(this, info);
	}

	@Override
	public TextureContext getTextureContext(BlockState state, BlockRenderView world, BlockPos pos, CTMTexture<?> texture) {
		return new TextureContextConnecting(state, world, pos, (AbstractConnectingTexture<?>) texture);
	}

	@Override
	public int requiredTextures() {
		return 1;
	}

	public Direction.Type getPlane() {
		return plane;
	}
}
