package team.chisel.ctm.client.mixin;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

import team.chisel.ctm.client.event.DeserializeModelJsonCallback;

@Mixin(JsonUnbakedModel.Deserializer.class)
public class JsonUnbakedModelDeserializerMixin {
	@Inject(at = @At("RETURN"), method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;")
	public void onDeserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<JsonUnbakedModel> cir) {
		JsonUnbakedModel jsonModel = cir.getReturnValue();
		DeserializeModelJsonCallback.EVENT.invoker().onDeserializeModelJson(jsonModel, jsonElement, type, jsonDeserializationContext);
	}
}
