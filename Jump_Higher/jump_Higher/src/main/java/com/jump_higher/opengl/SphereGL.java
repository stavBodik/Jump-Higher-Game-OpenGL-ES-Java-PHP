package com.jump_higher.opengl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import com.jump_higher.R;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
/**
 * @author Stav Bodik
 * This class used to hold sphere information for drawing with OpenGl.
 * Vertices,Texture,Position,Rotation etc .
 */
public class SphereGL {

	private float x,y,z;
	private float[] mModelMatrix = new float[16];
	
	// date sizes per vertex
	private final int mVerticesDataSize = 3;	
	private final int mColorDataSize = 4;	
	private final int mNormalDataSize = 3;
	private final int mTextureCoordinateDataSize = 3;
	private final int mNumberOfVertices = 34740;

	// Buffers for Ball
	private  FloatBuffer mVerticesBuff;
	private  FloatBuffer mBallColorsBuff;
	private  FloatBuffer mBallNormalsBuff;
	private  FloatBuffer mBallTextureCoordinatesBuff;
	
	// Handlers 
	
	// normal vertex + texture
	private int mPositionHandle;
	private int mColorHandle;
	private int mNormalHandle;
	private int mTextureDataHandle;
	private int mTextureCoordinateHandle;
	private int mTextureUniformHandle;
    private int mMVPMatrixHandle;
	private int mMVMatrixHandle;

	// sphere vertices array
	float[][] vertices;
	
	// next fields used for bloom/glow  of sphere effect when user level up 

	// rendered frame dimensions
	int frameWidth = 25;
	int frameHeight = 25;

	int screenW;
	int screenH;

	// 3 frames used to create the blur effect where on top we put main frame then
	// we add frame with horizontal blur and the last one with vertical blur.
	int main_frameBufferIDGL;
	int main_frameBufferTextureIDGL; 
	int main_renderBufferIDGL;

	int horizontal_frameBufferIDGL;
	int horizontal_frameBufferTextureIDGL; 
	int horizontal_renderBufferIDGL;

	int vertical_frameBufferIDGL;
	int vertical_frameBufferTextureIDGL; 
	int vertical_renderBufferIDGL;

	Mesh bluredSphereInsideFrame;

	// indicates when activity is on pause,used to stop loading sphere vertices file  if activity paused(on first load of renderer)
	private boolean isActivityOnPause=false;
     

	public SphereGL(final Context mActivityContext) throws NumberFormatException, IOException {
				
		loadBallVerticesBuff(mActivityContext);
		
		loadBallColorBuff();
		
		loadBallVNormalBuff();
		
		loadBallTextureBuff();
	}
	
	public void loadSphereTexture(Context mActivityContext){
        
		// Initiate for blur efftect
       CreateFrameBuffers();
				
		// load sphere texture
        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.ball,false,0);

