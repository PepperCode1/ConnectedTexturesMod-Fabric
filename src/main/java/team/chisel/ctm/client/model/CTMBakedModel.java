package team.chisel.ctm.client.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
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
import team.chisel.ctm.client.mixin.BakedQuadAccessor;
import team.chisel.ctm.client.util.RenderUtil;

public class CTMBakedModel extends ForwardingBakedModel {
	private static final Cache<BlockMeshCacheKey, Mesh> BLOCK_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();
	private static final Cache<BakedModel, Mesh> ITEM_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

	protected static final ThreadLocal<ObjectContainer> CONTAINERS = ThreadLocal.withInitial(ObjectContainer::new);

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
		ObjectContainer container = CONTAINERS.get();
		BakedModel parent = getParent();
		CTMModelInfo modelInfo = getModelInfo();
		TextureContextMap contextMap = container.contextMap;
		contextMap.fill(modelInfo.getTextures(), state, blockView, pos);

		Mesh mesh = null;
		try {
			mesh = BLOCK_MESH_CACHE.get(new BlockMeshCacheKey(parent, state, contextMap.toDataArray()), () -> createMesh(parent, modelInfo, contextMap, state, randomSupplier, container.meshBuilder));
		} catch (ExecutionException e) {
			throw new RuntimeException("Error getting CTM block mesh", e);
		} finally {
			contextMap.reset();
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
				return createMesh(getParent(), getModelInfo(), null, block == null ? null : block.getDefaultState(), randomSupplier, CONTAINERS.get().meshBuilder);
			});
		} catch (ExecutionException e) {
			throw new RuntimeException("Error getting CTM item mesh", e);
		}
		return mesh;
	}

	/**
	 * This method must be thread-safe as it may be called from multiple threads at once.
	 */
	protected Mesh createMesh(BakedModel parent, CTMModelInfo modelInfo, @Nullable TextureContextMap contextMap, @Nullable BlockState state, Supplier<Random> randomSupplier, MeshBuilder meshBuilder) {
		QuadEmitter emitter = meshBuilder.getEmitter();

		for (Direction cullFace : RenderUtil.CULL_FACES) {
			List<BakedQuad> parentQuads = parent.getQuads(state, cullFace, randomSupplier.get());

			// Gather all BakedQuads and map them to their CTMTextures
			// Pass BakedQuads that do not have an associated CTMTexture directly to the QuadEmitter
			for (BakedQuad bakedQuad : parentQuads) {
				Identifier spriteId = ((BakedQuadAccessor) bakedQuad).getSprite().getId();
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
					TextureContext context = contextMap == null ? null : contextMap.getContext(texture);
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

		return meshBuilder.build();
	}

	private static class BlockMeshCacheKey {
		private final BakedModel parent;
		private final BlockState blockState;
		private final long[] data;

		private BlockMeshCacheKey(BakedModel parent, BlockState blockState, long[] data) {
			this.parent = parent;
			this.blockState = blockState;
			this.data = data;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			BlockMeshCacheKey other = (BlockMeshCacheKey) obj;
			if (parent != other.parent) return false;
			if (blockState != other.blockState) return false;
			if (!Arrays.equals(data, other.data)) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return Objects.hash(parent, blockState, Arrays.hashCode(data));
		}
	}

	protected static class ObjectContainer {
		public TextureContextMap contextMap = new TextureContextMap();
		public MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
	}
}
