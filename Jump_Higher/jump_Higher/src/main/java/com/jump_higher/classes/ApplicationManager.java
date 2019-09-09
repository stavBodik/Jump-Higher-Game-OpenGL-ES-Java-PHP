package com.jump_higher.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import com.jump_higher.R;
import com.jump_higher.gui.ProfileLayout;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.VideoView;

import static android.media.MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT;

/**
 * @author Stav Bodik
 * This class used to manage the application , 
 * includes functions for connecting and make decisions between Game,Sound,DB,UI objects. 
 */
public class ApplicationManager {

	// logged in user
	private User loggedInUser;

	//main background sound between activities
	private MediaPlayer gameBackgroundSound;

	 // instance of server service.
 	private ServerService server;
 	
	// Game Manage,information from current game play .
	private static ApplicationManager instance = null;
	private int reachedLevel = 1;
	private float radius;
	private int nextHeight;
	private int rank = 0;
	private String reachedTime = "00:00:00";
	// indicates when sphere glow ends
	private boolean isSphereGlowEnds=false;
    // when sphere cross next level surface
	private boolean isLevelUp;
	private boolean isBouncing=false;

	// GUI Manage
	private ProfileLayout profileL;
	private int screenW,screenH;
	private VideoView mVideoView;
	private int videoCurrentPosition;
	
	// information from server
	private boolean isUserLoggedIn=false;
	private int numberOfRegistartedUsers=0;
	
	// indicates whether activity is on pause , used to manage background music behavior
    private boolean isSwitchActivity;
 	
	protected ApplicationManager() {
	}

	public static ApplicationManager getInstance() {
		if (instance == null) {
			instance = new ApplicationManager();
		}
		return instance;
	}

	public void setBouncing(boolean isBouncing) {
		this.isBouncing = isBouncing;
	}
	
	public boolean isBouncing() {
		return isBouncing;
	}
	
	public void setSphereGlowEnds(boolean isSphereGlowEnds) {
		this.isSphereGlowEnds = isSphereGlowEnds;
	}
	
	public boolean isSphereGlowEnds() {
		return isSphereGlowEnds;
	}
	
	public void setNumberOfRegistartedUsers(int numberOfRegistartedUsers) {
		this.numberOfRegistartedUsers = numberOfRegistartedUsers;
	}
	
	public int getNumberOfRegistartedUsers() {
		return numberOfRegistartedUsers;
	}
	
	public void setServer(ServerService server) {
		this.server = server;
	}
	
