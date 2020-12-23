package team.chisel.ctm.api.texture;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ObjectArrays;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.CTMClient;
import team.chisel.ctm.client.resource.CTMMetadataSectionV1;
import team.chisel.ctm.client.util.ResourceUtil;

public interface CTMMetadataSection {
	String SECTION_NAME = "ctm";

	int getVersion();

	TextureType getType();

	BlendMode getBlendMode();

	Identifier[] getAdditionalTextures();

	@Nullable
	String getProxy();

	JsonObject getExtraData();

	// TODO move elsewhere
	default CTMTexture<?> makeTexture(Sprite sprite, Function<SpriteIdentifier, Sprite> spriteGetter) {
		CTMMetadataSection meta = this;
		if (getProxy() != null) {
			Sprite proxySprite = spriteGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(getProxy())));
			try {
				meta = ResourceUtil.getMetadata(proxySprite);
				if (meta == null) {
					meta = new CTMMetadataSectionV1();
				}
				sprite = proxySprite;
			} catch (IOException e) {
				CTMClient.LOGGER.error("Could not parse metadata of proxy, ignoring proxy and using base texture." + getProxy(), e);
				meta = this;
			}
		}
		return meta.getType().makeTexture(new TextureInfo(Arrays.stream(ObjectArrays.concat(sprite.getId(), meta.getAdditionalTextures())).map(identifier -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, identifier)).map(spriteGetter::apply).toArray(Sprite[]::new), Optional.of(meta.getExtraData()), meta.getBlendMode()));
	}
}
