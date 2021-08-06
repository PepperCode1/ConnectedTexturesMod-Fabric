package team.chisel.ctm.client.util;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import team.chisel.ctm.client.mixin.BakedQuadAccessor;

public class RenderUtil {
	public static final Direction[] CULL_FACES = ArrayUtils.add(Direction.values(), null);

	public static final int U_OFFSET = 4;
	public static final int V_OFFSET = 5;

	public static BakedQuad retextureBakedQuad(BakedQuad quad, Sprite sprite) {
		int[] newData = quad.getVertexData().clone();
		retextureBakedQuadData(newData, ((BakedQuadAccessor) quad).getSprite(), sprite);
		return new BakedQuad(newData, quad.getColorIndex(), quad.getFace(), sprite, quad.hasShade());
	}

	public static void retextureBakedQuadData(int[] vertexData, Sprite oldSprite, Sprite sprite) {
		for (int i = 0; i < 4; ++i) {
			int j = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger() * i;
			vertexData[j + U_OFFSET] = Float.floatToRawIntBits(MathHelper.lerp(MathUtil.getLerpProgress(Float.intBitsToFloat(vertexData[j + U_OFFSET]), oldSprite.getMinU(), oldSprite.getMaxU()), sprite.getMinU(), sprite.getMaxU()));
			vertexData[j + V_OFFSET] = Float.floatToRawIntBits(MathHelper.lerp(MathUtil.getLerpProgress(Float.intBitsToFloat(vertexData[j + V_OFFSET]), oldSprite.getMinV(), oldSprite.getMaxV()), sprite.getMinV(), sprite.getMaxV()));
		}
	}

	public static int getColor(int alpha, int red, int green, int blue) {
		return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
	}

	public static int getLight(int skyLight, int blockLight) {
		return (skyLight & 0xF) << 20 | (blockLight & 0xF) << 4;
	}
}
