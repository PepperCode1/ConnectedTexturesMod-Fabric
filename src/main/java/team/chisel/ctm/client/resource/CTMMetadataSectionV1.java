package team.chisel.ctm.client.resource;

import java.util.Arrays;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.api.client.TextureTypeRegistry;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

public class CTMMetadataSectionV1 implements CTMMetadataSection {
	private TextureType type = TextureTypeNormal.INSTANCE;
	private BlendMode blendMode;
	private Identifier proxy;
	private Identifier[] additionalTextures = new Identifier[0];
	private JsonObject extraData = new JsonObject();

	public static CTMMetadataSection fromJson(JsonObject jsonObject) throws JsonParseException {
		CTMMetadataSectionV1 metadata = new CTMMetadataSectionV1();
		if (jsonObject.has("proxy")) {
			JsonElement proxyElement = jsonObject.get("proxy");
			if (proxyElement.isJsonPrimitive() && proxyElement.getAsJsonPrimitive().isString()) {
				String proxyString = proxyElement.getAsString();
				try {
					metadata.proxy = new Identifier(proxyString);
				} catch (InvalidIdentifierException e) {
					throw new JsonParseException("Invalid proxy identifier provided: " + proxyString);
				}
			}
			if (jsonObject.entrySet().stream().filter(e -> e.getKey().equals("ctm_version")).count() > 1) {
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
					JsonElement e = texturesArray.get(i);
					if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
						metadata.additionalTextures[i] = new Identifier(e.getAsString());
					}
				}
			}
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
	public Identifier getProxy() {
		return proxy;
	}

	@Override
	public Identifier[] getAdditionalTextures() {
		return additionalTextures;
	}

	@Override
	public JsonObject getExtraData() {
		return extraData;
	}

	@Override
	public String toString() {
		return "CTMMetadataSectionV1(type=" + getType() + ", blendMode=" + getBlendMode() + ", proxy=" + getProxy() + ", additionalTextures=" + Arrays.deepToString(getAdditionalTextures()) + ", extraData=" + getExtraData() + ")";
	}
}
