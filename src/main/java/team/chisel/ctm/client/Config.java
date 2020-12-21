package team.chisel.ctm.client;

import net.minecraft.client.MinecraftClient;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

public class Config {
	/**
	 * Disable connected textures entirely.
	 */
	public boolean disableCTM = false;
	
	/**
	 * Choose whether the inside corner is disconnected on a CTM block - http://imgur.com/eUywLZ4
	 */
	public boolean connectInsideCTM = false;
	
	@SuppressWarnings("resource")
	public static void onConfigChange() {
		AbstractCTMBakedModel.invalidateCaches();
		MinecraftClient.getInstance().worldRenderer.reload();
	}
}
