package team.chisel.ctm.api.client;

import java.util.Optional;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

import net.minecraft.client.texture.Sprite;

/**
 * Bean to hold information that a {@link TextureType} should use to make a {@link CTMTexture}.
 */
public class TextureInfo {
	private Sprite[] sprites;
	private BlendMode blendMode;
	private Optional<JsonObject> extraInfo;

	public TextureInfo(Sprite[] sprites, BlendMode blendMode, Optional<JsonObject> extraInfo) {
		this.sprites = sprites;
		this.blendMode = blendMode;
		this.extraInfo = extraInfo;
	}

	/**
	 * Gets the sprites to use for the texture.
	 */
	public Sprite[] getSprites() {
		return sprites;
	}

	/**
	 * Gets the BlendMode for the texture.
	 */
	public BlendMode getBlendMode() {
		return blendMode;
	}

	/**
	 * Gets the JsonObject that contains the extra information for the texture.
	 */
	public Optional<JsonObject> getExtraInfo() {
		return extraInfo;
	}
}
