package team.chisel.ctm.api.texture;

import net.minecraft.client.texture.Sprite;

public interface Submap {
	float getYOffset();

	float getXOffset();

	float getWidth();

	float getHeight();

	float getInterpolatedU(Sprite sprite, float u);

	float getInterpolatedV(Sprite sprite, float v);

	float[] toArray();

	Submap normalize();

	Submap relativize();
}
