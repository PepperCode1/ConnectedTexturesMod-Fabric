package team.chisel.ctm.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import team.chisel.ctm.client.event.ModelsLoadedCallback;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
	@Inject(at = @At("TAIL"), method = "apply(Lnet/minecraft/client/render/model/ModelLoader;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V")
	public void onFinishLoading(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		ModelsLoadedCallback.EVENT.invoker().onModelsLoaded(modelLoader, resourceManager, profiler);
	}
}
