package team.chisel.ctm.api.client;

import team.chisel.ctm.impl.client.TextureTypeRegistryImpl;

/**
 * Registry for {@link TextureType}. Use {@link TextureTypeRegistry#INSTANCE} to obtain an instance of this class.
 */
public interface TextureTypeRegistry {
	TextureTypeRegistry INSTANCE = new TextureTypeRegistryImpl();

	void register(String name, TextureType type);

	TextureType getType(String name);

	boolean isValid(String name);
}
