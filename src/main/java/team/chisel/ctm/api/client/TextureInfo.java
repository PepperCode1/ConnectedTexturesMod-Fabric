package team.chisel.ctm.api.client;

import java.util.Optional;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

import net.minecraft.client.texture.Sprite;

/**
 * Bean to hold information that the {@link TextureType} should use to make a {@link CTMTexture}.
 */
public class TextureInfo {
	private Sprite[] sprites;
	private BlendMode blendMode;
	private Optional<JsonObject> info;

	public TextureInfo(Sprite[] sprites, BlendMode blendMode, Optional<JsonObject> info) {
		this.sprites = sprites;
		this.blendMode = blendMode;
		this.info = info;
	}

	/**
	 * Gets the sprites to use for this texture.
	 */
	public Sprite[] getSprites() {
		return sprites;
	}

	/**
	 * Returns the BlendMode for this texture.
	 */
	public BlendMode getBlendMode() {
		return blendMode;
	}

	/**
	 * Gets a JsonObject that had the key "info" for extra texture information.
	 * This JsonObject might not exist.
	 */
	public Optional<JsonObject> getInfo() {
		return info;
	}
}
