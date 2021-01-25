package team.chisel.ctm.client.resource;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

import team.chisel.ctm.client.CTMClient;

public class ModelParserV1 implements ModelParser {
	private static final Gson GSON = new Gson();
	private static final Type OVERRIDE_TYPE = new TypeToken<Map<String, JsonElement>>() { } .getType();

	@Override
	@NotNull
	public Int2ObjectMap<JsonElement> parse(JsonUnbakedModel jsonModel, JsonObject jsonObject, Type type, JsonDeserializationContext context) {
		try {
			Map<String, JsonElement> unparsedOverrides = GSON.fromJson(jsonObject.getAsJsonObject("ctm_overrides"), OVERRIDE_TYPE);
			if (unparsedOverrides != null && unparsedOverrides.size() > 0) {
				Int2ObjectMap<JsonElement> overrides = new Int2ObjectArrayMap<>(unparsedOverrides.size());
				for (Map.Entry<String, JsonElement> entry : unparsedOverrides.entrySet()) {
					try {
						int tintIndex = Integer.parseInt(entry.getKey());
						overrides.put(tintIndex, entry.getValue());
					} catch (NumberFormatException e) {
						CTMClient.LOGGER.error("Error parsing model {}: \"{}\" is not a valid tintindex.", jsonObject, entry.getKey());
					}
				}
				return overrides;
			}
		} catch (Exception e) {
			CTMClient.LOGGER.error("Error parsing model " + jsonObject + ".", e);
		}
		return null;
	}
}
