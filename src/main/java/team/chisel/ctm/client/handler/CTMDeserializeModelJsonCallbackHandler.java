package team.chisel.ctm.client.handler;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import team.chisel.ctm.api.model.CTMUnbakedModel;
import team.chisel.ctm.api.model.ModelParser;
import team.chisel.ctm.client.event.DeserializeModelJsonCallback;
import team.chisel.ctm.client.resource.ModelParserV1;

public class CTMDeserializeModelJsonCallbackHandler implements DeserializeModelJsonCallback {
	private static final Map<Integer, ModelParser> PARSERS = ImmutableMap.of(
		1, new ModelParserV1()
		);
	
	private WrappingCache modelCache;
	
	public CTMDeserializeModelJsonCallbackHandler(WrappingCache modelCache) {
		this.modelCache = modelCache;
	}
	
	@Override
	public void onDeserializeModelJson(JsonUnbakedModel jsonModel, JsonElement jsonElement, Type type, JsonDeserializationContext context) {
		if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			if (jsonObject.has("ctm_version")) {
				ModelParser parser = PARSERS.get(jsonObject.get("ctm_version").getAsInt());
				if (parser == null) {
					throw new IllegalArgumentException("Invalid \"ctm_version\" in model " + jsonObject);
				}
				CTMUnbakedModel model = parser.parse(jsonModel, jsonObject, type, context);
				modelCache.jsonModelsToWrap.put(jsonModel, model);
			}
		}
	}
}
