package team.chisel.ctm.client.resource;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.api.client.TextureTypeRegistry;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

public class CTMMetadataSectionV1 implements CTMMetadataSection {
	private TextureType type = TextureTypeNormal.INSTANCE;
	private BlendMode blendMode = BlendMode.DEFAULT;
	private Identifier[] additionalTextures;
	private Identifier proxy;
	private JsonObject extraData;

	public static CTMMetadataSection fromJson(JsonObject jsonObject) throws JsonParseException {
		return fromJson(jsonObject, Identifier::new);
	}

	public static CTMMetadataSection fromJson(JsonObject jsonObject, Function<String, Identifier> identifierProvider) throws JsonParseException {
		CTMMetadataSectionV1 metadata = new CTMMetadataSectionV1();
		if (jsonObject.has("proxy")) {
			JsonElement proxyElement = jsonObject.get("proxy");
			if (proxyElement.isJsonPrimitive() && proxyElement.getAsJsonPrimitive().isString()) {
				String proxyString = proxyElement.getAsString();
				try {
					metadata.proxy = identifierProvider.apply(proxyString);
				} catch (InvalidIdentifierException e) {
					throw new JsonParseException("Invalid proxy identifier provided: " + proxyString);
				}
			}
			if (jsonObject.entrySet().stream().filter((element) -> !element.getKey().equals("ctm_version")).count() > 1) {
				throw new JsonParseException("Cannot define other fields when using proxy");
			}
		}
		if (jsonObject.has("type")) {
			JsonElement typeElement = jsonObject.get("type");
			if (typeElement.isJsonPrimitive() && typeElement.getAsJsonPrimitive().isString()) {
				TextureType type = TextureTypeRegistry.INSTANCE.getType(typeElement.getAsString());
				if (type == null) {
					throw new JsonParseException("Invalid texture type provided: " + typeElement);
				} else {
					metadata.type = type;
				}
			}
		}
		if (jsonObject.has("layer")) {
			JsonElement layerElement = jsonObject.get("layer");
			if (layerElement.isJsonPrimitive() && layerElement.getAsJsonPrimitive().isString()) {
				try {
					metadata.blendMode = BlendMode.valueOf(layerElement.getAsString().toUpperCase(Locale.ROOT));
				} catch (IllegalArgumentException e) {
					throw new JsonParseException("Invalid render layer provided: " + layerElement);
				}
			}
		}
		if (jsonObject.has("textures")) {
			JsonElement texturesElement = jsonObject.get("textures");
			if (texturesElement.isJsonArray()) {
				JsonArray texturesArray = texturesElement.getAsJsonArray();
				metadata.additionalTextures = new Identifier[texturesArray.size()];
				for (int i = 0; i < texturesArray.size(); i++) {
					JsonElement textureElement = texturesArray.get(i);
					if (textureElement.isJsonPrimitive() && textureElement.getAsJsonPrimitive().isString()) {
						String textureString = textureElement.getAsString();
						try {
							metadata.additionalTextures[i] = identifierProvider.apply(textureString);
						} catch (InvalidIdentifierException e) {
							throw new JsonParseException("Invalid texture identifier provided: " + textureString);
						}
					}
				}
			}
		}
		if (metadata.additionalTextures == null) {
			metadata.additionalTextures = new Identifier[0];
		}
		if (jsonObject.has("extra") && jsonObject.get("extra").isJsonObject()) {
			metadata.extraData = jsonObject.getAsJsonObject("extra");
		}
		return metadata;
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public TextureType getType() {
		return type;
	}

	@Override
	public BlendMode getBlendMode() {
		return blendMode;
	}

	@Override
	public Identifier[] getAdditionalTextures() {
		return additionalTextures;
	}

	@Override
	public @Nullable Identifier getProxy() {
		return proxy;
	}

	@Override
	public @Nullable JsonObject getExtraData() {
		return extraData;
	}

	@Override
	public String toString() {
		return "CTMMetadataSectionV1(type=" + getType() + ", blendMode=" + getBlendMode() + ", additionalTextures=" + Arrays.deepToString(getAdditionalTextures()) + ", proxy=" + getProxy() + ", extraData=" + getExtraData() + ")";
	}
}
