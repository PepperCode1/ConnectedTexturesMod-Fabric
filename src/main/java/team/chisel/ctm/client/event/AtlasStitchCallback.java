package team.chisel.ctm.client.event;

import java.util.Set;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public interface AtlasStitchCallback {
	public static final Event<AtlasStitchCallback> EVENT = EventFactory.createArrayBacked(AtlasStitchCallback.class,
		(listeners) -> (SpriteAtlasTexture atlas, Set<Identifier> sprites) -> {
			for (AtlasStitchCallback callback : listeners) {
				callback.onAtlasStitch(atlas, sprites);
			}
		}
	);
	
	public void onAtlasStitch(SpriteAtlasTexture atlas, Set<Identifier> sprites);
}
