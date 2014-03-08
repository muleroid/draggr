package edu.uchicago.proprio.draggr.artools;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vec4F;
import com.qualcomm.vuforia.VideoBackgroundConfig;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

// helper functions for doing the crazy openGL stuff
// no fancy error checking, we fly or we die
public class GLTools {
	private static final String LOGTAG = "GLTools";

	static int initShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		
		// add source code and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		
		// TODO: probably add error checking so i dont fail this class
		return shader;
	}
	
	// shamelessly ripped from Vuforia sample app
	public static int createProgramFromShaders(String vertexShaderSrc, 
			String fragmentShaderSrc) {
		int vertShader = initShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc);
		int fragShader = initShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc);
		
		int program = GLES20.glCreateProgram();
		
		// attach the shaders
		GLES20.glAttachShader(program, vertShader);
		GLES20.glAttachShader(program, fragShader);
		
		GLES20.glLinkProgram(program);
		
		// TODO: error checking, but error checking is for chumps
		return program;
	}
	
	// given a projection and view matrix, and a touch coordinate, returns the place in world space
	public static PointF screenToWorld(float touchX, float touchY, float screenWidth,
			float screenHeight, float[] projM, float[] viewM) {
		//float screenWidth = (float) vuforiaAppSession.getScreenWidth();
		//float screenHeight = (float) vuforiaAppSession.getScreenHeight();
		
		float[] invertedMatrix, transformMatrix,
        	normalizedInPoint, outPoint;
		invertedMatrix = new float[16];
		transformMatrix = new float[16];
		normalizedInPoint = new float[4];
		outPoint = new float[4];
		
		int oglTouchY = (int) (screenHeight - touchY);
		
		normalizedInPoint[0] = (float) ((touchX) * 2.0f / screenWidth - 1.0);
		normalizedInPoint[1] = (float) ((oglTouchY) * 2.0f / screenHeight - 1.0);
		normalizedInPoint[2] = - 1.0f;
		normalizedInPoint[3] = 1.0f;
		Matrix.multiplyMM(transformMatrix, 0, projM, 0, viewM, 0);
		Matrix.invertM(invertedMatrix, 0, transformMatrix, 0);
		
		Matrix.multiplyMV(outPoint, 0, invertedMatrix, 0, normalizedInPoint, 0);
		
		if(outPoint[3] == 0.0) {
			Log.e(LOGTAG, "divide by zero error in calculating world coords");
			return new PointF();
		}
		
		float worldX = -1 * outPoint[0] / outPoint[3];
		float worldY = outPoint[1] / outPoint[3];
		
		return new PointF(worldX, worldY);
	}
}
