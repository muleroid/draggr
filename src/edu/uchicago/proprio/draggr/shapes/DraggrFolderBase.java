package edu.uchicago.proprio.draggr.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;


// represents the base "display" upon which individual files should be rendered
public class DraggrFolderBase {
	private DraggrFile mFile;
	
	private FloatBuffer vertexBuffer;
	
	private static final int COORDS_PER_VERTEX = 3;
	private final int vertexCount = folderCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4;
	
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	
	static float folderCoords[] = {
		-0.5f, 0.5f, 0.0f,
		0.5f, -0.5f, 0.0f,
		0.5f, 0.5f, 0.0f,
		-0.5f, 0.5f, 0.0f,
		-0.5f, -0.5f, 0.0f,
		0.5f, -0.5f, 0.0f
	};
	
	private float x1 = -0.5f;
	private float x2 = 0.5f;
	private float y1 = -0.5f;
	private float y2 = 0.5f;
	
	float color[] = { 0.4588235f, 0.2f, 0.0f, 0.5f };

	private final String vertexShaderCode = 
		"uniform mat4 uMVPMatrix;" +
		"attribute vec4 vPosition;" +
		"void main() {" +
		"	gl_Position = uMVPMatrix * vPosition;" +
		"}";
		
	private final String fragmentShaderCode = 
		"precision mediump float;" +
		"uniform vec4 vColor;" +
		"void main() {" +
		"	gl_FragColor = vColor;" +
		"}";
	
	public DraggrFolderBase() {
		ByteBuffer bb = ByteBuffer.allocateDirect(folderCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(folderCoords);
		vertexBuffer.position(0);
		
		mFile = new DraggrFile();
	}
	
	public void setFileTexture(Texture fTexture) {
		mFile.setTexture(fTexture);
	}
	
	public String vertexShader() {
		return vertexShaderCode;
	}
	
	public String fragmentShader() {
		return fragmentShaderCode;
	}
	
	public void draw(int mProgram, float[] mvpMatrix) {
		mFile.draw(mvpMatrix);
		GLES20.glUseProgram(mProgram);
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		// enables the handle to triangle's vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, 
				vertexStride, vertexBuffer);
		
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
		
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		// drawArrays uses vertexCount elements from the enabled handle to draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
		
		// disable
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}
	
	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		
		return shader;
	}
	
	public boolean isTouched(PointF screenP) {
		// convert screen to openGL coordinates
		float touchedX = screenP.x;
		float touchedY = screenP.y;
		if(touchedX >= x1 && touchedX <= x2 && touchedY >= y1 && touchedY <= y2)
			return true;
		return false;
	}
}
