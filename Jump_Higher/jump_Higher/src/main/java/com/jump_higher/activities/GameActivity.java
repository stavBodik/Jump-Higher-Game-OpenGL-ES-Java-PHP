package com.jump_higher.activities;
import java.util.ArrayList;
import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.ServerServiceConnection;
import com.jump_higher.classes.User;
import com.jump_higher.gui.DotsProgressBar;
import com.jump_higher.gui.EnergyProgressBar;
import com.jump_higher.gui.LevelinformationLayout;
import com.jump_higher.gui.ProfileLayout;
import com.jump_higher.opengl.Constants;
import com.jump_higher.opengl.GLRenderer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Stav Bodik
 * This Activity class used to show game play UI components and Holds OpenGL drawing tools and sensors information .
 */
public class GameActivity extends Activity implements SensorEventListener {

	// reference to OpenGL GLSurfaceView and GLRenderer 
	private GLSurfaceView mGLSurfaceView;
	private GLRenderer mRenderer;

	// sensors
	private float mLastX, mLastY, mLastZ; // Values from device orientation sensor
	private boolean mInitialized; // indicates about first installation of sensors
	private Sensor accelerometer; 
	private Sensor magnetometer;
	private SensorManager mSensorManager;
	private float deltaY; // difference between sensor old and current  height of device , used for make sphere jump.
	private final float NOISE = (float) 2.0; //used to filter noise in y axis
	private float oldSurfaceZrotate; // surface rotation degrees at Z Axis
	private float oldSurfaceXrotate; // surface rotation degrees at X Axis
	float[] mGravity = null,mGeomagnetic = null;
	ArrayList<Float>[] rollingAverage;
	private static final int MAX_SAMPLE_SIZE = 8; // used to make average of sensor results , for avoiding noise, more smooth changes between results .

	//GUI Components
	private RelativeLayout mainLayout;
	private LevelinformationLayout levelInformationL;
	private RelativeLayout.LayoutParams levelInformationLParams;
	private ProfileLayout profileL;
	private RelativeLayout.LayoutParams profileLayoutParams;
	private EnergyProgressBar eneryBarLayOut;
	private RelativeLayout.LayoutParams eneryBarLayOutParams;
	private int screenH;
	private int screenW;

	// popups
	private PopupWindow popUploadingGame ;
	private DotsProgressBar popUploadingGameLayOut;

	private PopupWindow popUpStartGame ;
	private LinearLayout popUplayoutStartGame;

	private PopupWindow popUpFinishRound ;
	private LinearLayout popUplayoutFinishRound;
	private TextView finishGameTV ; 

	// threads for running game
	private Runnable gameStartThread;
	private Runnable gameRestartThread;

	// connecting with server
	private   BroadcastReceiver mReceiver;
	protected ServiceConnection serviceConnection;
	private   IntentFilter intentFilter = new IntentFilter();

	// game background sound
	private MediaPlayer gameBackgroundSound;
	private User loggedInUser;

	// indicates whenever activity is onPause
	private boolean isActivityOnPause=false;

	// indicates whenever popUp start game is shown , used to prevent showing popUp 
	//finish round and start game together if user press on home button while game start (onResume).
	private boolean isStartPopUpShowen=false;

	// indicates whenever round finished 
	private boolean isFinishRound=false;
	
	// Indicates whenever this device support open gl es 2 and higher 
	private boolean  supportsEs2=false;
	
	// indicates whenever user got better record score.
	private boolean isRecordBetter=false;
	
	//Application manager
    private ApplicationManager manager;
        
		@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		manager = ApplicationManager.getInstance();
		
		initGameBackgroundMusic();

		loggedInUser=manager.getLoggedInUser();    

		loadGUI();

