package team.chisel.ctm.client.texture;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.Submap;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.RenderableList;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextCTM;
import team.chisel.ctm.client.texture.type.TextureTypeEdges;
import team.chisel.ctm.client.texture.type.TextureTypeEdges.CTMLogicEdges;
import team.chisel.ctm.client.util.ConnectionDirection;

public class TextureEdgesFull extends TextureEdges {
	public TextureEdgesFull(TextureTypeEdges type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
		SpriteUnbakedQuad quad = unbake(bakedQuad, cullFace);

		if (context == null) {
			quad.setUVBounds(sprites[0]);
			return quad;
		}

		CTMLogicEdges logic = (CTMLogicEdges) ((TextureContextCTM) context).getCTM(bakedQuad.getFace());
		Sprite sprite;
		Submap submap = null;
		// Short circuit zero connections, as this is almost always the most common case
		if (!logic.isObscured() && logic.connectedNone(ConnectionDirection.VALUES)) {
			sprite = sprites[0];
			submap = SubmapImpl.X1;
		} else {
			sprite = sprites[1];
			boolean top = logic.connected(ConnectionDirection.TOP) || logic.connectedAnd(ConnectionDirection.TOP_LEFT, ConnectionDirection.TOP_RIGHT);
			boolean right = logic.connected(ConnectionDirection.RIGHT) || logic.connectedAnd(ConnectionDirection.TOP_RIGHT, ConnectionDirection.BOTTOM_RIGHT);
			boolean bottom = logic.connected(ConnectionDirection.BOTTOM) || logic.connectedAnd(ConnectionDirection.BOTTOM_LEFT, ConnectionDirection.BOTTOM_RIGHT);
			boolean left = logic.connected(ConnectionDirection.LEFT) || logic.connectedAnd(ConnectionDirection.TOP_LEFT, ConnectionDirection.BOTTOM_LEFT);

			if (logic.isObscured() || (top && bottom) || (right && left)) {
				submap = SubmapImpl.X4[2][1];
			} else if (!(top || right || bottom || left) && logic.connectedAnd(ConnectionDirection.TOP_LEFT, ConnectionDirection.BOTTOM_RIGHT)) {
				submap = SubmapImpl.X4[0][1];
			} else if (!(top || right || bottom || left) && logic.connectedAnd(ConnectionDirection.TOP_RIGHT, ConnectionDirection.BOTTOM_LEFT)) {
				submap = SubmapImpl.X4[0][2];
			} else if (!(bottom || right) && logic.connectedOr(ConnectionDirection.LEFT, ConnectionDirection.BOTTOM_LEFT) && logic.connectedOr(ConnectionDirection.TOP, ConnectionDirection.TOP_RIGHT)) {
				submap = SubmapImpl.X4[0][3];
			} else if (!(bottom || left) && logic.connectedOr(ConnectionDirection.TOP, ConnectionDirection.TOP_LEFT) && logic.connectedOr(ConnectionDirection.RIGHT, ConnectionDirection.BOTTOM_RIGHT)) {
				submap = SubmapImpl.X4[1][3];
			} else if (!(top || left) && logic.connectedOr(ConnectionDirection.RIGHT, ConnectionDirection.TOP_RIGHT) && logic.connectedOr(ConnectionDirection.BOTTOM, ConnectionDirection.BOTTOM_LEFT)) {
				submap = SubmapImpl.X4[2][3];
			} else if (!(top || right) && logic.connectedOr(ConnectionDirection.BOTTOM, ConnectionDirection.BOTTOM_RIGHT) && logic.connectedOr(ConnectionDirection.LEFT, ConnectionDirection.TOP_LEFT)) {
				submap = SubmapImpl.X4[3][3];
			} else if (bottom) {
				submap = SubmapImpl.X4[1][1];
			} else if (right) {
				submap = SubmapImpl.X4[2][0];
			} else if (left) {
				submap = SubmapImpl.X4[2][2];
			} else if (top) {
				submap = SubmapImpl.X4[3][1];
			} else if (logic.connected(ConnectionDirection.BOTTOM_LEFT)) {
				submap = SubmapImpl.X4[1][2];
			} else if (logic.connected(ConnectionDirection.BOTTOM_RIGHT)) {
				submap = SubmapImpl.X4[1][0];
			} else if (logic.connected(ConnectionDirection.TOP_RIGHT)) {
				submap = SubmapImpl.X4[3][0];
			} else if (logic.connected(ConnectionDirection.TOP_LEFT)) {
				submap = SubmapImpl.X4[3][2];
			}

			if (submap == null) {
				submap = SubmapImpl.X1;
			}
		}

		if (quadGoal == 1) {
			quad.setUVBounds(sprite);
			quad.applySubmap(submap);
			return quad;
		} else {
			List<UnbakedQuad> quads = new ArrayList<>();
			for (SpriteUnbakedQuad quad1 : quad.toQuadrants()) {
				if (quad1 != null) {
					quad1.setUVBounds(sprite);
					quad1.applySubmap(submap);
					quads.add(quad1);
				}
			}
			return new RenderableList(quads);
		}
	}
}
