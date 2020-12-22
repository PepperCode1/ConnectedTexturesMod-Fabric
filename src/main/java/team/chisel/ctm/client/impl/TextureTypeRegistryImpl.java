package team.chisel.ctm.client.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.texture.TextureTypeRegistry;

public class TextureTypeRegistryImpl implements TextureTypeRegistry {
	private Map<String, TextureType> types = new HashMap<>();

	public void register(String name, TextureType type) {
		String key = name.toLowerCase(Locale.ROOT);
		if (types.containsKey(key) && types.get(key) != type) {
			throw new IllegalArgumentException("Render Type with name " + key + " has already been registered!");
		} else if (types.get(key) != type) {
			types.put(key, type);
		}
	}

	public TextureType getType(String name) {
		String key = name.toLowerCase(Locale.ROOT);
		return types.get(key);
	}

	public boolean isValid(String name) {
		return getType(name) != null;
	}
}
