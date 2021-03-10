package team.chisel.ctm.client.texture;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.api.client.TextureType;
import team.chisel.ctm.client.render.UnbakedQuad;

/**
 * Abstract implementation of {@link CTMTexture}.
 */
public abstract class AbstractTexture<T extends TextureType> implements CTMTexture<T> {
	protected static final ThreadLocal<MaterialFinder> FINDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().materialFinder());

	protected T type;
	@NotNull
	protected Sprite[] sprites;
	protected RenderMaterial material;
	protected boolean hasLight;
	protected int skyLight;
	protected int blockLight;

	public AbstractTexture(T type, TextureInfo info) {
		this.type = type;
		sprites = info.getSprites();

		boolean isEmissive = false;
		if (info.getExtraInfo().isPresent()) {
			JsonElement light = info.getExtraInfo().get().get("light");
			if (light != null) {
				if (light.isJsonPrimitive()) {
					hasLight = true;
					skyLight = blockLight = parseLightValue(light);
				} else if (light.isJsonObject()) {
					this.hasLight = true;
					JsonObject lightObject = light.getAsJsonObject();
					skyLight = parseLightValue(lightObject.get("sky"));
					blockLight = parseLightValue(lightObject.get("block"));
				}
				if (skyLight == 15 && blockLight == 15) {
					isEmissive = true;
				}
			}
		}

		BlendMode blendMode = info.getBlendMode();
		if (blendMode != BlendMode.DEFAULT || isEmissive) {
			material = FINDER.get().blendMode(0, blendMode).emissive(0, isEmissive).find();
		}
	}

	private static int parseLightValue(@Nullable JsonElement data) {
		if (data != null && data.isJsonPrimitive() && data.getAsJsonPrimitive().isNumber()) {
			return MathHelper.clamp(data.getAsInt(), 0, 15);
		}
		return 0;
	}

	@Override
	public Collection<Identifier> getTextures() {
		return Arrays.stream(sprites).map(Sprite::getId).collect(Collectors.toList());
	}

	@Override
	public T getType() {
		return type;
	}

	@Override
	public Sprite getParticle() {
		return sprites[0];
	}

	protected UnbakedQuad unbake(BakedQuad bakedQuad, Direction cullFace) {
		UnbakedQuad quad = new UnbakedQuad(bakedQuad);
		quad.cullFace = cullFace;
		quad.material = material;
		if (hasLight) {
			quad.setLight(skyLight, blockLight);
		}
		return quad;
	}
}
