package team.chisel.ctm.client.texture.type;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.TextureMap;
import team.chisel.ctm.client.texture.TextureMap.MapType;
import team.chisel.ctm.client.texture.context.TextureContextPosition;

public class TextureTypeMap implements TextureType {
	public static final TextureTypeMap R = new TextureTypeMap(MapType.RANDOM);
	public static final TextureTypeMap V = new TextureTypeMap(MapType.PATTERNED);
	
	private final MapType type;

	public TextureTypeMap(final MapType type) {
		this.type = type;
	}

	@Override
	public TextureMap makeTexture(TextureInfo info) {
		return new TextureMap(this, info);
	}

	@Override
	public TextureContext getTextureContext(BlockState state, BlockView world, @NotNull BlockPos pos, CTMTexture<?> tex) {
		return type.getContext(pos, (TextureMap) tex);
	}

	@Override
	public TextureContext getContextFromData(long data) {
		return new TextureContextPosition(BlockPos.fromLong(data));
	}
	
	public MapType getMapType() {
		return this.type;
	}
}
