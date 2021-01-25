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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.mixin.BakedQuadAccessor;
import team.chisel.ctm.client.render.RenderUtil;

public class CTMBakedModel extends AbstractCTMBakedModel {
	private static final ThreadLocal<MeshBuilder> MESH_BUILDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().meshBuilder());
	private static final Direction[] CULL_FACES = ObjectArrays.concat(Direction.values(), (Direction) null);

	public CTMBakedModel(BakedModel parent, CTMModelInfo modelInfo) {
		super(parent, modelInfo);
	}

	@Override
	protected Mesh createMesh(BakedModel parent, CTMModelInfo modelInfo, @Nullable TextureContextList contextList, @Nullable BlockState state, Random random) {
		MeshBuilder builder = MESH_BUILDER.get();
		QuadEmitter emitter = builder.getEmitter();

		for (Direction cullFace : CULL_FACES) {
			List<BakedQuad> parentQuads = parent.getQuads(state, cullFace, random);

			// Gather all BakedQuads and map them to their CTMTextures
			// Pass BakedQuads that do not have an associated CTMTexture directly to the QuadEmitter
			for (BakedQuad bakedQuad : parentQuads) {
				Identifier spriteId = ((BakedQuadAccessor) bakedQuad).getSprite().getId();
				int tintIndex = bakedQuad.getColorIndex();

				Sprite overrideSprite = getOverrideSprite(random, tintIndex);
				if (overrideSprite != null) {
					bakedQuad = RenderUtil.retextureQuad(bakedQuad, overrideSprite);
					spriteId = overrideSprite.getId();
				}

				CTMTexture<?> texture = getOverrideTexture(random, tintIndex, spriteId);
				if (texture == null) {
					texture = getTexture(random, spriteId);
				}

				if (texture != null) {
					TextureContext context = contextList == null ? null : contextList.getContext(texture);
					Renderable renderable = texture.transformQuad(bakedQuad, cullFace, context);
					if (renderable != null) {
						renderable.render(emitter);
					}
				} else {
					emitter.fromVanilla(bakedQuad, null, cullFace);
					emitter.emit();
				}
			}
		}

		return builder.build();
	}

	@Override
	public Sprite getSprite() {
		CTMTexture<?> texture = getModelInfo().getTexture(getParent().getSprite().getId());
		if (texture != null) {
			return texture.getParticle();
		}
		return super.getSprite();
	}
}
