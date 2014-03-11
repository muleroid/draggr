package edu.uchicago.proprio.draggr.shapes;

public class DraggrPair {
	private final Texture mTexture;
	private final DraggrFile mFile;
	
	public DraggrPair(Texture t, DraggrFile f) {
		mTexture = t;
		mFile = f;
	}
	
	public Texture getTexture() {
		return mTexture;
	}
	
	public DraggrFile getFile() {
		return mFile;
	}
}
