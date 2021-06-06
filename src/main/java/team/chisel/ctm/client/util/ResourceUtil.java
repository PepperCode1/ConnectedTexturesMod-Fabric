package team.chisel.ctm.client.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.resource.CTMMetadataSection;

/**
 * All methods are thread-safe.
 */
public class ResourceUtil {
	private static final Map<Identifier, CTMMetadataSection> METADATA_CACHE = new ConcurrentHashMap<>();
	private static final Set<Identifier> NO_METADATA = Sets.newConcurrentHashSet();

	public static Resource getResource(Identifier identifier) throws IOException {
		return MinecraftClient.getInstance().getResourceManager().getResource(identifier);
	}

	public static Resource getResource(Sprite sprite) throws IOException {
		return getResource(toTextureIdentifier(sprite.getId()));
	}

	public static Resource getResourceUnsafe(Identifier identifier) {
		try {
			return getResource(identifier);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static CTMMetadataSection getMetadata(Identifier identifier) throws IOException, RuntimeException {
		if (NO_METADATA.contains(identifier)) {
			return null;
		}
		CTMMetadataSection metadata = METADATA_CACHE.get(identifier);
		if (metadata == null) {
			metadata = getResource(identifier).getMetadata(CTMMetadataReader.INSTANCE);
			if (metadata == null) {
				NO_METADATA.add(identifier);
			} else {
				METADATA_CACHE.put(identifier, metadata);
			}
		}
		return metadata;
	}

	@Nullable
	public static CTMMetadataSection getMetadata(Sprite sprite) throws IOException {
		return getMetadata(toTextureIdentifier(sprite.getId()));
	}

	@Nullable
	public static CTMMetadataSection getMetadataUnsafe(Sprite sprite) {
		try {
			return getMetadata(sprite);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static CTMMetadataSection getMetadataSafe(Identifier identifier) {
		try {
			return getMetadata(identifier);
		} catch (FileNotFoundException e) {
			//
		} catch (Exception e) {
			CTMClient.LOGGER.error("Error loading metadata for resource " + identifier + ".", e);
		}
		return null;
	}

	@Nullable
	public static CTMMetadataSection getMetadataSafe(Sprite sprite) {
		try {
			return getMetadata(sprite);
		} catch (FileNotFoundException e) {
			// For virtual sprites, such as missingno
		} catch (Exception e) {
			CTMClient.LOGGER.error("Error loading metadata for sprite " + sprite.getId() + ".", e);
		}
		return null;
	}

	public static Identifier toTextureIdentifier(Identifier identifier) {
		String path = identifier.getPath();
		if (!path.startsWith("textures/")) {
			path = "textures/" + path;
		}
		if (!path.endsWith(".png")) {
			path = path + ".png";
		}
		return path.equals(identifier.getPath()) ? identifier : new Identifier(identifier.getNamespace(), path);
	}

	public static void invalidateCaches() {
		METADATA_CACHE.clear();
		NO_METADATA.clear();
	}
}
