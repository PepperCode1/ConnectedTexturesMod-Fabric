package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.ProfileUtil;
import team.chisel.ctm.client.util.RegionCache;

public class TextureContextList {
	private static final ThreadLocal<RegionCache> REGION_CACHE = ThreadLocal.withInitial(() -> new RegionCache(BlockPos.ORIGIN, 0, null));

	private final Map<CTMTexture<?>, TextureContext> contextMap = new IdentityHashMap<>();
	private final Object2LongMap<CTMTexture<?>> serialized = new Object2LongOpenCustomHashMap<>(new IdentityStrategy<>());

	public TextureContextList(BlockState state, Collection<CTMTexture<?>> textures, final BlockView world, BlockPos pos) {
		ProfileUtil.push("ctm_region_cache_update");
		BlockView cachedWorld = REGION_CACHE.get().updateWorld(world);

		ProfileUtil.swap("ctm_context_gather");
		for (CTMTexture<?> texture : textures) {
			TextureContext context = texture.getType().getTextureContext(state, cachedWorld, pos, texture);
			if (context != null) {
				contextMap.put(texture, context);
				serialized.put(texture, context.getCompressedData());
			}
		}
		ProfileUtil.pop();
	}

	@Nullable
	public TextureContext getTextureContext(CTMTexture<?> texture) {
		return contextMap.get(texture);
	}

	public boolean contains(CTMTexture<?> texture) {
		return contextMap.containsKey(texture);
	}

	public Object2LongMap<CTMTexture<?>> serialized() {
		return serialized;
	}
}
