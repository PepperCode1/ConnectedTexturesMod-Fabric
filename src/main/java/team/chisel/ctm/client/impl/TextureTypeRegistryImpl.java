package team.chisel.ctm.client.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.texture.TextureTypeRegistry;

public class TextureTypeRegistryImpl implements TextureTypeRegistry {
	private Map<String, TextureType> typeMap = new HashMap<>();

	public void register(String name, TextureType type) {
		String key = name.toLowerCase(Locale.ROOT);
		if (typeMap.containsKey(key) && typeMap.get(key) != type) {
			throw new IllegalArgumentException("Render Type with name " + key + " has already been registered!");
		} else if (typeMap.get(key) != type) {
			typeMap.put(key, type);
		}
	}

	public TextureType getType(String name) {
		String key = name.toLowerCase(Locale.ROOT);
		return typeMap.get(key);
	}

	public boolean isValid(String name) {
		return getType(name) != null;
	}
}
