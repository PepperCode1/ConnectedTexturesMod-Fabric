package team.chisel.ctm.client.texture.context;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.util.connection.PillarConnectionLogic;
import team.chisel.ctm.client.util.connection.SpacialConnectionLogic;

public class TextureContextPillar implements TextureContext {
	private SpacialConnectionLogic logic;
	private long serialized;

	public TextureContextPillar(BlockRenderView world, BlockPos pos) {
		logic = new PillarConnectionLogic();
		logic.buildConnectionMap(world, pos);
		serialized = logic.serialize();
	}

	public TextureContextPillar(long data) {
		this.serialized = data;
		logic = new PillarConnectionLogic();
		logic.deserialize(this.serialized);
	}

	public SpacialConnectionLogic getLogic() {
		return logic;
	}

	@Override
	public long serialize() {
		return serialized;
	}
}
