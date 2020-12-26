package team.chisel.ctm.client.resource;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

import team.chisel.ctm.client.model.CTMUnbakedModel;

public interface ModelParser {
	@NotNull
	CTMUnbakedModel parse(JsonUnbakedModel jsonModel, JsonObject jsonObject, Type type, JsonDeserializationContext context);
}
