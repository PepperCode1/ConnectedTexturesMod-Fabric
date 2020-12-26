package team.chisel.ctm.client.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import team.chisel.ctm.client.event.AtlasStitchCallback;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;

public class CTMAtlasStitchCallbackHandler implements AtlasStitchCallback {
	private WrappingCache wrappingCache;

	public CTMAtlasStitchCallbackHandler(WrappingCache wrappingCache) {
		this.wrappingCache = wrappingCache;
	}

	@Override
	public void onAtlasStitch(SpriteAtlasTexture atlas, Set<Identifier> sprites) {
		Set<Identifier> registeredTextures = wrappingCache.registeredTextures;
		Set<Identifier> newSprites = new HashSet<>();

		for (Identifier identifier : sprites) {
			try {
				identifier = new Identifier(identifier.getNamespace(), "textures/" + identifier.getPath() + ".png");
				CTMMetadataSection metadata = ResourceUtil.getMetadata(identifier);
				if (metadata != null) {
					// Load proxy data
					if (metadata.getProxy() != null) {
						Identifier proxy = metadata.getProxy();
						CTMMetadataSection proxyMetadata = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(proxy));
						// Load proxy's base sprite
						newSprites.add(proxy);
						if (proxyMetadata != null) {
							// Load proxy's additional textures
							for (Identifier id : proxyMetadata.getAdditionalTextures()) {
								if (registeredTextures.add(id)) {
									newSprites.add(id);
								}
							}
						}
					}
					// Load additional textures
					for (Identifier id : metadata.getAdditionalTextures()) {
						if (registeredTextures.add(id)) {
							newSprites.add(id);
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
}
