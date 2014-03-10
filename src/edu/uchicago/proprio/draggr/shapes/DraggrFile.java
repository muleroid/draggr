package edu.uchicago.proprio.draggr.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;

import edu.uchicago.proprio.draggr.artools.SampleMath;

import android.R.bool;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;

// representation of a file on the screen that is drawn by OpenGL
public class DraggrFile {
	private static final String LOGTAG = "DraggrFile";
	
	// metadata relating to the file
	private String mFilename = "default";
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer texBuffer;
	
	private static final int COORDS_PER_VERTEX = 3;
	private final int vertexCount = folderCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4;
	
	private int mPositionHandle;
	private int mProgram;
	
	// texture stuff
	private Texture mTexture;
	private int mTexUniformHandle;
	private int mTexCoordHandle;
	private int mMVPMatrixHandle;
	
	public boolean onScreen = false;
	
	private final String vertexShaderCode = 
		"uniform mat4 uMVPMatrix;" +
		"attribute vec4 vPosition;" +
		"attribute vec2 vTexPosition;" +
		"varying vec2 vTexCoord;" +
		"void main() {" +
		"	gl_Position = uMVPMatrix * vPosition;" +
		"	vTexCoord = vTexPosition;" +
		"}";
			
	private final String fragmentShaderCode = 
		"precision mediump float;" +
		"varying vec2 vTexCoord;" +
		"uniform vec4 vColor;" +
		"uniform sampler2D uTexture;" +
		"void main() {" +
		"	gl_FragColor = texture2D(uTexture, vTexCoord);" +
		"}";
	
	private float leftX;
	private float rightX;
	private float downY;
	private float upY;
	
	static float folderCoords[] = {
		-0.2f, 0.2f, 0.0f,
		0.2f, -0.2f, 0.0f,
		0.2f, 0.2f, 0.0f,
		-0.2f, 0.2f, 0.0f,
		-0.2f, -0.2f, 0.0f,
		0.2f, -0.2f, 0.0f
	};
	
	/*static float folderCoords[] = {
		-0.5f, 0.5f, 0.0f,
		0.5f, -0.5f, 0.0f,
		0.5f, 0.5f, 0.0f,
		-0.5f, 0.5f, 0.0f,
		-0.5f, -0.5f, 0.0f,
		0.5f, -0.5f, 0.0f
	};*/
	
	static float texCoords[] = {
		0.0f, 1.0f,
		1.0f, 0.0f,
		1.0f, 1.0f,
		0.0f, 1.0f,
		0.0f, 0.0f,
		1.0f, 0.0f
	};
	
	public Matrix44F mModelViewMatrix = new Matrix44F();
	public Matrix44F mProjectionMatrix = new Matrix44F();
	private float[] mTranslationMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
	
	float color[] = { 0.5019608f, 0.5019608f, 0.5019608f, 1.0f };
	
	public DraggrFile() {
		ByteBuffer bb = ByteBuffer.allocateDirect(folderCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(folderCoords);
		vertexBuffer.position(0);
		
		bb = ByteBuffer.allocateDirect(texCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		
		texBuffer = bb.asFloatBuffer();
		texBuffer.put(texCoords);
		texBuffer.position(0);
		
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);
		
		// initialize translation matrix...
		resetPosition();
		
		// set the boundaries of this file in GL coordinate space
		leftX = -0.2f;
		rightX = 0.2f;
		downY = -0.2f;
		upY = 0.2f;
		
		//Log.d(LOGTAG, "File bounded by : (" + leftX + "," + rightX + ") (" + downY + "," + upY + ")");
	}
	
	public void setTexture(Texture t) {
		mTexture = t;
	}
	
	public boolean onTouch(float x, float y) {
		Vec3F intersection;
		
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        intersection = SampleMath.getPointToPlaneIntersection(SampleMath
            .Matrix44FInverse(mProjectionMatrix),
            mModelViewMatrix, metrics.widthPixels, metrics.heightPixels,
            new Vec2F(x, y), new Vec3F(0, 0, 0), new Vec3F(0, 0, 1));
        
        float touchedX = intersection.getData()[0];
        float touchedY = intersection.getData()[1];
        Log.d(LOGTAG, "Intersects: " + intersection.getData()[0] + "," + intersection.getData()[1]);
        if(touchedX >= leftX && touchedX <= rightX && touchedY >= downY && touchedY <= upY)
        	return true;
        return false;
        	//Log.d(LOGTAG, "TOUCHHHHH KITAAAAA");
	}
	
	public boolean isTouched(float touchX, float touchY) {
		if(touchX >= leftX && touchX <= rightX && touchY >= downY && touchY <= upY)
			return true;
		return false;
	}
		
	public void draw(float[] mvpMatrix) {
		GLES20.glUseProgram(mProgram);
		checkGlError("useProgram");
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
			
		// enables the handle to triangle's vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, 
				vertexStride, vertexBuffer);
		
		
		mTexUniformHandle = GLES20.glGetAttribLocation(mProgram, "uTexture");
		mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "vTexPosition");

		GLES20.glEnableVertexAttribArray(mTexCoordHandle);
		checkGlError("glEnableVertexAttribArray");
		GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
		checkGlError("glVertexAttribPointer");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		checkGlError("glActiveTexture");
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture.mTextureID[0]);
		checkGlError("glBindTexture");
		GLES20.glUniform1i(mTexUniformHandle, 0);
		
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		
		// translate if necessary
		//mMVPMatrix = mvpMatrix;
		Matrix.multiplyMM(mMVPMatrix, 0, mvpMatrix, 0, mTranslationMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		// drawArrays uses vertexCount elements from the enabled handle to draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
		checkGlError("glDrawArrays");
			
		// disable
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mTexCoordHandle);
	}
	
	public void resetPosition() {
		Matrix.setIdentityM(mTranslationMatrix, 0);
	}
		
	private int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		
		return shader;
	}
	
	private static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(LOGTAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
	
	public void translate(float dx, float dy) {
		Matrix.translateM(mTranslationMatrix, 0, -1 * dx, dy, 0);
	}
	
	public void setFilename(String fn) {
		mFilename = fn;
	}

	public String getFilename() {
		return mFilename;
	}
}