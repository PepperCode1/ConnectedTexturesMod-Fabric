package team.chisel.ctm.client.resource;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import team.chisel.ctm.api.client.TextureType;

public interface CTMMetadataSection {
	int getVersion();

	TextureType getType();

	BlendMode getBlendMode();

	Identifier[] getAdditionalTextures();

	@Nullable
	Identifier getProxy();

	@Nullable
	JsonObject getExtraData();
}
