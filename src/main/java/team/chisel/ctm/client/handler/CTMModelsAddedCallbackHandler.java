package team.chisel.ctm.client.handler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import team.chisel.ctm.client.event.ModelsAddedCallback;
import team.chisel.ctm.client.model.CTMUnbakedModel;
import team.chisel.ctm.client.model.CTMUnbakedModelImpl;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;

public class CTMModelsAddedCallbackHandler implements ModelsAddedCallback {
	private WrappingCache wrappingCache;

	public CTMModelsAddedCallbackHandler(WrappingCache wrappingCache) {
		this.wrappingCache = wrappingCache;
	}

	@Override
	public void onModelsAdded(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, Map<Identifier, UnbakedModel> unbakedModels, Map<Identifier, UnbakedModel> modelsToBake) {
		Object2BooleanMap<Identifier> wrappedModels = wrappingCache.wrappedModels;
		Map<JsonUnbakedModel, UnbakedModel> jsonModelsToWrap = wrappingCache.jsonModelsToWrap;
		Map<Identifier, UnbakedModel> newUnbakedModels = new HashMap<>();
		Map<Identifier, UnbakedModel> newModelsToBake = new HashMap<>();

		UnbakedModel wrapped;
		boolean shouldWrap;
		Deque<Identifier> dependencies = new ArrayDeque<>();
		Set<Identifier> seenModels = new HashSet<>();

		for (Map.Entry<Identifier, UnbakedModel> entry : modelsToBake.entrySet()) {
			Identifier identifier = entry.getKey();
			UnbakedModel unbakedModel = entry.getValue();

			// don't wrap already wrapped models
			if (unbakedModel instanceof CTMUnbakedModel) {
				continue;
			}

			wrapped = jsonModelsToWrap.get(unbakedModel);
			if (wrapped == null) {
				if (wrappedModels.containsKey(identifier)) {
					shouldWrap = wrappedModels.getBoolean(identifier);
				} else {
					dependencies.push(identifier);
					seenModels.add(identifier);
					shouldWrap = wrappedModels.getOrDefault(identifier, false);
					// Breadth-first loop through dependencies, exiting as soon as a CTM texture is found, and skipping duplicates/cycles
					while (!shouldWrap && !dependencies.isEmpty()) {
						Identifier dependency = dependencies.pop();
						UnbakedModel model = unbakedModels.get(dependency);
						if (model == null) {
							continue;
						}

						Set<SpriteIdentifier> textures = new HashSet<>(model.getTextureDependencies(modelLoader::getOrLoadModel, new HashSet<>()));
						Set<Identifier> newDependencies = new HashSet<>(model.getModelDependencies());
						for (SpriteIdentifier texture : textures) {
							CTMMetadataSection metadata = null;
							// Cache all dependent texture metadata
							try {
								metadata = ResourceUtil.getMetadata(ResourceUtil.toTextureIdentifier(texture.getTextureId()));
							} catch (IOException e) {
								// Fallthrough
							}
							if (metadata != null) {
								// At least one texture has CTM metadata, so we should wrap this model
								shouldWrap = true;
								break;
							}
						}
						for (Identifier newDependency : newDependencies) {
							if (seenModels.add(newDependency)) {
								dependencies.push(newDependency);
							}
						}
					}

					dependencies.clear();
					seenModels.clear();

					// called after loop so root model can be set
					if (unbakedModel instanceof JsonUnbakedModel && ((JsonUnbakedModel) unbakedModel).getRootModel() == ModelLoader.GENERATION_MARKER) {
						wrappedModels.put(identifier, false);
						continue;
					}

					wrappedModels.put(identifier, shouldWrap);
				}

				if (shouldWrap) {
					wrapped = new CTMUnbakedModelImpl(unbakedModel);
				}
			}

			if (wrapped != null) {
				newUnbakedModels.put(identifier, wrapped);
				newModelsToBake.put(identifier, wrapped);
			}
		}

		unbakedModels.putAll(newUnbakedModels);
		modelsToBake.putAll(newModelsToBake);
	}
}
