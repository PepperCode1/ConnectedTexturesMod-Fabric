package team.chisel.ctm.client.texture.context;

import java.util.EnumMap;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.client.render.CTMLogic;
import team.chisel.ctm.client.texture.TextureCTM;

public class TextureContextCTM implements TextureContext {
	protected final TextureCTM<?> texture;
	private EnumMap<Direction, CTMLogic> ctmData = new EnumMap<>(Direction.class);
	private long data;

	public TextureContextCTM(@NotNull BlockState state, BlockView world, BlockPos pos, TextureCTM<?> texture) {
		this.texture = texture;

		for (Direction face : Direction.values()) {
			CTMLogic logic = createCTM(state);
			logic.createSubmapIndices(world, pos, face);
			ctmData.put(face, logic);
			this.data |= logic.serialized() << (face.ordinal() * 10);
		}
	}

	protected CTMLogic createCTM(@NotNull BlockState state) {
		CTMLogic logic = new CTMLogic()
				.ignoreStates(texture.ignoreStates())
				.stateComparator(texture::connectTo);
		logic.disableObscuredFaceCheck = texture.connectInside();
		return logic;
	}

	public CTMLogic getCTM(Direction face) {
		return ctmData.get(face);
	}

	@Override
	public long getCompressedData() {
		return this.data;
	}
}
