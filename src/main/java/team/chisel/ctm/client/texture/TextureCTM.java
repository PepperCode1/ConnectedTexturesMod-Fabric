package team.chisel.ctm.client.texture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.CTMLogic;
import team.chisel.ctm.client.render.CTMLogic.StateComparisonCallback;
import team.chisel.ctm.client.render.RenderableArray;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.resource.BlockStatePredicateParser;
import team.chisel.ctm.client.texture.context.TextureContextCTM;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.ParseUtil;

public class TextureCTM<T extends TextureTypeCTM> extends AbstractTexture<T> {
	private final Optional<Boolean> connectInside;
	private final boolean ignoreStates;
	private final boolean untransform;

	@Nullable
	private final BiPredicate<Direction, BlockState> connectionChecks;
	private final Map<CacheKey, Object2ByteMap<BlockState>> connectionCache = new HashMap<>();

	public TextureCTM(T type, TextureInfo info) {
		super(type, info);

		this.connectInside = info.getInfo().flatMap(obj -> ParseUtil.getBoolean(obj, "connect_inside"));
		this.ignoreStates = info.getInfo().map(obj -> JsonHelper.getBoolean(obj, "ignore_states", false)).orElse(false);
		this.untransform = info.getInfo().map(obj -> JsonHelper.getBoolean(obj, "untransform", false)).orElse(false);

		this.connectionChecks = info.getInfo().map(obj -> BlockStatePredicateParser.INSTANCE.parse(obj.get("connect_to"))).orElse(null);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, TextureContext context, int quadGoal, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);

		if (context == null || CTMClient.getConfigManager().getConfig().disableCTM) {
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		int[] submapIndexes = ((TextureContextCTM) context).getLogic(bakedQuad.getFace()).getSubmapIndices();
		SpriteUnbakedQuad[] quads = quad.toQuadrants();
		for (int i = 0; i < quads.length; i++) {
			if (quads[i] != null) {
				int quadrant = (i + 3) % 4;
				int submapIndex = submapIndexes[quadrant];

				int id = submapIndex / 2;
				id = id < 8 ? (((id < 4) ? 0 : 2) + (id % 2 == 0 ? 0 : 1)) : 4;
				Submap submap = getSubmap(id, quad.getAbsoluteUVRotation());

				quads[i].setUVBounds(sprites[submapIndex > 15 ? 0 : 1]);
				quads[i].applySubmap(submap);
			}
		}

		return new RenderableArray(quads);
	}

	@Override
	protected SpriteUnbakedQuad unbake(BakedQuad bakedQuad, Direction cullFace) {
		SpriteUnbakedQuad quad = super.unbake(bakedQuad, cullFace);
		if (untransform) {
			quad.untransformUVs();
		}
		return quad;
	}

	protected Submap getSubmap(int id, int rotation) {
		if (id == 4) {
			return SubmapImpl.X1;
		}
		if (rotation % 2 == 1) {
			if (id == 1) {
				id = 2;
			} else if (id == 2) {
				id = 1;
			}
		}
		return SubmapImpl.X2[id / 2][id % 2];
	}

	public Optional<Boolean> connectInside() {
		return connectInside;
	}

	public boolean ignoreStates() {
		return ignoreStates;
	}

	public boolean connectTo(CTMLogic logic, BlockState from, BlockState to, Direction dir) {
		synchronized (connectionCache) {
			Object2ByteMap<BlockState> sidecache = connectionCache.computeIfAbsent(new CacheKey(from, dir), k -> {
				Object2ByteMap<BlockState> map = new Object2ByteOpenCustomHashMap<>(new IdentityStrategy<>());
				map.defaultReturnValue((byte) -1);
				return map;
			});
			byte cached = sidecache.getByte(to);
			if (cached == -1) {
				sidecache.put(to, cached = (byte) ((connectionChecks == null ? StateComparisonCallback.DEFAULT.connects(logic, from, to, dir) : connectionChecks.test(dir, to)) ? 1 : 0));
			}
			return cached == 1;
		}
	}

	private static final class CacheKey {
		private final BlockState from;
		private final Direction dir;

		CacheKey(final BlockState from, final Direction dir) {
			this.from = from;
			this.dir = dir;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dir.hashCode();
			result = prime * result + System.identityHashCode(from);
			return result;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CacheKey other = (CacheKey) obj;
			if (dir != other.dir) return false;
			if (from != other.from) return false;
			return true;
		}
	}
}
