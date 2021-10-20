package team.chisel.ctm.client.texture.context;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.util.connection.ConnectionLogicEdges;

public class TextureContextEdges extends TextureContextConnecting {
	public TextureContextEdges(@NotNull BlockState state, BlockRenderView world, BlockPos pos, AbstractConnectingTexture<?> texture) {
		super(state, world, pos, texture);
	}

	@Override
	protected ConnectionLogicEdges createLogic(BlockRenderView world, BlockPos pos, Direction face) {
		ConnectionLogicEdges logic = new ConnectionLogicEdges();
		texture.configureLogic(logic);
		logic.buildConnectionMap(world, pos, face);
		return logic;
	}

	@Override
	public ConnectionLogicEdges getLogic(Direction face) {
		return (ConnectionLogicEdges) super.getLogic(face);
	}
}
