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

	public static CTMMetadataSection fromJson(JsonObject obj) throws JsonParseException {
		CTMMetadataSectionV1 metadata = new CTMMetadataSectionV1();
		if (obj.has("proxy")) {
			JsonElement proxyEle = obj.get("proxy");
			if (proxyEle.isJsonPrimitive() && proxyEle.getAsJsonPrimitive().isString()) {
				metadata.proxy = proxyEle.getAsString();
			}
			if (obj.entrySet().stream().filter(e -> e.getKey().equals("ctm_version")).count() > 1) {
				throw new JsonParseException("Cannot define other fields when using proxy");
			}
		}
		if (obj.has("type")) {
			JsonElement typeEle = obj.get("type");
			if (typeEle.isJsonPrimitive() && typeEle.getAsJsonPrimitive().isString()) {
				TextureType type = TextureTypeRegistry.INSTANCE.getType(typeEle.getAsString());
				if (type == null) {
					throw new JsonParseException("Invalid render type given: " + typeEle);
				} else {
					metadata.type = type;
				}
			}
		}
		if (obj.has("layer")) {
			JsonElement layerEle = obj.get("layer");
			if (layerEle.isJsonPrimitive() && layerEle.getAsJsonPrimitive().isString()) {
				try {
					metadata.blendMode = BlendMode.valueOf(layerEle.getAsString());
				} catch (IllegalArgumentException e) {
					throw new JsonParseException("Invalid block layer given: " + layerEle);
				}
			}
		}
		if (obj.has("textures")) {
			JsonElement texturesEle = obj.get("textures");
			if (texturesEle.isJsonArray()) {
				JsonArray texturesArr = texturesEle.getAsJsonArray();
				metadata.additionalTextures = new Identifier[texturesArr.size()];
				for (int i = 0; i < texturesArr.size(); i++) {
					JsonElement e = texturesArr.get(i);
					if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
						metadata.additionalTextures[i] = new Identifier(e.getAsString());
					}
				}
			}
		}
		if (obj.has("extra") && obj.get("extra").isJsonObject()) {
			metadata.extraData = obj.getAsJsonObject("extra");
		}
		return metadata;
	}

	@Override
	public String toString() {
		return "CTMMetadataSectionV1(type=" + this.getType() + ", blendMode=" + this.getBlendMode() + ", proxy=" + this.getProxy() + ", additionalTextures=" + java.util.Arrays.deepToString(this.getAdditionalTextures()) + ", extraData=" + this.getExtraData() + ")";
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
}
