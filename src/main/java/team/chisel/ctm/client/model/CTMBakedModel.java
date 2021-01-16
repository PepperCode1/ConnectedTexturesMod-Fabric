package team.chisel.ctm.client.model;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ObjectArrays;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.mixin.BakedQuadAccessor;
import team.chisel.ctm.client.render.BakedQuadUtil;

public class CTMBakedModel extends AbstractCTMBakedModel {
	private static final ThreadLocal<MeshBuilder> MESH_BUILDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().meshBuilder());
	private static final Direction[] CULL_FACES = ObjectArrays.concat(Direction.values(), (Direction) null);

	public CTMBakedModel(CTMUnbakedModel unbakedModel, BakedModel parent) {
		super(unbakedModel, parent);
	}

	@Override
	protected Mesh createMesh(@Nullable BlockState state, CTMUnbakedModel unbakedModel, BakedModel parent, @Nullable TextureContextList contextList, Random random) {
		MeshBuilder builder = MESH_BUILDER.get();
		QuadEmitter emitter = builder.getEmitter();

		while (parent instanceof CTMBakedModel) {
			parent = ((AbstractCTMBakedModel) parent).getParent(random);
		}

		for (Direction cullFace : CULL_FACES) {
			List<BakedQuad> parentQuads = parent.getQuads(state, cullFace, random);

			// Gather all quads and map them to their textures
			// All quads should have an associated CTMTexture, so ignore any that do not
			for (BakedQuad bakedQuad : parentQuads) {
				CTMTexture<?> texture = getOverrideTexture(random, bakedQuad.getColorIndex(), ((BakedQuadAccessor) bakedQuad).getSprite().getId());
				if (texture == null) {
					texture = getTexture(random, ((BakedQuadAccessor) bakedQuad).getSprite().getId());
				}

				if (texture != null) {
					Sprite spriteReplacement = getOverrideSprite(random, bakedQuad.getColorIndex());
					if (spriteReplacement != null) {
						bakedQuad = BakedQuadUtil.retextureQuad(bakedQuad, spriteReplacement);
					}

					TextureContext context = contextList == null ? null : contextList.getTextureContext(texture);
					Renderable renderable = texture.transformQuad(bakedQuad, context, cullFace);
					if (renderable != null) {
						renderable.render(emitter);
					}
				}
			}
		}

		return builder.build();
	}

	@Override
	public Sprite getSprite() {
		CTMTexture<?> texture = getUnbakedModel().getTexture(getParent().getSprite().getId());
		if (texture != null) {
			return texture.getParticle();
		}
		return getParent().getSprite();
	}
}
