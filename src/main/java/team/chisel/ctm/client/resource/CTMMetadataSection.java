package team.chisel.ctm.client.resource;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.util.ResourceUtil;

public interface CTMMetadataSection {
	int getVersion();

	TextureType getType();

	BlendMode getBlendMode();

	Identifier[] getAdditionalTextures();

	@Nullable
	Identifier getProxy();

	JsonObject getExtraData();

	// TODO move elsewhere
	default CTMTexture<?> makeTexture(Sprite sprite, Function<SpriteIdentifier, Sprite> spriteGetter) {
		CTMMetadataSection metadata = this;
		if (getProxy() != null) {
			Sprite proxySprite = spriteGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, getProxy()));
			CTMMetadataSection proxyMetadata = null;
			try {
				proxyMetadata = ResourceUtil.getMetadata(proxySprite);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (proxyMetadata != null) {
				sprite = proxySprite;
				metadata = proxyMetadata;
			} else {
				CTMClient.LOGGER.error("Could not get metadata of proxy sprite {}. Ignoring proxy and using base texture.", getProxy());
			}
		}

		Identifier[] textures = metadata.getAdditionalTextures();
		int provided = textures.length;
		int required = metadata.getType().requiredTextures()+1;
		Sprite[] sprites = new Sprite[required];
		sprites[0] = sprite;
		for (int i = 1; i < required; i++) {
			Identifier identifier = null;
			if (i <= provided) {
				identifier = textures[i-1];
			}
			sprites[i] = spriteGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, identifier));
		}
		return metadata.getType().makeTexture(new TextureInfo(sprites, metadata.getBlendMode(), Optional.of(metadata.getExtraData())));
	}
}
