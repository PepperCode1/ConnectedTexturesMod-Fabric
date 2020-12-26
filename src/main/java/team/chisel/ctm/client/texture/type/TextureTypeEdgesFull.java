package team.chisel.ctm.client.texture.type;

import team.chisel.ctm.api.client.CTMTexture;
import team.chisel.ctm.api.client.TextureInfo;
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
