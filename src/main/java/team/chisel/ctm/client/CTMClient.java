package team.chisel.ctm.client;

import java.io.File;
import java.nio.file.Path;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.texture.TextureTypeRegistry;
import team.chisel.ctm.client.event.AtlasStitchCallback;
import team.chisel.ctm.client.event.DeserializeModelJsonCallback;
import team.chisel.ctm.client.event.ModelsAddedCallback;
import team.chisel.ctm.client.event.ModelsLoadedCallback;
import team.chisel.ctm.client.handler.CTMAtlasStitchCallbackHandler;
import team.chisel.ctm.client.handler.CTMDeserializeModelJsonCallbackHandler;
import team.chisel.ctm.client.handler.CTMModelsAddedCallbackHandler;
import team.chisel.ctm.client.handler.CTMModelsLoadedCallbackHandler;
import team.chisel.ctm.client.handler.WrappingCache;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;
import team.chisel.ctm.client.texture.type.TextureTypeEdges;
import team.chisel.ctm.client.texture.type.TextureTypeEdgesFull;
import team.chisel.ctm.client.texture.type.TextureTypeEldritch;
import team.chisel.ctm.client.texture.type.TextureTypeMap;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;
import team.chisel.ctm.client.texture.type.TextureTypePillar;
import team.chisel.ctm.client.texture.type.TextureTypePlane;
import team.chisel.ctm.client.texture.type.TextureTypeSCTM;

public class CTMClient implements ClientModInitializer {
	public static final String MOD_ID = "ctm";
	public static final Logger LOGGER = LogManager.getLogger();

	private static CTMClient instance;
	private static ConfigManager configManager;

	public static CTMClient getInstance() {
		return instance;
	}

	public static ConfigManager getConfigManager() {
		if (configManager == null) {
			Path configPath = FabricLoader.getInstance().getConfigDir();
			File configFile = new File(configPath.toFile(), "ctm.json");
			configManager = new ConfigManager(configFile);
			configManager.load();
		}
		return configManager;
	}

	@Override
	public void onInitializeClient() {
		instance = this;

		getConfigManager();

		WrappingCache wrappingCache = new WrappingCache();
		DeserializeModelJsonCallback.EVENT.register(new CTMDeserializeModelJsonCallbackHandler(wrappingCache));
		ModelsAddedCallback.EVENT.register(new CTMModelsAddedCallbackHandler(wrappingCache));
		AtlasStitchCallback.EVENT.register(new CTMAtlasStitchCallbackHandler(wrappingCache));
		ModelsLoadedCallback.EVENT.register(new CTMModelsLoadedCallbackHandler(wrappingCache));

		TextureType type;
		TextureTypeRegistry.INSTANCE.register("ctm", new TextureTypeCTM());
		TextureTypeRegistry.INSTANCE.register("edges", new TextureTypeEdges());
		TextureTypeRegistry.INSTANCE.register("edges_full", new TextureTypeEdgesFull());
		TextureTypeRegistry.INSTANCE.register("eldritch", new TextureTypeEldritch());
		TextureTypeRegistry.INSTANCE.register("r", TextureTypeMap.R);
		TextureTypeRegistry.INSTANCE.register("random", TextureTypeMap.R);
		TextureTypeRegistry.INSTANCE.register("v", TextureTypeMap.V);
		TextureTypeRegistry.INSTANCE.register("pattern", TextureTypeMap.V);
		TextureTypeRegistry.INSTANCE.register("normal", TextureTypeNormal.INSTANCE);
		type = new TextureTypePillar();
		TextureTypeRegistry.INSTANCE.register("ctmv", type);
		TextureTypeRegistry.INSTANCE.register("pillar", type);
		TextureTypeRegistry.INSTANCE.register("ctmh", TextureTypePlane.HORIZONRAL);
		TextureTypeRegistry.INSTANCE.register("ctm_horizontal", TextureTypePlane.HORIZONRAL);
		TextureTypeRegistry.INSTANCE.register("ctm_vertical", TextureTypePlane.VERTICAL);
		type = new TextureTypeSCTM();
		TextureTypeRegistry.INSTANCE.register("sctm", type);
		TextureTypeRegistry.INSTANCE.register("ctm_simple", type);
	}
}
