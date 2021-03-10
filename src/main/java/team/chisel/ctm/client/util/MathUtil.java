package team.chisel.ctm.client.util;

public class MathUtil {
	public static int lerp(float delta, int start, int end) {
		return (int) (start + delta * (end - start));
	}

	public static float getLerpProgress(float value, float start, float end) {
		return (value - start) / (end - start);
	}
}
