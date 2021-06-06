package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;

public class TextureContextList {
	private final Map<CTMTexture<?>, TextureContext> contextMap = new HashMap<>();
	private final int hashCode;

	public TextureContextList(Collection<CTMTexture<?>> textures, BlockState state, BlockRenderView world, BlockPos pos) {
		int hash = 0;
		for (CTMTexture<?> texture : textures) {
			TextureContext context = texture.getType().getTextureContext(state, world, pos, texture);
			if (context != null) {
				contextMap.put(texture, context);
				hash += texture.hashCode() ^ Long.hashCode(context.getCompressedData());
			}
		}
		hashCode = hash;
	}

	@Nullable
	public TextureContext getContext(CTMTexture<?> texture) {
		return contextMap.get(texture);
	}

	public boolean containsContext(CTMTexture<?> texture) {
		return contextMap.containsKey(texture);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TextureContextList other = (TextureContextList) obj;
		if (hashCode != other.hashCode) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
