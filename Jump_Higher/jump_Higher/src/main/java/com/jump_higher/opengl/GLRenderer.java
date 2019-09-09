package com.jump_higher.opengl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.jump_higher.R;
import com.jump_higher.activities.GameActivity;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.User;
import com.jump_higher.opengl.Geometry.Point;
import com.jump_higher.opengl.Geometry.Vector;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

/**
 * @author Stav Bodik
 * This class used to render game 3d scene to device screen, 
 * Also calculates every movement of sphere and surface to draw on scene.
 * Gets information from GameActivity class which listen to device sensors .
 */
public class GLRenderer implements GLSurfaceView.Renderer 
{	
	public static final float CAMERA_Y=10f;
	public static final float CAMERA_Z=10f;
	
	private int screenWith,screenHeight;
	
	public boolean startGame=false;
	public boolean restartGame=false;
	public boolean isPlaying=false;

	int level=1;
	int nextLvlHeight=Constants.START_LEVEL_HEIGHT;
	float  diskRaduis=5f;
	float  raduisLvlUpScale=0.1f;
	float  diskRaduisCurrent=diskRaduis;
	float  diskRaduisNextLVL=diskRaduis;
	boolean isLevelUp=false;

	// rotation of surface by user
	public float surfaceXrotate=0;
	public float surfaceZrotate=0;
	
	// this boolean/timer used to filter sensor when user jump after x time
	private boolean isOkToHit=false;

	// energy of sphere 
	private int currentSphereEnergy=0;
	// coordinates of Sphere
	private float mSphere_x=0;
	private float mSphere_z=0;
	private float mSphere_y=0f;
	private float mSphere_xOld=0;
	private float mSphere_zOld=0;
	
	// timers
	private float currentTimeXZ=0;
	private float timeMilisecY=0;
	private float startTimeXZ=0;
	private float startTimeY=0;
	private float startTimeHit=0;
	private float startTimeJump=0;
    private float currentTimeY;


	// acceleration of the Sphere
    float fxParalel=0 ;
	float fzParalel=0 ;

	private float mxAccaloration=0;
	private float mzAccaloration=0;
    public float hitAcceloration=0; // hit from surface when user lift the device up/down
    public float hitAccelorationTemp=0; // used to store hitAcceloration without energy effect
    // sphere velocity (x,y,z)
	private float mSphereMass = 10000f; // g / 10kg
	private float gForce = 30.806f;
	
	// sphere direction rotate
    int rotationDirectionX;
    int rotationDirectionZ;
    
    // sphere disntace from 0,0,0
    float sphereRaduisDistance;
    
    //sphere jump 
    boolean isFalling=false;
    boolean isBouncing=false;
    private boolean onTheAir=false;
	private boolean diskHitedByUser=false;

    float SphereStartVelocityY=0;
    float SphereCurrentVelocityY=0;
    float currentSphereHeight=0;
    
    // indicates that sphere vertices file is loaded , used to know when to reload it again if user go to on pause in middel of loading
    private boolean isSphereLoaded=false;

    // game manager
	private ApplicationManager manager;

        
    // main activity context
	private final Context mActivityContext;

	/** Camera world space matrix */
	private float[] mViewMatrix = new float[16];

	/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
	private float[] mProjectionMatrix = new float[16];
	
	/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
	private float[] mMVPMatrix = new float[16];

	/**  Stores a copy of the model matrix specifically for the light position.*/
	private float[] mLightModelMatrix = new float[16];	
	
