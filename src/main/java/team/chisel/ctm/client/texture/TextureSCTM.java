package team.chisel.ctm.client.texture;

import java.util.Optional;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.type.TextureTypeSCTM;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;

public class TextureSCTM extends AbstractConnectingTexture<TextureTypeSCTM> {
	public TextureSCTM(TextureTypeSCTM type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);

		int submapId = 0;
		if (!CTMClient.getConfigManager().getConfig().disableCTM && context instanceof TextureContextConnecting) {
			submapId = getSubmapId(((TextureContextConnecting) context).getLogic(quad.nominalFace));
		}

		quad.setUVBounds(sprites[0]);
		quad.applySubmap(TextureCTM.getX2Submap(submapId, quad.areUVsRotatedOnce()));
		return quad;
	}

	private int getSubmapId(ConnectionLogic logic) {
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
