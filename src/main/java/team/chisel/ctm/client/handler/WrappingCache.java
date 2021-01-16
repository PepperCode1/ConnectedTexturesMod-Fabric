package team.chisel.ctm.client.handler;

import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

public class WrappingCache {
	public final Map<JsonUnbakedModel, UnbakedModel> jsonModelsToWrap = new HashMap<>();
	public final Object2BooleanMap<Identifier> wrappedModels = new Object2BooleanLinkedOpenHashMap<>();

	public void invalidate() {
		jsonModelsToWrap.clear();
		wrappedModels.clear();
	}
}
