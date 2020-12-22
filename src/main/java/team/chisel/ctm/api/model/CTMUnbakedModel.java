package team.chisel.ctm.api.model;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import team.chisel.ctm.api.texture.CTMTexture;

public interface CTMUnbakedModel extends UnbakedModel {
	Collection<CTMTexture<?>> getCTMTextures();

	CTMTexture<?> getTexture(Identifier identifier);

	@Nullable
	Sprite getOverrideSprite(int colorIndex);

	@Nullable
	CTMTexture<?> getOverrideTexture(int colorIndex, Identifier identifier);
}
