package team.chisel.ctm.client.util;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.MathHelper;

import team.chisel.ctm.client.mixin.BakedQuadAccessor;

public class RenderUtil {
	public static final int UV_OFFSET = 4;

	public static BakedQuad retextureBakedQuad(BakedQuad quad, Sprite sprite) {
		int[] newData = quad.getVertexData().clone();
		retextureBakedQuadData(newData, ((BakedQuadAccessor) quad).getSprite(), sprite);
		return new BakedQuad(newData, quad.getColorIndex(), quad.getFace(), sprite, quad.hasShade());
	}

	public static void retextureBakedQuadData(int[] vertexData, Sprite oldSprite, Sprite sprite) {
		for (int i = 0; i < 4; ++i) {
			int j = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger() * i;
			vertexData[j + UV_OFFSET] = Float.floatToRawIntBits(MathHelper.lerp(MathUtil.getLerpProgress(Float.intBitsToFloat(vertexData[j + UV_OFFSET]), oldSprite.getMinU(), oldSprite.getMaxU()), sprite.getMinU(), sprite.getMaxU()));
			vertexData[j + UV_OFFSET + 1] = Float.floatToRawIntBits(MathHelper.lerp(MathUtil.getLerpProgress(Float.intBitsToFloat(vertexData[j + UV_OFFSET + 1]), oldSprite.getMinV(), oldSprite.getMaxV()), sprite.getMinV(), sprite.getMaxV()));
		}
	}

	public static int getColor(int alpha, int red, int green, int blue) {
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	public static int getLight(int skyLight, int blockLight) {
		return skyLight << 20 | blockLight << 4;
	}
}
