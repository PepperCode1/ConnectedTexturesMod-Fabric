package team.chisel.ctm.client.texture.context;

import java.util.EnumMap;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.util.connection.ConnectionLogic;

public class TextureContextConnecting implements TextureContext {
	public static final int CONNECTION_DATA_LENGTH = 10;

	protected final AbstractConnectingTexture<?> texture;
	private EnumMap<Direction, ConnectionLogic> logicMap = new EnumMap<>(Direction.class);
	private long serialized;

	public TextureContextConnecting(@NotNull BlockState state, BlockRenderView world, BlockPos pos, AbstractConnectingTexture<?> texture) {
		this.texture = texture;

		for (Direction face : Direction.values()) {
			ConnectionLogic logic = createLogic(world, pos, face);
			logicMap.put(face, logic);
			serialized |= (logic.serialize() & ((1 << CONNECTION_DATA_LENGTH) - 1)) << (face.ordinal() * CONNECTION_DATA_LENGTH);
		}
	}

	protected ConnectionLogic createLogic(BlockRenderView world, BlockPos pos, Direction face) {
		ConnectionLogic logic = new ConnectionLogic();
		texture.configureLogic(logic);
		logic.buildConnectionMap(world, pos, face);
		return logic;
	}

	public ConnectionLogic getLogic(Direction face) {
		return logicMap.get(face);
	}

	@Override
	public long serialize() {
		return serialized;
	}
}
