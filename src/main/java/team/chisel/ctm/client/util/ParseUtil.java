package team.chisel.ctm.client.util;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ParseUtil {
	public static Optional<Boolean> getOptionalBoolean(JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
			return Optional.of(jsonElement.getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<Boolean> getOptionalBoolean(JsonObject jsonObject, String memberName) {
		if (jsonObject.has(memberName)) {
			return getOptionalBoolean(jsonObject.get(memberName));
		}
		return Optional.empty();
	}
}
