package team.chisel.ctm.client.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.event.AtlasStitchCallback;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;

public class CTMAtlasStitchCallbackHandler implements AtlasStitchCallback {
	@Override
	public void onAtlasStitch(SpriteAtlasTexture atlas, Set<Identifier> sprites) { // TODO multithread?
		Set<Identifier> newSprites = new HashSet<>();

		for (Identifier identifier : sprites) {
			try {
				CTMMetadataSection metadata = ResourceUtil.getMetadata(ResourceUtil.toTextureIdentifier(identifier));
				if (metadata != null) {
					addTextures(identifier, metadata, newSprites);
					// Load proxy data
					if (metadata.getProxy() != null) {
						Identifier proxy = metadata.getProxy();
						// Add proxy's base sprite
						newSprites.add(proxy);
						CTMMetadataSection proxyMetadata = ResourceUtil.getMetadata(ResourceUtil.toTextureIdentifier(proxy));
						if (proxyMetadata != null) {
							addTextures(proxy, proxyMetadata, newSprites);
						}
					}
				}
			} catch (FileNotFoundException e) { // Ignore these, they are reported by vanilla
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		sprites.addAll(newSprites);
	}

	private void addTextures(Identifier identifier, CTMMetadataSection metadata, Set<Identifier> newSprites) {
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
