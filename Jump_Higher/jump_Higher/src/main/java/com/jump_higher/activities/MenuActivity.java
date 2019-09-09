package com.jump_higher.activities;

import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.ServerServiceConnection;
import com.jump_higher.gui.HowToPlayScrollView;
import com.jump_higher.gui.ProfileLayout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * @author Stav Bodik
 * This class used to show game menu activity UI components (how to play,play,profile,top 50)
 */
public class MenuActivity extends Activity {

	// GUI Csomponents
	private RelativeLayout mainLayoutwithBackGrounVideo;
	private LinearLayout mainLayout;
	private int screenH,screenW;
	private Runnable animationHowToPLayThread,animationPlayThread,animationProfileThread,animationTOP50Thread;
	private Button howToPlay,playB,profile,top50;
	private PopupWindow popUpHowToPlay;
	private boolean isTutorialPopUpShown=false;
	private boolean isOtherButtonClicked=false;
	
	// connecting with server
	private   BroadcastReceiver mReceiver;
    protected ServiceConnection serviceConnection;
	private   IntentFilter intentFilter = new IntentFilter();
	
	//Application manager
    private ApplicationManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		// display dominations
		manager = ApplicationManager.getInstance();
        screenH = manager.getScreenH();
        screenW = manager.getScreenW();
		manager.setSwitchActivity(false);

        //buttons animation
        final Animation animation = new AlphaAnimation(120, 0); // Change alpha from fully visible to invisible
        animation.setDuration(1); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

         
        mainLayoutwithBackGrounVideo = new RelativeLayout(this);
		mainLayoutwithBackGrounVideo.setBackgroundColor(Color.BLACK);
		mainLayoutwithBackGrounVideo.setGravity(Gravity.CENTER);
	    mainLayoutwithBackGrounVideo.addView(ApplicationManager.getInstance().getBackGroundVideoLayOut(this),new LayoutParams(screenW,screenH));

