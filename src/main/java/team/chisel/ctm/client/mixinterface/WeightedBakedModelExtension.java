package team.chisel.ctm.client.mixinterface;

import java.util.Random;

import net.minecraft.client.render.model.BakedModel;

public interface WeightedBakedModelExtension {
	BakedModel getRandomModel(Random random);
}
