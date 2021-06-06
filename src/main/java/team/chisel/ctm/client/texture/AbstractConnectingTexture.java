package team.chisel.ctm.client.texture;

import java.util.Optional;
import java.util.function.BiPredicate;

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
import team.chisel.ctm.client.util.ParseUtil;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.client.util.connection.ConnectionLogic.StateComparator;

public abstract class AbstractConnectingTexture<T extends TextureType> extends AbstractTexture<T> {
	protected Optional<Boolean> connectInside;
	protected boolean ignoreStates;
	protected boolean untransform;

	@Nullable
	private final BiPredicate<Direction, BlockState> connectionChecks;
	private final StateComparator comparator;

	public AbstractConnectingTexture(T type, TextureInfo info) {
		super(type, info);

		connectInside = info.getExtraInfo().flatMap((obj) -> ParseUtil.getOptionalBoolean(obj, "connect_inside"));
		ignoreStates = info.getExtraInfo().map((obj) -> JsonHelper.getBoolean(obj, "ignore_states", false)).orElse(false);
		untransform = info.getExtraInfo().map((obj) -> JsonHelper.getBoolean(obj, "untransform", false)).orElse(false);

		connectionChecks = info.getExtraInfo().map((obj) -> BlockStatePredicateParser.INSTANCE.parse(obj.get("connect_to"))).orElse(null);
		if (connectionChecks == null) {
			comparator = StateComparator.DEFAULT;
		} else {
			comparator = (logic, from, to, side) -> connectionChecks.test(side, to);
		}
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

	public void configureLogic(@NotNull ConnectionLogic logic) {
		logic.disableObscuredFaceCheck = connectInside();
		logic.ignoreStates(ignoreStates());
		logic.setStateComparator(comparator);
	}
}
