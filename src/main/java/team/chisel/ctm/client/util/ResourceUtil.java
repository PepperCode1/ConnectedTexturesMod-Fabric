package team.chisel.ctm.client.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.resource.CTMMetadataSection;

public class ResourceUtil {
	private static final Map<Identifier, CTMMetadataSection> METADATA_CACHE = new HashMap<>();

	public static Resource getResource(Sprite sprite) throws IOException {
		return getResource(toTextureIdentifier(sprite.getId()));
	}

	public static Identifier toTextureIdentifier(Identifier identifier) {
		if (!identifier.getPath().startsWith("textures/")) {
			identifier = new Identifier(identifier.getNamespace(), "textures/" + identifier.getPath());
		}
		if (!identifier.getPath().endsWith(".png")) {
			identifier = new Identifier(identifier.getNamespace(), identifier.getPath() + ".png");
		}
		return identifier;
	}

	public static Resource getResource(Identifier identifier) throws IOException {
		return MinecraftClient.getInstance().getResourceManager().getResource(identifier);
	}

	public static Resource getResourceUnsafe(Identifier identifier) {
		try {
			return getResource(identifier);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static CTMMetadataSection getMetadata(Identifier identifier) throws IOException {
		// Note, semantically different from computeIfAbsent, as we DO care about keys mapped to null values
		if (METADATA_CACHE.containsKey(identifier)) {
			return METADATA_CACHE.get(identifier);
		}
		CTMMetadataSection metadata;
		try (Resource resource = getResource(identifier)) {
			metadata = resource.getMetadata(CTMMetadataReader.INSTANCE);
		} catch (FileNotFoundException e) {
			metadata = null;
		} catch (JsonParseException e) {
			throw new IOException("Error loading metadata for location " + identifier, e);
		}
		METADATA_CACHE.put(identifier, metadata);
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

	public static void invalidateCaches() {
		METADATA_CACHE.clear();
	}
}