     // main transparent layOut
 	    mainLayout = new LinearLayout(this);
 		mainLayout.setId(0);
 		mainLayout.setOrientation(LinearLayout.VERTICAL);
 		mainLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams mainLayOutParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));  

     		
        LinearLayout buttonPanel = new LinearLayout(this);
        RelativeLayout.LayoutParams buttonPanelParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenH*0.8));  
        buttonPanel.setOrientation(LinearLayout.VERTICAL);
        buttonPanel.setGravity(Gravity.CENTER);
        
        
        ProfileLayout profileL = new ProfileLayout(this);
        RelativeLayout.LayoutParams profileLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenH*0.2));  
        profileLayoutParams.addRule(RelativeLayout.ALIGN_TOP, mainLayout.getId());
        mainLayout.addView(profileL,profileLayoutParams);
        ApplicationManager.getInstance().setProfileL(profileL);
        
        
        howToPlay = createButton(manager);
        howToPlay.setText(getString(R.string.howtoplay));
        RelativeLayout.LayoutParams howToPlayBeParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));  
        buttonPanel.addView(howToPlay,howToPlayBeParams);
        
        
        
        final ScrollView howToPlayScrollV = new HowToPlayScrollView(this);
        howToPlayScrollV.setBackgroundColor(Color.WHITE);
        howToPlayScrollV.getBackground().setAlpha(80);
        
        popUpHowToPlay = new PopupWindow(this);
        popUpHowToPlay.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popUpHowToPlay.setContentView(howToPlayScrollV);
        popUpHowToPlay.setOutsideTouchable(true);
        popUpHowToPlay.setFocusable(true);



        howToPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				 if(!isOtherButtonClicked){
					 isOtherButtonClicked=true;
					 howToPlay.startAnimation(animation); 
					 Handler loginHandler = new Handler();
					 loginHandler.postDelayed(animationHowToPLayThread, 300); 
						
					 isTutorialPopUpShown=true;
					 popUpHowToPlay.showAtLocation(howToPlayScrollV,Gravity.CENTER,0, 0);
					 popUpHowToPlay.update((int)(screenW*0.95f), (int)(screenH*0.7f));	
				 }
			}
		});
        
        
        TextView space = new TextView(this);
        RelativeLayout.LayoutParams spaceBParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,10));  
        buttonPanel.addView(space,spaceBParams);
        
        playB = createButton(manager);
        playB.setText(getString(R.string.play));
        RelativeLayout.LayoutParams playBBeParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));  
        buttonPanel.addView(playB,playBBeParams);

        space = new TextView(this);
        buttonPanel.addView(space,spaceBParams);
        
        profile = createButton(manager);
        profile.setText(getString(R.string.profile));
        RelativeLayout.LayoutParams profileParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));  
        buttonPanel.addView(profile,profileParams);
        
        space = new TextView(this);
        buttonPanel.addView(space,spaceBParams);
        
        top50 = createButton(manager);
        top50.setText(getString(R.string.TOP50));
        RelativeLayout.LayoutParams top50Params = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));  
        buttonPanel.addView(top50,top50Params);
        
        top50.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {			
			  
				if(!isOtherButtonClicked){
					isOtherButtonClicked=true;
					top50.startAnimation(animation);
					Handler loginHandler = new Handler();
					loginHandler.postDelayed(animationTOP50Thread, 300);   
				}		      

			}
		});

        mainLayout.addView(buttonPanel,buttonPanelParams);
        
        mainLayoutwithBackGrounVideo.addView(mainLayout,mainLayOutParams);

		setContentView(mainLayoutwithBackGrounVideo,mainLayOutParams);
		
		playB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {	
				if(!isOtherButtonClicked){
					isOtherButtonClicked=true;
					playB.startAnimation(animation);
					Handler loginHandler = new Handler();
					loginHandler.postDelayed(animationPlayThread, 300);	
				}
			}
		});

		profile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!isOtherButtonClicked){
				isOtherButtonClicked=true;
				profile.startAnimation(animation);
				Handler loginHandler = new Handler();
			    loginHandler.postDelayed(animationProfileThread, 300);
				}
			}
		});
		
		animationHowToPLayThread = new Runnable()
        {
            @Override
            public void run()
            {
              howToPlay.clearAnimation();	
            }
         };
         
 		animationPlayThread = new Runnable()
        {
            @Override
            public void run()
            {
              playB.clearAnimation();	
              Intent intent = new Intent(MenuActivity.this, GameActivity.class);
			  mainLayout.removeAllViews();
			  mainLayoutwithBackGrounVideo.removeAllViews();
			  startActivity(intent);
			  finish();

            }
         };

        animationProfileThread = new Runnable()
         {
             @Override
             public void run()
             {
             	profile.clearAnimation();	
             	Intent intent = new Intent(MenuActivity.this, Register_EditProfileActivity.class);
             	mainLayout.removeAllViews();
  			    manager.setSwitchActivity(true);
  				mainLayoutwithBackGrounVideo.removeAllViews();
				startActivity(intent);	
				finish();
             }
          };
          
        animationTOP50Thread = new Runnable()
          {
              @Override
              public void run()
              {
              top50.clearAnimation();
              Intent intent = new Intent(MenuActivity.this, TOPActivity.class);
              mainLayout.removeAllViews();
			  manager.setSwitchActivity(true);
			  mainLayoutwithBackGrounVideo.removeAllViews();
  			  startActivity(intent);
  			  finish();		
  			  
              }
           };
	
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 

	}
	
	public void  reciveFromServer(){
		intentFilter.addAction("android.intent.action.UPDATERANK");
		intentFilter.addAction("android.intent.action.GETRANK");
		mReceiver = new BroadcastReceiver() {
       	 
 			@Override
 			public void onReceive(Context context, Intent intent) {
                    
 				    
 				   if(intent.getAction().equals("android.intent.action.GETRANK")){						
 					  String userInfo = intent.getStringExtra("USERINFO");
						String userInfoArr[] = userInfo.split(",");
						//userinfoArr= code,userid,username,password,email,country,isloggedin,rank,level,time,totalusers,,responecode
						manager.updateUserRankAndBestRecordViews(MenuActivity.this, userInfoArr);
					}
				

 			}
 		};
 		//registering our receiver
 		this.registerReceiver(mReceiver, intentFilter);
	}
	@Override
	
	
	protected void onResume() {
		super.onResume();
        manager.startPlayBackgroundMusic(this);
        serviceConnection = manager.connectActivityToService(this);
        reciveFromServer();	
        manager.startPlayBackgroundMusic(this);
        if(!manager.isSwitchActivity()){
    		manager.resumePlayBackgroundMusic();
            manager.resumeVideo();
    	}
	}
	@Override
	
	public void onWindowFocusChanged(boolean hasFocus) {
		if(isTutorialPopUpShown){
			isTutorialPopUpShown=false;
			isOtherButtonClicked=false;
		}
	};
	
	protected void onPause() {
		super.onPause();
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
		
		if(!manager.isSwitchActivity()){
			manager.pausePlayBackgroundMusic(this);
			manager.pauseVideo();
			}
	}
	
	@Override
	public void onBackPressed() {
		
		if(isTutorialPopUpShown){
			isTutorialPopUpShown=false;
			popUpHowToPlay.dismiss();
			return;
		}
		
		super.onBackPressed();
		ApplicationManager.getInstance().logOut();
		Intent i = new Intent(MenuActivity.this,LoginActivity.class);
		mainLayoutwithBackGrounVideo.removeAllViews();
		manager.setSwitchActivity(true);
		startActivity(i);
		finish();
	}
	
	public Button createButton(ApplicationManager m){
		Button b = new Button(this);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX,m.getButtonTextSize());
        b.getBackground().setAlpha(30);
        
        return b;
	}
	
	
}
	
	
	
