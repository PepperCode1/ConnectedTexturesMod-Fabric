package team.chisel.ctm.client.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Sets;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.event.AtlasStitchCallback;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;

public class CTMAtlasStitchCallbackHandler implements AtlasStitchCallback {
	@Override
	public void onAtlasStitch(SpriteAtlasTexture atlas, Set<Identifier> sprites) {
		Set<Identifier> newSprites = Sets.newConcurrentHashSet();
		List<CompletableFuture<Void>> futures = new ArrayList<>(sprites.size());

		for (Identifier identifier : sprites) {
			futures.add(CompletableFuture.runAsync(() -> addSprites(identifier, newSprites), Util.getMainWorkerExecutor()));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
		sprites.addAll(newSprites);
	}

	private void addSprites(Identifier identifier, Set<Identifier> newSprites) {
		CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(identifier));
		if (metadata != null) {
			addSprites(identifier, metadata, newSprites);
			// Load proxy data
			if (metadata.getProxy() != null) {
				Identifier proxy = metadata.getProxy();
				// Add proxy's base sprite
				newSprites.add(proxy);
				CTMMetadataSection proxyMetadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(proxy));
				if (proxyMetadata != null) {
					addSprites(proxy, proxyMetadata, newSprites);
				}
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