        bluredSphereInsideFrame = new Mesh(mActivityContext);
	    bluredSphereInsideFrame.setMeshSize(8);
	}
	
	public void setViewPortForDrawingFrame(float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[]){
		
		// Set the OpenGL viewport.
		GLES20.glViewport(0, 0, frameWidth, frameHeight);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float meshSize=5;
		final float ratio = (float) frameWidth / frameWidth;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -ratio;
		final float top = ratio;
		final float near = 1f;
		final float far = 1000.0f;

		Matrix.orthoM(mProjectionMatrix, 0, meshSize*left, meshSize*right, meshSize*bottom, meshSize*top, near, far);
		          

	//	Matrix.setLookAtM(mFrameViewMatrix, 0,    0f, 10f, 20f,     0f, 10f, 0f,     0f, 1f, 0f);		

	//	Matrix.multiplyMM(mFrameMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

		
	}

	public void CreateFrameBuffers(){

    	// temp array used to retrive componnents id's from opengl
    	int[] tempArr = new int[1];

    	// create main frame buffer w/o effect and store the id from opengl
    	GLES20.glGenFramebuffers(1, tempArr, 0);
    	main_frameBufferIDGL = tempArr[0];

    	// create texture and store the id from opengl
    	GLES20.glGenTextures(1, tempArr, 0);
    	main_frameBufferTextureIDGL = tempArr[0];

    	// create render buffer and store the id from opengl
    	GLES20.glGenRenderbuffers(1, tempArr, 0);
    	main_renderBufferIDGL = tempArr[0];

    	
    	// configuration of opengl for new frame buffer
    	main_frameBufferTextureIDGL = InitiateFrameBuffer(main_frameBufferIDGL, main_frameBufferTextureIDGL, main_renderBufferIDGL);




    	// same steps for 2 more frame buffers(horizontal and vertical blur)

    	// for horizontal blur
    	GLES20.glGenFramebuffers(1, tempArr, 0);
    	horizontal_frameBufferIDGL = tempArr[0];

    	GLES20.glGenTextures(1, tempArr, 0);
    	horizontal_frameBufferTextureIDGL = tempArr[0];

    	GLES20.glGenRenderbuffers(1, tempArr, 0);
    	horizontal_renderBufferIDGL = tempArr[0];

    	horizontal_frameBufferTextureIDGL = InitiateFrameBuffer(horizontal_frameBufferIDGL, horizontal_frameBufferTextureIDGL,horizontal_renderBufferIDGL);

    	// for vertical blur
    	GLES20.glGenFramebuffers(1, tempArr, 0);
    	vertical_frameBufferIDGL = tempArr[0];

    	GLES20.glGenTextures(1, tempArr, 0);
    	vertical_frameBufferTextureIDGL = tempArr[0];

    	GLES20.glGenRenderbuffers(1, tempArr, 0);
    	vertical_renderBufferIDGL = tempArr[0];


    	// final blurred texture which will be rendered to scene
    	vertical_frameBufferTextureIDGL = InitiateFrameBuffer(vertical_frameBufferIDGL, vertical_frameBufferTextureIDGL,vertical_renderBufferIDGL);


    }   
	
	public int InitiateFrameBuffer(int fbo, int tex, int rid)
    {
            //Bind Frame buffer 
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
                  
            //Bind texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
            //Define texture parameters 
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,frameWidth, frameHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            //Bind render buffer and define buffer dimension
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, rid);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, frameWidth, frameHeight);
            //Attach texture FBO color attachment
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex, 0);
            //Attach render buffer to depth attachment
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, rid);
            
            //rest
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
	        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            
            
            return tex;
    }
	public void draw(float sphereHeight,int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[],int screenWith,int screenHeight){
		
		this.screenW=screenWith;
		this.screenH=screenHeight;

	    // use main shader program, the main shader program is a simple code from the files per_pixel_vertex_shader.glsl
        // and per_pixel_fragment_shader the files contains lines wrriten in OpenGL shading language based on c language
        //  which telling to OpenGL what to do with each vertex when executing draw in parallel using matrix multiplications.
	    GLES20.glUseProgram(mProgramHandle);

	    // bind the default frame buffer to openGL, means we are going to draw to default buffer of OpenGL which is at index 0.
        // OpenGl on the GPU uses this buffer in order to draw our objects with respect to our shader programs.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	   // draw our sphere on default openGL frame buffer
        

        // use to blend the texture with the sphere base color
        // we enable the blending here and disable it after the actual drawSphere(done)
        GLES20.glDepthMask(true);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendColor(1f, 1f, 1f, 1f);
        
        drawSphere(mProgramHandle,mViewMatrix,mMVPMatrix,mProjectionMatrix,mTextureDataHandle);
        
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}
	
	public void drawGlow(float sphereHeight,int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[],int screenWith,int screenHeight){
		 
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ZERO, GLES20.GL_ONE);
        GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendColor(0, 0, 0, 0);

		// set view port to mesh size 
		setViewPortForDrawingFrame(mViewMatrix, mMVPMatrix, mProjectionMatrix);

		// for screen shoot we look at the sphere at 0 height , because the view port is only in 5*5 range
     	Matrix.setLookAtM(mViewMatrix, 0, 0, 10, 20, 0, 0, 0, 0, 1, 0);

		// for screen shoot we translate the sphere to 0 height , because the view port is only in 5*5 range
		translate(0, 0, 0);
		
		// bind main frame buffer to openGL
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, main_frameBufferIDGL);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);			        

      //draw our sphere to main frame buffer
	   drawSphere(mProgramHandle,mViewMatrix,mMVPMatrix,mProjectionMatrix,mTextureDataHandle);
       
	
       //make from main frame buffer an texture , 
       //apply on it blur effect and draw it inside horizontal frame buffer on quad mesh
      horizontalBlur(mProgramHandle,mViewMatrix,mMVPMatrix,mProjectionMatrix);
       
       //make from horizontal frame buffer an texture , 
       //apply on it blur effect and draw it inside vertical frame buffer on quad mesh
       verticalBlur(mProgramHandle,mViewMatrix,mMVPMatrix,mProjectionMatrix);
       

       // draw the texture from vertical frame on default frame buffer

       // translate the sphere  back
		translate(0, sphereHeight, 0);
		// set look at back
    	Matrix.setLookAtM(mViewMatrix, 0, 0, GLRenderer.CAMERA_Y, GLRenderer.CAMERA_Z, 0, sphereHeight, 0, 0, 1, 0);

	   restoreViewPortToScreenSize(screenWith,screenHeight,mProjectionMatrix,mViewMatrix,mMVPMatrix);

        //return to default frame buffer
	   GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	   
       drawBluredSphere(sphereHeight,mProgramHandle,mViewMatrix,mMVPMatrix,mProjectionMatrix);

       GLES20.glDisable(GLES20.GL_BLEND);
       GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	public void restoreViewPortToScreenSize(int screenWith,int screenHeight,float[] mProjectionMatrix,float[] mViewMatrix,float[] mMVPMatrix){
		// Set the OpenGL viewport to the same size as the surface.
				GLES20.glViewport(0, 0, screenWith, screenHeight);

				// Create a new perspective projection matrix. The height will stay the same
				// while the width will vary as per aspect ratio.
				final float ratio = (float) screenWith / screenHeight;
				final float left = -ratio;
				final float right = ratio;
				final float bottom = -1.0f;
				final float top = 1.0f;
				final float near = 1f;
				final float far = 1000.0f;

				Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
				

				//Matrix.multiplyMM(mMVPMatrix, 0, mProjBackUpMatrix, 0, mViewBackUpMatrix, 0);
			
	}

