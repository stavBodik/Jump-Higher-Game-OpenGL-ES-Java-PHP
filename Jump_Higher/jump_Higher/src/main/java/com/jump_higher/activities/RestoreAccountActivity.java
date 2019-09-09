package com.jump_higher.activities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.ServerServiceConnection;
import com.jump_higher.gui.DotsProgressBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Stav Bodik
 * This Activity class used to show Restore information UI components , 
 * Used in case when user forgot his login information 
 */
public class RestoreAccountActivity extends Activity {

	// GUI Components
	private RelativeLayout mainLayoutwithBackGrounVideo;
	private int screenW;
	private int screenH;
	private Button restoreBT;
	private RelativeLayout mainLayout;
	private EditText restoreET;
	private TextView restoreTV;
	private PopupWindow popUploading;
	private DotsProgressBar popUploadingLayOut;

	// connecting with server
	private   BroadcastReceiver mReceiver;
	protected ServiceConnection serviceConnection;
	private   IntentFilter intentFilter = new IntentFilter();

	private boolean isPopupShowen=false;
	private Runnable restoreBTThread;
		
	//Application manager
    private ApplicationManager manager;
    
	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		// display dominations
		manager = ApplicationManager.getInstance();
        screenW = manager.getScreenW();
        screenH = manager.getScreenH();
        manager.setSwitchActivity(false);
		
		int tvSIZE=(int) ((100/1920.0)*screenH);

		mainLayoutwithBackGrounVideo = new RelativeLayout(this);
		mainLayoutwithBackGrounVideo.setBackgroundColor(Color.BLACK);
		mainLayoutwithBackGrounVideo.setGravity(Gravity.CENTER);
	    mainLayoutwithBackGrounVideo.addView(ApplicationManager.getInstance().getBackGroundVideoLayOut(this),new LayoutParams(screenW,screenH));

		
		// main transparent layOut
	    mainLayout = new RelativeLayout(this);
		mainLayout.setId(0);
		mainLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams mainLayOutParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        
        
