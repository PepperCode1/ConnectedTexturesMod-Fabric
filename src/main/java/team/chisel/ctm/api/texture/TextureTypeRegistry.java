package team.chisel.ctm.api.texture;

import team.chisel.ctm.client.impl.TextureTypeRegistryImpl;

/**
 * Registry for {@link TextureType}. Use {@link TextureTypeRegistry#INSTANCE} to obtain an instance of this class.
 */
public interface TextureTypeRegistry {
	public static final TextureTypeRegistry INSTANCE = new TextureTypeRegistryImpl();
	
	public void register(String name, TextureType type);
	
	public TextureType getType(String name);
	
	public boolean isValid(String name);
}
