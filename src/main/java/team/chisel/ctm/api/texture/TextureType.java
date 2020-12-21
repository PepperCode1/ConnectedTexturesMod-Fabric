package team.chisel.ctm.api.texture;

import team.chisel.ctm.api.util.TextureInfo;

/**
 * Root interface representing a type of CTM texture. To register, use {@link TextureTypeRegistry}.
 */
public interface TextureType extends ContextProvider {
	/**
	 * Make a CTM Texture from a list of sprites.<br>
	 * Tip: The return of this method can be explicitly typed without warnings or errors. For instance,
	 * <blockquote>
	 * <code>public ICTMTexture{@literal <}MyRenderType{@literal >} makeTexture(...) {...}</code>
	 * </blockquote>
	 * is a valid override of this method.
	 * @param info A {@link TextureInfo} object which contains all the information that about this texture
	 */
	<T extends TextureType> CTMTexture<? extends T> makeTexture(TextureInfo info);

	/**
	 * Gets the amount of quads per cull face.
	 * @return The number of quads.
	 */
	default int getQuadsPerSide() {
		return 1;
	}

	/**
	 * The number of textures required for this texture type.
	 * For instance, {@link team.chisel.ctm.client.texture.type.TextureTypeCTM} requires two.
	 * @return The number of textures required.
	 */
	default int requiredTextures() {
		return 1;
	}
}
