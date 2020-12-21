package team.chisel.ctm.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;

@Mixin(WeightedBakedModel.Entry.class)
public interface WeightedBakedModelEntryAccessor {
	@Accessor("model")
	BakedModel getModel();
}
