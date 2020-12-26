package team.chisel.ctm.client.render;

import net.minecraft.client.texture.Sprite;

public interface Submap {
	float getWidth();

	float getHeight();

	float getYOffset();

	float getXOffset();

	Submap normalize();

	Submap relativize();

	float getInterpolatedU(Sprite sprite, float u);

	float getInterpolatedV(Sprite sprite, float v);

	float[] toArray();
}
