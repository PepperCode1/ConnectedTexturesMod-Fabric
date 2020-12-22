package team.chisel.ctm.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

public interface ModelsLoadedCallback {
	Event<ModelsLoadedCallback> EVENT = EventFactory.createArrayBacked(ModelsLoadedCallback.class,
			(listeners) -> (ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler) -> {
				for (ModelsLoadedCallback callback : listeners) {
					callback.onModelsLoaded(modelLoader, resourceManager, profiler);
				}
			}
	);

	void onModelsLoaded(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler);
}
