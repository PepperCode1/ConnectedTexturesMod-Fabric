package team.chisel.ctm.client.texture.type;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.client.texture.TextureMap;
import team.chisel.ctm.client.texture.TextureMap.MapType;
import team.chisel.ctm.client.texture.TextureMap.MapTypeImpl;
import team.chisel.ctm.client.texture.context.TextureContextPosition;

public class TextureTypeMap implements TextureType {
	public static final TextureTypeMap RANDOM = new TextureTypeMap(MapTypeImpl.RANDOM);
	public static final TextureTypeMap PATTERN = new TextureTypeMap(MapTypeImpl.PATTERNED);

	private final MapType type;

	public TextureTypeMap(final MapType type) {
		this.type = type;
	}

	@Override
	public TextureMap makeTexture(TextureInfo info) {
		return new TextureMap(this, info);
	}

	@Override
	public TextureContext getTextureContext(BlockState state, BlockRenderView world, @NotNull BlockPos pos, CTMTexture<?> texture) {
		if (!(texture instanceof TextureMap)) {
			return new TextureContextPosition(pos);
		}

		return type.getContext(pos, (TextureMap) texture);
	}

	@Override
	public TextureContext deserializeContext(long data) {
		return new TextureContextPosition(BlockPos.fromLong(data));
	}

	public MapType getType() {
		return type;
	}
}
