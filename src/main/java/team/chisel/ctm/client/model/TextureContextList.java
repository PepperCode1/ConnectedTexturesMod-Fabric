package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.ProfileUtil;

public class TextureContextList {
	private final Map<CTMTexture<?>, TextureContext> contextMap = new IdentityHashMap<>();
	private final Object2LongMap<CTMTexture<?>> serialized = new Object2LongOpenCustomHashMap<>(new IdentityStrategy<>());

	public TextureContextList(BlockState state, Collection<CTMTexture<?>> textures, BlockRenderView world, BlockPos pos) {
		ProfileUtil.push("ctm_context_gather");
		for (CTMTexture<?> texture : textures) {
			TextureContext context = texture.getType().getTextureContext(state, world, pos, texture);
			if (context != null) {
				contextMap.put(texture, context);
				serialized.put(texture, context.getCompressedData());
			}
		}
		ProfileUtil.pop();
	}

	@Nullable
	public TextureContext getContext(CTMTexture<?> texture) {
		return contextMap.get(texture);
	}

	public boolean containsContext(CTMTexture<?> texture) {
		return contextMap.containsKey(texture);
	}

	public Object2LongMap<CTMTexture<?>> serialized() {
		return serialized;
	}
}