	/** This will be used to pass in the light position. */
	private int mLightPosHandle;
		
	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	 *  we multiply this by our transformation matrices. */
	private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];
	
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];
	
	/** This is a handle to our cube shading program. */
	private int mProgramHandle;
		
	/** This is a handle to our light point program. */
	private int mPointProgramHandle;

	private SphereGL sphere;
	
	private CircleSurface circleSurface_level_0;
	//private CircleSurface circleSurface_level_1;

	
	// Position the eye in front of the origin.
	final float eyeX = 1f;
	final float eyeY = CAMERA_Y;
	final float eyeZ = CAMERA_Z;

	// look to coordinates
	final float lookX = 0.0f;
	final float lookY = 0.0f;
	final float lookZ = 0.0f;

	// Set our up vector. This is where our head would be pointing were we holding the camera.
	final float upX = 0.0f;
	final float upY = 1.0f;
	final float upZ = 0.0f;

	//GUI 
	private GameActivity gameActivity ;
	private Date startTimeGUI;
	private SimpleDateFormat sdf;
	
	//player
	private User loggedInUser;
	
	
    //when user level up sphere glow effect
	int glowCounter=0;


	//particle effects components used to draw particle system when sphere jumps/hits the surface.
	 private int particleSystemCounter=0;
	 ArrayList<ParticleSystem> pSystems = new ArrayList<ParticleSystem>();
	 private long particleStartTime;
	 private float particleElapsedTime;
	 private int particleTexture;
	 boolean drawParticle=false;
	 private int particleColor=Color.rgb(0, 210, 255);
	 
	 private Vector particleDirection = new Vector(0f, -1f, 0f);
	 private float angleVarianceInDegrees = 10f;
	 private float speedVariance = 50f;
     
	//sounds
	private MediaPlayer mPlayerJumpWoho;
	private MediaPlayer mPlayerJumpYeeHa;

	private ArrayList<MediaPlayer> passSounds = new ArrayList<MediaPlayer>();
	private int passSoundCounter=0;


	private ArrayList<MediaPlayer> ballBouncingSounds = new ArrayList<MediaPlayer>();
	private int bounceSoundCounter=0;

	// flag indicates that draw began
	private boolean isDrawBeganAlredy=false;
	
	public GLRenderer(final Context activityContext)
	{	
		mActivityContext = activityContext;
	}
	
	public void setGameActivity(GameActivity gameActivity) {
		this.gameActivity = gameActivity;
	}
	
	public void setStartGame(boolean startGame) {
		this.startGame = startGame;
	}

	public void setRestartGame(boolean restartGame) {
		this.restartGame = restartGame;
	}	public void onRestartGame(){
    	diskHitedByUser=true;
		isFalling=false;
		level=1;
		nextLvlHeight=Constants.START_LEVEL_HEIGHT;
		diskRaduis=5f;
		raduisLvlUpScale=0.1f;
		diskRaduisCurrent=diskRaduis;
		diskRaduisNextLVL=diskRaduis;

		// rotation of surface
		surfaceXrotate=0;
		surfaceZrotate=0;
		

		// coordinates of Sphere
		mSphere_x=0;
		mSphere_z=0;
		mSphere_y=0;
		mSphere_xOld=0;
		mSphere_zOld=0;
		// timers
		currentTimeXZ=0;
		timeMilisecY=0;
		startTimeXZ=0;
		startTimeY=0;

		// acceleration of the Sphere
		mxAccaloration=0;
		mzAccaloration=0;

	  
		// sphere direction rotate
	    rotationDirectionX=0;
	    rotationDirectionZ=0;
	    
	    sphereRaduisDistance=0;
	    
	    //rest energy
	    currentSphereEnergy=0;
	    gameActivity.getEneryBarLayOut().setProgressCount(currentSphereEnergy);
		particleColor = gameActivity.getEneryBarLayOut().getCurrentProgressColor();
		hitAcceloration=0;
		
		restTimerXZ();
	    

		
	}
	
	public void setDirectionX(int directionX) {
		this.rotationDirectionX = directionX;
	}

	public void setDirectionZ(int directionZ) {
		this.rotationDirectionZ = directionZ;
	}
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
		
		if(gameActivity.isActivityOnPause())return;
		
		manager = ApplicationManager.getInstance();

		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		GLES20.glDepthFunc(GLES20.GL_LEQUAL);

		// set camera settings
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);		

		// Define and link shader program ( vertex and fragment), includes light
        // what shader means in opengl ? https://learnopengl.com/Getting-started/Shaders
        // its an program that runs on the device GPU , some function which can run in parallel on many cores for faster job.
		final String vertexShader = ShaderHelper.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader);  		
		final String fragmentShader = ShaderHelper.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader);				

		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		

		mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, new String[] {"a_Position",  "a_Color", "a_Normal", "a_TexCoordinate"},"Main 1");								                                							       

		// Define and link shader program ( vertex and fragment) for point,not includes light
		final String pointVertexShader = ShaderHelper.readTextFileFromRawResource(mActivityContext, R.raw.point_vertex_shader);        	       
		final String pointFragmentShader = ShaderHelper.readTextFileFromRawResource(mActivityContext, R.raw.point_fragment_shader);

		final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
		final int pointFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
		mPointProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle, new String[] {"a_Position"},"Main 2"); 

		startTimeXZ = SystemClock.uptimeMillis();

		// create sphere
		try {
			if(sphere==null || !isSphereLoaded){
			sphere = new SphereGL(mActivityContext);
			
			sphere.setActivityOnPause(false);
			
			if(!gameActivity.isActivityOnPause())
			isSphereLoaded=true;
			
			}
			
			sphere.loadSphereTexture(mActivityContext);
			
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}

		if(circleSurface_level_0==null)
		circleSurface_level_0 = new CircleSurface(mActivityContext);
		circleSurface_level_0.loadSurfaceTexture(mActivityContext);

		// setup timer for gui
		startTimeGUI = new Date(); // time right now
		sdf = new SimpleDateFormat("mm:ss:SS",Locale.US);

		//Create audio attributes to be used by all media players
		AudioAttributes audioAttributes= new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_MEDIA)
				.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
				.build();


		// get logged in user
		loggedInUser = manager.getLoggedInUser();

		mPlayerJumpWoho = MediaPlayer.create(mActivityContext, R.raw.jump);
		mPlayerJumpWoho.setAudioAttributes(audioAttributes);

		mPlayerJumpYeeHa = MediaPlayer.create(mActivityContext, R.raw.yeehaw);
		mPlayerJumpYeeHa.setAudioAttributes(audioAttributes);

		
		// sounds are loaded ones when activity created , when user comes back from onPause , no need to create them again .
		if(ballBouncingSounds.size()==0){
			for(int i=0; i<35; i++){
				int soundID = mActivityContext.getResources().getIdentifier("jm"+(i+1) , "raw", mActivityContext.getPackageName());
				MediaPlayer m  =  MediaPlayer.create(mActivityContext,soundID);
				ballBouncingSounds.add(m);
				ballBouncingSounds.get(i).setAudioAttributes(audioAttributes);

			}		
		}
		
		if(passSounds.size()==0){
			for(int i=0; i<2; i++){
				MediaPlayer m  =  MediaPlayer.create(mActivityContext,R.raw.pass);
				passSounds.add(m);
				passSounds.get(i).setAudioAttributes(audioAttributes);
			}
		}


		//Note: whenever game comes back from pause(on activity resume) there is no need to recreate the particle systems again , only load shader and texture.
		if(pSystems.size()==0){

		   particleDirection = new Vector(0f, -1f, 0f);
           angleVarianceInDegrees = 10f;
           speedVariance = 50f;
		
           for(int i=0; i<20; i++){
        	   pSystems.add(new ParticleSystem(mActivityContext,10000));
        	   pSystems.get(i).loadVerticesAndShader(mActivityContext);
        	   pSystems.get(i).setShooterSettings(new Point(0f, 0f, 0f), particleDirection, particleColor, angleVarianceInDegrees, speedVariance);
           }
           
		}else{
			for(int i=0; i<pSystems.size(); i++){
				pSystems.get(i).loadVerticesAndShader(mActivityContext);
			}
		}
	
	     
          // init start time
          particleStartTime = System.nanoTime();

          particleTexture = TextureHelper.loadTexture(mActivityContext, R.drawable.smoke,false,0);

	}	
		
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{

		this.screenWith=width;
		this.screenHeight=height;
		
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1f;
		final float far = 1000.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	}	

	@Override
	public void onDrawFrame(GL10 glUnused) 
	{		
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		if(!isDrawBeganAlredy){
		gameActivity.onOpenGLfinishLoading();
		isDrawBeganAlredy=true;
		}
		
		// update timer gui
		if(isPlaying){
			Date now = new Date();
			if(gameActivity!=null){
				gameActivity.getLevelInformationL().setTimer(sdf.format(new Date(now.getTime() - startTimeGUI.getTime())));
			}
		}
		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);			        
                             
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);
        // Set program handles for light drawing.
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
               
        // Calculate position of the light.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0, mSphere_y, 5f);      

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);                         
        
        

        if(restartGame){
    		System.out.println("Restart game");

    		startTimeXZ = SystemClock.uptimeMillis();
    		startTimeY = SystemClock.uptimeMillis();

            startTimeGUI = new Date(); // timer gui restart
            gameActivity.getLevelInformationL().restart();
        	restartGame=false;
        	
        	onRestartGame();
        	startGame=true;
        }
        
        if(startGame){
    		System.out.println("start game");
        	startGame=false;
            isPlaying=true;
        	setHit(15);
            startTimeGUI = new Date(); // time right now
        }
        
        

  
        // set camera settings, y looks at ball height .
        if(mSphere_y>=0)
     	Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, mSphere_y, lookZ, upX, upY, upZ);
        else Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        //applay light position
         GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1],mLightPosInEyeSpace[2]);

         Thread t = new Thread(new  Runnable() {
			public void run() {
				
                 // timer used to calculate ball movement on x,z axis .
                calculateSphereTimerOnXZAxis();

                // calulate sphere jump if disk hitted
                sphereJump();

                // the actual sphere coordinates calculation includes acceleration/forces
                sphereMovementXZ();

                // calculate when sphere has to fall
                sphereFalling();
				
            }
		});
         t.start();
         
       
     	
     	// Translate the sphere to his new coordinates
        sphere.translate(mSphere_x, mSphere_y,-mSphere_z);
        //sphere.translate(0, mSphere_y,0);


     	// rotation of the sphere depends on surface angels and ball velocity
		sphereRoatation();
		

        circleSurface_level_0.translate(0, 0, 0);
        circleSurface_level_0.scale(diskRaduisCurrent);
        circleSurface_level_0.rotate(-surfaceXrotate, 1, 0, 0);
        circleSurface_level_0.rotate(-surfaceZrotate, 0, 1, 0);
        circleSurface_level_0.draw(mProgramHandle, mViewMatrix, mMVPMatrix, mProjectionMatrix);

        
        circleSurface_level_0.translate(0, nextLvlHeight, 0);
        circleSurface_level_0.scale(diskRaduisCurrent);
        circleSurface_level_0.draw(mProgramHandle, mViewMatrix, mMVPMatrix, mProjectionMatrix);

        
        sphere.draw(mSphere_y,mProgramHandle, mViewMatrix, mMVPMatrix, mProjectionMatrix,screenWith,screenHeight);

        // when user level up draw sphere glow
        if(manager.isLevelUp()){
	        if(!manager.isSphereGlowEnds())
	        sphere.drawGlow(mSphere_y,mProgramHandle, mViewMatrix, mMVPMatrix, mProjectionMatrix,screenWith,screenHeight);
	        else {
	        	manager.setLevelUp(false);
	        }
        }
        
    	particleElapsedTime = (System.nanoTime() - particleStartTime) / 1000000000f;        
    	
        // draw particle only when user hit and sphere is going up
        if(SphereCurrentVelocityY>0){
        

    	if(particleSystemCounter>=pSystems.size())particleSystemCounter=0;

		pSystems.get(particleSystemCounter).translate(mSphere_x, mSphere_y, -mSphere_z);

		particleDirection = new Vector(0f, -1f, 0f);
		angleVarianceInDegrees = SphereStartVelocityY-SphereCurrentVelocityY;
		speedVariance = SphereCurrentVelocityY;

		pSystems.get(particleSystemCounter).setShooterSettings(new Point(0f, 0f, 0f), particleDirection, particleColor, angleVarianceInDegrees, speedVariance);
		pSystems.get(particleSystemCounter).addParticles(particleElapsedTime,particleStartTime, 20);

        }
    	
        	
        
        // when level up rest all particles to sphere height
		if(manager.isLevelUp()){
		    for(int i=0; i<pSystems.size(); i++){
		    	pSystems.get(i).translate(0, mSphere_y, 0);
		    }
		}

        // draw particle
        for(int i=0; i<pSystems.size(); i++){
            pSystems.get(i).draw(mViewMatrix,mMVPMatrix,mProjectionMatrix, particleElapsedTime, particleTexture);
        }
        
        // Draw a point to indicate the light,unused only for debug light .
        //GLES20.glUseProgram(mPointProgramHandle);        
        //drawLight();
	}				

	public void setSurfaceXrotate(float surfaceXrotate) {		
		if(isPlaying && !diskHitedByUser)
	  	this.surfaceXrotate = surfaceXrotate;
	}
	
	public void setSurfaceZrotate(float surfaceZrotate) {
		if(isPlaying && !diskHitedByUser)
		this.surfaceZrotate = surfaceZrotate;
	}
	
	public boolean isOkToRotate(){
		if(!diskHitedByUser)return true;
		else return false;
	}
	
	public void sphereJump(){
			
	    	// check if surface got hit from disk by user phone acceleration
	    	if(diskHitedByUser && !isFalling){
	    		startTimeJump=SystemClock.uptimeMillis();
	    		SphereStartVelocityY=hitAcceloration;
	    		isBouncing=true;
	    		manager.setBouncing(isBouncing);
	    		diskHitedByUser=false;
	    	}
	    	
	    	// sphere is bouncing
	    	if(isBouncing){
	    		currentTimeY=(SystemClock.uptimeMillis()-startTimeJump)*0.001f;	    		
	    		SphereCurrentVelocityY=SphereStartVelocityY -gForce*currentTimeY;
	    		mSphere_y=SphereStartVelocityY*currentTimeY-(float) (0.5*gForce*Math.pow(currentTimeY, 2)); 

	    		
	    		// is level up ?
	    		if(mSphere_y>=nextLvlHeight){
	    	    	passSounds.get(passSoundCounter).start();
	    	    	passSoundCounter+=1;
	    			if(passSoundCounter==2)passSoundCounter=0;
	    			manager.setSphereGlowEnds(false);
	    		    manager.setLevelUp(true);
	    		    isLevelUp=true;
	        		SphereStartVelocityY=SphereCurrentVelocityY;
	    			level++;
	    			nextLvlHeight++;
	    			diskRaduisNextLVL=(float) (Math.round(diskRaduisNextLVL * 100.0) / 100.0);
	    			if(gameActivity.getLevelInformationL()!=null){
						gameActivity.getLevelInformationL().setLevel(level);
						loggedInUser.setReachedLevel(level);
	    				Date now = new Date();
			        	loggedInUser.setReachedTime(sdf.format(new Date(now.getTime() - startTimeGUI.getTime())));
			        	gameActivity.onLevelUp();
	    			}
	    		}
	    		
	    		

	    		
	    		// sphere is on floor back from air start velocity equals end velocity
	    		if(Math.abs(SphereCurrentVelocityY)>=SphereStartVelocityY && currentTimeY!=0 && isPlaying){
	    			// bounce again but now the velocity is lower
	    			SphereStartVelocityY*=0.5f;
	        		startTimeJump=SystemClock.uptimeMillis();
	        		bounceSoundCounter++;
                    if(bounceSoundCounter==ballBouncingSounds.size())bounceSoundCounter=0;
	        		if(!manager.isLevelUp())
	        		ballBouncingSounds.get(bounceSoundCounter).start();
	    			particleSystemCounter++;

	    		}
	    		
	    	}
	    	
	    	
	    	// ball jump stopped
	    	if(Math.floor(SphereStartVelocityY)<=0){
	    		// rest the acceleration on x/z when ball comes back from air need to start calculate acceleration from new  		
	    		if(isBouncing)restTimerXZ();
	    		isBouncing=false;
	    		manager.setBouncing(isBouncing);
	    		bounceSoundCounter=0;
	    	}
	    	
	    	    	
			
		}

    //makes the sphere jump with respect to some acceleration value
	public void setHit(float newAcceloration) {
		// used to filter acceleration when user jump
		if((SystemClock.uptimeMillis()-startTimeHit)>1000){
			isOkToHit=true;
		}
		
		
		if(mSphere_y<=2 && isPlaying && isOkToHit){
			this.diskHitedByUser=true;
			isOkToHit=false;
			drawParticle=true;
			this.hitAcceloration=SphereStartVelocityY+newAcceloration*2;
			currentSphereEnergy+=(int)newAcceloration;
			gameActivity.getEneryBarLayOut().setProgressCount(currentSphereEnergy);
			particleColor = gameActivity.getEneryBarLayOut().getCurrentProgressColor();
			particleSystemCounter++;
			
			// if sphere got 100% energy sphere acceleration increase
			if(currentSphereEnergy>=100){
				particleColor=Color.rgb(255, 0, 85);
				hitAccelorationTemp=this.hitAcceloration;
				this.hitAcceloration*=5;
	        	currentSphereEnergy=0;
	    		gameActivity.getEneryBarLayOut().setProgressCount(currentSphereEnergy);
	    		mPlayerJumpYeeHa.start();
	
	        }else mPlayerJumpWoho.start();
	
			
			
	    	ballBouncingSounds.get(0).start();
			startTimeHit = SystemClock.uptimeMillis();
		}
		
		
	}	public void calculateSphereTimerOnXZAxis(){
	     	currentTimeXZ = SystemClock.uptimeMillis() - startTimeXZ;
	     	currentTimeXZ=currentTimeXZ/1000;
	}
    
	public void onPauseGame(){
		
		
		
    	isPlaying=false;
    	if(sphere!=null)
    	sphere.setActivityOnPause(true);    	
    }
 
    public void restTimerXZ(){
		currentTimeXZ=0;
 		startTimeXZ=SystemClock.uptimeMillis();
	}

	public void sphereMovementXZ(){
		
		
		// calculate ball movement
    	if(!isBouncing){

    	fxParalel = (float) (mSphereMass*9.8*Math.sin(Math.toRadians(surfaceZrotate)));
    	fzParalel = (float) (mSphereMass*9.8*Math.sin(Math.toRadians(surfaceXrotate)));

    	mxAccaloration = fxParalel/mSphereMass;
    	mzAccaloration = fzParalel/mSphereMass;
             	
    	mSphere_x=mSphere_xOld +(float) (0.5*mxAccaloration*Math.pow(currentTimeXZ, 2));
    	mSphere_z=mSphere_zOld +(float) (0.5*mzAccaloration*Math.pow(currentTimeXZ, 2));
    	
		
		mSphere_xOld=mSphere_x;
		mSphere_zOld=mSphere_z;
		
    	}else{
    	
    	// sphere rotation
    			
    	fxParalel = (float) (mSphereMass*9.8*Math.sin(Math.toRadians(surfaceZrotate)));
    	fzParalel = (float) (mSphereMass*9.8*Math.sin(Math.toRadians(surfaceXrotate)));

    	mxAccaloration = fxParalel/mSphereMass;
    	mzAccaloration = fzParalel/mSphereMass;
    	
    	/*mSphere_x=mSphere_xOld +(float) (0.1*mxAccaloration*Math.pow(currentTimeY, 2));
    	mSphere_z=mSphere_zOld +(float) (0.1*mzAccaloration*Math.pow(currentTimeY, 2));
    	
		
		mSphere_xOld=mSphere_x;
		mSphere_zOld=mSphere_z;*/
    	
    	}

    	
    		
    	
		
	}

	public void  sphereFalling(){
		 
		sphereRaduisDistance = (float) Math.sqrt(Math.pow(mSphere_x, 2)+Math.pow(mSphere_z, 2));
		    
		 	if(sphereRaduisDistance>diskRaduis){
		 		if(onTheAir)diskHitedByUser=false;
		 		if(isFalling==false){
		 			System.out.println("Game over "+sphereRaduisDistance);
		 			startTimeY=SystemClock.uptimeMillis();isFalling=true;
		        	isPlaying=false;
		        	gameActivity.onFinishRound();
		 		}
		 		timeMilisecY=(SystemClock.uptimeMillis()-startTimeY)/1000f;
		 		mSphere_y = (float) (-0.5f*gForce*Math.pow(timeMilisecY, 2));
		 	}
	}

	public  void sphereRoatation(){
		
		// calc surface normal
    	float p1x = (float) (Math.cos(Math.toRadians(-surfaceZrotate)));
    	float p1y = (float) (Math.sin(Math.toRadians(-surfaceZrotate)));
    	float p1z = 0;

    	float p2x = (float) (Math.cos(Math.toRadians(surfaceXrotate)));
    	float p2y = (float) (Math.sin(Math.toRadians(surfaceXrotate)));
    	float p2z = 0;
    	
	    float coefficients[] = findSurfaceEquastion(p1x,p1y,p1z,p2z,p2y,p2x,0,0,0);
	    
	 // Find rotation vector
	 	float normal1[]={0,1,0};
	 	float normal2[]=normlizeVector(new float[]{0,0,0}, coefficients);
	 	
	 	float ballTotalVelocity=0;
	 	if(!isBouncing)
        ballTotalVelocity = (float) Math.sqrt(Math.pow(mxAccaloration*currentTimeXZ, 2)+Math.pow(mzAccaloration*currentTimeXZ, 2));
	 	else
	    ballTotalVelocity = (float) Math.sqrt(Math.pow(mxAccaloration*currentTimeY, 2)+Math.pow(mzAccaloration*currentTimeY, 2));

	 		
		// Calc rotation speed
        float angleInDegrees = (360.0f /1000.0f)* ( ballTotalVelocity*1000);  
        
        float cross[] =crossProduct(normal1,normal2);
        
        if(ballTotalVelocity>0.15){
        sphere.rotate(angleInDegrees, cross[0], cross[1],cross[2]);
        }
        
	}

	// used to draw light source point when build light to the scene, unused in play mode .
	@SuppressWarnings("unused")
	private void drawLight()
	{
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");
        
		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);  
		
		// Pass in the transformation matrix.
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}
	
   public float[] crossProduct(float A[],float B[]){
	  
	   return new float[]{A[1]*B[2]-A[2]*B[1],A[2]*B[0]-A[0]*B[2],A[0]*B[1]-A[1]*B[0]};
   }

   public float[] normlizeVector(float vectorStart[],float vectorEnd[]){
		
		float size=(float) Math.sqrt(Math.pow(vectorEnd[0]-vectorStart[0], 2)+Math.pow(vectorEnd[1]-vectorStart[1], 2)+Math.pow(vectorEnd[2]-vectorStart[2], 2));
		
		return new float[]{(vectorEnd[0]-vectorStart[0])/size,(vectorEnd[1]-vectorStart[1])/size,(vectorEnd[2]-vectorStart[2])/size};
	}

   public float dotProduct(float v1[],float v2[]){			    
		return Vector.dot(v1, v2);
	}
   
   public float[] findSurfaceEquastion(float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3){
		   // given 3 points return equation  ax+by+cz=d   of triangle surface (part of the mesh)
		   
		   //1. converting the 3 points to 2 vectors (subtract v3-v1,v2-v1)
		   float v1[] = {x3-x1,y3-y1,z3-z1};
		   float v2[] = {x2-x1,y2-y1,z2-z1};

		   //2. find cross product of the vectors which is the normal/perpendicular vector to v1,v2 to the place .
		   float normal[] = {v1[1]*v2[2]-v1[2]*v2[1],v1[2]*v2[0]-v1[0]*v2[2],v1[0]*v2[1]-v1[1]*v2[0]};
		   
		   float d=normal[0]*x1+normal[1]*y1+normal[2]*z1;
		   
		   return new float[]{normal[0],normal[1],normal[2],d};
	   }
	
	
}