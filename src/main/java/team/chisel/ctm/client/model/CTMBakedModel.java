package team.chisel.ctm.client.model;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.api.client.TextureContext;
import team.chisel.ctm.client.util.RenderUtil;

public class CTMBakedModel extends ForwardingBakedModel {
	private static final Cache<BlockMeshCacheKey, Mesh> BLOCK_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();
	private static final Cache<BakedModel, Mesh> ITEM_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

	@NotNull
	protected CTMModelInfo modelInfo;
	protected Sprite sprite;

	public CTMBakedModel(@NotNull final BakedModel parent, @NotNull final CTMModelInfo modelInfo) {
		this.wrapped = Objects.requireNonNull(parent, "parent is marked non-null but is null");
		this.modelInfo = Objects.requireNonNull(modelInfo, "modelInfo is marked non-null but is null");
		sprite = initSprite();
	}

	public static void invalidateCaches() {
		BLOCK_MESH_CACHE.invalidateAll();
		ITEM_MESH_CACHE.invalidateAll();
	}

	@NotNull
	public BakedModel getParent() {
		return wrapped;
	}

	@NotNull
	public CTMModelInfo getModelInfo() {
		return modelInfo;
	}

	protected Sprite initSprite() {
		CTMTexture<?> texture = getModelInfo().getTexture(getParent().getSprite().getId());
		if (texture != null) {
			return texture.getParticle();
		}
		return null;
	}

	@Override
	public Sprite getSprite() {
		if (sprite != null) {
			return sprite;
		}
		return super.getSprite();
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(getBlockMesh(state, blockView, pos, randomSupplier));
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(getItemMesh(stack, randomSupplier));
	}

	public Mesh getBlockMesh(BlockState state, BlockRenderView blockView, BlockPos pos, Supplier<Random> randomSupplier) {
		Mesh mesh = null;
		try {
			TextureContextList contextList = new TextureContextList(getModelInfo().getTextures(), state, blockView, pos);
			BakedModel parent = getParent();
			mesh = BLOCK_MESH_CACHE.get(new BlockMeshCacheKey(contextList, parent, state), () -> createMesh(parent, getModelInfo(), contextList, state, randomSupplier));
		} catch (ExecutionException e) {
			throw new RuntimeException("Error getting CTM block mesh", e);
		}
		return mesh;
	}

	public Mesh getItemMesh(ItemStack itemStack, Supplier<Random> randomSupplier) {
		Mesh mesh = null;
		try {
			mesh = ITEM_MESH_CACHE.get(this, () -> {
				Item item = itemStack.getItem();
				Block block = null;
				if (item instanceof BlockItem) {
					block = ((BlockItem) item).getBlock();
				}
				return createMesh(getParent(), getModelInfo(), null, block == null ? null : block.getDefaultState(), randomSupplier);
			});
		} catch (ExecutionException e) {
			throw new RuntimeException("Error getting CTM item mesh", e);
		}
		return mesh;
	}

	/**
	 * This method must be thread-safe as it may be called from multiple threads at once.
	 */
	protected Mesh createMesh(BakedModel parent, CTMModelInfo modelInfo, @Nullable TextureContextList contextList, @Nullable BlockState state, Supplier<Random> randomSupplier) {
		MeshBuilder builder = RenderUtil.MESH_BUILDER.get();
		QuadEmitter emitter = builder.getEmitter();

		for (Direction cullFace : RenderUtil.CULL_FACES) {
			List<BakedQuad> parentQuads = parent.getQuads(state, cullFace, randomSupplier.get());

			// Gather all BakedQuads and map them to their CTMTextures
			// Pass BakedQuads that do not have an associated CTMTexture directly to the QuadEmitter
			for (BakedQuad bakedQuad : parentQuads) {
				Identifier spriteId = bakedQuad.getSprite().getId();
				int tintIndex = bakedQuad.getColorIndex();

				Sprite overrideSprite = modelInfo.getOverrideSprite(tintIndex);
				if (overrideSprite != null) {
					bakedQuad = RenderUtil.retextureBakedQuad(bakedQuad, overrideSprite);
					spriteId = overrideSprite.getId();
				}

				CTMTexture<?> texture = modelInfo.getOverrideTexture(tintIndex, spriteId);
				if (texture == null) {
					texture = modelInfo.getTexture(spriteId);
				}

				boolean renderFallback = false;
				if (texture != null) {
					TextureContext context = contextList == null ? null : contextList.getContext(texture);
					Renderable renderable = texture.transformQuad(bakedQuad, cullFace, context);
					if (renderable != null) {
						renderable.render(emitter);
					} else {
						renderFallback = true;
					}
				} else {
					renderFallback = true;
				}

				if (renderFallback) {
					emitter.fromVanilla(bakedQuad, null, cullFace);
					emitter.emit();
				}
			}
		}

		return builder.build();
	}

	private static class BlockMeshCacheKey {
		private final int hashCode;

		private BlockMeshCacheKey(TextureContextList contextList, BakedModel parent, BlockState blockState) {
			hashCode = Objects.hash(contextList, parent, blockState);
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			BlockMeshCacheKey other = (BlockMeshCacheKey) obj;
			if (hashCode != other.hashCode) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
