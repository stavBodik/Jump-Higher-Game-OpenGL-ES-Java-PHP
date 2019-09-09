package com.jump_higher.opengl;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * @author Stav Bodik
 * This class used to hold mesh/plane which has square shape for drawing with OpenGl.
 * Vertices,Texture,Position,Rotation etc .
 */
public class Mesh {
	
	private float[] mModelMatrix = new float[16];

	// size of Float in Bytes
	private final int mBytesPerFloat = 4;	
	
	private  int meshSize=4;
	// date sizes per vertex
	private final int mPositionDataSize = 3;	
	private final int mColorDataSize = 4;	
	private final int mNormalDataSize = 3;
	private final int mTextureCoordinateDataSize = 2;

	// Buffers for Mesh
	private FloatBuffer mVerticesBuff;
	private final FloatBuffer mMeshColorsBuff;
	private final FloatBuffer mMeshNormalsBuff;
	private final FloatBuffer mMeshTextureCoordinatesBuff;
	
	// Handlers
	private int mPositionHandle;
	private int mColorHandle;
	private int mNormalHandle;
	private int mTextureCoordinateHandle;
	private int mTextureUniformHandle;
    private int mMVPMatrixHandle;
	private int mMVMatrixHandle;

    // X, Y, Z
	private  float[] MeshVerticsData =   
	{
			// 2 triangles
			-meshSize,-meshSize,0,				
			-meshSize,meshSize, 0,
			 meshSize,-meshSize,0, 
			
			-meshSize,meshSize,0 , 
			 meshSize,meshSize,0 ,
			 meshSize,-meshSize,0 ,
	};		
	// R, G, B, A
	private float[] MeshColorData =
	{				
			// Front face (white)
			1.0f, 1.0f, 1.0f, 1.0f,				
			1.0f, 1.0f, 1.0f, 1.0f,				
			1.0f, 1.0f, 1.0f, 1.0f,				
			1.0f, 1.0f, 1.0f, 1.0f,				
			1.0f, 1.0f, 1.0f, 1.0f,				
			1.0f, 1.0f, 1.0f, 1.0f,				
	};	
	// X, Y, Z, surface normals
	private float[] MeshNormalData =
	{												
			// Front face
			0.0f, 0.0f, 1.0f,				
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,				
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
	};
	// Texture coordinate data.
	private float[] MeshTextureCoordinateData =
		{												
				// Front face
				0.0f, 0.0f, 				
				0.0f, 1.0f,
				1.0f, 0.0f,
				
				0.0f, 1.0f,
				1.0f, 1.0f,
				1.0f, 0.0f,				
		};
		
	// Handles for blur effects
    int blurDirectionHandle, blurAmountHandle,blurScaleHandle, blureStrengthHandle;
	private int mBloomProgramHandle;
	private int mBlureProgramHandle;
    private Context mActivityContext;
	private int mainProgramTex2IDHandle;
	private int mainProgramTex1IDHandle; 
	private float glowCounter=0;
	
	public Mesh(final Context mActivityContext) {

		
		this.mActivityContext=mActivityContext;
		
		// Initialize the buffers.
		mVerticesBuff = ByteBuffer.allocateDirect(MeshVerticsData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mVerticesBuff.put(MeshVerticsData).position(0);		
		
		mMeshColorsBuff = ByteBuffer.allocateDirect(MeshColorData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mMeshColorsBuff.put(MeshColorData).position(0);
		
		mMeshNormalsBuff = ByteBuffer.allocateDirect(MeshNormalData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mMeshNormalsBuff.put(MeshNormalData).position(0);
		
		mMeshTextureCoordinatesBuff = ByteBuffer.allocateDirect(MeshTextureCoordinateData.length * mBytesPerFloat)
		.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mMeshTextureCoordinatesBuff.put(MeshTextureCoordinateData).position(0);
		
		LoadShaders();
		
	
	}
	
	public void setMeshSize(int meshSize) {
		this.meshSize = meshSize;
		
		MeshVerticsData = new float[]  
			{
					// 2 triangles
					-meshSize,-meshSize,0,				
					-meshSize,meshSize, 0,
					 meshSize,-meshSize,0, 
					
					-meshSize,meshSize,0 , 
					 meshSize,meshSize,0 ,
					 meshSize,-meshSize,0 ,
			};	
		
		mVerticesBuff = ByteBuffer.allocateDirect(MeshVerticsData.length * mBytesPerFloat)
	   .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mVerticesBuff.put(MeshVerticsData).position(0);	
				
	}
	
	public void LoadShaders()
    {
    	String bloomVertexShader = 
    			        "attribute vec4 a_position;" +
    					"attribute vec2 a_texCoords;" +
    					"uniform mat4 u_mvpMatrix;"+
    					"varying vec2 v_texCoords;" +
    					"void main()" +
    					"{" +
    					"gl_Position = u_mvpMatrix * a_position;" +
    					"v_texCoords = a_texCoords;" +
    					"}";
    	String bloomFragmentShader = 
    					"precision mediump float;" +
    					"varying vec2 v_texCoords;" +
    					"uniform sampler2D u_texId;" +                          
    					"uniform sampler2D u_texId1;" +
    					"uniform sampler2D u_texId2;" +
    					"void main()" +
    					"{" +
    					"vec4 src = texture2D(u_texId, v_texCoords);" +
    					"vec4 dst = texture2D(u_texId1, v_texCoords);" +
    					"vec4 bloomcolor = clamp((src + dst) - (src * dst), 0.0, 1.0);" +
    					"gl_FragColor = bloomcolor + texture2D(u_texId2, v_texCoords);" +
    					"gl_FragColor.a = 1.0;"+
    					"}";




    	// Define and link shader program ( vertex and fragment), for bloom
    	final int bloomVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, bloomVertexShader);		
		final int bloomFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, bloomFragmentShader);		
		
		mBloomProgramHandle = ShaderHelper.createAndLinkProgram(bloomVertexShaderHandle, bloomFragmentShaderHandle, new String[] {"a_position","a_texCoords"},"Mesh bloom"); 
		
    	// Define and link shader program ( vertex and fragment), for blur
		final String blurVertexShader = ShaderHelper.readTextFileFromRawResource(mActivityContext, R.raw.vertex_blur);  		
		final String blurFragmenShader = ShaderHelper.readTextFileFromRawResource(mActivityContext, R.raw.gaussianblur_fragment);				

		final int blurVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, blurVertexShader);		
		final int blureFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, blurFragmenShader);		
		
