package team.chisel.ctm.client.texture.context;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.util.connection.PillarConnectionLogic;
import team.chisel.ctm.client.util.connection.SpacialConnectionLogic;

public class TextureContextPillar implements TextureContext {
	private SpacialConnectionLogic logic;
	private long data;

	public TextureContextPillar(BlockRenderView world, BlockPos pos) {
		logic = new PillarConnectionLogic();
		logic.buildConnectionMap(world, pos);
		data = logic.serialize();
	}

	public TextureContextPillar(long data) {
		this.data = data;
		logic = new PillarConnectionLogic();
		logic.deserialize(this.data);
	}

	public SpacialConnectionLogic getLogic() {
		return logic;
	}

	@Override
	public long getCompressedData() {
		return data;
	}
}
