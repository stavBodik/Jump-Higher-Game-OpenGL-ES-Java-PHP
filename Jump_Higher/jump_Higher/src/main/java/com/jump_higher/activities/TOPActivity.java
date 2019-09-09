package com.jump_higher.activities;

import java.util.ArrayList;
import java.util.Arrays;

import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.ServerServiceConnection;
import com.jump_higher.gui.DotsProgressBar;
import com.jump_higher.gui.SingleRowTop50Layout;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
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
* This Activity class used to show users ranks UI components in top rank list.
*/
public class TOPActivity extends Activity {

	//GUI
	private RelativeLayout mainLayoutwithBackGrounVideo;
	private LinearLayout mainLayout;
	private LinearLayout scrollLayout;
	private ScrollView scrollSV;
	private int screenW,screenH;
	private Runnable myRankThread;
	private ArrayList<SingleRowTop50Layout> rowsViews = new ArrayList<SingleRowTop50Layout>();
	private Button myRankBT;
	private boolean isTopRank=true;
	private int loggedInUserLayOutRowOffset=0;
	private int singelRowHeight;
	
	//popup
	private PopupWindow popUploading ;
    private DotsProgressBar popUploadingLayOut;
	
	// connecting with server
	private  BroadcastReceiver mReceiver;
    protected ServiceConnection serviceConnection;
	private IntentFilter intentFilter = new IntentFilter();
		
	// this boolean used for loading layout , if for some reason there is error from server do not dismiss
	// loading popup after receive all profile images
    boolean isError=false;

  //Application manager
    private ApplicationManager manager;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // display dimensions
		manager = ApplicationManager.getInstance();
        screenW = manager.getScreenW();
        screenH = manager.getScreenH();
		manager.setSwitchActivity(false);
		singelRowHeight=(int) (screenH*0.05f);

		int tvSIZE=(int) ((100/1920.0)*screenH);