	public ServerService getServer() {
		return server;
	}
	
	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
	}
	
	public void setLevelUp(boolean isLevelUp) {
		this.isLevelUp = isLevelUp;
	}
	
	public boolean isLevelUp() {
		return isLevelUp;
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public boolean isUserLoggedIn() {
		return isUserLoggedIn;
	}
	
	public void setUserLoggedIn(boolean isUserLoggedIn) {
		this.isUserLoggedIn = isUserLoggedIn;
	}
	
	public void logOut(){
		if(this.isUserLoggedIn){
			loggedInUser.setUserID(-1);
		}
		this.isUserLoggedIn=false;
	}
	
	public int getReachedLevel() {
		return reachedLevel;
	}
	
	public int getScreenH() {
		return screenH;
	}
	
	public int getScreenW() {
		return screenW;
	}
	
	@SuppressLint("NewApi")
	public void loadScreenProperties(Activity t){
		final DisplayMetrics metrics = new DisplayMetrics(); 
	    Display display = t.getWindowManager().getDefaultDisplay();     
	    Method mGetRawH = null,mGetRawW = null;

        // For JellyBeans and onward
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN){
            display.getRealMetrics(metrics);
            screenW = metrics.widthPixels;
            screenH = metrics.heightPixels;
        }else{
            try {
				mGetRawH = Display.class.getMethod("getRawHeight");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
            try {
				mGetRawW = Display.class.getMethod("getRawWidth");
			} catch (NoSuchMethodException e) {e.printStackTrace();}

            	try {
					screenW = (Integer) mGetRawW.invoke(display);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {e.printStackTrace();
				}
                try {
					screenH = (Integer) mGetRawH.invoke(display);} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {e.printStackTrace();} 
        }
	}
	
	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public int getNextHeight() {
		return nextHeight;
	}

	public void setNextHeight(int nextHeight) {
		this.nextHeight = nextHeight;
	}

	public void setReachedLevel(int level) {
		this.reachedLevel = level;
	}

	public String getReachedTime() {
		return reachedTime.substring(0, 8);
	}

	public void setReachedTime(String time) {
		this.reachedTime = time;
	}

	public int getButtonTextSize(){
		return (int) (screenW/22f);
	}
	
	public int getTVtextSize(){
		return (int) (screenW/30f);

	}

	public String getPlayerName() {
		return loggedInUser.getUserName();
	}

	public void setSwitchActivity(boolean isSwitchActivity) {
		this.isSwitchActivity = isSwitchActivity;
	}
	
	public boolean isSwitchActivity() {
		return isSwitchActivity;
	}
	
	public void startPlayBackgroundMusic(Context context){
		// start play the music only ones when login activity created
		if(gameBackgroundSound==null){
			gameBackgroundSound =MediaPlayer.create(context, R.raw.epicbackground);
			gameBackgroundSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
			gameBackgroundSound.setLooping(true);
			gameBackgroundSound.start();
		}
	}
	
	public void stopPlayBackgroundMusic(Context context){	
			    if(gameBackgroundSound.isPlaying() && gameBackgroundSound!=null){
				gameBackgroundSound.stop();
				gameBackgroundSound.release();
			    gameBackgroundSound=null;
			    }
	}
	
	public void pausePlayBackgroundMusic(Context context){
		
		try{
			if(gameBackgroundSound!=null && gameBackgroundSound.isPlaying())
				gameBackgroundSound.pause();
		}catch(IllegalStateException e){
			e.printStackTrace();
		}
	}
	
	public void resumePlayBackgroundMusic(){		
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
	}
	
	public VideoView getBackGroundVideoLayOut(Context context){
		
		if(mVideoView==null){
		String fileName = "android.resource://" + context.getPackageName() + "/" + R.raw.backgroundv;
	    Uri uri = Uri.parse(fileName); 
	    mVideoView  = new VideoView(context);
	    mVideoView.setVideoURI(uri);
	    mVideoView.start();
	    
	    mVideoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });


		mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				return true;
			}
		});
		
		}
	    return mVideoView;
	    
	}
	
	public void pauseVideo(){
		if(mVideoView!=null && mVideoView.isPlaying()){
		videoCurrentPosition = mVideoView.getCurrentPosition();
		mVideoView.pause();
		}
		
	}
	
	public void resumeVideo(){
		mVideoView.seekTo(videoCurrentPosition);
		mVideoView.start();
	}
	
	public void stopVideo(){
		if(mVideoView.isPlaying()){
		mVideoView.stopPlayback();
		mVideoView=null;
		}
	}
	
	public void setProfileL(ProfileLayout profileL) {
		this.profileL = profileL;
	}
	
	public ProfileLayout getProfileL() {
		return profileL;
	}
	
	public Bitmap getProfileImageFromProfileView(){
		return profileL.getProfileImage();
	}
	
	public Bitmap getProfileImageFromDisk(int userID){
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/JumpHigher";
		String profileImage=Integer.toString(userID)+".png";
		File dir = new File(file_path);
		if (!dir.exists())dir.mkdirs();
		
		File[] listFiles = dir.listFiles();
		for(int i=0; i<listFiles.length; i++){
			if(listFiles[i].getName().equals(Integer.toString(userID)+".png")){
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				Bitmap bitmap = BitmapFactory.decodeFile(file_path+"/"+profileImage, options);
				return bitmap;
			}
		}
		
		return null;
	}
	
	public HashMap<String, String> getCountryNamesHashMap(Context context){
		HashMap<String, String> countryNames = new HashMap<String, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open("country_flag_list.csv"), "UTF-8")); 

			String mLine;
			while ((mLine = reader.readLine()) != null) {
				String splited[] = mLine.split(",");
				countryNames.put(splited[1].toLowerCase(Locale.getDefault()).trim(), splited[0].trim());
			}
		} catch (IOException e) {} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return countryNames;

	}
	
	public void saveProfileImageToDisk(Bitmap bitmap,String userID){
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/JumpHigher";
		File dir = new File(file_path);
		if (!dir.exists())dir.mkdirs();
		File file = new File(dir,userID + ".png");
		FileOutputStream fOut;
		
		try {
			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void updateUserRankAndBestRecordViews(Context context,String[] userInfoArr){
		//userinfoArr= code,userid,username,password,email,country,isloggedin,rank,level,time,totalusers,,responecode
		getLoggedInUser().setRank(Integer.parseInt(userInfoArr[7]));
		setNumberOfRegistartedUsers(Integer.parseInt(userInfoArr[10]));
		getProfileL().updateRankViews();
		getLoggedInUser().setBestLevel(Integer.parseInt(userInfoArr[8]));
		getLoggedInUser().setBestTime(userInfoArr[9], "");
		getProfileL().updateBestRecordView(context);
		System.out.println("UPDATING\n"+Arrays.toString(userInfoArr));
	}
	public ServiceConnection connectActivityToService(Context context){
		
		
		 // Calling startService() first prevents it from being killed on unbind()
		Intent intent = new Intent(context, ServerService.class);
		
		try{
		context.startService(intent);		}
		catch(NullPointerException e ){e.printStackTrace();}

	 
	    // Now connect to it
		ServiceConnection serviceConnection = new ServerServiceConnection() {};

	    boolean result = context.bindService(
	  		
	    		
	      new Intent(context, ServerService.class),
	      serviceConnection,
	      Context.BIND_AUTO_CREATE
	    );

	    if (!result) {
	      throw new RuntimeException("Unable to bind with service.");
	    }else{
	    	System.out.println("BIND SUCCEED");
	    }
	    
	    return serviceConnection;
	}
}