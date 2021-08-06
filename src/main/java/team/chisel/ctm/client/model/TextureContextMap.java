package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;

public class TextureContextMap {
	private final Map<CTMTexture<?>, TextureContext> contextMap = new HashMap<>();
	private final LongList dataList = new LongArrayList();

	public void fill(Collection<CTMTexture<?>> textures, BlockState state, BlockRenderView world, BlockPos pos) {
		for (CTMTexture<?> texture : textures) {
			TextureContext context = texture.getType().getTextureContext(state, world, pos, texture);
			if (context != null) {
				contextMap.put(texture, context);
				dataList.add(context.serialize());
			}
		}
	}

	public void reset() {
		contextMap.clear();
		dataList.clear();
	}

	@Nullable
	public TextureContext getContext(CTMTexture<?> texture) {
		return contextMap.get(texture);
	}

	public boolean containsContext(CTMTexture<?> texture) {
		return contextMap.containsKey(texture);
	}

	public long[] toDataArray() {
		return dataList.toLongArray();
	}
}
