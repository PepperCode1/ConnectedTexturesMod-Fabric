package team.chisel.ctm.client.texture.context;

import java.util.EnumMap;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

import team.chisel.ctm.client.texture.TextureMap;
import team.chisel.ctm.client.util.FaceOffset;

public abstract class TextureContextGrid extends TextureContextPosition {
	private final EnumMap<Direction, Point2i> textureCoords = new EnumMap<>(Direction.class);
	private final long serialized;

	public TextureContextGrid(BlockPos pos, TextureMap tex, boolean applyOffset) {
		super(pos);
		// Since we can only return a long, we must limit to 10 bits of data per face = 60 bits
		Preconditions.checkArgument(tex.getXSize() * tex.getYSize() < 1024, "V* Texture size too large for texture %s", tex.getParticle());
		if (applyOffset) {
			applyOffset();
		}
		long serialized = 0;
		for (@NotNull Direction side : Direction.values()) {
			BlockPos modifiedPosition = position.add(FaceOffset.getBlockPosOffsetFromFaceOffset(side, tex.getXOffset(), tex.getYOffset()));
			Point2i coords = calculateTextureCoord(modifiedPosition, tex.getXSize(), tex.getYSize(), side);
			textureCoords.put(side, coords);
			// Calculate a unique index for a submap (x + (y * x-size)), then shift it left by the max bit storage (10 bits = 1024 unique indices)
			serialized |= (coords.x + (coords.y * tex.getXSize())) << (10 * side.ordinal());
		}
		this.serialized = serialized;
	}

	protected abstract Point2i calculateTextureCoord(BlockPos pos, int w, int h, Direction side);

	public Point2i getTextureCoords(Direction side) {
		return textureCoords.get(side);
	}

	@Override
	public long getCompressedData() {
		return serialized;
	}

	public static final class Point2i {
		private final int x;
		private final int y;

		public Point2i(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof TextureContextGrid.Point2i)) return false;
			final TextureContextGrid.Point2i other = (TextureContextGrid.Point2i) o;
			if (this.getX() != other.getX()) return false;
			if (this.getY() != other.getY()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getX();
			result = result * PRIME + this.getY();
			return result;
		}

		@Override
		public String toString() {
			return "TextureContextGrid.Point2i(x=" + this.getX() + ", y=" + this.getY() + ")";
		}
	}

	public static class Patterned extends TextureContextGrid {
		public Patterned(BlockPos pos, TextureMap tex, boolean applyOffset) {
			super(pos, tex, applyOffset);
		}

		@Override
		protected Point2i calculateTextureCoord(BlockPos pos, int w, int h, Direction side) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			int tx;
			int ty;
			// Calculate submap x/y from x/y/z by ignoring the direction which the side is offset on
			// Negate the y coordinate when calculating non-vertical directions, otherwise it is reverse order
			if (side.getAxis().isVertical()) {
				// DOWN || UP
				tx = x % w;
				ty = (side.getOffsetY() * z + 1) % h;
			} else if (side.getAxis() == Axis.Z) {
				// NORTH || SOUTH
				tx = x % w;
				ty = -y % h;
			} else {
				// WEST || EAST
				tx = (z + 1) % w;
				ty = -y % h;
			}
			// Reverse x order for north and east
			if (side == Direction.NORTH || side == Direction.EAST) {
				tx = (w - tx - 1) % w;
			}
			// Remainder can produce negative values, so wrap around
			if (tx < 0) {
				tx += w;
			}
			if (ty < 0) {
				ty += h;
			}
			return new Point2i(tx, ty);
		}
	}

	public static class Random extends TextureContextGrid {
		private static final java.util.Random RANDOM = new java.util.Random();

		public Random(BlockPos pos, TextureMap tex, boolean applyOffset) {
			super(pos, tex, applyOffset);
		}

		@Override
		protected Point2i calculateTextureCoord(BlockPos pos, int w, int h, Direction side) {
			RANDOM.setSeed(MathHelper.hashCode(pos) + side.ordinal());
			RANDOM.nextBoolean();
			int tx = RANDOM.nextInt(w) + 1;
			int ty = RANDOM.nextInt(h) + 1;
			return new Point2i(tx, ty);
		}
	}
}