		// popup loading
	    popUploading = new PopupWindow(this);
	    popUploading.setBackgroundDrawable(null);
	    popUploadingLayOut = new DotsProgressBar(this,screenW,screenH);
	    popUploadingLayOut.setGravity(Gravity.CENTER);
	    popUploadingLayOut.setStopAnimation(false);
	    popUploading.setContentView(popUploadingLayOut);
	    
	    
	    popUploadingLayOut.post(new Runnable() {
		    public void run() {
		    	popUploading.showAtLocation(popUploadingLayOut, Gravity.CENTER,screenW/2, screenH/2);
		    	popUploading.update(screenW/2, screenH/2, screenW,screenH);	
			    }
		});
		
	  
		mainLayout = new LinearLayout(this);
		mainLayout.setId(0);
		mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams mainLayOutParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	
        mainLayoutwithBackGrounVideo = new RelativeLayout(this);
		mainLayoutwithBackGrounVideo.setBackgroundColor(Color.BLACK);
        mainLayoutwithBackGrounVideo.setGravity(Gravity.CENTER);
	    mainLayoutwithBackGrounVideo.addView(ApplicationManager.getInstance().getBackGroundVideoLayOut(this),new LayoutParams(screenW,screenH));

      	    
        scrollLayout = new LinearLayout(this);
        scrollLayout.setId(1);
        scrollLayout.setOrientation(LinearLayout.VERTICAL);
        scrollLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams scrollLayoutParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.9f),(int)(screenH*0.8f)));
        
        
        scrollSV = new ScrollView(this);
        scrollSV.setId(2);
        scrollSV.setLayoutParams(new LayoutParams((int) (screenW*0.9f),(int)(screenH*0.8f)));
        scrollSV.setBackgroundColor(Color.WHITE);
        scrollSV.getBackground().setAlpha(40);
        scrollSV.addView(scrollLayout,scrollLayoutParams);
        
        mainLayout.addView(scrollSV);
        
        
        TextView seperateline = new TextView(this);
        seperateline.setId(3);
        seperateline.setBackgroundDrawable(null);
        seperateline.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,20));
        
        
        mainLayout.addView(seperateline);

        final Animation animation = new AlphaAnimation(120, 0); // Change alpha from fully visible to invisible
        animation.setDuration(1); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        
        myRankBT = new Button(this);
        myRankBT.setId(4);
        myRankBT.setTextColor(Color.WHITE);
        myRankBT.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getButtonTextSize());
        myRankBT.setVisibility(View.INVISIBLE);
		myRankBT.setText(getString(R.string.myrank));
        myRankBT.setBackgroundColor(Color.WHITE);
        myRankBT.getBackground().setAlpha(90);
        RelativeLayout.LayoutParams myRankBTParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.6f),tvSIZE+20)); 
        myRankBTParams.addRule(RelativeLayout.BELOW,seperateline.getId());
        myRankBTParams.topMargin=20;
        
        
        myRankBT.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				myRankBT.startAnimation(animation);
				Handler loginHandler = new Handler();
		        loginHandler.postDelayed(myRankThread, 300);
				
			}
		});
        
        myRankThread = new Runnable()
        {
            @SuppressLint("NewApi")
			@Override
            public void run()
            {
            	myRankBT.clearAnimation();
            	isError=false;
            	
            	popUploadingLayOut.setStopAnimation(false);
            	
            	rowsViews.clear();
            	scrollLayout.removeAllViews();
            	
            	  popUploadingLayOut.post(new Runnable() {
          		    public void run() {
          		    	popUploading.showAtLocation(popUploadingLayOut, Gravity.CENTER,screenW/2, screenH/2);
          		    	popUploading.update(screenW/2, screenH/2, screenW,screenH);	
          			    }
          		});
            	  
            	if(isTopRank){
            		isTopRank=false;
            		myRankBT.setText(getString(R.string.toprank));
            		ServerServiceConnection.serverService.getTOP50(true,ApplicationManager.getInstance().getLoggedInUser().getRank());
            	}else{
            		myRankBT.setText(getString(R.string.myrank));
            		isTopRank=true;
            		ServerServiceConnection.serverService.getTOP50(false,0);
            	}
            }
         };

        mainLayout.addView(myRankBT,myRankBTParams);
        
        mainLayoutwithBackGrounVideo.addView(mainLayout,mainLayOutParams);

		setContentView(mainLayoutwithBackGrounVideo,mainLayOutParams);        

		ServerServiceConnection.serverService.getTOP50(false,0);
		
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 


	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = new Intent(TOPActivity.this,MenuActivity.class);
		mainLayoutwithBackGrounVideo.removeAllViews();
		manager.setSwitchActivity(true);
		startActivity(i);
		finish();
	}
	
	public void reciveFromServer(){
		intentFilter.addAction("android.intent.action.TOP50");
		intentFilter.addAction("android.intent.action.SAVESERVER");

		mReceiver = new BroadcastReceiver() {
       	 
 			@SuppressLint("NewApi")
			@Override
 			public void onReceive(Context context, Intent intent) {
            // get result from server
 				
 		    if(intent.getAction().equals("android.intent.action.TOP50")){
 		    	
 		    	System.out.println("android.intent.action.TOP50");
 		    	
				int responeCode = intent.getIntExtra("HTTPRESPONE_CODE", 0);
				System.out.println("TOP 50 RES CODE : "+responeCode);
                if(responeCode==200){
                	String rankTable = intent.getStringExtra("rankTable");

                	System.out.println("TABLE :\n"+rankTable);

                	String rows[] = rankTable.split("\\$");
                	//each row has SELECT users.id,rank,username,level,time,country


                	for(int i=0; i<rows.length; i++){
                    	
                		String rowArr[] = rows[i].split(",");
                		
                    	System.out.println(Arrays.toString(rowArr));

                		SingleRowTop50Layout row = new SingleRowTop50Layout(TOPActivity.this,screenW,screenH);

                    	row.setBackgroundColor(Color.WHITE);
                    	row.getBackground().setAlpha(80);
                    	if(Integer.parseInt(rowArr[1])==ApplicationManager.getInstance().getLoggedInUser().getUserID()){
                    		row.setBackgroundColor(Color.WHITE);
                        	row.getBackground().setAlpha(80);
                    	}
                    	int userID = Integer.parseInt(rowArr[1]);
                    	if(userID==manager.getLoggedInUser().getUserID()){
                    		loggedInUserLayOutRowOffset=i;
                        	row.getBackground().setAlpha(150);
                    	}
                    	
                    	String rowNumber=rowArr[2]+".";
                    	rowNumber=String.format("%-4s", rowNumber).replace(' ', ' ');
                    	row.setRowNumber(rowNumber);
                    	String username = rowArr[3].replaceAll("_", " ");
                    	
                    	if(username.length()>=10)username=username.substring(0,8);
                 	    String level = rowArr[4];
                 	    level = String.format("%-4s", level).replace(' ', ' ');
                    	row.setUserNameText(username);
                    	row.setUserRecordText(getString(R.string.level)+level+" "+getString(R.string.time)+""+rowArr[5].trim());
						
                    	// SET FLAG FOR THIS ROW
						String userFlagShortName=rowArr[6].trim();
			            String uri = "drawable/"+userFlagShortName;
			            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    	row.setFlagIcon(BitmapFactory.decodeResource(context.getResources(), imageResource), context);
                    	
                    	//"Stanislav bodik".substring(0, 10)+" Level: 50 Time: 10:00:00"	
                        TextView seperateline = new TextView(TOPActivity.this);
                        seperateline.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,4));
                        
                        ServerServiceConnection.serverService.getImageFromServer(Integer.parseInt(rowArr[1]), false,false,i);
                        
                        row.setVisibility(View.INVISIBLE);
                        rowsViews.add(row);
                        
                        scrollLayout.addView(seperateline);
                        scrollLayout.addView(row,new LayoutParams(LayoutParams.MATCH_PARENT,singelRowHeight));
                    }
                	
                }else{
                	// error
                	isError=true;
                	popUploadingLayOut.setStopAnimation(true);
                	popUploadingLayOut.setTextColor(Color.RED);
                	popUploadingLayOut.setText(getString(R.string.internetproblem));
                }

 		    }
 		    
 		    
 		    if(intent.getAction().equals("android.intent.action.SAVESERVER")){
 		    	
 		    	System.out.println("android.intent.action.SAVESERVER");
 		    	
 		    	int code = intent.getIntExtra("CODE", 0);
		    	int viewID=intent.getIntExtra("viewID", -1);

 		    	if(code!=0){
 		    		try{
 		    			byte[] byteArray = intent.getByteArrayExtra("image");
 		    			Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
 		    			rowsViews.get(viewID).setProfileImage(bmp, context);
 		    			
 		    		}catch(Exception e){}		
 		    	}else{
 		    		// error in case no image found , default profile image will be displayed
 		    	}
 		    	
 		    	System.out.println((rowsViews.size()-1) +"view id " + viewID);
 		    	System.out.println((rowsViews.size()-1));

 		    	// finish loading all existing rows (max 50)
 		    	if(!isError && viewID==(rowsViews.size()-1)){
                	popUploadingLayOut.setStopAnimation(true);
 		    		popUploading.dismiss();
 		    		for(int i=0; i<rowsViews.size(); i++){
 		    			rowsViews.get(i).setVisibility(View.VISIBLE);
 		    		}
 		    		myRankBT.setVisibility(View.VISIBLE);
 		    		
 		    		if(!isTopRank){
 		    			System.out.println("Scroling");
		        	scrollSV.scrollTo(0, (loggedInUserLayOutRowOffset+1)*(singelRowHeight)); 
 		    		}
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
}
