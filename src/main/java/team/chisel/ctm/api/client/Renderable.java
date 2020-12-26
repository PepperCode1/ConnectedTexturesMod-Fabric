package team.chisel.ctm.api.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public interface Renderable {
	void render(QuadEmitter emitter);
}
