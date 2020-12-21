package team.chisel.ctm.client.render;

import java.util.Arrays;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import team.chisel.ctm.api.texture.Renderable;
import team.chisel.ctm.api.texture.Submap;
import team.chisel.ctm.client.util.BitUtil;

/**
 * A class to assist in manipulating various attributes of quads, mainly UVs.
 */
public class UnbakedQuad implements Renderable, Cloneable {
	public Vertex[] vertexes = new Vertex[4];
	public Direction cullFace;
	public Direction nominalFace;
	public RenderMaterial material;
	public int colorIndex;
	
	public UnbakedQuad() {
	}
	
	public UnbakedQuad(BakedQuad bakedQuad) {
		int[] data = bakedQuad.getVertexData();
		Vertex vertex;
		int offset;
		int color;
		int light;
//		int normal;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			offset = vertexId * 8;
			vertex = new Vertex();
			vertexes[vertexId] = vertex;
			vertex.x = Float.intBitsToFloat(data[offset]);
			vertex.y = Float.intBitsToFloat(data[offset+1]);
			vertex.z = Float.intBitsToFloat(data[offset+2]);
			color = data[offset+3];
			vertex.alpha = (byte) (color >> 24);
			vertex.red = (byte) (color >> 16);
			vertex.green = (byte) (color >> 8);
			vertex.blue = (byte) (color);
			vertex.u = Float.intBitsToFloat(data[offset+4]);
			vertex.v = Float.intBitsToFloat(data[offset+5]);
			light = data[offset+6];
			vertex.skyLight = (short) (light >> 16);
			vertex.blockLight = (short) (light);
//			normal = data[offset+7];
//			vertex.normalX = ((normal >> 24) & 255) / 127.0f;
//			vertex.normalY = ((normal >> 16) & 255) / 127.0f;
//			vertex.normalZ = ((normal >> 8) & 255) / 127.0f;
		}
		colorIndex = bakedQuad.getColorIndex();
	}
	
	protected void basicInit() {
		Vertex vertex;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertex = new Vertex();
			vertexes[vertexId] = vertex;
			vertex.red = -1;
			vertex.green = -1;
			vertex.blue = -1;
			vertex.alpha = -1;
			vertex.normalX = Float.NaN;
			vertex.normalY = Float.NaN;
			vertex.normalZ = Float.NaN;
		}
		colorIndex = -1;
	}

	@Override
	public void render(QuadEmitter emitter) {
		Vertex vertex;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertex = vertexes[vertexId];
			emitter.pos(vertexId, vertex.x, vertex.y, vertex.z);
			emitter.spriteColor(vertexId, 0, BitUtil.bitIntCast(vertex.alpha) << 24 | BitUtil.bitIntCast(vertex.red) << 16 | BitUtil.bitIntCast(vertex.green) << 8 | BitUtil.bitIntCast(vertex.blue));
			emitter.sprite(vertexId, 0, vertex.u, vertex.v);
			emitter.lightmap(vertexId, BitUtil.bitIntCast(vertex.skyLight) << 16 | BitUtil.bitIntCast(vertex.blockLight));
//			emitter.normal(vertexId, vertex.normalX, vertex.normalY, vertex.normalZ);
		}
		if (cullFace != null) {
			emitter.cullFace(cullFace);
		} else if (nominalFace != null) {
			emitter.nominalFace(nominalFace);
		}
		if (material != null) {
			emitter.material(material);
		}
		emitter.colorIndex(colorIndex);
		emitter.emit();
	}
	
	/**
	 * Sets the color values for all vertexes.
	 */
	public void setColor(byte red, byte green, byte blue, byte alpha) {
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertexes[vertexId].red = red;
			vertexes[vertexId].green = green;
			vertexes[vertexId].blue = blue;
			vertexes[vertexId].alpha = alpha;
		}
	}
	
	/**
	 * Sets the color values for all vertexes.
	 * @param red int 0-255
	 * @param green int 0-255
	 * @param blue int 0-255
	 * @param alpha int 0-255
	 */
	public void setColor(int red, int green, int blue, int alpha) {
		setColor((byte) red, (byte) green, (byte) blue, (byte) alpha);
	}
	
	/**
	 * Sets the light values for all vertexes.
	 * @param skyLight int 0-15
	 * @param blockLight int 0-15
	 */
	public void setLight(int skyLight, int blockLight) {
		short skyLightS = (short) (MathHelper.clamp(skyLight, 0, 15) << 4);
		short blockLightS = (short) (MathHelper.clamp(blockLight, 0, 15) << 4);
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertexes[vertexId].skyLight = skyLightS;
			vertexes[vertexId].blockLight = blockLightS;
		}
	}
	
	/**
	 * Sets new UV bounds relative the passed UV bounds.
	 * Uses linear interpolation to let vertex UVs remain in the same position relative to the new bounds.
	 * @param bounds UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 * @param newBounds UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 */
	public void setUVBounds(float[] bounds, float[] newBounds) {
		Vertex vertex;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertex = vertexes[vertexId];
			vertex.u = MathHelper.lerp((float) MathHelper.getLerpProgress(vertex.u, bounds[0], bounds[2]), newBounds[0], newBounds[2]);
			vertex.v = MathHelper.lerp((float) MathHelper.getLerpProgress(vertex.v, bounds[1], bounds[3]), newBounds[1], newBounds[3]);
		}
	}
	
	/**
	 * Applies a submap to the passed bounds and sets them as the new bounds relative to the passed bounds.
	 * @param bounds UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 * @param submap The submap to apply.
	 */
	public void applySubmap(float[] bounds, Submap submap) {
		Submap normalizedSubmap = submap.normalize();
		float[] newBounds = Arrays.copyOf(bounds, bounds.length);
		float width = newBounds[2] - newBounds[0];
		float height = newBounds[3] - newBounds[1];
		newBounds[0] += normalizedSubmap.getXOffset() * width;
		newBounds[1] += normalizedSubmap.getYOffset() * height;
		newBounds[2] -= (1 - (normalizedSubmap.getXOffset() + normalizedSubmap.getWidth())) * width;
		newBounds[3] -= (1 - (normalizedSubmap.getYOffset() + normalizedSubmap.getHeight())) * height;
		setUVBounds(bounds, newBounds);
	}
	
	/**
	 * Interpolates the passed bounds toward their center point by the specified amount and sets them as the new bounds relative to the passed bounds.
	 * @param bounds UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 * @param delta float 0-1. The factor by which to interpolate.
	 */
	public void interpolateUVBounds(float[] bounds, float delta) {
		float[] newBounds = Arrays.copyOf(bounds, bounds.length);
		float centerU = (newBounds[0] + newBounds[2]) / 2;
		float centerV = (newBounds[1] + newBounds[3]) / 2;
		newBounds[0] = MathHelper.lerp(delta, newBounds[0], centerU);
		newBounds[1] = MathHelper.lerp(delta, newBounds[1], centerV);
		newBounds[2] = MathHelper.lerp(delta, newBounds[2], centerU);
		newBounds[3] = MathHelper.lerp(delta, newBounds[3], centerV);
		setUVBounds(bounds, newBounds);
	}
	
	/**
	 * Rotates the UVs by 90 degree intervals counter-clockwise around the provided center point.
	 * @param center A point in the form of float[] {u, v}.
	 * @param rotation The rotation. If it is not 0-3, modulo will be used to fit it to that range.
	 */
	public void rotateUVs(float[] center, int rotation) {
		rotation %= 4;
		if (rotation < 0) {
			rotation += 4;
		}
		boolean check1 = rotation / 2 == 1;
		boolean check2 = rotation % 2 == 1;
		
		Vertex vertex;
		float u;
		float v;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertex = vertexes[vertexId];
			
			u = vertex.u - center[0];
			v = vertex.v - center[1];
			
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
	
	/**
	 * Reflect the UVs across the provided center point.
	 * @param center A point in the form of float[] {u, v}.
	 * @param reflection The way to reflect.
	 */
	public void reflectUVs(float[] center, Reflection reflection) {
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			if (reflection == Reflection.HORIZONTAL) {
				vertexes[vertexId].u = -(vertexes[vertexId].u - center[0]) + center[0];
			} else if (reflection == Reflection.VERTICAL) {
				vertexes[vertexId].v = -(vertexes[vertexId].v - center[1]) + center[1];
			}
		}
	}
	
	/**
	 * Untransforms the UVs so that they are not rotated or reflected.
	 * @param center A point in the form of float[] {u, v}.
	 */
	public void untransformUVs(float[] center) {
		reflectUVs(center, getUVReflection());
		rotateUVs(center, -getUVRotation());
	}
	
	/**
	 * <b>Does not take UV reflection into account. Use {@link #getAbsoluteUVRotation()} if UVs might be reflected.</b><br>
	 * Calculates the UV rotation by checking which vertex has the smallest UVs by using {@code u*u+v*v}.
	 * @return The UV rotation.
	 */
	public int getUVRotation() {
		int minVertex = -1;
		double minDistance = 2;
		Vertex vertex;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertex = vertexes[vertexId];
			double distance = vertex.u*vertex.u + vertex.v*vertex.v;
			if (distance < minDistance) {
				minDistance = distance;
				minVertex = vertexId;
			}
		}
		return minVertex;
	}
	
	/**
	 * Calculates the UV reflection.
	 * @return The UV reflection.
	 */
	public Reflection getUVReflection() {
		boolean horizontal = false;
		boolean vertical = false;
		if (vertexes[0].u > vertexes[3].u && vertexes[1].u > vertexes[2].u) {
			horizontal = true;
		}
		if (vertexes[0].v > vertexes[1].v && vertexes[3].v > vertexes[2].v) {
			vertical = true;
		}
		
		if (horizontal && !vertical) {
			return Reflection.HORIZONTAL;
		}
		if (vertical && !horizontal) {
			return Reflection.VERTICAL;
		}
		
		return Reflection.NONE;
	}
	
	/**
	 * Calculates the absolute UV rotation by reflecting the result from {@link #getUVReflection()}.
	 * @return The absolute UV rotation.
	 */
	public int getAbsoluteUVRotation() {
		Reflection reflection = getUVReflection();
		int rotation = getUVRotation();
		if (reflection != Reflection.NONE) {
			if (rotation % 2 == (reflection == Reflection.HORIZONTAL ? 0 : 1)) {
				rotation += 2;
			}
			rotation = (rotation+1) % 4;
		}
		return rotation;
	}
	
	/**
	 * Calculates UV bounds that form the smallest rectangle that encompasses all vertex UVs.
	 * @return UV bounds in the form of float[] {minU, minV, maxU, maxV}.
	 */
	public float[] getSmallestUVBounds() {
		float minU = 2.0F;
		float minV = 2.0F;
		float maxU = 0.0F;
		float maxV = 0.0F;
		
		Vertex vertex;
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			vertex = vertexes[vertexId];
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
	 * @param delta float 0-1. Where to create new vertexes in relation to the current ones. For example {@code 0.5f} divides the quad in half.
	 * @param shift Whether to shift all operations by one vertex or not. This will flip the division line from horizontal to vertical or vice versa, depending on vertex order.
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
	 * Divides this quad into four quads by using {@link #divide(float, boolean)} twice.
	 * @return An UnbakedQuad[4]. The quad at each index will contain a vertex with the same values as the one at the same index in this quad.
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
	 * @return The new UnbakedQuad.
	 */
	public UnbakedQuad cloneProperties() {
		UnbakedQuad quad = new UnbakedQuad();
		quad.cullFace = cullFace;
		quad.nominalFace = nominalFace;
		quad.material = material;
		quad.colorIndex = colorIndex;
		return quad;
	}
	
	@Override
	public UnbakedQuad clone() {
		UnbakedQuad quad = cloneProperties();
		quad.vertexes = new Vertex[4];
		for (int vertexId = 0; vertexId < 4; vertexId++) {
			quad.vertexes[vertexId] = vertexes[vertexId].clone();
		}
		return quad;
	}
	
	public static enum Reflection {
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
		public byte red;
		public byte green;
		public byte blue;
		public byte alpha;
		public float u;
		public float v;
		public short skyLight;
		public short blockLight;
		public float normalX;
		public float normalY;
		public float normalZ;
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Vertex) {
				Vertex other = (Vertex) obj;
				return
					other.x == x &&
					other.y == y &&
					other.z == z &&
					other.red == red &&
					other.green == green &&
					other.blue == blue &&
					other.alpha == alpha &&
					other.u == u &&
					other.v == v &&
					other.skyLight == skyLight &&
					other.blockLight == blockLight &&
					other.normalX == normalX &&
					other.normalY == normalY &&
					other.normalZ == normalZ
				;
			}
			return super.equals(obj);
		}
		
		@Override
		public Vertex clone() {
			Vertex newVertex = new Vertex();
			newVertex.x = x;
			newVertex.y = y;
			newVertex.z = z;
			newVertex.red = red;
			newVertex.green = green;
			newVertex.blue = blue;
			newVertex.alpha = alpha;
			newVertex.u = u;
			newVertex.v = v;
			newVertex.skyLight = skyLight;
			newVertex.blockLight = blockLight;
			newVertex.normalX = normalX;
			newVertex.normalY = normalY;
			newVertex.normalZ = normalZ;
			return newVertex;
		}
		
		public static Vertex lerp(Vertex min, Vertex max, float delta) {
			Vertex newVertex = new Vertex();
			newVertex.x = MathHelper.lerp(delta, min.x, max.x);
			newVertex.y = MathHelper.lerp(delta, min.y, max.y);
			newVertex.z = MathHelper.lerp(delta, min.z, max.z);
			newVertex.red = (byte) MathHelper.lerp(delta, BitUtil.bitIntCast(min.red), BitUtil.bitIntCast(max.red));
			newVertex.green = (byte) MathHelper.lerp(delta, BitUtil.bitIntCast(min.green), BitUtil.bitIntCast(max.green));
			newVertex.blue = (byte) MathHelper.lerp(delta, BitUtil.bitIntCast(min.blue), BitUtil.bitIntCast(max.blue));
			newVertex.alpha = (byte) MathHelper.lerp(delta, BitUtil.bitIntCast(min.alpha), BitUtil.bitIntCast(max.alpha));
			newVertex.u = MathHelper.lerp(delta, min.u, max.u);
			newVertex.v = MathHelper.lerp(delta, min.v, max.v);
			newVertex.skyLight = (short) MathHelper.lerp(delta, BitUtil.bitIntCast(min.skyLight), BitUtil.bitIntCast(max.skyLight));
			newVertex.blockLight = (short) MathHelper.lerp(delta, BitUtil.bitIntCast(min.blockLight), BitUtil.bitIntCast(max.blockLight));
			newVertex.normalX = MathHelper.lerp(delta, min.normalX, max.normalX);
			newVertex.normalY = MathHelper.lerp(delta, min.normalY, max.normalY);
			newVertex.normalZ = MathHelper.lerp(delta, min.normalZ, max.normalZ);
			return newVertex;
		}
	}
}
