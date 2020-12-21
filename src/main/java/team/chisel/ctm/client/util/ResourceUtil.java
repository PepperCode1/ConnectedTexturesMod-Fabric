package team.chisel.ctm.client.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import team.chisel.ctm.api.texture.CTMMetadataSection;
import team.chisel.ctm.client.resource.CTMMetadataReader;

public class ResourceUtil {
	private static final Map<Identifier, CTMMetadataSection> METADATA_CACHE = new HashMap<>();
	private static final CTMMetadataReader READER = new CTMMetadataReader();
	
	public static Resource getResource(Sprite sprite) throws IOException {
		return getResource(spriteToAbsolute(sprite.getId()));
	}
	
	public static Identifier spriteToAbsolute(Identifier sprite) {
		if (!sprite.getPath().startsWith("textures/")) {
			sprite = new Identifier(sprite.getNamespace(), "textures/" + sprite.getPath());
		}
		if (!sprite.getPath().endsWith(".png")) {
			sprite = new Identifier(sprite.getNamespace(), sprite.getPath() + ".png");
		}
		return sprite;
	}
	
	public static Resource getResource(Identifier res) throws IOException {
		return MinecraftClient.getInstance().getResourceManager().getResource(res);
	}
	
	public static Resource getResourceUnsafe(Identifier res) {
		try {
			return getResource(res);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static CTMMetadataSection getMetadata(Identifier res) throws IOException {
		// Note, semantically different from computeIfAbsent, as we DO care about keys mapped to null values
		if (METADATA_CACHE.containsKey(res)) {
			return METADATA_CACHE.get(res);
		}
		CTMMetadataSection ret;
		try (Resource resource = getResource(res)) {
			ret = resource.getMetadata(READER);
		} catch (FileNotFoundException e) {
			ret = null;  
		} catch (JsonParseException e) {
			throw new IOException("Error loading metadata for location " + res, e);
		}
		METADATA_CACHE.put(res, ret);
		return ret;
	}
	
	@Nullable
	public static CTMMetadataSection getMetadata(Sprite sprite) throws IOException {
		return getMetadata(spriteToAbsolute(sprite.getId()));
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
