package team.chisel.ctm.api.client;

import team.chisel.ctm.impl.client.TextureTypeRegistryImpl;

// TODO: Switch from String to Identifier
/**
 * Registry for {@link TextureType}. Use {@link TextureTypeRegistry#INSTANCE} to obtain an instance of this class.
 */
public interface TextureTypeRegistry {
	TextureTypeRegistry INSTANCE = TextureTypeRegistryImpl.INSTANCE;

	void register(String name, TextureType type);

	TextureType getType(String name);

	boolean isValid(String name);
}
