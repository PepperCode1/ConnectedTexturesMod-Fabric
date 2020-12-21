package team.chisel.ctm.client.render;

import java.util.List;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import team.chisel.ctm.api.texture.Renderable;

public class RenderableList implements Renderable {
	private List<? extends Renderable> elements;
	
	public RenderableList(List<? extends Renderable> elements) {
		this.elements = elements;
	}
	
	public List<? extends Renderable> getQuads() {
		return elements;
	}
	
	@Override
	public void render(QuadEmitter emitter) {
		for (Renderable element : elements) {
			if (element != null) {
				element.render(emitter);
			}
		}
	}
}
