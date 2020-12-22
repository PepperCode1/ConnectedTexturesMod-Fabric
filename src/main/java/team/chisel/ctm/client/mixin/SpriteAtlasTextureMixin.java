package team.chisel.ctm.client.mixin;

import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import team.chisel.ctm.client.event.AtlasStitchCallback;

@Mixin(value = SpriteAtlasTexture.class, priority = 100)
public class SpriteAtlasTextureMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/SpriteAtlasTexture;loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;"), method = "stitch(Lnet/minecraft/resource/ResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/util/profiler/Profiler;I)Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;", locals = LocalCapture.CAPTURE_FAILHARD)
	public void beforeAtlasStitch(ResourceManager resourceManager, Stream<Identifier> idStream, Profiler profiler, int mipmapLevel, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir, Set<Identifier> set) {
		AtlasStitchCallback.EVENT.invoker().onAtlasStitch((SpriteAtlasTexture) (Object) this, set);
	}
}
