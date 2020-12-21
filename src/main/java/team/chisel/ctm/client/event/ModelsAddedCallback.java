package team.chisel.ctm.client.event;

import java.util.Map;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public interface ModelsAddedCallback {
	public static final Event<ModelsAddedCallback> EVENT = EventFactory.createArrayBacked(ModelsAddedCallback.class,
		(listeners) -> (ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, Map<Identifier, UnbakedModel> unbakedModels, Map<Identifier, UnbakedModel> modelsToBake) -> {
			for (ModelsAddedCallback callback : listeners) {
				callback.onModelsAdded(modelLoader, resourceManager, profiler, unbakedModels, modelsToBake);
			}
		}
	);
		
	public void onModelsAdded(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, Map<Identifier, UnbakedModel> unbakedModels, Map<Identifier, UnbakedModel> modelsToBake);
}
