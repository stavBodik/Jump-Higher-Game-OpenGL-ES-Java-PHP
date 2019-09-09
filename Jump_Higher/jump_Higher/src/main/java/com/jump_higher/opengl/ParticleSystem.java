package com.jump_higher.opengl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import com.jump_higher.R;

/**
 * @author http://plattysoft.github.io/Leonids/ edited by Stav Bodik
 * This class used to hold Particle system which is lot of square shapes drawn with OpenGl.
 * Particle system in graphics has many uses , for example drawing fire or smoke. . .
 * In this application its used to draw sphere energy power when its jumps .
 */
public class ParticleSystem {

	private float mModelMatrix[] = new float[16];
	private  float angleVariance;
    private  float speedVariance;

    private Random random = new Random();

    private float rotationMatrix[] = new float[16];
    private float directionVector[] = new float[4];
    private float resultVector[] = new float[4];

    private  Geometry.Point position;
    private  int color;
	    
	private int particleProgram;

    private static final int POSITION_COMPONENT_COUNT = 3;                                  // [ x, y, z ]
    private static final int COLOR_COMPONENT_COUNT = 3;                                     // [ r, g, b ]
    private static final int VECTOR_COMPONENT_COUNT = 3;                                    // [ x2, y2, z2 ]
    private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT
                                                   + COLOR_COMPONENT_COUNT
                                                   + VECTOR_COMPONENT_COUNT //A_DIRECTION_VECTOR_LOCATION
                                                   + PARTICLE_START_TIME_COMPONENT_COUNT;   // Floats values per particle

    private static int STRIDE = TOTAL_COMPONENT_COUNT * Constants.BYTES_PER_FLOAT;          // Bytes per particles

    private float particles[];

    private int maxParticleCount=0;
    private int currentParticleCount=0;
    private int nextParticle=0;
    
    
 // Uniform locations.
    private  int uMatrixLocation;
    private  int uTimeLocation;
    private  int uTextureLocation;

	private  FloatBuffer verticesBuffer;

    public ParticleSystem(Context context,int maxParticleCount){
        this.maxParticleCount = maxParticleCount;

    }
    
    public void loadVerticesAndShader(Context context){
        loadShader(context);
        loadVertices();
    }

    public void loadVertices(){
    	
        particles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];

        
    	verticesBuffer = ByteBuffer.allocateDirect(particles.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(particles);
    }
    
    public void loadShader(Context context){

        String attributes[] = { "a_Position", "a_Color", "a_DirectionVector", "a_ParticleStartTime" };
        
    	particleProgram = ShaderHelper.buildProgram(ShaderHelper.readTextFileFromRawResource(context, R.raw.particle_vertex_shader),
    			ShaderHelper.readTextFileFromRawResource(context, R.raw.particle_fragment_shader), attributes);

    }
    
    public void setShooterSettings(Geometry.Point position, Geometry.Vector direction, int color,float angleVarianceInDegrees, float speedVariance){
    	this.position = position;
    	this.color = color;

    	this.angleVariance = angleVarianceInDegrees;
    	this.speedVariance = speedVariance;

    	directionVector[0] = direction.x;
    	directionVector[1] = direction.y;
    	directionVector[2] = direction.z;
    }
    
    public void setShooterColor(int color){
    	this.color=color;
    }
    
    public void addParticles( float currentTime,float practicalStartTime, int count){
        for(int i = 0; i < count; i++){
            // Generate random rotation matrix and rotate particle speed vector
        	Matrix.setRotateEulerM(rotationMatrix, 0,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance);

            Matrix.multiplyMV(resultVector, 0, rotationMatrix, 0, directionVector, 0);

            float speedAdjustment = 1f + random.nextFloat() * speedVariance;

            Geometry.Vector direction = new Geometry.Vector(
                    resultVector[0] * speedAdjustment,
                    resultVector[1] * speedAdjustment,
                    resultVector[2] * speedAdjustment);
            
            
            final int particleOffset = nextParticle * TOTAL_COMPONENT_COUNT;
            int currentOffset = particleOffset;
            nextParticle++;

            if(currentParticleCount < maxParticleCount){
                currentParticleCount++;
            }
            if(nextParticle == maxParticleCount){
                nextParticle = 0;
            }

            particles[currentOffset++] = position.x;
            particles[currentOffset++] = position.y;
            particles[currentOffset++] = position.z;

            particles[currentOffset++] = Color.red(color) / 255f;
            particles[currentOffset++] = Color.green(color) / 255f;
            particles[currentOffset++] = Color.blue(color) / 255f;

            particles[currentOffset++] = direction.x;
            particles[currentOffset++] = direction.y;
            particles[currentOffset++] = direction.z;

            particles[currentOffset++] = currentTime;

            updateVertices(particles, particleOffset, TOTAL_COMPONENT_COUNT); // Refresh only requested part of the native memory (one particle at a time)
        
        }
    }

    public void updateVertices(float vertexData[], int start, int count){
    	verticesBuffer.position(start);
    	verticesBuffer.put(vertexData, start, count);
    	verticesBuffer.position(0);
    }
 
    public void bindData(){
    	
    	int A_POSITION_LOCATION = 0;
        int A_COLOR_LOCATION = 1;
        int A_DIRECTION_VECTOR_LOCATION = 2;
        int A_PARTICLE_START_TIME_LOCATION = 3;

        
        int dataOffset = 0;
        setVertexAttribPointer(dataOffset, A_POSITION_LOCATION, POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset += POSITION_COMPONENT_COUNT;

        setVertexAttribPointer(dataOffset, A_COLOR_LOCATION, COLOR_COMPONENT_COUNT, STRIDE);
        dataOffset += COLOR_COMPONENT_COUNT;

        setVertexAttribPointer(dataOffset, A_DIRECTION_VECTOR_LOCATION, VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset += VECTOR_COMPONENT_COUNT;

        setVertexAttribPointer(dataOffset, A_PARTICLE_START_TIME_LOCATION, PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride){
		verticesBuffer.position(dataOffset);
		glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, verticesBuffer);
		glEnableVertexAttribArray(attributeLocation);
		verticesBuffer.position(0);
	}
    
    public void translate(float x,float y,float  z){
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, x, y, z);
		Matrix.scaleM(mModelMatrix, 0, 2, 2, 2);
	}
    
    public void draw(float mViewMatrix[],float mMVPMatrix[],float mProjectionMatrix[],float elapsedTime,int textureID){
    	
    	GLES20.glUseProgram(particleProgram);
    	 	
    	bindData();
    	
    	uMatrixLocation = glGetUniformLocation(particleProgram, "u_Matrix");
        uTimeLocation = glGetUniformLocation(particleProgram, "u_Time");
        uTextureLocation = glGetUniformLocation(particleProgram, "u_TextureUnit_1");
                
     // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0); 
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        
        
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
        glUniform1f(uTimeLocation, elapsedTime);
        glActiveTexture(textureID);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(uTextureLocation, 0);  
    	
        
      //  GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);
        
    	GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ZERO, GLES20.GL_ONE);
        GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendColor(0, 0, 0, 0);
        
        //glDrawArrays(GL_POINTS, 0, currentParticleCount);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, currentParticleCount);    

        
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDepthMask(true);

    }
}
