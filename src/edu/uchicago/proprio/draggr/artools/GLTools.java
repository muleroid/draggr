package edu.uchicago.proprio.draggr.artools;

import android.opengl.GLES20;

// helper functions for doing the crazy openGL stuff
// no fancy error checking, we fly or we die
public class GLTools {

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
}
