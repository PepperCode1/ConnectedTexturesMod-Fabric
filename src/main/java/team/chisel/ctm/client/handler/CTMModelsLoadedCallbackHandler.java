package team.chisel.ctm.client.handler;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

import team.chisel.ctm.client.event.ModelsLoadedCallback;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;
import team.chisel.ctm.client.util.ResourceUtil;

public class CTMModelsLoadedCallbackHandler implements ModelsLoadedCallback {
	@Override
	public void onModelsLoaded(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler) {
		AbstractCTMBakedModel.invalidateCaches();
		ResourceUtil.invalidateCaches();
	}
}
