package team.chisel.ctm.client.resource;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

public interface ModelParser {
	@Nullable
	Int2ObjectMap<JsonElement> parse(JsonUnbakedModel jsonModel, JsonObject jsonObject, Type type, JsonDeserializationContext context);
}
