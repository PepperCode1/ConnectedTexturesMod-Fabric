package team.chisel.ctm.client.mixin;

import java.util.Map;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelAccessor {
	@Accessor("textureMap")
	Map<String, Either<SpriteIdentifier, String>> getTextureMap();
}
