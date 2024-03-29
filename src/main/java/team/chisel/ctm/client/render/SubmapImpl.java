package team.chisel.ctm.client.render;

import net.minecraft.client.texture.Sprite;

public class SubmapImpl implements Submap {
	public static final float FACTOR = 16.0F;
	private static final float DIV3 = FACTOR / 3.0F;

	public static final Submap X1 = new SubmapImpl(16, 16, 0, 0);
	public static final Submap[][] X2 = new Submap[][] {
		{new SubmapImpl(8, 8, 0, 0), new SubmapImpl(8, 8, 8, 0)},
		{new SubmapImpl(8, 8, 0, 8), new SubmapImpl(8, 8, 8, 8)}
	};
	public static final Submap[][] X3 = new Submap[][] {
		{new SubmapImpl(DIV3, DIV3, 0, 0), new SubmapImpl(DIV3, DIV3, DIV3, 0), new SubmapImpl(DIV3, DIV3, DIV3 * 2, 0)},
		{new SubmapImpl(DIV3, DIV3, 0, DIV3), new SubmapImpl(DIV3, DIV3, DIV3, DIV3), new SubmapImpl(DIV3, DIV3, DIV3 * 2, DIV3)},
		{new SubmapImpl(DIV3, DIV3, 0, DIV3 * 2), new SubmapImpl(DIV3, DIV3, DIV3, DIV3 * 2), new SubmapImpl(DIV3, DIV3, DIV3 * 2, DIV3 * 2)}
	};
	public static final Submap[][] X4 = new Submap[][] {
		{new SubmapImpl(4, 4, 0, 0), new SubmapImpl(4, 4, 4, 0), new SubmapImpl(4, 4, 8, 0), new SubmapImpl(4, 4, 12, 0)},
		{new SubmapImpl(4, 4, 0, 4), new SubmapImpl(4, 4, 4, 4), new SubmapImpl(4, 4, 8, 4), new SubmapImpl(4, 4, 12, 4)},
		{new SubmapImpl(4, 4, 0, 8), new SubmapImpl(4, 4, 4, 8), new SubmapImpl(4, 4, 8, 8), new SubmapImpl(4, 4, 12, 8)},
		{new SubmapImpl(4, 4, 0, 12), new SubmapImpl(4, 4, 4, 12), new SubmapImpl(4, 4, 8, 12), new SubmapImpl(4, 4, 12, 12)}
	};

	private final float width;
	private final float height;
	private final float xOffset;
	private final float yOffset;

	private final NormalizedSubmap normalized = new NormalizedSubmap(this);

	public SubmapImpl(float width, float height, float xOffset, float yOffset) {
		this.width = width;
		this.height = height;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getXOffset() {
		return xOffset;
	}

	@Override
	public float getYOffset() {
		return yOffset;
	}

	@Override
	public NormalizedSubmap normalize() {
		return normalized;
	}

	@Override
	public Submap relativize() {
		return this;
	}

	@Override
	public float getInterpolatedU(Sprite sprite, float u) {
		return sprite.getFrameU(getXOffset() + u / getWidth());
	}

	@Override
	public float getInterpolatedV(Sprite sprite, float v) {
		return sprite.getFrameV(getYOffset() + v / getWidth());
	}

	@Override
	public float[] toArray() {
		return new float[] {getXOffset(), getYOffset(), getXOffset() + getWidth(), getYOffset() + getHeight()};
	}

	public NormalizedSubmap getNormalized() {
		return normalized;
	}

	public SubmapImpl multiply(Submap submap) {
		Submap normal = submap.normalize();
		float newWidth = width * normal.getWidth();
		float newHeight = height * normal.getHeight();
		float newXOffset = xOffset + normal.getWidth() * submap.getXOffset();
		float newYOffset = yOffset + normal.getHeight() * submap.getYOffset();
		return new SubmapImpl(newWidth, newHeight, newXOffset, newYOffset);
	}

	public static Submap getX2Submap(int id) {
		return SubmapImpl.X2[id / 2][id % 2];
	}

	private static class NormalizedSubmap implements Submap {
		private final Submap parent;

		NormalizedSubmap(final Submap parent) {
			this.parent = parent;
		}

		@Override
		public float getWidth() {
			return parent.getWidth() / FACTOR;
		}

		@Override
		public float getHeight() {
			return parent.getHeight() / FACTOR;
		}

		@Override
		public float getXOffset() {
			return parent.getXOffset() / FACTOR;
		}

		@Override
		public float getYOffset() {
			return parent.getYOffset() / FACTOR;
		}

		@Override
		public Submap normalize() {
			return this;
		}

		@Override
		public Submap relativize() {
			return parent;
		}

		@Override
		public float getInterpolatedU(Sprite sprite, float u) {
			return parent.getInterpolatedU(sprite, u);
		}

		@Override
		public float getInterpolatedV(Sprite sprite, float v) {
			return parent.getInterpolatedV(sprite, v);
		}

		@Override
		public float[] toArray() {
			return parent.toArray();
		}
	}
}
