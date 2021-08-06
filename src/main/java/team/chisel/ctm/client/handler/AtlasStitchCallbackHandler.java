package team.chisel.ctm.client.handler;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.event.AtlasStitchCallback;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;

public class AtlasStitchCallbackHandler implements AtlasStitchCallback {
	@Override
	public void onAtlasStitch(SpriteAtlasTexture atlas, Set<Identifier> sprites) {
		if (!atlas.getId().equals(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)) {
			return;
		}

		Set<Identifier> newSprites = new HashSet<>();
		for (Identifier identifier : sprites) {
			addSprites(identifier, newSprites);
		}
		sprites.addAll(newSprites);
	}

	private void addSprites(Identifier identifier, Set<Identifier> newSprites) {
		CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(identifier));
		if (metadata != null) {
			// Load proxy data
			if (metadata.getProxy() != null) {
				Identifier proxy = metadata.getProxy();
				// Add proxy's base sprite
				newSprites.add(proxy);
				CTMMetadataSection proxyMetadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(proxy));
				if (proxyMetadata != null) {
					addSprites(proxy, proxyMetadata, newSprites);
				}
			} else {
				addSprites(identifier, metadata, newSprites);
			}
		}
	}

	private void addSprites(Identifier identifier, CTMMetadataSection metadata, Set<Identifier> newSprites) {
		// Validate additional texture amount
		int required = metadata.getType().requiredTextures();
		int provided = metadata.getAdditionalTextures().length + 1;
		if (required > provided) {
			CTMClient.LOGGER.error("Too few textures provided for sprite {}: TextureType {} requires {} textures, but {} were provided.", identifier, metadata.getType(), required, provided);
		} else if (required < provided) {
			CTMClient.LOGGER.warn("Too many textures provided for sprite {}: TextureType {} requires {} textures, but {} were provided.", identifier, metadata.getType(), required, provided);
		}
		// Add additional textures
		for (Identifier id : metadata.getAdditionalTextures()) {
			newSprites.add(id);
		}
	}
}
