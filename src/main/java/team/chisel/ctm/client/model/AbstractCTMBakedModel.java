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
import net.minecraft.client.MinecraftClient;
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
	private final CTMUnbakedModel unbakedModel;
	@NotNull
	private final BakedModel parent;

	public AbstractCTMBakedModel(@NotNull final CTMUnbakedModel unbakedModel, @NotNull final BakedModel parent) {
		if (unbakedModel == null) {
			throw new NullPointerException("unbakedModel is marked non-null but is null");
		}
		if (parent == null) {
			throw new NullPointerException("parent is marked non-null but is null");
		}
		this.unbakedModel = unbakedModel;
		this.parent = parent;
	}

	public static void invalidateCaches() {
		ITEM_CACHE.invalidateAll();
		STATE_CACHE.invalidateAll();
	}

	@NotNull
	public CTMUnbakedModel getUnbakedModel() {
		return unbakedModel;
	}

	@NotNull
	public BakedModel getParent() {
		return parent;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
		//ModelHelper.toQuadLists(mesh);
		return parent.getQuads(state, face, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return parent.useAmbientOcclusion();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}

	@Override
	public boolean hasDepth() {
		return parent.hasDepth();
	}

	@Override
	public boolean isBuiltin() {
		return parent.isBuiltin();
	}

	@Override
	@NotNull
	public Sprite getSprite() {
		return parent.getSprite();
	}

	@Override
	public ModelTransformation getTransformation() {
		return parent.getTransformation();
	}

	@Override
	public boolean isSideLit() {
		return parent.isSideLit();
	}

	/**
	 * Random sensitive parent, will proxy to {@link WeightedBakedModel} if possible.
	 */
	@NotNull
	public BakedModel getParent(Random random) {
		if (getParent() instanceof WeightedBakedModel) {
			return ((WeightedBakedModelExtension) getParent()).getRandomModel(random);
		}
		return getParent();
	}

	private <T> T applyToParent(Random rand, Function<AbstractCTMBakedModel, T> func) {
		BakedModel parent = getParent(rand);
		if (parent instanceof AbstractCTMBakedModel) {
			return func.apply((AbstractCTMBakedModel) parent);
		}
		return null;
	}

	protected CTMTexture<?> getOverrideTexture(Random rand, int tintIndex, Identifier texture) {
		CTMTexture<?> ret = getUnbakedModel().getOverrideTexture(tintIndex, texture);
		if (ret == null) {
			ret = applyToParent(rand, parent -> parent.getOverrideTexture(rand, tintIndex, texture));
		}
		return ret;
	}

	protected CTMTexture<?> getTexture(Random rand, Identifier texture) {
		CTMTexture<?> ret = getUnbakedModel().getTexture(texture);
		if (ret == null) {
			ret = applyToParent(rand, parent -> parent.getTexture(rand, texture));
		}
		return ret;
	}

	protected Sprite getOverrideSprite(Random rand, int tintIndex) {
		Sprite ret = getUnbakedModel().getOverrideSprite(tintIndex);
		if (ret == null) {
			ret = applyToParent(rand, parent -> parent.getOverrideSprite(rand, tintIndex));
		}
		return ret;
	}

	public Collection<CTMTexture<?>> getCTMTextures() {
		ImmutableList.Builder<CTMTexture<?>> builder = ImmutableList.builder();
		builder.addAll(getUnbakedModel().getCTMTextures());
		if (getParent() instanceof AbstractCTMBakedModel) {
			builder.addAll(((AbstractCTMBakedModel) getParent()).getCTMTextures());
		}
		return builder.build();
	}

	@SuppressWarnings("resource")
	public Mesh getBlockMesh(BlockState state, Random random, CTMRenderContext renderContext) {
		ProfileUtil.push("ctm_models");
		BakedModel parent = getParent(random);
		Mesh mesh = null;
		try {
			if (MinecraftClient.getInstance().world != null && renderContext != null) {
				ProfileUtil.push("state_creation");
				TextureContextList contextList = renderContext.getContextList(state, this);
				Object2LongMap<CTMTexture<?>> serialized = contextList.serialized();
				ProfileUtil.swap("model_creation");
				mesh = STATE_CACHE.get(new State(state, serialized, parent), () -> createMesh(state, unbakedModel, parent, contextList, random));
				ProfileUtil.pop();
			} else if (state != null) {
				ProfileUtil.push("model_creation");
				mesh = STATE_CACHE.get(new State(state, null, parent), () -> createMesh(state, unbakedModel, parent, null, random));
				ProfileUtil.pop();
			}
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		ProfileUtil.pop();
		return mesh;
	}

	public Mesh getItemMesh(ItemStack itemStack, Random random) {
		ProfileUtil.push("ctm_models");
		Item item = itemStack.getItem();
		Mesh mesh = null;
		try {
			mesh = ITEM_CACHE.get(item, () -> {
				Block block = null;
				if (item instanceof BlockItem) {
					block = ((BlockItem) item).getBlock();
				}
				return createMesh(block == null ? null : block.getDefaultState(), unbakedModel, getParent(random), null, random);
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		ProfileUtil.pop();
		return mesh;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(getBlockMesh(state, randomSupplier.get(), new CTMRenderContext(blockView, pos)));
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
	protected abstract Mesh createMesh(BlockState state, @NotNull CTMUnbakedModel unbakedModel, BakedModel parent, TextureContextList contextList, Random random);

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
