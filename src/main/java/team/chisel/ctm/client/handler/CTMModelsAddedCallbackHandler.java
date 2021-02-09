package team.chisel.ctm.client.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import team.chisel.ctm.client.event.ModelsAddedCallback;
import team.chisel.ctm.client.mixin.JsonUnbakedModelAccessor;
import team.chisel.ctm.client.model.CTMUnbakedModel;
import team.chisel.ctm.client.model.JsonCTMUnbakedModel;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;
import team.chisel.ctm.client.util.VoidSet;

public class CTMModelsAddedCallbackHandler implements ModelsAddedCallback {
	private Map<JsonUnbakedModel, Int2ObjectMap<JsonElement>> jsonOverrideMap;

	public CTMModelsAddedCallbackHandler(Map<JsonUnbakedModel, Int2ObjectMap<JsonElement>> jsonOverrideMap) {
		this.jsonOverrideMap = jsonOverrideMap;
	}

	@Override
	public void onModelsAdded(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, Map<Identifier, UnbakedModel> unbakedModels, Map<Identifier, UnbakedModel> modelsToBake) {
		Map<Identifier, UnbakedModel> wrappedModels = new HashMap<>();

		// check which models should be wrapped
		for (Map.Entry<Identifier, UnbakedModel> entry : unbakedModels.entrySet()) {
			Identifier identifier = entry.getKey();
			UnbakedModel unbakedModel = entry.getValue();

			Collection<SpriteIdentifier> dependencies = unbakedModel.getTextureDependencies(modelLoader::getOrLoadModel, VoidSet.get());
			if (unbakedModel instanceof JsonUnbakedModel) {
				JsonUnbakedModel jsonModel = (JsonUnbakedModel) unbakedModel;
				// do not wrap builtin models
				// root model check after getTextureDependencies so it's actually set
				if (jsonModel.getRootModel() == ModelLoader.GENERATION_MARKER || jsonModel.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
					continue;
				}
				Int2ObjectMap<JsonElement> overrides = getOverrides(jsonModel);
				if (overrides != null && !overrides.isEmpty()) {
					// wrap models with overrides
					wrappedModels.put(identifier, new JsonCTMUnbakedModel(jsonModel, overrides));
					continue;
				}
			}
			for (SpriteIdentifier spriteId : dependencies) {
				CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(spriteId.getTextureId()));
				if (metadata != null) {
					// at least one texture has CTM metadata, so this model should be wrapped
					wrappedModels.put(identifier, new CTMUnbakedModel(unbakedModel));
					break;
				}
			}
		}
		jsonOverrideMap.clear();

		// inject wrapped models
		for (Map.Entry<Identifier, UnbakedModel> entry : wrappedModels.entrySet()) {
			Identifier identifier = entry.getKey();
			UnbakedModel wrapped = entry.getValue();

			unbakedModels.put(identifier, wrapped);
			if (modelsToBake.containsKey(identifier)) {
				modelsToBake.put(identifier, wrapped);
			}
		}
	}

	private Int2ObjectMap<JsonElement> getOverrides(JsonUnbakedModel unbakedModel) {
		Int2ObjectMap<JsonElement> overrides = jsonOverrideMap.get(unbakedModel);
		if (overrides == null) {
			JsonUnbakedModel parent = ((JsonUnbakedModelAccessor) unbakedModel).getParent();
			if (parent != null) {
				return getOverrides(parent);
			}
			return null;
		}
		return overrides;
	}
}
