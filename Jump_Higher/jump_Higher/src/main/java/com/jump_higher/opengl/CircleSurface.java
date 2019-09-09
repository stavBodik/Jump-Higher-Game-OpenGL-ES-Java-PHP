package com.jump_higher.opengl;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import com.jump_higher.R;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * @author Stav Bodik
 * This class used to hold the surface which has circle shape where sphere placed on information for drawing with OpenGl.
 * Vertices,Texture,Position,Rotation etc .
 */
public class CircleSurface {

	private float[] mModelMatrix = new float[16];

	// size of Float in Bytes
	private final int mBytesPerFloat = 4;	
	
	// date sizes per vertex
	private final int mVerticesDataSize = 3;	
	private final int mColorDataSize = 4;	
	private final int mNormalDataSize = 3;
	private final int mTextureCoordinateDataSize = 3;
	private final int mNumberOfVertices = 180;

	// Buffers for Circle
	private  FloatBuffer mVerticesBuff;
	private  FloatBuffer mCircleColorsBuff;
	private  FloatBuffer mCircleNormalsBuff;
	private  FloatBuffer mCircleTextureCoordinatesBuff;
	
	// Handlers
	private int mPositionHandle;
	private int mColorHandle;
	private int mNormalHandle;
	private int mTextureDataHandle;
	private int mTextureCoordinateHandle;
	private int mTextureUniformHandle;
    private int mMVPMatrixHandle;
	private int mMVMatrixHandle;
	private int bitmapWith=444;
	private Context mActivityContext;
	
	
	// vertices array
	float[] vertices;

	public CircleSurface(final Context mActivityContext){	
		
		this.mActivityContext=mActivityContext;
		
		loadCircleVerticesBuff(mActivityContext);
		
		loadCircleColorBuff();
		
		loadCircleVNormalBuff();
		
		loadCircleTextureBuff();
		
		
	
	}
	
	public void loadSurfaceTexture(Context mActivityContext){
		// load texture
        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.circle,true,bitmapWith);

	}
	
	public void draw(int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[]){
		
        GLES20.glUseProgram(mProgramHandle);

		// Set program handles for Circle drawing.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix"); 
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal"); 
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
        
     // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);  
        
		// Pass in the position information
		mVerticesBuff.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mVerticesDataSize, GLES20.GL_FLOAT, false,0, mVerticesBuff);        
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        mCircleColorsBuff.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,0, mCircleColorsBuff);        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // Pass in the normal information
        mCircleNormalsBuff.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mCircleNormalsBuff);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        // Pass in the texture coordinate information
        mCircleTextureCoordinatesBuff.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mCircleTextureCoordinatesBuff);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);   
        
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
                
        // Draw the Circle, 6 is number of floating points inside CirclePositionData
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mNumberOfVertices);    
	}
	
	public void translate(float x,float y,float  z){
		Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y-1, z);
        Matrix.rotateM(mModelMatrix, 0, 90, 1, 0, 0);        

	}
	
	public void scale(float size){
        Matrix.scaleM(mModelMatrix, 0, size, size, size);
	}
	
	public void scaleTexture(float size){
		// load texture, 5 is default size
        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.circle,true,bitmapWith*(size/5));

	}
	
	public void rotate(float angel,float x,float y,float  z){
        Matrix.rotateM(mModelMatrix, 0, angel, x, y, z);        
	}
	
	public void loadCircleTextureBuff(){
		mCircleTextureCoordinatesBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mTextureCoordinateDataSize * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();							
		
		       for (int i = 0; i < (mNumberOfVertices * mVerticesDataSize); i += 3) {   
			   vertices[i] = ((vertices[i])+1f)/2;
			   vertices[i + 1] = ((vertices[i+1])+1f)/2;
			   vertices[i + 2] = 0;

			   }
		       
		mCircleTextureCoordinatesBuff.put(vertices);
		mCircleTextureCoordinatesBuff.position(0);
	}
	
	public void loadCircleVNormalBuff(){
		mCircleNormalsBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mNormalDataSize * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();							
        // set normals with normaliztion
		for(int i=0; i<mNumberOfVertices; i++){
        mCircleNormalsBuff.put(new float[]{0,1,0});
        }
		mCircleNormalsBuff.position(0);
		
	}
	
	public void loadCircleColorBuff(){
		mCircleColorsBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mColorDataSize * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();							
        
		// set base color to white
        for(int i=0; i<mNumberOfVertices*mColorDataSize; i++){
        		mCircleColorsBuff.put(new float[]{1.0f});
        	
        }
        
        mCircleColorsBuff.position(0);

	}
	
	public void loadCircleVerticesBuff(Context mActivityContext){
		
		   mVerticesBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mVerticesDataSize * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();							
		   vertices = new float[mNumberOfVertices * mVerticesDataSize];
		   float theta = 0;

		   for (int i = 0; i < (mNumberOfVertices * mVerticesDataSize); i += 3) {   
		   vertices[i] = (float) (((float) Math.cos(theta)));
		   vertices[i + 1] = (float) ((float) Math.sin(theta));
		   vertices[i + 2] = 0;
		   theta += Math.PI / 90;
		   }
		   
		   mVerticesBuff.put(vertices);
		   mVerticesBuff.position(0);          
	}
		
}
