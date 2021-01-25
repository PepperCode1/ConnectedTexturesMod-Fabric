package team.chisel.ctm.client.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		Set<Identifier> toWrap = new HashSet<>();

		// check which models should be wrapped
		for (Map.Entry<Identifier, UnbakedModel> entry : modelsToBake.entrySet()) {
			Identifier identifier = entry.getKey();
			UnbakedModel unbakedModel = entry.getValue();

			for (SpriteIdentifier spriteId : unbakedModel.getTextureDependencies(unbakedModels::get, VoidSet.get())) {
				CTMMetadataSection metadata = null;
				try {
					metadata = ResourceUtil.getMetadata(ResourceUtil.toTextureIdentifier(spriteId.getTextureId()));
				} catch (IOException e) {
					// Fallthrough
				}
				if (metadata != null) {
					// At least one texture has CTM metadata, so we should wrap this model
					// root model check after getTextureDependencies so it's not null
					if (!(unbakedModel instanceof JsonUnbakedModel) || ((JsonUnbakedModel) unbakedModel).getRootModel() != ModelLoader.GENERATION_MARKER) {
						toWrap.add(identifier);
					}
					break;
				}
			}
		}

		Map<Identifier, UnbakedModel> wrappedModels = new HashMap<>();

		// wrap json models with overrides
		for (Map.Entry<Identifier, UnbakedModel> entry : unbakedModels.entrySet()) {
			if (entry.getValue() instanceof JsonUnbakedModel) {
				Identifier identifier = entry.getKey();
				JsonUnbakedModel unbakedModel = (JsonUnbakedModel) entry.getValue();

				Int2ObjectMap<JsonElement> overrides = getOverrides(unbakedModel);
				if (overrides != null && !overrides.isEmpty()) {
					wrappedModels.put(identifier, new JsonCTMUnbakedModel(unbakedModel, overrides));
				}
			}
		}
		jsonOverrideMap.clear();

		// wrap normal models from before
		for (Identifier identifier : toWrap) {
			if (!wrappedModels.containsKey(identifier)) {
				wrappedModels.put(identifier, new CTMUnbakedModel(unbakedModels.get(identifier)));
			}
		}

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