        restoreTV = new TextView(this);
        restoreTV.setId(1);
        restoreTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());
        restoreTV.setTextColor(Color.WHITE);
        restoreTV.setText(getString(R.string.accountInformationrestore));
        mainLayout.addView(restoreTV);
        
        restoreET = new EditText(this);
        restoreET.setId(2);
        restoreET.setTextColor(Color.WHITE);
        restoreET.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());
        restoreET.setBackgroundColor(Color.WHITE);
        restoreET.getBackground().setAlpha(50);
        RelativeLayout.LayoutParams restoreETParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.8f),tvSIZE)); 
        restoreETParams.addRule(RelativeLayout.BELOW,restoreTV.getId());
        mainLayout.addView(restoreET,restoreETParams);
        
        
        final Animation animation = new AlphaAnimation(120, 0); // Change alpha from fully visible to invisible
        animation.setDuration(1); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        
               
        restoreBT = new Button(this);
        restoreBT.setId(5);
        restoreBT.setTextColor(Color.WHITE);
        restoreBT.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getButtonTextSize());
        restoreBT.setText(getString(R.string.restoreBT));
        restoreBT.setBackgroundColor(Color.WHITE);
        restoreBT.getBackground().setAlpha(90);
        RelativeLayout.LayoutParams restoreBTParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.6f),tvSIZE+20)); 
        restoreBTParams.addRule(RelativeLayout.BELOW,restoreET.getId());
        restoreBTParams.topMargin=20;
        restoreBTParams.leftMargin=(int) ((screenW*0.2f)/2);
        mainLayout.addView(restoreBT,restoreBTParams);
        
        
        restoreBT.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// black pop up on top of menu for showing progress bar and errors
				showPopup();

				if(validateEmail(restoreET.getText().toString()) && !restoreET.getText().toString().isEmpty()){
				
				popUploadingLayOut.setStopAnimation(false);
		        restoreBT.startAnimation(animation);
				
		        Handler restoreBTHandler = new Handler();
		        restoreBTHandler.postDelayed(restoreBTThread, 300);
		        
		        ServerServiceConnection.serverService.isUserEmailFoundOnDB(restoreET.getText().toString());
		        
				}else{
					popUploadingLayOut.setText(getString(R.string.wrongemail));
			    	popUploadingLayOut.setTextColor(Color.RED);
	                restoreBT.clearAnimation();
				}
				
			}
		});
                
        
        restoreBTThread = new Runnable()
        {
            @Override
            public void run()
            {
            	restoreBT.clearAnimation();
            }
         };
         
         
         mainLayoutwithBackGrounVideo.addView(mainLayout,mainLayOutParams);
         
         setContentView(mainLayoutwithBackGrounVideo,mainLayOutParams);
         
	}

	public void  reciveFromServer(){
		intentFilter.addAction("android.intent.action.RESTORE");
		intentFilter.addAction("android.intent.action.EMAIL");

		mReceiver = new BroadcastReceiver() {
       	 
 			@Override
 			public void onReceive(Context context, Intent intent) {
            // check if email exist on server
 				
			if(intent.getAction().equals("android.intent.action.EMAIL")){
				int code = intent.getIntExtra("CODE", 0);
				int responsecode= intent.getIntExtra("HTTPRESPONE_CODE", 0);
				String message = intent.getStringExtra("MSG");
				System.out.println("RESPONE GOT " +responsecode );
				if(responsecode==200){
					if(code==-1){
						// email not exist
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.RED);
						popUploadingLayOut.setText(message);
					}else{
						// email exist now try send email
						String[] messageArray=message.split(",");
						String userName=messageArray[3];
						String password=messageArray[4];
						ServerServiceConnection.serverService.restoreInformation(restoreET.getText().toString(),userName.replaceAll("_", " "),password);
					}
				 
				}else{
					// connection error
					popUploadingLayOut.setStopAnimation(true);
					popUploadingLayOut.setTextColor(Color.RED);
					popUploadingLayOut.setText(message+"("+responsecode+")");
				}
				
				
				
				
			}
 				
 				
			if(intent.getAction().equals("android.intent.action.RESTORE")){
				int code = intent.getIntExtra("CODE", 0);
				
				if(code==1){
					popUploadingLayOut.setStopAnimation(true);
					popUploadingLayOut.setTextColor(Color.WHITE);
					popUploadingLayOut.setText(getString(R.string.restoreSucceed));
					
				}else{
					popUploadingLayOut.setStopAnimation(true);
					popUploadingLayOut.setTextColor(Color.RED);
					popUploadingLayOut.setText(getString(R.string.internetproblem));
				}
				
			}
				
				

 			}
 		};
 		//registering our receiver
 		this.registerReceiver(mReceiver, intentFilter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        serviceConnection = manager.connectActivityToService(this);
        reciveFromServer();
        manager.startPlayBackgroundMusic(this);
      
        if(!manager.isSwitchActivity()){
            manager.resumePlayBackgroundMusic();
            manager.resumeVideo();
    	}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = new Intent(RestoreAccountActivity.this,LoginActivity.class);
		mainLayoutwithBackGrounVideo.removeAllViews();
		manager.setSwitchActivity(true);
		startActivity(i);
		finish();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(isPopupShowen){
			popUploading.dismiss();
			isPopupShowen=false;
		}
		if (serviceConnection != null) {
		    System.out.println("ServiceConnection Unbinded ");
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
	
	public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
	
	private void showPopup(){
		if(!isPopupShowen){
			isPopupShowen=true;
			popUploading = new PopupWindow(RestoreAccountActivity.this);
		    popUploading.setBackgroundDrawable(null);
		    popUploadingLayOut = new DotsProgressBar(RestoreAccountActivity.this,screenW,(int) (screenH*0.2f));
		    popUploading.setContentView(popUploadingLayOut);	
		    
		    popUploadingLayOut.post(new Runnable() {
			    public void run() {
			    	popUploading.showAtLocation(popUploadingLayOut, Gravity.TOP,0, 0);
			    	popUploading.update(0, 0, screenW,(int) (screenH*0.2f));	
				    }
			});
		}
		else{
			popUploadingLayOut.post(new Runnable() {
			    public void run() {
			    	popUploading.showAtLocation(popUploadingLayOut, Gravity.TOP,0, 0);
			    	popUploading.update(0, 0, screenW,(int) (screenH*0.2f));	
				    }
			});
		}
	}
}
