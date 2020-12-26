package team.chisel.ctm.client.texture;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.api.client.util.NotNullType;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;

/**
 * Abstract implementation of {@link CTMTexture}.
 */
public abstract class AbstractTexture<T extends TextureType> implements CTMTexture<T> {
	protected static final ThreadLocal<MaterialFinder> FINDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().materialFinder());
	protected static final Map<BlendMode, RenderMaterial> BLEND_MODE_CACHE = new ConcurrentHashMap<>();

	protected T type;
	protected BlendMode blendMode;
	@NotNullType
	protected Sprite[] sprites;
	protected boolean hasLight;
	protected int skyLight;
	protected int blockLight;

	public AbstractTexture(T type, TextureInfo info) {
		this.type = type;
		this.blendMode = info.getBlendMode();
		this.sprites = info.getSprites();
		if (info.getInfo().isPresent()) {
			JsonElement light = info.getInfo().get().get("light");
			if (light != null) {
				if (light.isJsonPrimitive()) {
					this.hasLight = true;
					this.skyLight = this.blockLight = parseLightValue(light);
				} else if (light.isJsonObject()) {
					this.hasLight = true;
					JsonObject lightObject = light.getAsJsonObject();
					this.skyLight = parseLightValue(lightObject.get("sky"));
					this.blockLight = parseLightValue(lightObject.get("block"));
				}
			}
		}
	}

	private static int parseLightValue(@Nullable JsonElement data) {
		if (data != null && data.isJsonPrimitive() && data.getAsJsonPrimitive().isNumber()) {
			return MathHelper.clamp(data.getAsInt(), 0, 15);
		}
		return 0;
	}

	@Override
	public Sprite getParticle() {
		return sprites[0];
	}

	@Override
	public Collection<Identifier> getTextures() {
		return Arrays.stream(sprites).map(Sprite::getId).collect(Collectors.toList());
	}

	public T getType() {
		return type;
	}

	public BlendMode getBlendMode() {
		return blendMode;
	}

	protected SpriteUnbakedQuad unbake(BakedQuad bakedQuad, Direction cullFace) {
		SpriteUnbakedQuad quad = new SpriteUnbakedQuad(bakedQuad);
		quad.cullFace = cullFace;
		quad.nominalFace = bakedQuad.getFace();
		if (hasLight) {
			quad.setLight(skyLight, blockLight);
		}
		if (blendMode != null) {
			quad.material = BLEND_MODE_CACHE.get(blendMode);
			if (quad.material == null) {
				quad.material = FINDER.get().blendMode(0, blendMode).find();
				BLEND_MODE_CACHE.put(blendMode, quad.material);
			}
		}
		return quad;
	}
}
