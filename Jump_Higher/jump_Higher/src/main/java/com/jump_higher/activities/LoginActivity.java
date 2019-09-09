package com.jump_higher.activities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.FlagData;
import com.jump_higher.classes.ServerServiceConnection;
import com.jump_higher.classes.User;
import com.jump_higher.gui.DotsProgressBar;
import com.jump_higher.gui.ProfileLayout;


import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Stav Bodik
 * This Activity class used to show Main login activity UI components , connects with FaceBook and logs in the server .
 */
public class LoginActivity extends Activity {

	// GUI components
	private RelativeLayout mainLayoutwithBackGrounVideo;
	private TextView userNameTV;
	private EditText userNameET;
	private TextView passwordTV;
	private EditText passwordET;
	private Button   loginBT;
	private TextView restoreTV;
	private TextView registerTV;
	private Button   signInFaceBookBT;

	private boolean isPopupShowen=false;
	private DotsProgressBar popUploadingLayOut;
	private PopupWindow popUploading;


    private Runnable loginThread;
    private Runnable loginDelayed;
    private int userID=-1;
	private int screenW,screenH;
	
	// connecting with server
	private   BroadcastReceiver mReceiver;
    protected ServiceConnection serviceConnection;
	private   IntentFilter intentFilter = new IntentFilter();

	//FaceBook login
	private CallbackManager callbackManager;
	private String userNameFB="";
	private String emailFB="";
	private String countryFB="";
	private String profileImageURIFB="";

	//Application manager
    private ApplicationManager manager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// install FaceBook login
		FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logOut();

        
		// load device window display parameters, used to scale images / fonts
        manager = ApplicationManager.getInstance();
        manager.loadScreenProperties(this);
        screenW = manager.getScreenW();
        screenH = manager.getScreenH();
		manager.setSwitchActivity(false);
        
     // create default user
 		if(!ApplicationManager.getInstance().isUserLoggedIn()){
 		User u = new User("1", "1", "1@gmail.com", 1, new FlagData("Israel","IL", R.drawable.il),this);
 		ApplicationManager.getInstance().setLoggedInUser(u);
 		}
     				
		int tvSIZE=(int) ((100/1920.0)*screenH);

		mainLayoutwithBackGrounVideo = new RelativeLayout(this);
		mainLayoutwithBackGrounVideo.setBackgroundColor(Color.BLACK);
		mainLayoutwithBackGrounVideo.setGravity(Gravity.CENTER);
	    mainLayoutwithBackGrounVideo.addView(ApplicationManager.getInstance().getBackGroundVideoLayOut(this),new LayoutParams(screenW,screenH));

