package team.chisel.ctm.client.render;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.texture.Renderable;

public class BakedQuadPassthrough implements Renderable {
	private BakedQuad bakedQuad;
	private RenderMaterial material;
	private Direction cullFace;

	public BakedQuadPassthrough(BakedQuad bakedQuad, RenderMaterial material, Direction cullFace) {
		this.bakedQuad = bakedQuad;
		this.material = material;
		this.cullFace = cullFace;
	}

	@Override
	public void render(QuadEmitter emitter) {
		emitter.fromVanilla(bakedQuad, material, cullFace);
		emitter.emit();
	}
}
