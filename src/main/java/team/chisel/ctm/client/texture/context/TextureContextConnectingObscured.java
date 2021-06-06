package team.chisel.ctm.client.texture.context;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.util.connection.ConnectionLogicObscured;

public class TextureContextConnectingObscured extends TextureContextConnecting {
	public TextureContextConnectingObscured(@NotNull BlockState state, BlockRenderView world, BlockPos pos, AbstractConnectingTexture<?> texture) {
		super(state, world, pos, texture);
	}

	@Override
	protected ConnectionLogicObscured createLogic(BlockRenderView world, BlockPos pos, Direction face) {
		ConnectionLogicObscured logic = new ConnectionLogicObscured();
		texture.configureLogic(logic);
		logic.buildConnectionMap(world, pos, face);
		return logic;
	}

	@Override
	public ConnectionLogicObscured getLogic(Direction face) {
		return (ConnectionLogicObscured) super.getLogic(face);
	}
}
