package team.chisel.ctm.client.render;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.texture.Sprite;
import team.chisel.ctm.client.mixin.BakedQuadAccessor;

public class BakedQuadUtil {
	public static BakedQuad retextureQuad(BakedQuad quad, Sprite texture) {
		int[] newData = quad.getVertexData().clone();
		remapQuad(newData, ((BakedQuadAccessor) quad).getSprite(), texture);
		return new BakedQuad(newData, quad.getColorIndex(), BakedQuadFactory.decodeDirection(quad.getVertexData()), texture, quad.hasShade());
	}
	
	public static void remapQuad(int[] vertexData, Sprite oldTexture, Sprite texture) {
		int uvIndex = 4;
		for (int i = 0; i < 4; ++i) {
			int j = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger() * i;
			vertexData[j + uvIndex] = Float.floatToRawIntBits(texture.getFrameU(uninterpolateU(oldTexture, Float.intBitsToFloat(vertexData[j + uvIndex]))));
			vertexData[j + uvIndex + 1] = Float.floatToRawIntBits(texture.getFrameV(uninterpolateV(oldTexture, Float.intBitsToFloat(vertexData[j + uvIndex + 1]))));
		}
	}
	
	public static float uninterpolateU(Sprite sprite, float u) {
		float f = sprite.getMaxU() - sprite.getMinU();
		return (u - sprite.getMinU()) / f * 16.0F;
	}
	
	public static float uninterpolateV(Sprite sprite, float v) {
		float f = sprite.getMaxV() - sprite.getMinV();
		return (v - sprite.getMinV()) / f * 16.0F;
	}
}
