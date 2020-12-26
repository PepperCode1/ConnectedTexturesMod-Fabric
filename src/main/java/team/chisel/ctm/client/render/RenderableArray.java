package team.chisel.ctm.client.render;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

import team.chisel.ctm.api.client.Renderable;

public class RenderableArray implements Renderable {
	private Renderable[] array;

	public RenderableArray(Renderable[] array) {
		this.array = array;
	}

	public Renderable[] getArray() {
		return array;
	}

	@Override
	public void render(QuadEmitter emitter) {
		for (Renderable element : array) {
			if (element != null) {
				element.render(emitter);
			}
		}
	}
}
