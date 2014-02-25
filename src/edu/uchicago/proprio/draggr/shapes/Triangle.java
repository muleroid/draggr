package edu.uchicago.proprio.draggr.shapes;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;


// dumb shape for drawing and stuff
public class Triangle {
	// buffer to store el vertices
	private FloatBuffer vertexBuffer;
	
	private static final int COORDS_PER_VERTEX = 3;
	private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4;
	
	private int mPositionHandle;
	private int mColorHandle;
	
	static float triangleCoords[] = {
		0.0f, 0.6220008459f, 0.0f, // top
		-0.5f, -0.311004243f, 0.0f, // bottom left
		0.5f, -0.311004243f, 0.0f // bottom right
	};
	
	float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
	
	private final String vertexShaderCode = 
		"attribute vec4 vPosition;" +
		"void main() {" +
		"	gl_Position = vPosition;" +
		"}";
	
	private final String fragmentShaderCode = 
		"precision mediump float;" +
		"uniform vec4 vColor;" +
		"void main() {" +
		"	gl_FragColor = vColor;" +
		"}";
	
	public Triangle() {
		ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(triangleCoords);
		vertexBuffer.position(0);
	}
	
	public String vertexShader() {
		return vertexShaderCode;
	}
	
	public String fragmentShader() {
		return fragmentShaderCode;
	}

	public void draw(int mProgram) {
		GLES20.glUseProgram(mProgram);
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		// enables the handle to triangle's vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, 
				vertexStride, vertexBuffer);
		
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
		
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
}