public void drawBluredSphere(float sphereHeight,int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[]){

	// Calculate angel between camera eye and sphere location.
	float dy = GLRenderer.CAMERA_Y-y;
	float dz = GLRenderer.CAMERA_Z-0; // ball always at x,z 0
	float angel = (float) Math.atan2(dy, dz);

	//rotate and translate bluredSphereInsideFrame mesh to face into the front of the camera eye.
	bluredSphereInsideFrame.translate(x, sphereHeight,z);
	bluredSphereInsideFrame.rotate(-(float)Math.toDegrees(angel), 1, 0, 0);
	bluredSphereInsideFrame.draw(mProgramHandle, mViewMatrix, mMVPMatrix, mProjectionMatrix, true, vertical_frameBufferTextureIDGL);


	// clear vertical frame (the last/top most frame was used to create the blur effect)
	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, main_frameBufferIDGL);
	GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

	  //return to default frame buffer
	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
}

	public void verticalBlur(int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[]){
		// tell openGL to user vertical frame buffer
		  GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, vertical_frameBufferIDGL); 
		  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);			        
			
		  bluredSphereInsideFrame.translate(0, 0, 0);
		  bluredSphereInsideFrame.draw(mProgramHandle, mViewMatrix, mMVPMatrix, mProjectionMatrix, false, horizontal_frameBufferTextureIDGL);

		  // clear horizontal frame
		  GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, horizontal_frameBufferIDGL); 
		  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	}
	
	public void horizontalBlur(int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[]){
        // tell openGL to user horizontal frame buffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, horizontal_frameBufferIDGL); 
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);			        
	
		bluredSphereInsideFrame.translate(0, 0, 0);
		bluredSphereInsideFrame.draw(mProgramHandle, mViewMatrix, mMVPMatrix, mProjectionMatrix, true, main_frameBufferTextureIDGL);

		
		 // clear main frame
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, main_frameBufferIDGL);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	
	}
	
	private void drawSphere(int mProgramHandle,float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[],int textureHandel){

	    // get handles which will used on the GPU, they are declared in the program shader files in order to draw the sphere.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
     // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandel);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the position information
        mVerticesBuff.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mVerticesDataSize, GLES20.GL_FLOAT, false,0, mVerticesBuff);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        mBallColorsBuff.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,0, mBallColorsBuff);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Pass in the normal information
        mBallNormalsBuff.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mBallNormalsBuff);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // Pass in the texture coordinate information
        mBallTextureCoordinatesBuff.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mBallTextureCoordinatesBuff);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the Ball, 6 is number of floating points inside BallPositionData
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mNumberOfVertices);
	}
	
	public void loadBallTextureBuff(){
		mBallTextureCoordinatesBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mTextureCoordinateDataSize * Constants.BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();							
		for(int i=0; i<mNumberOfVertices; i++){
			mBallTextureCoordinatesBuff.put(vertices[i]);
    }
		mBallTextureCoordinatesBuff.position(0);
		
	}

	public void translate(float x,float y,float  z){
		this.x=x;this.y=y;this.z=z;
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, x, y, z);
	}
	
	public void rotate(float angel,float x,float y,float  z){
		Matrix.rotateM(mModelMatrix, 0, angel, x, y, z);  
        
	}
	
	public void scale(float x,float y,float  z){
		Matrix.scaleM(mModelMatrix, 0, x, y, z);  
        
	}

	public void loadBallVNormalBuff(){
		mBallNormalsBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mNormalDataSize * Constants.BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();							
        // set normals with normaliztion
		for(int i=0; i<mNumberOfVertices; i++){
        		mBallNormalsBuff.put(normlizeVector(new float[]{0,0,0}, vertices[i]));
        }
		mBallNormalsBuff.position(0);
		
	}
	public void loadBallColorBuff(){
		mBallColorsBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mColorDataSize * Constants.BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();							
        
		// set base color to white
        for(int i=0; i<mNumberOfVertices; i++){
        	for (int j=0; j<mColorDataSize; j++){
        		mBallColorsBuff.put(new float[]{0.8f});
        	}
        }
        
        mBallColorsBuff.position(0);

	}
	
	public void setActivityOnPause(boolean isActivityOnPause) {
		this.isActivityOnPause = isActivityOnPause;
	}
	
	public void loadBallVerticesBuff(Context mActivityContext) throws NumberFormatException, IOException{
		
        vertices = new float[mNumberOfVertices][mVerticesDataSize];
    	
        mVerticesBuff = ByteBuffer.allocateDirect(mNumberOfVertices * mVerticesDataSize * Constants.BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();							
		
       
        
        int lineCount=0;
    	InputStream myInput = null;
    	String str=null;
    	try {
			 myInput = mActivityContext.getAssets().open("ball.txt");
		} catch (IOException e) {
		}
		
    	//Random randomGenerator = new Random();
    	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(myInput));
        while ((str = reader.readLine()) != null && !isActivityOnPause) {    
           String line[] = str.split(",");
          // int rand = randomGenerator.nextInt(10);
           vertices[lineCount][0]=Float.parseFloat(line[0].trim());
           vertices[lineCount][1]=Float.parseFloat(line[1].trim());
           vertices[lineCount][2]=Float.parseFloat(line[2].trim());

           mVerticesBuff.put(vertices[lineCount]);
           lineCount++;
           
        }
	}

    public float[] normlizeVector(float vectorStart[],float vectorEnd[]){
		
		float size=(float) Math.sqrt(Math.pow(vectorEnd[0]-vectorStart[0], 2)+Math.pow(vectorEnd[1]-vectorStart[1], 2)+Math.pow(vectorEnd[2]-vectorStart[2], 2));
		
		return new float[]{(vectorEnd[0]-vectorStart[0])/size,(vectorEnd[1]-vectorStart[1])/size,(vectorEnd[2]-vectorStart[2])/size};
	}	
			
}
