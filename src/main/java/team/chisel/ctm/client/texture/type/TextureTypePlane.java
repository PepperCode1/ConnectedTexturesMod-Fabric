package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.Direction;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
import team.chisel.ctm.client.texture.TexturePlane;

public class TextureTypePlane extends TextureTypeCTM {
	public static final TextureTypePlane HORIZONRAL = new TextureTypePlane(Direction.Type.HORIZONTAL);
	public static final TextureTypePlane VERTICAL = new TextureTypePlane(Direction.Type.VERTICAL);

	private final Direction.Type plane;

	public TextureTypePlane(Direction.Type plane) {
		this.plane = plane;
	}

	@Override
	public CTMTexture<TextureTypePlane> makeTexture(TextureInfo info) {
		return new TexturePlane(this, info);
	}

	@Override
	public int getQuadsPerSide() {
		return 1;
	}

	@Override
	public int requiredTextures() {
		return 1;
	}

	public Direction.Type getPlane() {
		return plane;
	}
}
