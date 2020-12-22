package team.chisel.ctm.client.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

public class WrappingCache {
	public final Set<Identifier> registeredTextures = new HashSet<>();
	public final Map<JsonUnbakedModel, UnbakedModel> jsonModelsToWrap = new HashMap<>();
	public final Object2BooleanMap<Identifier> wrappedModels = new Object2BooleanLinkedOpenHashMap<>();

	public void invalidate() {
		registeredTextures.clear();
		jsonModelsToWrap.clear();
		wrappedModels.clear();
	}
}
