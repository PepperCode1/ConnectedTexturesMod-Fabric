package team.chisel.ctm.client.event;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

public interface DeserializeModelJsonCallback {
	Event<DeserializeModelJsonCallback> EVENT = EventFactory.createArrayBacked(DeserializeModelJsonCallback.class,
			(listeners) -> (JsonUnbakedModel jsonModel, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) -> {
				for (DeserializeModelJsonCallback callback : listeners) {
					callback.onDeserializeModelJson(jsonModel, jsonElement, type, jsonDeserializationContext);
				}
			}
	);

	void onDeserializeModelJson(JsonUnbakedModel jsonModel, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext);
}
