package team.chisel.ctm.api.texture;

import team.chisel.ctm.client.impl.TextureTypeRegistryImpl;

/**
 * Registry for {@link TextureType}. Use {@link TextureTypeRegistry#INSTANCE} to obtain an instance of this class.
 */
public interface TextureTypeRegistry {
	TextureTypeRegistry INSTANCE = new TextureTypeRegistryImpl();

	void register(String name, TextureType type);

	TextureType getType(String name);

	boolean isValid(String name);
}
