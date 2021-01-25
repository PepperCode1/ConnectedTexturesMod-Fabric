package team.chisel.ctm.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelAccessor {
	@Accessor("parent")
	JsonUnbakedModel getParent();
}
