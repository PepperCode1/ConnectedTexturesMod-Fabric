package team.chisel.ctm.client.util;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ParseUtil {
	public static Optional<Boolean> getBoolean(JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
			return Optional.of(jsonElement.getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<Boolean> getBoolean(JsonObject jsonObject, String memberName) {
		if (jsonObject.has(memberName)) {
			return getBoolean(jsonObject.get(memberName));
		}
		return Optional.empty();
	}
}
