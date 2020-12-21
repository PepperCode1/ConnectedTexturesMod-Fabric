package team.chisel.ctm.client.mixin;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.util.collection.WeightedPicker;
import team.chisel.ctm.client.mixinterface.WeightedBakedModelExtension;

@Mixin(WeightedBakedModel.class)
public class WeightedBakedModelMixin implements WeightedBakedModelExtension {
	@Shadow
	@Final
	private int totalWeight;
	@Shadow
	@Final
	private List<WeightedBakedModel.Entry> models;
	
	@Override
	public BakedModel getRandomModel(Random random) {
		return ((WeightedBakedModelEntryAccessor) WeightedPicker.getAt(models, Math.abs((int) random.nextLong()) % totalWeight)).getModel();
	}
}
