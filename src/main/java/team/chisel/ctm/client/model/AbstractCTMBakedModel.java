package team.chisel.ctm.client.model;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.client.mixinterface.WeightedBakedModelExtension;
import team.chisel.ctm.client.util.ProfileUtil;

public abstract class AbstractCTMBakedModel implements BakedModel, FabricBakedModel {
	private static final Cache<State, Mesh> STATE_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).<State, Mesh>build();
	private static final Cache<Item, Mesh> ITEM_CACHE = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).<Item, Mesh>build();

	@NotNull
	private final BakedModel parent;
	@NotNull
	private final CTMModelInfo modelInfo;

	public AbstractCTMBakedModel(@NotNull final BakedModel parent, @NotNull final CTMModelInfo modelInfo) {
		if (parent == null) {
			throw new NullPointerException("parent is marked non-null but is null");
		}
		if (modelInfo == null) {
			throw new NullPointerException("modelInfo is marked non-null but is null");
		}
		this.parent = parent;
		this.modelInfo = modelInfo;
	}

	public static void invalidateCaches() {
		ITEM_CACHE.invalidateAll();
		STATE_CACHE.invalidateAll();
	}

	@NotNull
	public BakedModel getParent() {
		return parent;
	}

	@NotNull
	public CTMModelInfo getModelInfo() {
		return modelInfo;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
		return parent.getQuads(state, face, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return parent.useAmbientOcclusion();
	}

	@Override
	public boolean hasDepth() {
		return parent.hasDepth();
	}

	@Override
	public boolean isSideLit() {
		return parent.isSideLit();
	}

	@Override
	public boolean isBuiltin() {
		return parent.isBuiltin();
	}

	@Override
	public Sprite getSprite() {
		return parent.getSprite();
	}

	@Override
	public ModelTransformation getTransformation() {
		return parent.getTransformation();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}

	/**
	 * Random sensitive parent, will proxy to {@link WeightedBakedModel} if possible.
	 */
	@NotNull
	public BakedModel getParent(Random random) {
		if (parent instanceof WeightedBakedModel) {
			return ((WeightedBakedModelExtension) parent).getRandomModel(random);
		}
		return parent;
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
		if (parent instanceof AbstractCTMBakedModel) {
			builder.addAll(((AbstractCTMBakedModel) parent).getCTMTextures());
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

	public Mesh getBlockMesh(BlockState state, Random random, BlockRenderView blockView, BlockPos pos) {
		ProfileUtil.push("ctm_model_block");
		BakedModel parent = getParent(random);
		Mesh mesh = null;
		try {
			ProfileUtil.push("ctm_context_creation");
			TextureContextList contextList = new TextureContextList(state, getCTMTextures(), blockView, pos);
			Object2LongMap<CTMTexture<?>> serialized = contextList.serialized();
			ProfileUtil.swap("ctm_mesh_creation");
			mesh = STATE_CACHE.get(new State(state, serialized, parent), () -> createMesh(parent, modelInfo, contextList, state, random));
			ProfileUtil.pop();
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		ProfileUtil.pop();
		return mesh;
	}

	public Mesh getItemMesh(ItemStack itemStack, Random random) {
		ProfileUtil.push("ctm_model_item");
		Item item = itemStack.getItem();
		Mesh mesh = null;
		try {
			mesh = ITEM_CACHE.get(item, () -> {
				Block block = null;
				if (item instanceof BlockItem) {
					block = ((BlockItem) item).getBlock();
				}
				return createMesh(getParent(random), modelInfo, null, block == null ? null : block.getDefaultState(), random);
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		ProfileUtil.pop();
		return mesh;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(getBlockMesh(state, randomSupplier.get(), blockView, pos));
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(getItemMesh(stack, randomSupplier.get()));
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	/**
	 * This method must be thread-safe as it may be called from multiple threads at once.
	 */
	protected abstract Mesh createMesh(BakedModel parent, @NotNull CTMModelInfo modelInfo, TextureContextList contextList, BlockState state, Random random);

	private static class State {
		@NotNull
		private final BlockState cleanState;
		@Nullable
		private final Object2LongMap<CTMTexture<?>> serializedContext;
		@NotNull
		private final BakedModel parent;

		State(@NotNull final BlockState cleanState, @Nullable final Object2LongMap<CTMTexture<?>> serializedContext, @NotNull final BakedModel parent) {
			if (cleanState == null) {
				throw new NullPointerException("cleanState is marked non-null but is null");
			}
			if (parent == null) {
				throw new NullPointerException("parent is marked non-null but is null");
			}
			this.cleanState = cleanState;
			this.serializedContext = serializedContext;
			this.parent = parent;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			State other = (State) obj;
			if (cleanState != other.cleanState) {
				return false;
			}
			if (parent != other.parent) {
				return false;
			}
			if (serializedContext == null) {
				if (other.serializedContext != null) {
					return false;
				}
			} else if (!serializedContext.equals(other.serializedContext)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			// for some reason blockstates hash their properties, we only care about the identity hash
			result = prime * result + System.identityHashCode(cleanState);
			result = prime * result + (parent == null ? 0 : parent.hashCode());
			result = prime * result + (serializedContext == null ? 0 : serializedContext.hashCode());
			return result;
		}

		@NotNull
		public BlockState getCleanState() {
			return cleanState;
		}

		@Nullable
		public Object2LongMap<CTMTexture<?>> getSerializedContext() {
			return serializedContext;
		}

		@NotNull
		public BakedModel getParent() {
			return parent;
		}

		@Override
		public String toString() {
			return "AbstractCTMBakedModel.State(cleanState=" + getCleanState() + ", serializedContext=" + getSerializedContext() + ", parent=" + getParent() + ")";
		}
	}
}
