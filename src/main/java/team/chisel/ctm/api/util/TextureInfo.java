package team.chisel.ctm.api.util;

import java.util.Optional;

import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.texture.Sprite;
import team.chisel.ctm.api.texture.CTMTexture;

/**
 * Bean to hold information that the {@link TextureType} should use to make a {@link CTMTexture}.
 */
public class TextureInfo {
	private Sprite[] sprites;
	private Optional<JsonObject> info;
	private BlendMode blendMode;

	public TextureInfo(Sprite[] sprites, Optional<JsonObject> info, BlendMode layer) {
		this.sprites = sprites;
		this.info = info;
		this.blendMode = layer;
	}

	/**
	 * Gets the sprites to use for this texture.
	 */
	public Sprite[] getSprites() {
		return this.sprites;
	}

	/**
	 * Gets a JsonObject that had the key "info" for extra texture information.
	 * This JsonObject might not exist.
	 */
	public Optional<JsonObject> getInfo() {
		return this.info;
	}

	/**
	 * Returns the BlendMode for this texture.
	 */
	public BlendMode getBlendMode(){
		return this.blendMode;
	}
}
