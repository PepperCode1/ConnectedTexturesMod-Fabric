package team.chisel.ctm.api.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.ProfileUtil;
import team.chisel.ctm.client.util.RegionCache;

public class TextureContextList {
	private static final ThreadLocal<RegionCache> REGION_CACHE = ThreadLocal.withInitial(() -> new RegionCache(BlockPos.ORIGIN, 0, null));

	private final Map<CTMTexture<?>, TextureContext> contextMap = Maps.newIdentityHashMap();
	private final Object2LongMap<CTMTexture<?>> serialized = new Object2LongOpenCustomHashMap<>(new IdentityStrategy<>());

	public TextureContextList(BlockState state, Collection<CTMTexture<?>> textures, final BlockView world, BlockPos pos) {
		ProfileUtil.push("ctm_region_cache_update");
		BlockView cachedWorld = REGION_CACHE.get().updateWorld(world);

		ProfileUtil.swap("ctm_context_gather");
		for (CTMTexture<?> tex : textures) {
			TextureType type = tex.getType();
			TextureContext ctx = type.getTextureContext(state, cachedWorld, pos, tex);
			if (ctx != null) {
				contextMap.put(tex, ctx);
			}
		}

		ProfileUtil.swap("ctm_context_serialize");
		for (Entry<CTMTexture<?>, TextureContext> e : contextMap.entrySet()) {
			serialized.put(e.getKey(), e.getValue().getCompressedData());
		}
		ProfileUtil.pop();
	}

	public @Nullable TextureContext getTextureContext(CTMTexture<?> tex) {
		return this.contextMap.get(tex);
	}

	public boolean contains(CTMTexture<?> tex) {
		return getTextureContext(tex) != null;
	}

	public Object2LongMap<CTMTexture<?>> serialized() {
		return serialized;
	}
}
