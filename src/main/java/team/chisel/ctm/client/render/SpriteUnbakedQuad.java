package team.chisel.ctm.client.render;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import team.chisel.ctm.api.texture.Submap;
import team.chisel.ctm.client.mixin.BakedQuadAccessor;

public class SpriteUnbakedQuad extends UnbakedQuad {
	public Sprite sprite;
	
	public SpriteUnbakedQuad() {
	}
	
	public SpriteUnbakedQuad(BakedQuad bakedQuad) {
		super(bakedQuad);
		sprite = ((BakedQuadAccessor) bakedQuad).getSprite();
	}
	
	public void setUVBounds(float[] newBounds) {
		setUVBounds(getUVBounds(sprite), newBounds);
	}
	
	public void setUVBounds(Sprite sprite) {
		if (this.sprite != sprite) {
			setUVBounds(getUVBounds(this.sprite), getUVBounds(sprite));
			this.sprite = sprite;
		}
	}
	
	public void applySubmap(Submap submap) {
		applySubmap(getUVBounds(sprite), submap);
	}
	
	public void interpolateUVBounds(float delta) {
		interpolateUVBounds(getUVBounds(sprite), delta);
	}
	
	public void rotateUVs(int rotation) {
		rotateUVs(getUVCenter(sprite), rotation);
	}
	
	public void reflectUVs(Reflection reflection) {
		reflectUVs(getUVCenter(sprite), reflection);
	}
	
	public void untransformUVs() {
		untransformUVs(getUVCenter(sprite));
	}
	
	@Override
	public SpriteUnbakedQuad[] toQuadrants() {
		UnbakedQuad[] quads = super.toQuadrants();
		SpriteUnbakedQuad[] spriteQuads = new SpriteUnbakedQuad[quads.length];
		for (int i = 0; i < quads.length; i++) {
			spriteQuads[i] = (SpriteUnbakedQuad) quads[i];
		}
		return spriteQuads;
	}
	
	@Override
	public SpriteUnbakedQuad cloneProperties() {
		SpriteUnbakedQuad quad = new SpriteUnbakedQuad();
		quad.cullFace = cullFace;
		quad.nominalFace = nominalFace;
		quad.material = material;
		quad.colorIndex = colorIndex;
		quad.sprite = sprite;
		return quad;
	}
	
	public static float[] getUVBounds(Sprite sprite) {
		return new float[] {sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV()};
	}
	
	public static float[] getUVCenter(Sprite sprite) {
		return new float[] {(sprite.getMinU()+sprite.getMaxU())/2, (sprite.getMinV()+sprite.getMaxV())/2};
	}
}
