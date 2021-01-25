package team.chisel.ctm.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.MinecraftClient;

import team.chisel.ctm.client.model.AbstractCTMBakedModel;

public class ConfigManager {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();

	private File file;
	private Config config;

	public ConfigManager(File file) {
		this.file = file;
	}

	public Config getConfig() {
		return config;
	}

	public void load() {
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				config = GSON.fromJson(reader, Config.class);
			} catch (IOException e) {
				CTMClient.LOGGER.error("Error loading config.", e);
			}
		}
		if (config == null) {
			config = new Config();
			save();
		}
	}

	public void save() {
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(GSON.toJson(config));
		} catch (IOException e) {
			CTMClient.LOGGER.error("Error saving config.", e);
		}
	}

	@SuppressWarnings("resource")
	public void onConfigChange() {
		AbstractCTMBakedModel.invalidateCaches();
		MinecraftClient.getInstance().worldRenderer.reload();
		save();
	}

	public static class Config {
		/**
		 * Disable connected textures entirely.
		 */
		public boolean disableCTM = false;

		/**
		 * Choose whether the inside corner is disconnected on a CTM block. https://imgur.com/eUywLZ4
		 */
		public boolean connectInsideCTM = false;
	}
}
