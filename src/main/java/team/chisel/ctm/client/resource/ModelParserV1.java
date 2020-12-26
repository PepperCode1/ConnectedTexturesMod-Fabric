package team.chisel.ctm.client.resource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

import team.chisel.ctm.client.model.CTMUnbakedModel;
import team.chisel.ctm.client.model.CTMUnbakedModelImpl;

public class ModelParserV1 implements ModelParser {
	private static final Gson GSON = new Gson();
	private static final Type MAP_TOKEN = new TypeToken<Map<String, JsonElement>>() { } .getType();

	@Override
	@NotNull
	public CTMUnbakedModel parse(JsonUnbakedModel jsonModel, JsonObject jsonObject, Type type, JsonDeserializationContext context) {
		try {
			Map<String, JsonElement> parsed = GSON.fromJson(jsonObject.getAsJsonObject("ctm_overrides"), MAP_TOKEN);
			if (parsed == null) {
				parsed = Collections.emptyMap();
			}
			Int2ObjectMap<JsonElement> replacements = new Int2ObjectArrayMap<>(parsed.size());
			for (Entry<String, JsonElement> entry : parsed.entrySet()) {
				int index = Integer.parseInt(entry.getKey());
				replacements.put(index, entry.getValue());
			}
			return new CTMUnbakedModelImpl(jsonModel, replacements);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