        mBlureProgramHandle = ShaderHelper.createAndLinkProgram(blurVertexShaderHandle, blureFragmentShaderHandle, new String[] {"a_position","a_texCoords"},"Mesh blur"); 

    }   
	
	public void draw(int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[],boolean isBlurHorizontal,int mTextureDataHandle){

		GLES20.glUseProgram(mBloomProgramHandle);

		mPositionHandle = GLES20.glGetAttribLocation(mBloomProgramHandle, "a_position");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mBloomProgramHandle, "a_texCoords");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mBloomProgramHandle, "u_mvpMatrix");
		//mainProgramTexIDHandle = GLES20.glGetUniformLocation(mBloomProgramHandle, "u_texId");
		mainProgramTex1IDHandle = (GLES20.glGetUniformLocation(mBloomProgramHandle, "u_texId1"));
		mainProgramTex2IDHandle = GLES20.glGetUniformLocation(mBloomProgramHandle, "u_texId2");

		GLES20.glUseProgram(mProgramHandle);

		mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
		mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
		mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");

		GLES20.glUseProgram(mBlureProgramHandle);

		blurDirectionHandle = GLES20.glGetUniformLocation(mBlureProgramHandle, "direction");
		blurScaleHandle = GLES20.glGetUniformLocation(mBlureProgramHandle, "blurScale");
		blurAmountHandle = GLES20.glGetUniformLocation(mBlureProgramHandle, "blurAmount");
		blureStrengthHandle = GLES20.glGetUniformLocation(mBlureProgramHandle, "blurStrength");

		if(glowCounter==10){
			glowCounter=0.0f;
			ApplicationManager.getInstance().setSphereGlowEnds(true);
			return;
		}
		glowCounter+=5/20f;


		// apply horizontal/vertical blur effect on it
		if(isBlurHorizontal) GLES20.glUniform1i(blurDirectionHandle, 0);
		else GLES20.glUniform1i(blurDirectionHandle, 1);

		GLES20.glUniform1f(blurScaleHandle,  glowCounter);
		GLES20.glUniform1f(blurAmountHandle, 25);
		GLES20.glUniform1f(blureStrengthHandle, 0.1f);
            

        

     // unbind old texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        
     // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
		if(isBlurHorizontal) {
			GLES20.glUniform1i(mainProgramTex2IDHandle, 0);
		}
		else{
			GLES20.glUniform1i(mainProgramTex1IDHandle, 0);
		}


        
		// Pass in the position information
		mVerticesBuff.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,0, mVerticesBuff);        
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        mMeshColorsBuff.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,0, mMeshColorsBuff);        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // Pass in the normal information
        mMeshNormalsBuff.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mMeshNormalsBuff);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        // Pass in the texture coordinate information
        mMeshTextureCoordinatesBuff.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mMeshTextureCoordinatesBuff);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);   
        
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
                
        // Draw the Mesh, 6 is number of floating points inside MeshPositionData
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);    
	}
	
	public void translate(float x,float y,float  z){
		Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
	}
	
	public void scale(float size){
        Matrix.scaleM(mModelMatrix, 0, size, size, size);
	}

	public void rotate(float angel,float x,float y,float  z){
        Matrix.rotateM(mModelMatrix, 0, angel, x, y, z);        
	}		
}
