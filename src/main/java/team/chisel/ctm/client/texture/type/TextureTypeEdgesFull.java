package team.chisel.ctm.client.texture.type;

import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.TextureEdgesFull;

public class TextureTypeEdgesFull extends TextureTypeEdges {
	@Override
	public CTMTexture<? extends TextureTypeCTM> makeTexture(TextureInfo info) {
		return new TextureEdgesFull(this, info);
	}

	@Override
	public int requiredTextures() {
		return 2;
	}

	@Override
	public int getQuadsPerSide() {
		return 1;
	}
}
