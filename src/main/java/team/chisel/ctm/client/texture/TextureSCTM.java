package team.chisel.ctm.client.texture;

import java.util.Optional;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.CTMLogic;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextCTM;
import team.chisel.ctm.client.texture.type.TextureTypeSCTM;
import team.chisel.ctm.client.util.ConnectionDirection;

public class TextureSCTM extends TextureCTM<TextureTypeSCTM> {
	public TextureSCTM(TextureTypeSCTM type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, TextureContext context, int quadGoal, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);
		CTMLogic logic = null;
		if (context instanceof TextureContextCTM) {
			logic = ((TextureContextCTM) context).getCTM(bakedQuad.getFace());
		}
		quad.setUVBounds(sprites[0]);
		quad.applySubmap(getSubmap(getSubmapId(logic), quad.getAbsoluteUVRotation()));
		return quad;
	}

	private int getSubmapId(CTMLogic logic) {
		if (logic == null) {
			return 0;
		}
		boolean top = logic.connected(ConnectionDirection.TOP);
		boolean bottom = logic.connected(ConnectionDirection.BOTTOM);
		boolean left = logic.connected(ConnectionDirection.LEFT);
		boolean right = logic.connected(ConnectionDirection.RIGHT);
		if (top || bottom || left || right) {
			if (!top || !bottom) {
				return (left && right) ? 1 : 0;
			}
			if (!left || !right) {
				return 2;
			}
			if (logic.connected(ConnectionDirection.TOP_LEFT) && logic.connected(ConnectionDirection.TOP_RIGHT)) {
				if (logic.connected(ConnectionDirection.BOTTOM_LEFT) && logic.connected(ConnectionDirection.BOTTOM_RIGHT)) {
					return 3;
				}
			}
		}
		return 0;
	}

	@Override
	public Optional<Boolean> connectInside() {
		return Optional.of(true);
	}
}