		// main transparent layOut
		LinearLayout mainLayout = new LinearLayout(this);
		mainLayout.setId(0);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		mainLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams mainLayOutParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT)); 


	            
        LinearLayout userNameLayout = new LinearLayout(this);
        userNameLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams userNameLayoutParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)); 

        
        userNameTV = new TextView(this);
        userNameTV.setId(1);
        userNameTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());
        userNameTV.setTextColor(Color.WHITE);
        userNameTV.setText(getString(R.string.username));

        userNameLayout.addView(userNameTV);
        
        userNameET = new EditText(this);
        userNameET.setId(2);
        userNameET.setTextColor(Color.WHITE);
        userNameET.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());
        userNameET.setBackgroundColor(Color.WHITE);
        userNameET.getBackground().setAlpha(50);
		userNameET.setSingleLine();
        RelativeLayout.LayoutParams userNameETParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.8f),LayoutParams.WRAP_CONTENT));
        userNameETParams.addRule(RelativeLayout.BELOW,userNameTV.getId());
        userNameLayout.addView(userNameET,userNameETParams);
        mainLayout.addView(userNameLayout,userNameLayoutParams);
        
        LinearLayout passwordLayout = new LinearLayout(this);
        passwordLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams passwordLayoutParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)); 

        
        passwordTV = new TextView(this);
        passwordTV.setId(3);
        passwordTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());
        passwordTV.setTextColor(Color.WHITE);
        passwordTV.setText(getString(R.string.password));
        RelativeLayout.LayoutParams passwordTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)); 
        passwordTVParams.addRule(RelativeLayout.BELOW,userNameET.getId());
        passwordLayout.addView(passwordTV,passwordTVParams);
        
        passwordET = new EditText(this);
        passwordET.setId(4);
        passwordET.setTextColor(Color.WHITE);
        passwordET.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());
        passwordET.setBackgroundColor(Color.WHITE);
        passwordET.getBackground().setAlpha(50);
        passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        RelativeLayout.LayoutParams passwordETParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.8f),LayoutParams.WRAP_CONTENT));
        passwordETParams.addRule(RelativeLayout.BELOW,passwordTV.getId());
        passwordLayout.addView(passwordET,passwordETParams);
        
        mainLayout.addView(passwordLayout,passwordLayoutParams);
        
        
        TextView seperateline = new TextView(this);
        seperateline.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,25));
        mainLayout.addView(seperateline);
        

        final Animation animation = new AlphaAnimation(120, 0); // Change alpha from fully visible to invisible
        animation.setDuration(1); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        
        loginBT = new Button(this);
        loginBT.setId(5);
        loginBT.setTextColor(Color.WHITE);
        loginBT.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getButtonTextSize());
        loginBT.setText(getString(R.string.login));
        loginBT.setBackgroundColor(Color.WHITE);
        loginBT.getBackground().setAlpha(90);
        RelativeLayout.LayoutParams loginBTParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.6f),tvSIZE+20)); 
        loginBTParams.addRule(RelativeLayout.BELOW,passwordET.getId());
		loginBTParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //loginBTParams.topMargin=20;
        //loginBTParams.leftMargin=(int) ((screenW*0.2f)/2);
        mainLayout.addView(loginBT,loginBTParams);
        
        
		
        loginBT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// black pop up on top of menu for showing progress bar and errors
				showPopup();
				
				popUploadingLayOut.setStopAnimation(false);
		        loginBT.startAnimation(animation);
		        Handler loginHandler = new Handler();
		        loginHandler.postDelayed(loginThread, 300);
		        
		        ServerServiceConnection.serverService.login(userNameET.getText().toString(), passwordET.getText().toString());
				
			}
		});
        
        
        loginThread = new Runnable()
        {
            @Override
            public void run()
            {
            	loginBT.clearAnimation();
            }
         };
        

         seperateline = new TextView(this);
         seperateline.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,25));
         mainLayout.addView(seperateline);
         
        restoreTV = new TextView(this);
        restoreTV.setGravity(Gravity.CENTER);
        restoreTV.setId(6);
        restoreTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize());
        restoreTV.setTextColor(Color.WHITE);
        restoreTV.setText(getString(R.string.restoreaccount));
        RelativeLayout.LayoutParams restoreTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)); 
        restoreTVParams.addRule(RelativeLayout.BELOW,loginBT.getId());
		restoreTVParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        restoreTVParams.topMargin=20;
        //restoreTVParams.leftMargin=(int) ((screenW*0.2f)/2);

        restoreTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(LoginActivity.this,RestoreAccountActivity.class);
				manager.setSwitchActivity(true);
				startActivity(i);
				finish();
				mainLayoutwithBackGrounVideo.removeAllViews();

			}
		});
        mainLayout.addView(restoreTV,restoreTVParams);
        
        seperateline = new TextView(this);
        seperateline.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,30));
        mainLayout.addView(seperateline);
        
        
        registerTV = new TextView(this);
        registerTV.setGravity(Gravity.CENTER);
        registerTV.setId(7);
        registerTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,manager.getTVtextSize()*1.3f);
        registerTV.setTextColor(Color.WHITE);
        registerTV.setText(getString(R.string.noaccuntyet));
        RelativeLayout.LayoutParams registerTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)); 
        registerTVParams.addRule(RelativeLayout.BELOW,restoreTV.getId());
        registerTVParams.topMargin=40;
        mainLayout.addView(registerTV,registerTVParams);
        
        registerTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				manager.setSwitchActivity(true);
				Intent intent = new Intent(LoginActivity.this, Register_EditProfileActivity.class);
				startActivity(intent);
				finish();
				mainLayoutwithBackGrounVideo.removeAllViews();

			}
		});
        
        
        seperateline = new TextView(this);
        seperateline.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,30));
        mainLayout.addView(seperateline);
        
        signInFaceBookBT = new Button(this);
        signInFaceBookBT.setId(8);
        signInFaceBookBT.setBackgroundResource(R.drawable.fbsignin);
        RelativeLayout.LayoutParams signInFaceBookBTParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.6f),tvSIZE+20)); 
        signInFaceBookBTParams.addRule(RelativeLayout.BELOW,registerTV.getId());
        signInFaceBookBTParams.topMargin=20;
        //signInFaceBookBTParams.leftMargin=(int) ((screenW*0.2f)/2);
		signInFaceBookBTParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        
        signInFaceBookBT.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        manager.setSwitchActivity(true);
		        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile","email","user_location"));				
			}
		});
        
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult arg0) {

                    	GraphRequest request = GraphRequest.newMeRequest(
        						arg0.getAccessToken(),
        					    new GraphRequest.GraphJSONObjectCallback() {
        					        @Override
        					        public void onCompleted(JSONObject object, GraphResponse response) {
        					        	try {
        									userNameFB=object.get("name").toString();
        								} catch (JSONException e1) {}
        					        	
        					        	try {
        									emailFB=object.get("email").toString();
        								} catch (JSONException e1) {}
        					        	
        							    try {
        									String location = (String)object.getJSONObject("location").get("name").toString();
        									String[] arr = location.split(",");
        									countryFB=arr[arr.length-1].trim();
        								} catch (Exception e) {}
        					      
        							    try {

        							    Profile profile = Profile.getCurrentProfile();
        								profileImageURIFB = profile.getProfilePictureUri(160, 160).toString();
        							    }
        								catch (Exception e) {}
        								

        								//System.out.println(userNameFB+"\n"+emailFB+"\n"+countryFB+"\n"+profileImageURIFB);
        					            Intent i = new Intent(LoginActivity.this,Register_EditProfileActivity.class);
        					            i.putExtra("username", userNameFB);
        					            i.putExtra("email", emailFB);
        					            i.putExtra("country", countryFB);
        					            i.putExtra("profileImageURI", profileImageURIFB);
        						        LoginManager.getInstance().logOut();

        						        manager.setSwitchActivity(true);
        					            startActivity(i);
        					            finish();
        						        mainLayoutwithBackGrounVideo.removeAllViews();

        					        
        					        }
        					    });
        					Bundle parameters = new Bundle();
        					parameters.putString("fields", "name,email,location");
        					request.setParameters(parameters);
        					request.executeAsync();
                    	
                    	
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {
                    	showPopup();
						popUploadingLayOut.setTextColor(Color.RED);
						popUploadingLayOut.setText(getString(R.string.errorface)+"\n"+exception.getMessage());
				        manager.resumeVideo();
                    }
                });
        
      
        
        mainLayout.addView(signInFaceBookBT,signInFaceBookBTParams);
        
        mainLayoutwithBackGrounVideo.addView(mainLayout,mainLayOutParams);
        
        setContentView(mainLayoutwithBackGrounVideo,mainLayOutParams);
        
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 

        
        /////////////DEBUG UNCOMMENT IN ORDER TO LOGIN WITHOUT INTERNET ///////////////
        ProfileLayout profileL = new ProfileLayout(this);
        RelativeLayout.LayoutParams profileLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenH*0.2));
        manager.setProfileL(profileL);
        mainLayoutwithBackGrounVideo.removeAllViews();

        Intent i = new Intent(LoginActivity.this,MenuActivity.class);
        startActivity(i);
        finish();
        /////// END DEBUG ///////////

        
	
	
		}
	
	public void  reciveFromServer(){
		intentFilter.addAction("android.intent.action.LOGIN");
		intentFilter.addAction("android.intent.action.SAVESERVER");

		mReceiver = new BroadcastReceiver() {
       	 
 			@Override
 			public void onReceive(Context context, Intent intent) {
                    // get result from server
 				
				if (intent.getAction().equals("android.intent.action.LOGIN")) {
					int resPondCode = intent.getIntExtra("HTTPRESPONE_CODE", 0);
					
					if(resPondCode==200){
						int CODE = intent.getIntExtra("CODE", 0);
						if(CODE==1){
							//Login succeed 
							String userinfo = intent.getStringExtra("USERINFO");
							//userinfoArr= code,userid,username,password,email,country,isloggedin,rank,level,time,totalusers,,responecode
							String[] userinfoArr= userinfo.split(",");
							userID=Integer.parseInt(userinfoArr[1]);
							// create logged in user
							// get user country flagData
							HashMap<String, String> countryNames = ApplicationManager.getInstance().getCountryNamesHashMap(LoginActivity.this);
							String userFlagShortName=userinfoArr[5];
							String userFlagLongName = countryNames.get(userFlagShortName);
				            String uri = "drawable/"+userFlagShortName;
				            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
							FlagData userFlag = new FlagData(userFlagLongName, userFlagShortName, imageResource);
							User loggedInUser = new User(userinfoArr[2].replaceAll("_", " "), userinfoArr[3], userinfoArr[4], Integer.parseInt(userinfoArr[1]), userFlag, context);
							manager.setLoggedInUser(loggedInUser);
							manager.getLoggedInUser().setRank(Integer.parseInt(userinfoArr[7]));
							manager.getLoggedInUser().setBestLevel(Integer.parseInt(userinfoArr[8]));
							manager.getLoggedInUser().setBestTime(userinfoArr[9],"login");
							manager.setNumberOfRegistartedUsers(Integer.parseInt(userinfoArr[10]));
							
							
							//check if user profile image on disk if yes set image and login,else ask for download from server and wait for response
							Bitmap profileImage = ApplicationManager.getInstance().getProfileImageFromDisk(Integer.parseInt(userinfoArr[1]));
							if(profileImage==null){
								// ask for download from server and wait for another broatcast for response
								ServerServiceConnection.serverService.getImageFromServer(Integer.parseInt(userinfoArr[1]),true,true,-1);
							}else{
								ApplicationManager.getInstance().setUserLoggedIn(true);
								ApplicationManager.getInstance().getLoggedInUser().setProfileImage(profileImage);
								popUploadingLayOut.setStopAnimation(true);
								popUploadingLayOut.setTextColor(Color.WHITE);
								popUploadingLayOut.setText(getString(R.string.loginSecc));
								
								
								
								// go to game main menu as connected user
								Intent t = new Intent(LoginActivity.this,MenuActivity.class);
								manager.setSwitchActivity(true);
						        startActivity(t);
						        finish();
								mainLayoutwithBackGrounVideo.removeAllViews();

							}

						}
						else{
							popUploadingLayOut.setStopAnimation(true);
							popUploadingLayOut.setTextColor(Color.RED);
							popUploadingLayOut.setText(getString(R.string.wronglogin));
						}	
					}else{
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.RED);
						popUploadingLayOut.setText(getString(R.string.internetproblem)+"("+resPondCode+")");
					}
				}
				
				
				if (intent.getAction().equals("android.intent.action.SAVESERVER")) {
					int CODE = intent.getIntExtra("CODE", 0);

					if(CODE==1){						
						Handler loginHandler = new Handler();
				        loginHandler.postDelayed(loginDelayed, 200);
						
					}else{
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.RED);
						popUploadingLayOut.setText(getString(R.string.internetproblem)+"(profile image error)");
					}
					
				
				}
				
				// i did delay for login after download image from server need sec for refresh image
			    loginDelayed = new Runnable()
		        {
		            @Override
		            public void run()
		            {
		            	Bitmap profileImage = ApplicationManager.getInstance().getProfileImageFromDisk(userID);
						ApplicationManager.getInstance().setUserLoggedIn(true);
						ApplicationManager.getInstance().getLoggedInUser().setProfileImage(profileImage);
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.WHITE);
						popUploadingLayOut.setText(getString(R.string.loginSecc));
						
		            	// go to game main menu as connected user
						Intent t = new Intent(LoginActivity.this,MenuActivity.class);
						manager.setSwitchActivity(true);
						startActivity(t);	
				        finish();
						mainLayoutwithBackGrounVideo.removeAllViews();

				        
		            }
		         };

 			}
 		};
 		//registering our receiver
 		this.registerReceiver(mReceiver, intentFilter);
	}

	private void showPopup(){
		if(!isPopupShowen){
			isPopupShowen=true;
			popUploading = new PopupWindow(LoginActivity.this);
		    popUploading.setBackgroundDrawable(null);
		    popUploadingLayOut = new DotsProgressBar(LoginActivity.this,screenW,(int) (screenH*0.2f));
		    popUploading.setContentView(popUploadingLayOut);	
		    
		    popUploadingLayOut.post(new Runnable() {
			    public void run() {
			    	popUploading.showAtLocation(popUploadingLayOut, Gravity.TOP,0, 0);
			    	popUploading.update(0, 0, screenW,(int) (screenH*0.2f));	
				    }
			});
		}
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(newBase);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    callbackManager.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override	
	protected void onResume() {
		super.onResume();
        manager.startPlayBackgroundMusic(this);
        serviceConnection = manager.connectActivityToService(this);
        reciveFromServer();	
       // AppEventsLogger.activateApp(this); //facebook event looger, in comment because the app crash if no connection to facebook.
		System.out.println("SWITCHING ? " + manager.isSwitchActivity());
		if(!manager.isSwitchActivity()){
        manager.resumePlayBackgroundMusic();
        manager.resumeVideo();
		}
		
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
		
		AppEventsLogger.deactivateApp(this);
		if(!manager.isSwitchActivity()){
		manager.pausePlayBackgroundMusic(this);
		manager.pauseVideo();
		}

	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		manager.stopVideo();
		manager.stopPlayBackgroundMusic(this);
	}
	
	public static void showHashKey(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					"com.example.jump_higher", PackageManager.GET_SIGNATURES); //Your package name here
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
			}
		} catch (NameNotFoundException e) {
		} catch (NoSuchAlgorithmException e) {
		}
	}
}
