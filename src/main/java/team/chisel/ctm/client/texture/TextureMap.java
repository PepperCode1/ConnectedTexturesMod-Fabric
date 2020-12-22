package team.chisel.ctm.client.texture;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.Submap;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.RenderableList;
import team.chisel.ctm.client.render.SpriteUnbakedQuad;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.texture.context.TextureContextGrid;
import team.chisel.ctm.client.texture.context.TextureContextGrid.Point2i;
import team.chisel.ctm.client.texture.context.TextureContextPosition;
import team.chisel.ctm.client.texture.type.TextureTypeMap;

public class TextureMap extends AbstractTexture<TextureTypeMap> {
	private final int xSize;
	private final int ySize;
	private final int xOffset;
	private final int yOffset;
	private final MapType map;

	public TextureMap(TextureTypeMap type, TextureInfo info) {
		super(type, info);
		this.map = type.getMapType();
		if (info.getInfo().isPresent()) {
			JsonObject object = info.getInfo().get();
			if (object.has("width") && object.has("height")) {
				Preconditions.checkArgument(object.get("width").isJsonPrimitive() && object.get("width").getAsJsonPrimitive().isNumber(), "width must be a number!");
				Preconditions.checkArgument(object.get("height").isJsonPrimitive() && object.get("height").getAsJsonPrimitive().isNumber(), "height must be a number!");
				this.xSize = object.get("width").getAsInt();
				this.ySize = object.get("height").getAsInt();
			} else if (object.has("size")) {
				Preconditions.checkArgument(object.get("size").isJsonPrimitive() && object.get("size").getAsJsonPrimitive().isNumber(), "size must be a number!");
				this.xSize = object.get("size").getAsInt();
				this.ySize = object.get("size").getAsInt();
			} else {
				xSize = ySize = 2;
			}
			if (object.has("x_offset")) {
				Preconditions.checkArgument(object.get("x_offset").isJsonPrimitive() && object.get("x_offset").getAsJsonPrimitive().isNumber(), "x_offset must be a number!");
				this.xOffset = object.get("x_offset").getAsInt();
			} else {
				this.xOffset = 0;
			}
			if (object.has("y_offset")) {
				Preconditions.checkArgument(object.get("y_offset").isJsonPrimitive() && object.get("y_offset").getAsJsonPrimitive().isNumber(), "y_offset must be a number!");
				this.yOffset = object.get("y_offset").getAsInt();
			} else {
				this.yOffset = 0;
			}
		} else {
			xOffset = yOffset = 0;
			xSize = ySize = 2;
		}
		Preconditions.checkArgument(xSize > 0 && ySize > 0, "Cannot have a dimension of 0!");
	}

	@Override
	public Renderable transformQuad(BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
		return map.transformQuad(this, bakedQuad, context, quadGoal, cullFace);
	}

	public int getXSize() {
		return this.xSize;
	}

	public int getYSize() {
		return this.ySize;
	}

	public int getXOffset() {
		return this.xOffset;
	}

	public int getYOffset() {
		return this.yOffset;
	}

	public enum MapType {
		RANDOM {
			@Override
			protected Renderable transformQuad(TextureMap texture, BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
				Point2i textureCoords = context == null ? new Point2i(1, 1) : ((TextureContextGrid) context).getTextureCoords(bakedQuad.getFace());
				float intervalX = 16.0F / texture.getXSize();
				float intervalY = 16.0F / texture.getYSize();
				float maxU = textureCoords.getX() * intervalX;
				float maxV = textureCoords.getY() * intervalY;
				Submap submap = new SubmapImpl(intervalX, intervalY, maxU - intervalX, maxV - intervalY);
				// TODO move this code somewhere else, it's copied from below
				SpriteUnbakedQuad quad = texture.unbake(bakedQuad, cullFace);
				if (quadGoal == 4) {
					SpriteUnbakedQuad[] quads = quad.toQuadrants();
					for (int i = 0; i < quads.length; i++) {
						if (quads[i] != null) {
							quads[i].setUVBounds(texture.sprites[0]);
							quads[i].applySubmap(submap);
						}
					}
					return new RenderableList(List.of(quads));
				} else {
					quad.setUVBounds(texture.sprites[0]);
					quad.applySubmap(submap);
					return quad;
				}
			}

			@Override
			public TextureContext getContext(@NotNull BlockPos pos, @NotNull TextureMap tex) {
				return new TextureContextGrid.Random(pos, tex, true);
			}
		},
		PATTERNED {
			@Override
			protected Renderable transformQuad(TextureMap texture, BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace) {
				Point2i textureCoords = context == null ? new Point2i(0, 0) : ((TextureContextGrid) context).getTextureCoords(bakedQuad.getFace());
				float intervalU = 16.0F / texture.xSize;
				float intervalV = 16.0F / texture.ySize;
				// throw new RuntimeException(index % variationSize+" and "+index/variationSize);
				float minU = intervalU * textureCoords.getX();
				float minV = intervalV * textureCoords.getY();
				Submap submap = new SubmapImpl(intervalU, intervalV, minU, minV);
				SpriteUnbakedQuad quad = texture.unbake(bakedQuad, cullFace);
				if (quadGoal == 4) {
					SpriteUnbakedQuad[] quads = quad.toQuadrants();
					for (int i = 0; i < quads.length; i++) {
						if (quads[i] != null) {
							quads[i].setUVBounds(texture.sprites[0]);
							quads[i].applySubmap(submap);
						}
					}
					return new RenderableList(List.of(quads));
				} else {
					quad.setUVBounds(texture.sprites[0]);
					quad.applySubmap(submap);
					return quad;
				}
			}

			@Override
			public TextureContext getContext(@NotNull BlockPos pos, @NotNull TextureMap texture) {
				return new TextureContextGrid.Patterned(pos, texture, true);
			}
		};

		protected abstract Renderable transformQuad(TextureMap texture, BakedQuad bakedQuad, @Nullable TextureContext context, int quadGoal, Direction cullFace);

		@NotNull
		public TextureContext getContext(@NotNull BlockPos pos, @NotNull TextureMap texture) {
			return new TextureContextPosition(pos);
		}
	}
}
