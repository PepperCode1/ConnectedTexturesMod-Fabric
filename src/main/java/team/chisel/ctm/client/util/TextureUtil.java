package team.chisel.ctm.client.util;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

public class TextureUtil {
	public static void initializeTextures(Set<SpriteIdentifier> textureDependencies, Map<Identifier, CTMTexture<?>> textures, Function<SpriteIdentifier, Sprite> spriteGetter) {
		for (SpriteIdentifier identifier : textureDependencies) {
			Sprite sprite = spriteGetter.apply(identifier);
			CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(sprite);
			if (metadata != null) {
				textures.put(sprite.getId(), makeTexture(metadata, sprite, spriteGetter));
			}
		}
	}

	public static CTMTexture<?> makeTexture(CTMMetadataSection metadata, Sprite sprite, Function<SpriteIdentifier, Sprite> spriteGetter) {
		if (metadata == null) {
			return TextureTypeNormal.INSTANCE.makeTexture(new TextureInfo(new Sprite[] { sprite }, null, Optional.empty()));
		} else {
			if (metadata.getProxy() != null) {
				Sprite proxySprite = spriteGetter.apply(toSpriteIdentifier(metadata.getProxy()));
				CTMMetadataSection proxyMetadata = ResourceUtil.getMetadataSafe(proxySprite);
				if (proxyMetadata != null) {
					sprite = proxySprite;
					metadata = proxyMetadata;
				} else {
					CTMClient.LOGGER.error("Could not get metadata of proxy sprite {}. Ignoring proxy and using base texture.", metadata.getProxy());
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
				sprites[i] = spriteGetter.apply(toSpriteIdentifier(identifier));
			}
			return metadata.getType().makeTexture(new TextureInfo(sprites, metadata.getBlendMode(), Optional.ofNullable(metadata.getExtraData())));
		}
	}

	public static SpriteIdentifier toSpriteIdentifier(Identifier identifier) {
		return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, identifier);
	}
}
