package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.client.mixinterface.WeightedBakedModelExtension;

public abstract class AbstractCTMBakedModel extends ForwardingBakedModel {
	private static final Cache<BlockMeshCacheKey, Mesh> BLOCK_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();
	private static final Cache<BakedModel, Mesh> ITEM_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

	@NotNull
	private final CTMModelInfo modelInfo;

	public AbstractCTMBakedModel(@NotNull final BakedModel parent, @NotNull final CTMModelInfo modelInfo) {
		this.wrapped = Objects.requireNonNull(parent, "parent is marked non-null but is null");
		this.modelInfo = Objects.requireNonNull(modelInfo, "modelInfo is marked non-null but is null");
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

	/**
	 * Random sensitive parent, will proxy to {@link WeightedBakedModel} if possible.
	 */
	@NotNull
	public BakedModel getParent(Random random) {
		if (wrapped instanceof WeightedBakedModel) {
			return ((WeightedBakedModelExtension) wrapped).getRandomModel(random);
		}
		return wrapped;
	}

	private <T> T applyToParent(Random random, Function<AbstractCTMBakedModel, T> function) {
		BakedModel parent = getParent(random);
		if (parent instanceof AbstractCTMBakedModel) {
			return function.apply((AbstractCTMBakedModel) parent);
		}
		return null;
	}

	public Collection<CTMTexture<?>> getCTMTextures() {
		ImmutableList.Builder<CTMTexture<?>> builder = ImmutableList.builder();
		builder.addAll(modelInfo.getTextures());
		if (wrapped instanceof AbstractCTMBakedModel) {
			builder.addAll(((AbstractCTMBakedModel) wrapped).getCTMTextures());
		}
		return builder.build();
	}

	protected CTMTexture<?> getTexture(Random random, Identifier identifier) {
		CTMTexture<?> texture = modelInfo.getTexture(identifier);
		if (texture == null) {
			texture = applyToParent(random, (parent) -> parent.getTexture(random, identifier));
		}
		return texture;
	}

	protected Sprite getOverrideSprite(Random random, int tintIndex) {
		Sprite sprite = modelInfo.getOverrideSprite(tintIndex);
		if (sprite == null) {
			sprite = applyToParent(random, (parent) -> parent.getOverrideSprite(random, tintIndex));
		}
		return sprite;
	}

	protected CTMTexture<?> getOverrideTexture(Random random, int tintIndex, Identifier identifier) {
		CTMTexture<?> texture = modelInfo.getOverrideTexture(tintIndex, identifier);
		if (texture == null) {
			texture = applyToParent(random, (parent) -> parent.getOverrideTexture(random, tintIndex, identifier));
		}
		return texture;
	}

	public Mesh getBlockMesh(BlockState state, BlockRenderView blockView, BlockPos pos, Supplier<Random> randomSupplier) {
		Mesh mesh = null;
		try {
			TextureContextList contextList = new TextureContextList(getCTMTextures(), state, blockView, pos);
			BakedModel parent = getParent(randomSupplier.get());
			mesh = BLOCK_MESH_CACHE.get(new BlockMeshCacheKey(contextList, parent, state), () -> createMesh(parent, modelInfo, contextList, state, randomSupplier));
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
				return createMesh(getParent(randomSupplier.get()), modelInfo, null, block == null ? null : block.getDefaultState(), randomSupplier);
			});
		} catch (ExecutionException e) {
			throw new RuntimeException("Error getting CTM item mesh", e);
		}
		return mesh;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(getBlockMesh(state, blockView, pos, randomSupplier));
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(getItemMesh(stack, randomSupplier));
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	/**
	 * This method must be thread-safe as it may be called from multiple threads at once.
	 */
	protected abstract Mesh createMesh(BakedModel parent, CTMModelInfo modelInfo, @Nullable TextureContextList contextList, @Nullable BlockState state, Supplier<Random> randomSupplier);

	private static class BlockMeshCacheKey {
		@Nullable
		private final TextureContextList contextList;
		@NotNull
		private final BakedModel parent;
		@NotNull
		private final BlockState blockState;
		private final int hashCode;

		BlockMeshCacheKey(@Nullable final TextureContextList contextList, @NotNull final BakedModel parent, @NotNull final BlockState blockState) {
			this.contextList = contextList;
			this.parent = Objects.requireNonNull(parent, "parent is marked non-null but is null");
			this.blockState = Objects.requireNonNull(blockState, "blockState is marked non-null but is null");
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

		@Nullable
		public TextureContextList getContextList() {
			return contextList;
		}

		@NotNull
		public BakedModel getParent() {
			return parent;
		}

		@NotNull
		public BlockState getBlockState() {
			return blockState;
		}

		@Override
		public String toString() {
			return "AbstractCTMBakedModel.BlockMeshCacheKey(contextList=" + getContextList() + ", parent=" + getParent() + ", blockState=" + getBlockState() + ", hashCode=" + hashCode + ")";
		}
	}
}
