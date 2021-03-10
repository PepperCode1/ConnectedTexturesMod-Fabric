package team.chisel.ctm.client.render;

import java.util.Objects;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import team.chisel.ctm.api.client.Renderable;
import team.chisel.ctm.client.mixin.BakedQuadAccessor;
import team.chisel.ctm.client.util.MathUtil;
import team.chisel.ctm.client.util.RenderUtil;

public class UnbakedQuad implements Renderable, Cloneable {
	public static final float[] FULL_BOUNDS = new float[] {0.0F, 0.0F, 1.0F, 1.0F};
	public static final float CENTER = 0.5F;
	public static final float[] CENTER_POINT = new float[] {CENTER, CENTER};

	public Vertex[] vertexes;
	public Direction cullFace;
	public Direction lightFace;
	public int colorIndex;
	public Sprite sprite;
	public RenderMaterial material;

	public UnbakedQuad() {
	}

	public UnbakedQuad(BakedQuad bakedQuad) {
		lightFace = bakedQuad.getFace();
		colorIndex = bakedQuad.getColorIndex();
		sprite = ((BakedQuadAccessor) bakedQuad).getSprite();
		int[] data = bakedQuad.getVertexData();
		vertexes = new Vertex[4];
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			int offset = vertexId * 8;
			Vertex vertex = new Vertex();
			vertexes[vertexId] = vertex;
			vertex.x = Float.intBitsToFloat(data[offset]);
			vertex.y = Float.intBitsToFloat(data[offset+1]);
			vertex.z = Float.intBitsToFloat(data[offset+2]);
			vertex.decodeColor(data[offset+3]);
			vertex.u = MathUtil.getLerpProgress(Float.intBitsToFloat(data[offset+4]), sprite.getMinU(), sprite.getMaxU());
			vertex.v = MathUtil.getLerpProgress(Float.intBitsToFloat(data[offset+5]), sprite.getMinV(), sprite.getMaxV());
			vertex.decodeLight(data[offset+6]);
		}
	}

	public UnbakedQuad(QuadView quadView, Sprite sprite) {
		cullFace = quadView.cullFace();
		lightFace = quadView.lightFace();
		colorIndex = quadView.colorIndex();
		this.sprite = sprite;
		material = quadView.material();
		vertexes = new Vertex[4];
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			Vertex vertex = new Vertex();
			vertexes[vertexId] = vertex;
			vertex.x = quadView.x(vertexId);
			vertex.y = quadView.y(vertexId);
			vertex.z = quadView.z(vertexId);
			vertex.decodeColor(quadView.spriteColor(vertexId, 0));
			vertex.u = MathUtil.getLerpProgress(quadView.spriteU(vertexId, 0), sprite.getMinU(), sprite.getMaxU());
			vertex.v = MathUtil.getLerpProgress(quadView.spriteV(vertexId, 0), sprite.getMinV(), sprite.getMaxV());
			vertex.decodeLight(quadView.lightmap(vertexId));
		}
	}

	@Override
	public void render(QuadEmitter emitter) {
		Vertex vertex;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertex = vertexes[vertexId];
			emitter.pos(vertexId, vertex.x, vertex.y, vertex.z);
			emitter.spriteColor(vertexId, 0, RenderUtil.getColor(vertex.alpha, vertex.red, vertex.green, vertex.blue));
			emitter.sprite(vertexId, 0, MathHelper.lerp(vertex.u, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vertex.v, sprite.getMinV(), sprite.getMaxV()));
			emitter.lightmap(vertexId, RenderUtil.getLight(vertex.skyLight, vertex.blockLight));
		}
		emitter.cullFace(cullFace);
		emitter.colorIndex(colorIndex);
		emitter.material(material);
		emitter.emit();
	}

	/**
	 * Sets the color values for all vertexes.
	 *
	 * @param alpha int 0-255
	 * @param red int 0-255
	 * @param green int 0-255
	 * @param blue int 0-255
	 */
	public void setColor(int alpha, int red, int green, int blue) {
		alpha = alpha & 0xFF;
		red = red & 0xFF;
		green = green & 0xFF;
		blue = blue & 0xFF;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			Vertex vertex = vertexes[vertexId];
			vertex.alpha = alpha;
			vertex.red = red;
			vertex.green = green;
			vertex.blue = blue;
		}
	}

	/**
	 * Sets the light values for all vertexes.
	 *
	 * @param skyLight int 0-15
	 * @param blockLight int 0-15
	 */
	public void setLight(int skyLight, int blockLight) {
		skyLight = skyLight & 0xF;
		blockLight = blockLight & 0xF;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			Vertex vertex = vertexes[vertexId];
			vertex.skyLight = skyLight;
			vertex.blockLight = blockLight;
		}
	}

	/**
	 * Sets new UV bounds relative the passed UV bounds.
	 * Uses linear interpolation to let vertex UVs remain in the same position relative to the new bounds.
	 *
	 * @param bounds UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 * @param newBounds UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 */
	public void setUVBounds(float[] bounds, float[] newBounds) {
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			Vertex vertex = vertexes[vertexId];
			vertex.u = MathHelper.lerp((float) MathHelper.getLerpProgress(vertex.u, bounds[0], bounds[2]), newBounds[0], newBounds[2]);
			vertex.v = MathHelper.lerp((float) MathHelper.getLerpProgress(vertex.v, bounds[1], bounds[3]), newBounds[1], newBounds[3]);
		}
	}

	public void setUVBounds(float[] newBounds) {
		setUVBounds(FULL_BOUNDS, newBounds);
	}

	public void setUVBounds(Sprite sprite) {
		this.sprite = sprite;
	}

	/**
	 * Applies a submap to the passed bounds and sets them as the new bounds relative to the passed bounds.
	 *
	 * @param bounds UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 * @param submap The submap to apply.
	 */
	public void applySubmap(float[] bounds, Submap submap) {
		Submap normalizedSubmap = submap.normalize();
		float[] newBounds = bounds.clone();
		float width = newBounds[2] - newBounds[0];
		float height = newBounds[3] - newBounds[1];
		newBounds[0] += normalizedSubmap.getXOffset() * width;
		newBounds[1] += normalizedSubmap.getYOffset() * height;
		newBounds[2] -= (1 - (normalizedSubmap.getXOffset() + normalizedSubmap.getWidth())) * width;
		newBounds[3] -= (1 - (normalizedSubmap.getYOffset() + normalizedSubmap.getHeight())) * height;
		setUVBounds(bounds, newBounds);
	}

	public void applySubmap(Submap submap) {
		applySubmap(FULL_BOUNDS, submap);
	}

	/**
	 * Rotates the UVs by 90 degree intervals counter-clockwise around the provided center point.
	 *
	 * @param center A point in the form of float[] {u, v}.
	 * @param rotation The rotation. If it is not 0-3, modulo will be used to fit it to that range.
	 */
	public void rotateUVs(float[] center, int rotation) {
		rotation %= 4;
		if (rotation == 0) {
			return;
		}
		if (rotation < 0) {
			rotation += 4;
		}
		boolean check1 = rotation / 2 == 1;
		boolean check2 = rotation % 2 == 1;

		for (int vertexId = 0; vertexId < 4; vertexId++) {
			Vertex vertex = vertexes[vertexId];

			float u = vertex.u - center[0];
			float v = vertex.v - center[1];

			if (check1) {
				u *= -1;
				v *= -1;
			}
			if (check2) {
				float temp = u;
				u = -v;
				v = temp;
			}

			vertex.u = u + center[0];
			vertex.v = v + center[1];
		}
	}

	public void rotateUVs(int rotation) {
		rotateUVs(CENTER_POINT, rotation);
	}

	/**
	 * Reflect the UVs across the provided center point.
	 *
	 * @param line The U coordinate or V coordinate of the line to reflect across, depending on the reflection.
	 * @param reflection The way to reflect.
	 */
	public void reflectUVs(float line, Reflection reflection) {
		if (reflection == Reflection.HORIZONTAL) {
			for (int vertexId = 0; vertexId < 4; vertexId++) {
				vertexes[vertexId].u = -(vertexes[vertexId].u - line) + line;
			}
		} else if (reflection == Reflection.VERTICAL) {
			for (int vertexId = 0; vertexId < 4; vertexId++) {
				vertexes[vertexId].v = -(vertexes[vertexId].v - line) + line;
			}
		}
	}

	public void reflectUVs(Reflection reflection) {
		reflectUVs(CENTER, reflection);
	}

	/**
	 * Untransforms the UVs so that they are not rotated or reflected.
	 *
	 * @param center A point in the form of float[] {u, v}.
	 */
	public void untransformUVs(float[] center) {
		Winding winding = getUVWinding();
		int rotation = getUVRotation();
		if (winding == Winding.CLOCKWISE) {
			if (rotation == 1) {
				reflectUVs(center[1], Reflection.VERTICAL);
			} else if (rotation == 3) {
				reflectUVs(center[0], Reflection.HORIZONTAL);
			} else {
				reflectUVs(center[0], Reflection.HORIZONTAL);
				rotateUVs(center, rotation - 1);
			}
		} else {
			rotateUVs(center, -rotation);
		}
	}

	public void untransformUVs() {
		untransformUVs(CENTER_POINT);
	}

	/**
	 * <b>Assumes the UV winding is counterclockwise, meaning UV reflection is ignored.</b>
	 *
	 * <p>Calculates the counter-clockwise UV rotation by checking which vertex has the smallest UVs by using {@code u*u+v*v}.
	 *
	 * @return The counter-clockwise UV rotation.
	 */
	public int getUVRotation() {
		int minVertex = -1;
		float minDistance = 2.0F;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			Vertex vertex = vertexes[vertexId];
			float distance = vertex.u * vertex.u + vertex.v * vertex.v;
			if (distance < minDistance) {
				minDistance = distance;
				minVertex = vertexId;
			}
		}
		return minVertex;
	}

	/**
	 * <b>Behavior is undefined if the UV coordinates form a concave polygon or self-intersecting polygon.</b>
	 *
	 * <p>Calculates the winding direction of the UV coordinates, going in the vertex order.
	 *
	 * @return The winding direction of the UV coordinates.
	 */
	public Winding getUVWinding() {
		float val = (vertexes[3].u - vertexes[0].u) * (vertexes[1].v - vertexes[0].v) - (vertexes[3].v - vertexes[0].v) * (vertexes[1].u - vertexes[0].u);
		if (val > 0) {
			return Winding.COUNTERCLOCKWISE;
		} else if (val < 0) {
			return Winding.CLOCKWISE;
		}
		return Winding.UNDEFINED;
	}

	/**
	 * Checks if the UVs are rotated once with regards to UV winding, so that a double rotation or double reflection is ignored.
	 *
	 * @return True if a single rotation is applied, false otherwise.
	 */
	public boolean areUVsRotatedOnce() {
		return getUVRotation() % 2 == (getUVWinding() == Winding.COUNTERCLOCKWISE ? 1 : 0);
	}

	/**
	 * Calculates UV bounds that form the smallest rectangle that encompasses all vertex UVs.
	 *
	 * @return UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 */
	public float[] getSmallestUVBounds() {
		float minU = 1.0F;
		float minV = 1.0F;
		float maxU = 0.0F;
		float maxV = 0.0F;

		for (int vertexId = 0; vertexId < 4; vertexId++) {
			Vertex vertex = vertexes[vertexId];
			if (vertex.u < minU) {
				minU = vertex.u;
			} else if (vertex.u > maxU) {
				maxU = vertex.u;
			}
			if (vertex.v < minV) {
				minV = vertex.v;
			} else if (vertex.v > maxV) {
				maxV = vertex.v;
			}
		}

		return new float[] {minU, minV, maxU, maxV};
	}

	/**
	 * Divides this quad into two. Clones vertexes if the values don't change using {@link Vertex#clone()}.
	 *
	 * @param delta float 0-1. Where to create new vertexes in relation to the current ones. For example, {@code 0.5F} divides the quad in half.
	 * @param shift Whether to shift all operations by one vertex or not. This will flip the division line from horizontal to vertical or vice versa, depending on vertex order. With vanilla vertex order, {@code false} will result in a horizontal division line, and {@code true} will result in a vertical division line.
	 * @return An UnbakedQuad[2]. The first quad will always have its first vertex have the same values as the first vertex in this quad.
	 */
	public UnbakedQuad[] divide(float delta, boolean shift) {
		UnbakedQuad quad1 = cloneProperties();
		UnbakedQuad quad2 = cloneProperties();
		UnbakedQuad[] quads = new UnbakedQuad[] {quad1, quad2};

		Vertex vertex1;
		Vertex vertex2;
		if (!shift) {
			vertex1 = Vertex.lerp(vertexes[0], vertexes[1], delta);
			vertex2 = Vertex.lerp(vertexes[3], vertexes[2], delta);

			quad1.vertexes = new Vertex[] {vertexes[0].clone(), vertex1, vertex2, vertexes[3].clone()};
			quad2.vertexes = new Vertex[] {vertex1.clone(), vertexes[1].clone(), vertexes[2].clone(), vertex2.clone()};
		} else {
			vertex1 = Vertex.lerp(vertexes[0], vertexes[3], delta);
			vertex2 = Vertex.lerp(vertexes[1], vertexes[2], delta);

			quad1.vertexes = new Vertex[] {vertexes[0].clone(), vertexes[1].clone(), vertex2, vertex1};
			quad2.vertexes = new Vertex[] {vertex1.clone(), vertex2.clone(), vertexes[2].clone(), vertexes[3].clone()};
		}

		return quads;
	}

	/**
	 * Divides this quad into four quads by using {@link #divide(float, boolean)} three times.
	 *
	 * @return An UnbakedQuad[4]. The quads will go in the same order as the vertexes in this quad. This means that the x-th vertex of the x-th quad in the array will have the same values as the x-th vertex in this quad.
	 */
	public UnbakedQuad[] toQuadrants() {
		UnbakedQuad[] quadrants = new UnbakedQuad[4];
		UnbakedQuad[] quads = divide(0.5F, true);
		UnbakedQuad quad1 = quads[0];
		UnbakedQuad quad2 = quads[1];
		quads = quad1.divide(0.5F, false);
		quadrants[0] = quads[0];
		quadrants[1] = quads[1];
		quads = quad2.divide(0.5F, false);
		quadrants[2] = quads[1];
		quadrants[3] = quads[0];
		return quadrants;
	}

	/**
	 * Creates a new UnbakedQuad, but only clones the properties from this quad to the new one.
	 * This means that everything except vertexes is cloned.
	 *
	 * @return The new UnbakedQuad.
	 */
	public UnbakedQuad cloneProperties() {
		UnbakedQuad quad = new UnbakedQuad();
		quad.cullFace = cullFace;
		quad.lightFace = lightFace;
		quad.colorIndex = colorIndex;
		quad.sprite = sprite;
		quad.material = material;
		return quad;
	}

	@Override
	public UnbakedQuad clone() {
		UnbakedQuad quad = cloneProperties();
		vertexes = new Vertex[4];
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			quad.vertexes[vertexId] = vertexes[vertexId].clone();
		}
		return quad;
	}

	public enum Winding {
		COUNTERCLOCKWISE,
		CLOCKWISE,
		UNDEFINED;

		public Winding reverse() {
			if (this == UNDEFINED) {
				return this;
			}
			return this == CLOCKWISE ? COUNTERCLOCKWISE : CLOCKWISE;
		}
	}

	public enum Reflection {
		HORIZONTAL,
		VERTICAL,
		NONE;

		public Reflection opposite() {
			if (this == NONE) {
				return this;
			}
			return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
		}
	}

	public static class Vertex implements Cloneable {
		public float x;
		public float y;
		public float z;
		/** 0-255. */
		public int alpha;
		/** 0-255. */
		public int red;
		/** 0-255. */
		public int green;
		/** 0-255. */
		public int blue;
		/** Sprite normalized 0-1. */
		public float u;
		/** Sprite normalized 0-1. */
		public float v;
		/** 0-15. */
		public int skyLight;
		/** 0-15. */
		public int blockLight;

		public void decodeColor(int color) {
			alpha = (color >> 24) & 0xFF;
			red = (color >> 16) & 0xFF;
			green = (color >> 8) & 0xFF;
			blue = (color) & 0xFF;
		}

		public void decodeLight(int light) {
			skyLight = (light >> 20) & 0xF;
			blockLight = (light >> 4) & 0xF;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y, z, alpha, red, green, blue, u, v, skyLight, blockLight);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Vertex)) {
				return false;
			}
			Vertex other = (Vertex) obj;
			return Float.floatToIntBits(x) == Float.floatToIntBits(other.x)
					&& Float.floatToIntBits(y) == Float.floatToIntBits(other.y)
					&& Float.floatToIntBits(z) == Float.floatToIntBits(other.z)
					&& alpha == other.alpha
					&& red == other.red
					&& green == other.green
					&& blue == other.blue
					&& Float.floatToIntBits(u) == Float.floatToIntBits(other.u)
					&& Float.floatToIntBits(v) == Float.floatToIntBits(other.v)
					&& skyLight == other.skyLight
					&& blockLight == other.blockLight;
		}

		@Override
		public Vertex clone() {
			Vertex newVertex = new Vertex();
			newVertex.x = x;
			newVertex.y = y;
			newVertex.z = z;
			newVertex.alpha = alpha;
			newVertex.red = red;
			newVertex.green = green;
			newVertex.blue = blue;
			newVertex.u = u;
			newVertex.v = v;
			newVertex.skyLight = skyLight;
			newVertex.blockLight = blockLight;
			return newVertex;
		}

		public static Vertex lerp(Vertex min, Vertex max, float delta) {
			Vertex newVertex = new Vertex();
			newVertex.x = MathHelper.lerp(delta, min.x, max.x);
			newVertex.y = MathHelper.lerp(delta, min.y, max.y);
			newVertex.z = MathHelper.lerp(delta, min.z, max.z);
			newVertex.alpha = MathUtil.lerp(delta, min.alpha, max.alpha);
			newVertex.red = MathUtil.lerp(delta, min.red, max.red);
			newVertex.green = MathUtil.lerp(delta, min.green, max.green);
			newVertex.blue = MathUtil.lerp(delta, min.blue, max.blue);
			newVertex.u = MathHelper.lerp(delta, min.u, max.u);
			newVertex.v = MathHelper.lerp(delta, min.v, max.v);
			newVertex.skyLight = MathUtil.lerp(delta, min.skyLight, max.skyLight);
			newVertex.blockLight = MathUtil.lerp(delta, min.blockLight, max.blockLight);
			return newVertex;
		}
	}
}
