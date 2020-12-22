package team.chisel.ctm.client.texture;

import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.RenderableList;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextPosition;
import team.chisel.ctm.client.texture.type.TextureTypeEldritch;

public class TextureEldritch extends AbstractTexture<TextureTypeEldritch> {
	private static final Random RANDOM = new Random();

	public TextureEldritch(TextureTypeEldritch type, TextureInfo info) {
		super(type, info);
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
		UnbakedQuad quad = unbake(bakedQuad, cullFace);

		float[] bounds = quad.getSmallestUVBounds();
		Vec2f min = new Vec2f(bounds[0], bounds[1]);
		Vec2f max = new Vec2f(bounds[2], bounds[3]);

		BlockPos pos = context == null ? BlockPos.ORIGIN : ((TextureContextPosition) context).getPosition();
		RANDOM.setSeed(MathHelper.hashCode(pos) + (bakedQuad.getFace() == null ? -1 : bakedQuad.getFace().ordinal()));

		float xOffset = getRandomOffset();
		float yOffset = getRandomOffset();

		UnbakedQuad[] quads = quad.toQuadrants();
		for (int i = 0; i < quads.length; i++) {
			UnbakedQuad quadrant = quads[i];
			for (int j = 0; quadrant != null && j < 4; j++) {
				Vec2f uv = new Vec2f(quadrant.vertexes[j].u, quadrant.vertexes[j].v);
				if (uv.x != min.x && uv.x != max.x && uv.y != min.y && uv.y != max.y) {
					float xinterp = (float) MathHelper.getLerpProgress(uv.x, min.x, max.x);
					float yinterp = (float) MathHelper.getLerpProgress(uv.y, min.y, max.y);
					xinterp += xOffset;
					yinterp += yOffset;
					uv = new Vec2f(MathHelper.lerp(xinterp, min.x, max.x), MathHelper.lerp(yinterp, min.y, max.y));
					quadrant.vertexes[j].u = uv.x;
					quadrant.vertexes[j].v = uv.y;
				}
			}
		}

		return new RenderableList(List.of(quads));
	}

	private float getRandomOffset() {
		return (float) RANDOM.nextGaussian() * 0.08f;
	}
}
