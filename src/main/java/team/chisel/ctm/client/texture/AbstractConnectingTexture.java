package team.chisel.ctm.client.texture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.resource.BlockStatePredicateParser;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.ParseUtil;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.client.util.connection.ConnectionLogic.StateComparator;

public abstract class AbstractConnectingTexture<T extends TextureType> extends AbstractTexture<T> {
	protected Optional<Boolean> connectInside;
	protected boolean ignoreStates;
	protected boolean untransform;

	@Nullable
	private final BiPredicate<Direction, BlockState> connectionChecks;
	private final Map<CacheKey, Object2ByteMap<BlockState>> connectionCache = new HashMap<>();

	public AbstractConnectingTexture(T type, TextureInfo info) {
		super(type, info);

		connectInside = info.getExtraInfo().flatMap((obj) -> ParseUtil.getOptionalBoolean(obj, "connect_inside"));
		ignoreStates = info.getExtraInfo().map((obj) -> JsonHelper.getBoolean(obj, "ignore_states", false)).orElse(false);
		untransform = info.getExtraInfo().map((obj) -> JsonHelper.getBoolean(obj, "untransform", false)).orElse(false);

		connectionChecks = info.getExtraInfo().map((obj) -> BlockStatePredicateParser.INSTANCE.parse(obj.get("connect_to"))).orElse(null);
	}

	@Override
	protected UnbakedQuad unbake(BakedQuad bakedQuad, Direction cullFace) {
		UnbakedQuad quad = super.unbake(bakedQuad, cullFace);
		if (untransform) {
			quad.untransformUVs();
		}
		return quad;
	}

	public Optional<Boolean> connectInside() {
		return connectInside;
	}

	public boolean ignoreStates() {
		return ignoreStates;
	}

	public boolean connectTo(ConnectionLogic logic, BlockState from, BlockState to, Direction side) {
		synchronized (connectionCache) {
			Object2ByteMap<BlockState> sideCache = connectionCache.computeIfAbsent(new CacheKey(from, side), key -> {
				Object2ByteMap<BlockState> map = new Object2ByteOpenCustomHashMap<>(new IdentityStrategy<>());
				map.defaultReturnValue((byte) -1);
				return map;
			});
			byte cached = sideCache.getByte(to);
			if (cached == -1) {
				sideCache.put(to, cached = (byte) ((connectionChecks == null ? StateComparator.DEFAULT.connects(logic, from, to, side) : connectionChecks.test(side, to)) ? 1 : 0));
			}
			return cached == 1;
		}
	}

	public void configureLogic(@NotNull ConnectionLogic logic) {
		logic.disableObscuredFaceCheck = connectInside();
		logic.ignoreStates(ignoreStates());
		logic.setStateComparator(this::connectTo);
	}

	private static final class CacheKey {
		private final BlockState from;
		private final Direction side;

		CacheKey(final BlockState from, final Direction side) {
			this.from = from;
			this.side = side;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + side.hashCode();
			result = prime * result + System.identityHashCode(from);
			return result;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CacheKey other = (CacheKey) obj;
			if (side != other.side) return false;
			if (from != other.from) return false;
			return true;
		}
	}
}