		loadSensors();
	}

	@Override
	public void onAttachedToWindow() {

		loadOpenGL();
		super.onAttachedToWindow();
	}

	public void initGameBackgroundMusic(){
		gameBackgroundSound =MediaPlayer.create(this, R.raw.game);
		gameBackgroundSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
		gameBackgroundSound.setLooping(true);
		gameBackgroundSound.start();
	}




	public void onOpenGLfinishLoading() {


		runOnUiThread(new Runnable() {
			@SuppressLint("NewApi")
			public void run() {
				popUpStartGame.showAtLocation(popUplayoutStartGame, Gravity.CENTER,screenW/2, screenH/2);
				popUpStartGame.update(screenW/2, screenH/2, screenW,screenH);
				isStartPopUpShowen=true;
			}
		});



		levelInformationL.post(new Runnable() {
			public void run() {
				levelInformationL.setVisibility(View.VISIBLE);
			}
		});

		profileL.post(new Runnable() {
			public void run() {
				profileL.setVisibility(View.VISIBLE);
			}
		});

		eneryBarLayOut.post(new  Runnable() {
			public void run() {
				eneryBarLayOut.setVisibility(View.VISIBLE);
			}
		});
		popUploadingGameLayOut.post(new Runnable() {
			public void run() {
				popUploadingGameLayOut.setStopAnimation(true);
				popUploadingGame.dismiss();
			}
		});

	}

	public void  reciveFromServer(){
		intentFilter.addAction("android.intent.action.UPDATERANK");
		intentFilter.addAction("android.intent.action.GETRANK");
		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// get result from server
				if(intent.getAction().equals("android.intent.action.UPDATERANK")){

					int resPondCode = intent.getIntExtra("HTTPRESPONE_CODE", 0);

					if(resPondCode==200){
						int CODE = intent.getIntExtra("CODE", 0);

						if(CODE==1){
							// update information succeed
							String userinfo = intent.getStringExtra("USERINFO");
							//userinfoArr= code,userid,username,password,email,country,isloggedin,rank,level,time,totalusers,,responecode
							final String[] userinfoArr= userinfo.split(",");

							
							
							// update ui
							if(loggedInUser.getReachedLevel() == loggedInUser.getBestLevel() && (loggedInUser.getReachedTime().compareTo(loggedInUser.getBestTime())<0)){
								// level is the same but time record is better	so rank is better
								loggedInUser.setBestTime(loggedInUser.getReachedTime(),"game activity");
							}else if(loggedInUser.getReachedLevel() > loggedInUser.getBestLevel()){
								// level is bigger then user record so rank is better
								loggedInUser.setBestLevel(loggedInUser.getReachedLevel());
								loggedInUser.setBestTime(loggedInUser.getReachedTime(),"game activity");
							}								
							manager.getProfileL().updateBestRecordView(context);

							loggedInUser.setRank(Integer.parseInt(userinfoArr[7]));
							manager.getProfileL().updateRankViews();
							
							if(isFinishRound){
								finishGameTV.post(new  Runnable() {
									public void run() {
										finishGameTV.setText(getString(R.string.nice)+loggedInUser.getUserName()+" !\n"
												+getString(R.string.newrecord)
												+ getString(R.string.level)+ 
												loggedInUser.getBestLevel()
												+ "\n"+getString(R.string.time)+
												loggedInUser.getBestTime()+
												"\n"+getString(R.string.newrank)+
												userinfoArr[7] + getString(R.string.of) + userinfoArr[10]);
									}
								});

								popUploadingGame.dismiss();
								showFinishRoundPopup();
							}
							

						}else{
							if(isFinishRound){
								//DB error

								runOnUiThread(new Runnable() {
									public void run() {
										finishGameTV.setClickable(true);
										finishGameTV.setText(getString(R.string.cannotupdaterecord));
										popUploadingGame.dismiss();
									}
								});



								showFinishRoundPopup();
							}
						}



					}else{
						if(isFinishRound){


							runOnUiThread(new Runnable() {
								public void run() {
									finishGameTV.setClickable(true);
									finishGameTV.setText(getString(R.string.cannotupdaterecord));
								}
							});


							popUploadingGame.dismiss();
							showFinishRoundPopup();
						}
					}
				}


				if(intent.getAction().equals("android.intent.action.GETRANK")){

					String userInfo = intent.getStringExtra("USERINFO");
					String userInfoArr[] = userInfo.split(",");
					//userinfoArr= code,userid,username,password,email,country,isloggedin,rank,level,time,totalusers,,responecode

					manager.getLoggedInUser().setRank(Integer.parseInt(userInfoArr[7]));
					manager.setNumberOfRegistartedUsers(Integer.parseInt(userInfoArr[10]));
					manager.getProfileL().updateRankViews();


				}


			}
		};
		//registering our receiver
		this.registerReceiver(mReceiver, intentFilter);
	}

	public void onFinishRound(){

		isFinishRound=true;
		
		// update user rank on server only if user reached better score then before
		if(isRecordBetter){
			System.out.println("RECORD IS BETTER");

			// level is the same but time record is better	so rank is better
			showPopUpLoading();
			ServerServiceConnection.serverService.updateRankonFinishRound(loggedInUser.getUserID(), loggedInUser.getReachedTime(), loggedInUser.getReachedLevel());
		}	
		else{
			System.out.println("RECORD IS NOT BETTER");
			isRecordBetter=false;

			// user did not got better record neither in level nor time
			runOnUiThread(new Runnable() {
				public void run() {
					finishGameTV.setText(loggedInUser.getUserName() +getString(R.string.nonewrecord) + loggedInUser.getBestLevel()
							+ getString(R.string.timen) + loggedInUser.getBestTime() );
				}
			});


			popUploadingGame.dismiss();
			showFinishRoundPopup();
		}

	}
	
	public void onLevelUp(){
		
		System.out.println(loggedInUser.getBestLevel()+","+loggedInUser.getBestTime());
		isFinishRound=false;
			// update user rank on server only if user reached better score then before
		if(loggedInUser.getReachedLevel() == loggedInUser.getBestLevel() && (loggedInUser.getReachedTime().compareTo(loggedInUser.getBestTime())<0)){
			System.out.println("RECORD IS BETTER");
			isRecordBetter=true;
			// level is the same but time record is better	so rank is better
			ServerServiceConnection.serverService.updateRankonFinishRound(loggedInUser.getUserID(), loggedInUser.getReachedTime(), loggedInUser.getReachedLevel());
		}else if(loggedInUser.getReachedLevel() > loggedInUser.getBestLevel()){
			System.out.println("RECORD IS BETTER");
			isRecordBetter=true;
			// level is bigger then user record so rank is better
			ServerServiceConnection.serverService.updateRankonFinishRound(loggedInUser.getUserID(), loggedInUser.getReachedTime(), loggedInUser.getReachedLevel());
		}	
	}

	public void showFinishRoundPopup() {



		runOnUiThread(new Runnable() {
			public void run() {
				popUplayoutFinishRound.animate().alpha(200);
				popUpFinishRound.showAtLocation(popUplayoutFinishRound, Gravity.CENTER, screenW / 2, screenH / 2);
				popUpFinishRound.update(screenW / 2, screenH / 2, screenW, screenH);
			}
		});




	}

	@SuppressWarnings("unchecked")
	public void loadSensors(){
		// install sensors 
		rollingAverage=  new ArrayList[3];
		rollingAverage[0] = new ArrayList<Float>();
		rollingAverage[1] = new ArrayList<Float>();
		rollingAverage[2] = new ArrayList<Float>();

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

	}

	@SuppressLint("NewApi")
	public void loadGUI(){

		// display dimensions
		screenW = manager.getScreenW();
		screenH = manager.getScreenH();


		// popup loading
		popUploadingGame = new PopupWindow(this);
		popUploadingGame.setBackgroundDrawable(null);
		popUploadingGameLayOut = new DotsProgressBar(this,screenW,screenH);
		popUploadingGameLayOut.setGravity(Gravity.CENTER);
		popUploadingGameLayOut.setStopAnimation(false);
		popUploadingGame.setContentView(popUploadingGameLayOut);


		// pop up finish game/try again button
		popUpFinishRound = new PopupWindow(this);
		popUpFinishRound.setBackgroundDrawable(null);
		popUplayoutFinishRound = new LinearLayout(this);
		popUplayoutFinishRound.setOrientation(LinearLayout.VERTICAL);
		popUplayoutFinishRound.setBackgroundColor(Color.BLACK);
		popUplayoutFinishRound.getBackground().setAlpha(200);
		popUplayoutFinishRound.setGravity(Gravity.CENTER);

		finishGameTV = new TextView(this);
		finishGameTV.setTextColor(Color.WHITE);
		finishGameTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());

		// this action listener is used when round is finished and cannot connect to server
		finishGameTV.setClickable(false);
		finishGameTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				popUpFinishRound.dismiss();
				showPopUpLoading();
				onFinishRound();																					
			}
		});

		popUplayoutFinishRound.addView(finishGameTV);

		TextView space = new TextView(this);
		RelativeLayout.LayoutParams spaceBParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,10));  
		popUplayoutFinishRound.addView(space,spaceBParams);

		final Button restartB = new Button(this);
		restartB.setTextColor(Color.WHITE);
		restartB.setBackgroundColor(Color.WHITE);
		restartB.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getButtonTextSize());
		restartB.getBackground().setAlpha(30);
		RelativeLayout.LayoutParams restartBeParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));  
		restartB.setText(getString(R.string.bethebest));
		popUplayoutFinishRound.addView(restartB,restartBeParams);

		space = new TextView(this);
		spaceBParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,5));  
		popUplayoutFinishRound.addView(space,spaceBParams);

		final Button backHomeB = new Button(this);
		backHomeB.setTextColor(Color.WHITE);
		backHomeB.setBackgroundColor(Color.WHITE);
		backHomeB.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getButtonTextSize());
		backHomeB.getBackground().setAlpha(30);
		RelativeLayout.LayoutParams backHomeBParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));  
		backHomeB.setText(getString(R.string.finish));
		popUplayoutFinishRound.addView(backHomeB,backHomeBParams);

		backHomeB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GameActivity.this, MenuActivity.class);
				startActivity(intent);	
				finish();
			}
		});



		restartB.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// restart or go back game
				isRecordBetter=false;
				popUplayoutFinishRound.animate().alpha(0);
				Handler roundFinishHandler = new Handler();
				roundFinishHandler.postDelayed(gameRestartThread, 600);
				loggedInUser.setReachedLevel(1);
				loggedInUser.setReachedTime(Constants.NEW_USER_REGISTER_BEST_TIME);

			}
		});

		gameRestartThread = new Runnable()
		{
			@Override
			public void run()
			{
				popUpFinishRound.dismiss();
				mRenderer.setRestartGame(true); 
				finishGameTV.setClickable(false);
			}
		};

		popUpFinishRound.setContentView(popUplayoutFinishRound);


		// pop up layout with start game button
		popUpStartGame = new PopupWindow(this);
		popUpStartGame.setBackgroundDrawable(null);
		popUplayoutStartGame = new LinearLayout(this);
		popUplayoutStartGame.setBackgroundColor(Color.BLACK);
		popUplayoutStartGame.getBackground().setAlpha(200);
		popUplayoutStartGame.setGravity(Gravity.CENTER);

		final Button playB = new Button(this);
		playB.setTextColor(Color.WHITE);
		playB.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getButtonTextSize());
		playB.getBackground().setAlpha(0);
		RelativeLayout.LayoutParams playBeParams = new RelativeLayout.LayoutParams(new LayoutParams(450,450));  
		playB.setText(getString(R.string.start));
		popUplayoutStartGame.addView(playB,playBeParams);



		playB.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// start game
				popUplayoutStartGame.animate().alpha(0);
				Handler gameStartHandler = new Handler();
				gameStartHandler.postDelayed(gameStartThread, 600);

			}
		});

		gameStartThread = new Runnable()
		{
			@Override
			public void run()
			{
				isRecordBetter=false;
				popUpStartGame.dismiss();
				isStartPopUpShowen=false;
				mRenderer.setRestartGame(true);
			}
		};


		popUpStartGame.setContentView(popUplayoutStartGame);

		// main transparent layout over the openGLview
		mainLayout = new RelativeLayout(this);
		mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);

		mainLayout.setId(0);


		profileL = manager.getProfileL();
		profileL.setId(1);
		profileLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenH*0.2));  
		profileLayoutParams.addRule(RelativeLayout.ALIGN_TOP, mainLayout.getId());
		profileL.setVisibility(View.GONE);

		eneryBarLayOut = new EnergyProgressBar(this,screenH);
		eneryBarLayOut.setId(2);
		eneryBarLayOutParams = new RelativeLayout.LayoutParams((int) (screenW*0.05), (int) (screenH*0.5));  
		eneryBarLayOutParams.addRule(RelativeLayout.BELOW,profileL.getId());
		eneryBarLayOutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		eneryBarLayOut.setVisibility(View.GONE);

		levelInformationL = new LevelinformationLayout(this);
		levelInformationL.setId(3);
		levelInformationLParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenH*0.3));  
		levelInformationLParams.addRule(RelativeLayout.ALIGN_BOTTOM, mainLayout.getId());
		levelInformationLParams.topMargin = (int) (screenH-screenH*0.3);
		levelInformationL.setVisibility(View.GONE);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 

	}

	public EnergyProgressBar getEneryBarLayOut() {
		return eneryBarLayOut;
	}

	public void showPopUpLoading(){
		runOnUiThread(new Runnable() {
			public void run() {
				popUploadingGameLayOut.setStopAnimation(false);
				popUploadingGame.showAtLocation(popUploadingGameLayOut, Gravity.CENTER, screenW / 2, screenH / 2);
				popUploadingGame.update(screenW / 2, screenH / 2, screenW, screenH);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();

		popUploadingGameLayOut.setStopAnimation(true);
		if(supportsEs2)
			mRenderer.onPauseGame();

		popUpStartGame.dismiss();
		popUploadingGame.dismiss();
		popUpFinishRound.dismiss();


		try{
			gameBackgroundSound.stop();
			gameBackgroundSound.release();
		}catch(Exception e){
			e.printStackTrace();
		}


		Intent i = new Intent(GameActivity.this,MenuActivity.class);
		startActivity(i);
		finish();


	}
	
	public void loadOpenGL(){

		showPopUpLoading();

		// surface view used to manage the scene of opengl
		mGLSurfaceView = new GLSurfaceView(this);

		// Check if the system supports OpenGL ES 2.0.
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) 
		{
			runOnUiThread(new Runnable() {
				@SuppressLint("NewApi")
				public void run() {
					// Request an OpenGL ES 2.0 compatible context.
					mGLSurfaceView.setEGLContextClientVersion(2);
					mGLSurfaceView.setPreserveEGLContextOnPause(true);
					mRenderer = new GLRenderer(GameActivity.this);
					System.out.println("GL RENDERER LOAD FINISH");
					// Set the renderer to our renderer, defined below.
					mGLSurfaceView.setRenderer(mRenderer);

					// main content is the gl view 
					setContentView(mGLSurfaceView);

					mRenderer.setGameActivity(GameActivity.this);
				}
			});

		}else {

			popUploadingGameLayOut.setStopAnimation(true);
			popUploadingGameLayOut.setTextColor(Color.RED);
			popUploadingGameLayOut.setText(getString(R.string.opengles2notsupported));
		} 



		this.addContentView(mainLayout,new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mainLayout.addView(levelInformationL,levelInformationLParams);
		mainLayout.addView(eneryBarLayOut,eneryBarLayOutParams);
		mainLayout.addView(profileL,profileLayoutParams);

	}



	@Override
	protected void onResume() 
	{

		System.out.println("gameActivity onResume");

		if(supportsEs2)
			mGLSurfaceView.onResume();

		if(isActivityOnPause && !isStartPopUpShowen){
			onFinishRound();
			isActivityOnPause=false;
		}

		// resume sound when user come back from onPause
		try{
			if(!gameBackgroundSound.isPlaying()){
				int length = gameBackgroundSound.getCurrentPosition();
				gameBackgroundSound.seekTo(length);
				gameBackgroundSound.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		super.onResume();
        serviceConnection = manager.connectActivityToService(this);
		reciveFromServer();	
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onPause() 
	{
		isActivityOnPause=true;
		super.onPause();
		if(supportsEs2){
			mGLSurfaceView.onPause();
			mRenderer.onPauseGame();
		}

		if (serviceConnection != null) {
			System.out.println("ServiceConnection Unbinded ");
			ServerServiceConnection.serverService.stopSelf();
			unbindService(serviceConnection);
			serviceConnection = null;	        
		}

		if(mReceiver!=null){
			this.unregisterReceiver(this.mReceiver);
			this.mReceiver.clearAbortBroadcast();
			this.mReceiver=null;
		}

		mSensorManager.unregisterListener(this);

		try{
			if(gameBackgroundSound.isPlaying())
				gameBackgroundSound.pause();
		}catch(IllegalStateException e){
			e.printStackTrace();
		}

	}

	public boolean isActivityOnPause() {
		return isActivityOnPause;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {


		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			mGravity = event.values;
			updateAccalometer(event);  
		}


		if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
			mGeomagnetic = event.values;

		}
		//  mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);

				rollingAverage[0] = roll(rollingAverage[0], orientation[0]);
				rollingAverage[1] = roll(rollingAverage[1], orientation[1]);
				rollingAverage[2] = roll(rollingAverage[2], orientation[2]);

				orientation[0] = averageList(rollingAverage[0]);
				orientation[1] = averageList(rollingAverage[1]);
				orientation[2] = averageList(rollingAverage[2]);

				if(deltaY==0){					    

					if(!sameSign((float)Math.toDegrees(orientation[1]), oldSurfaceXrotate)){
						mRenderer.restTimerXZ();
						if(orientation[1]>0)mRenderer.setDirectionX(-1);
						else mRenderer.setDirectionX(1);
					}
					else if(!sameSign((float)Math.toDegrees(orientation[2]), oldSurfaceZrotate)){
						mRenderer.restTimerXZ();
						if(orientation[2]>0)mRenderer.setDirectionZ(-1);
						else mRenderer.setDirectionZ(1);
					}



					oldSurfaceZrotate=(float) Math.floor(Math.toDegrees(orientation[2]));
					if(oldSurfaceZrotate==0)oldSurfaceZrotate=0.1f;
					oldSurfaceXrotate=(float) Math.floor(Math.toDegrees(orientation[1]));	 
					if(oldSurfaceXrotate==0)oldSurfaceXrotate=0.1f;

					if(mRenderer.isOkToRotate()){
						mRenderer.setSurfaceXrotate(oldSurfaceXrotate);
						mRenderer.setSurfaceZrotate(oldSurfaceZrotate);
					}


				}

			}
		}			
	}

	public void updateAccalometer(SensorEvent event) {

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mInitialized = true;
		} else {

			float deltaX = Math.abs(mLastX - x);

			deltaY = Math.abs(mLastY - y);

			float deltaZ = Math.abs(mLastZ - z);

			if (deltaX < NOISE) deltaX = (float)0.0;
			if (deltaY < NOISE) deltaY = (float)0.0;
			if (deltaZ < NOISE) deltaZ = (float)0.0;
			mLastX = x;
			mLastY = y;
			mLastZ = z;


			if (deltaX > deltaY) {
				// horizontal
			} else if (deltaY > deltaX) {
				//vertical
				mRenderer.setHit(deltaY+deltaX+deltaZ);
			} 
		}

	}

	@SuppressWarnings("unused")
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public ArrayList<Float> roll(ArrayList<Float> list, float newMember){
		if(list.size() == MAX_SAMPLE_SIZE){
			list.remove(0);
		}
		list.add(newMember);
		return list;
	}

	public float averageList(ArrayList<Float> tallyUp){

		float total=0;
		for(float item : tallyUp ){
			total+=item;
		}
		total = total/tallyUp.size();

		return total;
	}

	protected float[] lowPass( float[] input, float[] output ) {

		float ALPHA=0.25f;

		if ( output == null ) return input;     
		for ( int i=0; i<input.length; i++ ) {
			output[i] = output[i] + ALPHA * (input[i] - output[i]);
		}
		return output;
	}

	public boolean sameSign(float f, float g)
	{
		return (f >= 0) ^ (g < 0);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}	

	@Override
	protected void onDestroy() {
			super.onDestroy();
			finish();
		}

	public LevelinformationLayout getLevelInformationL() {
			return levelInformationL;
		}
	    
}
