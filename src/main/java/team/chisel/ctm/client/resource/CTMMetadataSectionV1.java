package team.chisel.ctm.client.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

import net.minecraft.util.Identifier;

import team.chisel.ctm.api.texture.CTMMetadataSection;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.texture.TextureTypeRegistry;

public class CTMMetadataSectionV1 implements CTMMetadataSection {
	private TextureType type = TextureTypeRegistry.INSTANCE.getType("NORMAL");
	private BlendMode blendMode;
	private String proxy;
	private Identifier[] additionalTextures = new Identifier[0];
	private JsonObject extraData = new JsonObject();

	@Override
	public int getVersion() {
		return 1;
	}

	public static CTMMetadataSection fromJson(JsonObject jsonObject) throws JsonParseException {
		CTMMetadataSectionV1 metadata = new CTMMetadataSectionV1();
		if (jsonObject.has("proxy")) {
			JsonElement proxyElement = jsonObject.get("proxy");
			if (proxyElement.isJsonPrimitive() && proxyElement.getAsJsonPrimitive().isString()) {
				metadata.proxy = proxyElement.getAsString();
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
					throw new JsonParseException("Invalid render type given: " + typeElement);
				} else {
					metadata.type = type;
				}
			}
		}
		if (jsonObject.has("layer")) {
			JsonElement layerElement = jsonObject.get("layer");
			if (layerElement.isJsonPrimitive() && layerElement.getAsJsonPrimitive().isString()) {
				try {
					metadata.blendMode = BlendMode.valueOf(layerElement.getAsString());
				} catch (IllegalArgumentException e) {
					throw new JsonParseException("Invalid block layer given: " + layerElement);
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
	public TextureType getType() {
		return this.type;
	}

	@Override
	public BlendMode getBlendMode() {
		return this.blendMode;
	}

	@Override
	public String getProxy() {
		return this.proxy;
	}

	@Override
	public Identifier[] getAdditionalTextures() {
		return this.additionalTextures;
	}

	@Override
	public JsonObject getExtraData() {
		return this.extraData;
	}

	@Override
	public String toString() {
		return "CTMMetadataSectionV1(type=" + this.getType() + ", blendMode=" + this.getBlendMode() + ", proxy=" + this.getProxy() + ", additionalTextures=" + java.util.Arrays.deepToString(this.getAdditionalTextures()) + ", extraData=" + this.getExtraData() + ")";
	}
}
