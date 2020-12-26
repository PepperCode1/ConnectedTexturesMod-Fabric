package team.chisel.ctm.impl.client;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.api.client.TextureTypeRegistry;

public class TextureTypeRegistryImpl implements TextureTypeRegistry {
	private Map<String, TextureType> types = new HashMap<>();

	public void register(String name, TextureType type) {
		String key = name.toLowerCase(Locale.ROOT);
		if (types.containsKey(key) && types.get(key) != type) {
			throw new IllegalArgumentException("TextureType with name " + key + " has already been registered!");
		} else if (types.get(key) != type) {
			types.put(key, type);
		}
	}

	public TextureType getType(String name) {
		String key = name.toLowerCase(Locale.ROOT);
		return types.get(key);
	}

	public boolean isValid(String name) {
		String key = name.toLowerCase(Locale.ROOT);
		return types.containsKey(key);
	}
}
