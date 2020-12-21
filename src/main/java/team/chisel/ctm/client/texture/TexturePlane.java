package team.chisel.ctm.client.texture;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.Submap;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.CTMLogic;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.texture.context.TextureContextCTM;
import team.chisel.ctm.client.texture.type.TextureTypePlane;
import team.chisel.ctm.client.util.ConnectionDirection;

public class TexturePlane extends TextureCTM<TextureTypePlane> {
	private final Direction.Type plane;

	public TexturePlane(TextureTypePlane type, TextureInfo info) {
		super(type, info);
		this.plane = type.getPlane();
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, TextureContext context, int quadGoal, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);
		CTMLogic logic = null;
		if (context instanceof TextureContextCTM) {
			logic = ((TextureContextCTM) context).getCTM(bakedQuad.getFace());
		}
		quad.setUVBounds(sprites[0]);
		quad.applySubmap(getSubmap(logic));
		return quad;
	}

	private Submap getSubmap(CTMLogic logic) {
		if (logic == null) {
			return SubmapImpl.X2[0][0];
		}
		int u;
		int v;
		if (this.plane == Direction.Type.VERTICAL) {
			boolean top = logic.connected(ConnectionDirection.TOP);
			u = (top == logic.connected(ConnectionDirection.BOTTOM)) ? 0 : 1;
			v = top ? 1 : 0;
		} else {
			boolean left = logic.connected(ConnectionDirection.LEFT);
			u = left ? 1 : 0;
			v = (left == logic.connected(ConnectionDirection.RIGHT)) ? 0 : 1;
		}
		return SubmapImpl.X2[v][u];
	}
}
