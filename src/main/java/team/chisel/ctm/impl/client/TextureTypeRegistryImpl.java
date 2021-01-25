package team.chisel.ctm.impl.client;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.api.client.TextureTypeRegistry;
import team.chisel.ctm.client.CTMClient;

public class TextureTypeRegistryImpl implements TextureTypeRegistry {
	public static final TextureTypeRegistryImpl INSTANCE = new TextureTypeRegistryImpl();

	private Map<String, TextureType> types = new HashMap<>();

	public void register(String name, TextureType type) {
		String key = name.toLowerCase(Locale.ROOT);
		if (types.get(key) != type) {
			if (types.containsKey(key)) {
				CTMClient.LOGGER.error("TextureType with name {} has already been registered!", key);
			} else {
				types.put(key, type);
			}
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
