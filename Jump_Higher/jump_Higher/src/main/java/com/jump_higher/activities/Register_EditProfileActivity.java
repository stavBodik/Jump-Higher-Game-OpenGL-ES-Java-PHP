package com.jump_higher.activities;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.FlagData;
import com.jump_higher.classes.ServerServiceConnection;
import com.jump_higher.classes.User;
import com.jump_higher.gui.DotsProgressBar;
import com.jump_higher.gui.ProfileLayout;
import com.jump_higher.opengl.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author stav bodik
 * This Activity class used to register account and edit account UI components . 
 */
@SuppressLint({ "DefaultLocale", "ClickableViewAccessibility" })
public class Register_EditProfileActivity extends Activity {

	// UI components
	private RelativeLayout mainLayoutwithBackGrounVideo;
	private LinearLayout mainLayout;
	private TextView userNameTV;
	private EditText userNameET;
	private TextView passwordTV;
	private EditText passwordET;
	private TextView emailTV;
	private EditText emailET;
	private TextView countryTV;
	public  Spinner countrySpinner;
	private TextView profileImageTV;
	public  String selectedCountry;
	private Button progileImageBT;
	private Button   registerBT;
	private Animation registerBTanimation;
	public  RelativeLayout.LayoutParams registerBTParams;
	private PopupWindow popUploading;
	private DotsProgressBar popUploadingLayOut;
	private boolean isPopupShowen=false;
	private int chosedFlagIndex;
	private ArrayList<FlagData> flagViewsArray;
	private ProfileLayout profileL;
	
	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    
	private Runnable registerBTAnimationThread;

	// connecting with server
	private   BroadcastReceiver mReceiver;
    protected ServiceConnection serviceConnection;
	private IntentFilter intentFilter = new IntentFilter();

	private static final int SELECT_PHOTO = 100;
	static int profilImageSize;
	private int registartedUserID=-1;
	
	private boolean isFirstSelect=false;
	private boolean isFirstChange=false;
	private boolean isSelect=false;
	private boolean popUpOpencountryList=false;
    private boolean userLoggedIn=false;
	private User loggedInUser;
	
	private Map<String, String> countryNames;

    private int screenW,screenH;
	
    //Application manager
    private ApplicationManager manager;

	@SuppressLint({ "NewApi", "DefaultLocale" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        		
		userLoggedIn =ApplicationManager.getInstance().isUserLoggedIn();
		loggedInUser = null;
		if(userLoggedIn){
		loggedInUser = ApplicationManager.getInstance().getLoggedInUser();
		}
		
		// display dominations
		manager = ApplicationManager.getInstance();
        screenW = manager.getScreenW();
        screenH = manager.getScreenH();	
		manager.setSwitchActivity(false);

		int textSize=12;
		int textSize1=15;
		final int tvSIZE=LayoutParams.WRAP_CONTENT;
		profilImageSize=(int) (screenH*0.3f*0.4f);

		
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
        
        
        if(userLoggedIn){
        	profileL = ApplicationManager.getInstance().getProfileL();
        	profileL.setId(666);
            RelativeLayout.LayoutParams profileLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenH*0.2));  
            profileLayoutParams.addRule(RelativeLayout.ALIGN_TOP, mainLayout.getId());
            mainLayout.addView(profileL,profileLayoutParams);

            profileL.getProfileIV().setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					runOnUiThread(new  Runnable() {
					public void run() {
						popUploadingLayOut.setStopAnimation(false);
					}
				});
				
				final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				
				new Handler().postDelayed(new Runnable(){
			        @Override
			        public void run() {      
			        	manager.setSwitchActivity(true);
			        	photoPickerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
						startActivityForResult(photoPickerIntent, SELECT_PHOTO);
			        }
			    }, 2000);				
				}
			});
        }
        
        RelativeLayout buttonPanel = new RelativeLayout(this);
        RelativeLayout.LayoutParams buttonPanelParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenH*0.8));  
        //buttonPanel.setOrientation(LinearLayout.VERTICAL);
        buttonPanel.setGravity(Gravity.CENTER);
        
        userNameTV = new TextView(this);
        userNameTV.setId(1);
        userNameTV.setTextSize(textSize1);
        userNameTV.setTextColor(Color.WHITE);
        userNameTV.setText(getString(R.string.username));
       
        buttonPanel.addView(userNameTV);
        
        userNameET = new EditText(this);
        userNameET.setId(2);
        userNameET.setTextColor(Color.WHITE);
        userNameET.setTextSize(textSize);
        userNameET.setBackgroundColor(Color.WHITE);

        if(userLoggedIn){
        	userNameET.setText(loggedInUser.getUserName().toString());
        	userNameET.setEnabled(false);
        }
        userNameET.getBackground().setAlpha(50);
        RelativeLayout.LayoutParams userNameETParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.8f),tvSIZE));
        userNameETParams.addRule(RelativeLayout.BELOW,userNameTV.getId());
        buttonPanel.addView(userNameET,userNameETParams);
        
        
        passwordTV = new TextView(this);
        passwordTV.setId(3);
        passwordTV.setTextSize(textSize1);
        passwordTV.setTextColor(Color.WHITE);
        passwordTV.setText(getString(R.string.password));
        RelativeLayout.LayoutParams passwordTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)); 
        passwordTVParams.addRule(RelativeLayout.BELOW,userNameET.getId());
        buttonPanel.addView(passwordTV,passwordTVParams);
        
        passwordET = new EditText(this);
        passwordET.setId(4);
        passwordET.setTextColor(Color.WHITE);
        passwordET.setTextSize(textSize);
        passwordET.setBackgroundColor(Color.WHITE);
        if(userLoggedIn)passwordET.setText(loggedInUser.getPassword().toString());
        passwordET.getBackground().setAlpha(50);
        passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        RelativeLayout.LayoutParams passwordETParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.8f),tvSIZE)); 
        passwordETParams.addRule(RelativeLayout.BELOW,passwordTV.getId());
        buttonPanel.addView(passwordET,passwordETParams);
        
        // email
        emailTV = new TextView(this);
        emailTV.setId(5);
        emailTV.setTextSize(textSize1);
        emailTV.setTextColor(Color.WHITE);
        emailTV.setText(getString(R.string.email));
        RelativeLayout.LayoutParams emailTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)); 
        emailTVParams.addRule(RelativeLayout.BELOW,passwordET.getId());
        buttonPanel.addView(emailTV,emailTVParams);
        
        emailET = new EditText(this);
        emailET.setId(6);
        emailET.setTextColor(Color.WHITE);
        emailET.setTextSize(textSize);
        emailET.setBackgroundColor(Color.WHITE);
        if(userLoggedIn)emailET.setText(loggedInUser.getEmail().toString());
        emailET.getBackground().setAlpha(50);
        RelativeLayout.LayoutParams emailETParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.8f),tvSIZE)); 
        emailETParams.addRule(RelativeLayout.BELOW,emailTV.getId());
        buttonPanel.addView(emailET,emailETParams);
        
        // country        
        countryTV = new TextView(this);
        countryTV.setId(7);
        countryTV.setTextSize(textSize1);
        countryTV.setTextColor(Color.WHITE);
        countryTV.setText(getString(R.string.country));
        RelativeLayout.LayoutParams countryTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)); 
        countryTVParams.addRule(RelativeLayout.BELOW,emailET.getId());
        buttonPanel.addView(countryTV,countryTVParams);
        
        
        countryNames = ApplicationManager.getInstance().getCountryNamesHashMap(this);    
        flagViewsArray = new ArrayList<FlagData>();
        Iterator<Entry<String, String>> it = countryNames.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> pair = it.next();
            String uri = "drawable/"+(String)pair.getKey();
            // int imageResource = R.drawable.icon;
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            flagViewsArray.add(new FlagData((String)pair.getValue(),(String)pair.getKey(), imageResource));

        }
        
        selectedCountry = flagViewsArray.get(0).getCountryName();
        

        
        countrySpinner = new Spinner(this);
        
        try {
            java.lang.reflect.Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(countrySpinner);
            // Set popupWindow height 
            popupWindow.setHeight((int)(screenH*0.8f));
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
        }
        
        countrySpinner.setDropDownVerticalOffset(-screenH/2);
        countrySpinner.setBackgroundColor(Color.WHITE);
        countrySpinner.getBackground().setAlpha(0);
        countrySpinner.getPopupBackground().setAlpha(0);
        countrySpinner.setId(8);
        FlagArrayAdapter spinnerArrayAdapter = new FlagArrayAdapter(this,flagViewsArray); //selected item will look like a spinner set from XML
        countrySpinner.setAdapter(spinnerArrayAdapter);
        RelativeLayout.LayoutParams spinnerParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.8f),tvSIZE)); 
        spinnerParams.addRule(RelativeLayout.BELOW,countryTV.getId());
        buttonPanel.addView(countrySpinner,spinnerParams);
        
        if(userLoggedIn){
        	try{
                String country = loggedInUser.getFlagData().getCountryName();
                int i;
                for(i=0; i<flagViewsArray.size(); i++){
               	 if(flagViewsArray.get(i).getCountryName().toLowerCase().equals(country.toLowerCase())){
                    countrySpinner.setSelection(i);
                    chosedFlagIndex=i;
                    selectedCountry=flagViewsArray.get(i).getCountryName();
               	 break;
               	 }
                }
                }catch(Exception e){}
        
        
        }

        countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				if(!isFirstSelect)isFirstSelect=true;
				else{
					isSelect=true;
					userNameTV.setVisibility(View.VISIBLE);
			    	userNameET.setVisibility(View.VISIBLE);
			    	passwordTV.setVisibility(View.VISIBLE);
			    	passwordET.setVisibility(View.VISIBLE);
			    	emailTV.setVisibility(View.VISIBLE);
			    	emailET.setVisibility(View.VISIBLE);
			    	if(!userLoggedIn){
			    	profileImageTV.setVisibility(View.VISIBLE);
			    	progileImageBT.setVisibility(View.VISIBLE);
			    	}
			    	countryTV.setVisibility(View.VISIBLE);
			    	countrySpinner.setAlpha(1);
			    	registerBT.setVisibility(View.VISIBLE);
			    	
			    	TextView textView = (TextView)countrySpinner.getSelectedView();
			    	String result = textView.getText().toString();
			    	
			    	selectedCountry=result;
			    	chosedFlagIndex=countrySpinner.getSelectedView().getId();
			    	isSelect=false;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {				
			}
		});
        
        countrySpinner.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if(!isSelect && event.getAction()==0){
					
				// close keyboard if its on
				View view = Register_EditProfileActivity.this.getCurrentFocus();
				if (view != null) {  
				    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
				countrySpinner.performClick();

				userNameTV.setVisibility(View.GONE);
		    	userNameET.setVisibility(View.GONE);
		    	passwordTV.setVisibility(View.GONE);
		    	passwordET.setVisibility(View.GONE);
		    	emailTV.setVisibility(View.GONE);
		    	emailET.setVisibility(View.GONE);
		    	if(!userLoggedIn){
		    	profileImageTV.setVisibility(View.GONE);
		    	progileImageBT.setVisibility(View.GONE);
		    	}
		    	countryTV.setVisibility(View.GONE);
		    	//countrySpinner.setAlpha(0);
		    	registerBT.setVisibility(View.GONE);
				}
				
				return false;
			}
		});
        
        
        // profile image
        if(!userLoggedIn){
        profileImageTV = new TextView(this);
        profileImageTV.setId(9);
        profileImageTV.setTextSize(textSize1);
        profileImageTV.setTextColor(Color.WHITE);
        profileImageTV.setText("Profile image : ");
        RelativeLayout.LayoutParams profileImageTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)); 
        profileImageTVParams.addRule(RelativeLayout.BELOW,countrySpinner.getId());
        buttonPanel.addView(profileImageTV,profileImageTVParams);
        
        progileImageBT = new Button(this);
        progileImageBT.setId(10);
        progileImageBT.setBackgroundResource(R.drawable.anonymprofil);
        RelativeLayout.LayoutParams profilImageParams = new RelativeLayout.LayoutParams(new LayoutParams(profilImageSize,profilImageSize));         
        profilImageParams.addRule(RelativeLayout.BELOW,profileImageTV.getId());
        buttonPanel.addView(progileImageBT,profilImageParams);
		//showPopup();

        
        progileImageBT.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// popup loading
				
				runOnUiThread(new  Runnable() {
					public void run() {
						popUploadingLayOut.setStopAnimation(false);
					}
				});
				
				final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				
				new Handler().postDelayed(new Runnable(){
			        @Override
			        public void run() {  
						manager.setSwitchActivity(true);
			        	photoPickerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
						startActivityForResult(photoPickerIntent, SELECT_PHOTO);
			        }
			    }, 2000);
				
				
				

			}
		});
        }

        //Typeface typeFace=Typeface.createFromAsset(getAssets(),"fontawesome-webfont.ttf");
        registerBTanimation = new AlphaAnimation(120, 0); // Change alpha from fully visible to invisible
        registerBTanimation.setDuration(1); // duration - half a second
        registerBTanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        registerBTanimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        registerBTanimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        
        registerBT = new Button(this);
        registerBT.setId(11);
        registerBT.setTextColor(Color.WHITE);
        registerBT.setTextSize(textSize+5);
        registerBT.setText(getString(R.string.register));
        if(userLoggedIn)registerBT.setText(getString(R.string.save));
        registerBT.setBackgroundColor(Color.WHITE);
        registerBT.getBackground().setAlpha(90);
        registerBTParams = new RelativeLayout.LayoutParams(new LayoutParams((int) (screenW*0.6f),tvSIZE));
        if(!userLoggedIn)
        registerBTParams.addRule(RelativeLayout.BELOW,progileImageBT.getId());
        else{
            registerBTParams.addRule(RelativeLayout.BELOW,countrySpinner.getId());
        }
        
        registerBTParams.topMargin=40;
        registerBTParams.leftMargin=(int) ((screenW*0.2f)/2);
        buttonPanel.addView(registerBT,registerBTParams);
        
        registerBT.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View arg0) {				
				String name = userNameET.getText().toString();
				String password = passwordET.getText().toString();
				String email = emailET.getText().toString();
				// popup loading
				showPopup();
			    
                registerBT.startAnimation(registerBTanimation);
                Handler loginHandler = new Handler();
		        loginHandler.postDelayed(registerBTAnimationThread, 300);

                if(name.isEmpty() | password.isEmpty() | email.isEmpty()){
			    	popUploadingLayOut.setText(getString(R.string.filloutall));
			    	popUploadingLayOut.setTextColor(Color.RED);
	                registerBT.clearAnimation();
                }else if(name.length()>Constants.MAX_USERNAME_PASSWORD | password.length()>Constants.MAX_USERNAME_PASSWORD){
                	popUploadingLayOut.setText(getString(R.string.errorusernamelength));
			    	popUploadingLayOut.setTextColor(Color.RED);
	                registerBT.clearAnimation();
                }
                /*else if(!isAlphaNumeric(name)){
                	popUploadingLayOut.setText(getString(R.string.alphanomeric));
			    	popUploadingLayOut.setTextColor(Color.RED);
	                registerBT.clearAnimation();
                }*/else if(password.contains(" ") | email.contains(" ")){
                	popUploadingLayOut.setText(getString(R.string.spaceerror));
			    	popUploadingLayOut.setTextColor(Color.RED);
	                registerBT.clearAnimation();
                }
			    else if(!validateEmail(email)){
			    	popUploadingLayOut.setText(getString(R.string.wrongemail));
			    	popUploadingLayOut.setTextColor(Color.RED);
	                registerBT.clearAnimation();
			    }
			    
			    else{
					popUploadingLayOut.setStopAnimation(false);
			    	if(userLoggedIn)ServerServiceConnection.serverService.register_update(name.replaceAll(" ", "_"), password.replaceAll(" ", "_"), email.replaceAll(" ", "_"),flagViewsArray.get(chosedFlagIndex).getShortCountryName(),true,loggedInUser.getUserID());
			    	else ServerServiceConnection.serverService.register_update(name.replaceAll(" ", "_"), password.replaceAll(" ", "_"), email.replaceAll(" ", "_"),flagViewsArray.get(chosedFlagIndex).getShortCountryName(),false,-1);
			    }
			    

				
			}
		});
        
        registerBTAnimationThread = new Runnable()
        {
            @Override
            public void run()
            {
            	registerBT.clearAnimation();
            }
         };
         
         
         
         // try get information  that got from facebook login button
         Bundle extras = getIntent().getExtras();
         if(extras!=null){
         
         // user name
         try{
         userNameET.setText(extras.getString("username"));
         }catch(Exception e){}

         //email
         try{
         emailET.setText(extras.getString("email"));
         }catch(Exception e){}
         
         //country
         try{
         String country = extras.getString("country");
         int i;
         for(i=0; i<flagViewsArray.size(); i++){
        	 if(flagViewsArray.get(i).getCountryName().toLowerCase().equals(country.toLowerCase())){
             countrySpinner.setSelection(i);
             chosedFlagIndex=i;
             selectedCountry=flagViewsArray.get(i).getCountryName();
        	 break;
        	 }
         }
         }catch(Exception e){}
         
         // profile image
         try{
             String imgeURI = extras.getString("profileImageURI");
             getImageFromFaceBookURL(new URL(imgeURI));   
         }catch(Exception e){}
         
           
         
         }
 
        mainLayout.addView(buttonPanel,buttonPanelParams);
        
        mainLayoutwithBackGrounVideo.addView(mainLayout,mainLayOutParams);
		setContentView(mainLayoutwithBackGrounVideo,mainLayOutParams);
		
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 

	}


	@Override
	public void onAttachedToWindow() {

		showPopup();
		super.onAttachedToWindow();
	}



	private synchronized void getImageFromFaceBookURL(final URL profileImageURL) {
	    AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

	        @Override
	        public Bitmap doInBackground(Void... params) {
	            Bitmap profileImageBM = null;
	            URL profileImageURL1=profileImageURL;
	            try {
	            	HttpURLConnection ucon = (HttpURLConnection) profileImageURL1.openConnection();
	                ucon.setInstanceFollowRedirects(false);
	            	URL profileImageURL2 = new URL(ucon.getHeaderField("Location"));
	                profileImageBM = BitmapFactory.decodeStream(profileImageURL2.openConnection().getInputStream());
	            } catch (IOException e1) {
	            } 
	            return profileImageBM;
	        }

	        @SuppressWarnings("deprecation")
			@Override
	        protected void onPostExecute(Bitmap result) {
	        	
	        	if(result!=null){
	            progileImageBT.setBackgroundDrawable(new BitmapDrawable(getApplicationContext().getResources(),result));
	        	}
	       }

	    };
	    task.execute();
	}

	public boolean isAlphaNumeric(String s){
	    String pattern= "^[a-zA-Z0-9 ]*$";
	        if(s.matches(pattern)){
	            return true;
	        }
	        return false;   
	}
	
	public void reciveFromServer(){
		intentFilter.addAction("android.intent.action.REGISTER");
		intentFilter.addAction("android.intent.action.UPLOAD_IMAGE");
        intentFilter.addAction("android.intent.action.NUSERS");
        intentFilter.addAction("android.intent.action.GETRANK");

		mReceiver = new BroadcastReceiver() {
       	 
 			@Override
 			public void onReceive(Context context, Intent intent) {
                    // get result from server
 				
 				System.out.println(intent.getAction());
 				
				if (intent.getAction().equals("android.intent.action.REGISTER")) {
					int code = intent.getIntExtra("CODE", 0);
					int responeCode = intent.getIntExtra("HTTPRESPONE_CODE", 0);

					String message = intent.getStringExtra("MSG");
					// code -1 = email,user exits code 0  Internet/server error
					// , other code is userID after succeed register
					if (code <= 0) {
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.RED);
						if(code==0) popUploadingLayOut.setText(message+"("+responeCode+")");
						else if(code==-1){
							if(!userLoggedIn){
								popUploadingLayOut.setText(getString(R.string.useremailexist));
							}else{
								popUploadingLayOut.setText(getString(R.string.emailexist));
							}
						}
					} else {
						registartedUserID=code;
						// now try upload image
						if(!userLoggedIn)ServerServiceConnection.serverService.uploadUserImage(((BitmapDrawable)progileImageBT.getBackground()).getBitmap(), code+".png");
						else{
							ServerServiceConnection.serverService.uploadUserImage(ApplicationManager.getInstance().getProfileImageFromProfileView(), loggedInUser.getUserID()+".png");
						}
					}
				}
				
				if(intent.getAction().equals("android.intent.action.UPLOAD_IMAGE")){
					int code = intent.getIntExtra("CODE", 0);
					String message = intent.getStringExtra("MSG");
					// code 200 ok from server
					if (code !=200) {
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.RED);
						popUploadingLayOut.setText(message+"("+code+")");
					} else {
						
						// save image on disk
						if(!userLoggedIn)
						ApplicationManager.getInstance().saveProfileImageToDisk(((BitmapDrawable)progileImageBT.getBackground()).getBitmap(), Integer.toString(registartedUserID));
						else{
							ApplicationManager.getInstance().saveProfileImageToDisk(ApplicationManager.getInstance().getProfileImageFromProfileView(),Integer.toString(loggedInUser.getUserID()));
						}
						// create new logged in user
						loggedInUser = new User(userNameET.getText().toString(), passwordET.getText().toString(), emailET.getText().toString(), registartedUserID, flagViewsArray.get(chosedFlagIndex),Register_EditProfileActivity.this);
						if(!userLoggedIn){
						loggedInUser.setProfileImage(((BitmapDrawable)progileImageBT.getBackground()).getBitmap());
						loggedInUser.setBestTime(Constants.NEW_USER_REGISTER_BEST_TIME,"");
						//first time user logged in update rank table
						ServerServiceConnection.serverService.updateRankonFinishRound(loggedInUser.getUserID(),Constants.NEW_USER_REGISTER_BEST_TIME,1);

						}
						else{
							loggedInUser.setProfileImage(ApplicationManager.getInstance().getProfileImageFromProfileView());
						}
						ApplicationManager.getInstance().setLoggedInUser(loggedInUser);
						ApplicationManager.getInstance().setUserLoggedIn(true);
						
						// upload and save image ok , now get number of users
						ServerServiceConnection.serverService.getNumberOfRegistartedUsers();
						
						
						
					}

				}
 					
				if(intent.getAction().equals("android.intent.action.NUSERS")){
					int code = intent.getIntExtra("CODE", 0);
					
					if (code !=0){
						ApplicationManager.getInstance().setNumberOfRegistartedUsers(code);
						ApplicationManager.getInstance().getLoggedInUser().setRank(ApplicationManager.getInstance().getNumberOfRegistartedUsers());
						// image upload and register to server done
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.WHITE);
						if(!userLoggedIn)
						popUploadingLayOut.setText(getString(R.string.registerSecc));
						else popUploadingLayOut.setText(getString(R.string.updateSucced));

						// go to game main menu as connected user
						Intent t = new Intent(Register_EditProfileActivity.this,MenuActivity.class);
						mainLayoutwithBackGrounVideo.removeAllViews();
						manager.setSwitchActivity(true);
						startActivity(t);
				        finish();
					
					
					}else{
						int responeCode = intent.getIntExtra("HTTPRESPONE_CODE", 0);
						popUploadingLayOut.setStopAnimation(true);
						popUploadingLayOut.setTextColor(Color.RED);
						popUploadingLayOut.setText(getString(R.string.internetproblem)+"("+responeCode+")");
					}
					
					
				}
				
				
				
				if(userLoggedIn){
					
					if(intent.getAction().equals("android.intent.action.GETRANK")){
						String userInfo = intent.getStringExtra("USERINFO");
						String userInfoArr[] = userInfo.split(",");
						//userinfoArr= code,userid,username,password,email,country,isloggedin,rank,level,time,totalusers,,responecode
						manager.updateUserRankAndBestRecordViews(Register_EditProfileActivity.this, userInfoArr);
					}
	
				}

 			}
 		};
 		//registering our receiver
 		this.registerReceiver(mReceiver, intentFilter);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
		manager.setSwitchActivity(false);
		showPopup();
		
	    switch(requestCode) { 
	    case SELECT_PHOTO:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = imageReturnedIntent.getData();
	            
	            Bitmap progfilImage = null;
				try {
					progfilImage = getCorrectlyOrientedImage(this, selectedImage);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(!userLoggedIn)progileImageBT.setBackground(new BitmapDrawable(getApplicationContext().getResources(),progfilImage));
				else{
					profileL.getProfileIV().setImageDrawable(new BitmapDrawable(getApplicationContext().getResources(),progfilImage));
				}
				

	        }
	        
			popUploadingLayOut.setStopAnimation(true);

	    }
	}
	
	public static int getOrientation(Context context, Uri photoUri) {
	    /* it's on the external media. */
	    Cursor cursor = context.getContentResolver().query(photoUri,new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

	    if (cursor.getCount() != 1) {
	        return -1;
	    }

	    cursor.moveToFirst();
	    return cursor.getInt(0);
	}
	
	private void showPopup(){


		if(!isPopupShowen){
			isPopupShowen=true;
			popUploading = new PopupWindow(Register_EditProfileActivity.this);
		    popUploading.setBackgroundDrawable(null);
		    popUploadingLayOut = new DotsProgressBar(Register_EditProfileActivity.this,screenW,(int) (screenH*0.2f));
		    popUploadingLayOut.setStopAnimation(true);
		    popUploading.setContentView(popUploadingLayOut);	
		    //popUploadingLayOut.post(new Runnable() {
			 //   public void run() {
			    	if(!userLoggedIn){
			    	popUploading.showAtLocation(popUploadingLayOut, Gravity.TOP,0, 0);
			    	popUploading.update(0, 0, screenW,(int) (screenH*0.2f));	
				    }else{
				    	//(int) (screenHeight*0.2)
				    	popUploading.showAtLocation(popUploadingLayOut, Gravity.TOP,0, (int) (screenH*0.2));
				    	popUploading.update(0, (int) (screenH*0.2), screenW,(int) (screenH*0.2f));
				    }
			  //  }
			//});
		}
		else{
			//popUploadingLayOut.post(new Runnable() {
			  //  public void run() {
			    	
			    	if(!userLoggedIn){
				    	popUploading.showAtLocation(popUploadingLayOut, Gravity.TOP,0, 0);
				    	popUploading.update(0, 0, screenW,(int) (screenH*0.2f));	
					    }else{
					    	//(int) (screenHeight*0.2)
					    	popUploading.showAtLocation(popUploadingLayOut, Gravity.TOP,0, (int) (screenH*0.2));
					    	popUploading.update(0, (int) (screenH*0.2), screenW,(int) (screenH*0.2f));
					    }
				    }
			//});
		//}
	}
	
	public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri) throws IOException {
	    InputStream is = context.getContentResolver().openInputStream(photoUri);
	    BitmapFactory.Options dbo = new BitmapFactory.Options();
	    dbo.inJustDecodeBounds = true;
	    BitmapFactory.decodeStream(is, null, dbo);
	    is.close();

	    int rotatedWidth, rotatedHeight;
	    int orientation = getOrientation(context, photoUri);

	    if (orientation == 90 || orientation == 270) {
	        rotatedWidth = dbo.outHeight;
	        rotatedHeight = dbo.outWidth;
	    } else {
	        rotatedWidth = dbo.outWidth;
	        rotatedHeight = dbo.outHeight;
	    }

	    Bitmap srcBitmap;
	    is = context.getContentResolver().openInputStream(photoUri);
	    if (rotatedWidth > profilImageSize || rotatedHeight > profilImageSize) {
	        float widthRatio = ((float) rotatedWidth) / ((float) profilImageSize);
	        float heightRatio = ((float) rotatedHeight) / ((float) profilImageSize);
	        float maxRatio = Math.max(widthRatio, heightRatio);

	        // Create the bitmap from file
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = (int) maxRatio;
	        srcBitmap = BitmapFactory.decodeStream(is, null, options);
	    } else {
	        srcBitmap = BitmapFactory.decodeStream(is);
	    }
	    is.close();

	    /*
	     * if the orientation is not 0 (or -1, which means we don't know), we
	     * have to do a rotation.
	     */
	    if (orientation > 0) {
	        Matrix matrix = new Matrix();
	        matrix.postRotate(orientation);
	        srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),srcBitmap.getHeight(), matrix, true);
	    }

	    srcBitmap = Bitmap.createScaledBitmap(srcBitmap, profilImageSize, profilImageSize, true);
	    return srcBitmap;
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		System.out.println("FOCUS CHANGED");
		
        if (popUpOpencountryList){
        	userNameTV.setVisibility(View.VISIBLE);
	    	userNameET.setVisibility(View.VISIBLE);
	    	passwordTV.setVisibility(View.VISIBLE);
	    	passwordET.setVisibility(View.VISIBLE);
	    	emailTV.setVisibility(View.VISIBLE);
	    	emailET.setVisibility(View.VISIBLE);
	    	countryTV.setVisibility(View.VISIBLE);
	    	if(!userLoggedIn){
	    	profileImageTV.setVisibility(View.VISIBLE);
	    	progileImageBT.setVisibility(View.VISIBLE);
	    	}
	    	countrySpinner.setAlpha(1);
	    	registerBT.setVisibility(View.VISIBLE);
	    	isSelect=false;	
	    	popUpOpencountryList=false;
	    	isFirstChange=false;
        }
        
		if(!isFirstChange)isFirstChange=true;
        else if(isFirstChange){
        	popUpOpencountryList=true;
        }
        
		
	}
	
	public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		manager.stopVideo();
		Intent i=null;
		if(userLoggedIn)
		     i = new Intent(Register_EditProfileActivity.this,MenuActivity.class);
		else
			 i = new Intent(Register_EditProfileActivity.this,LoginActivity.class);

		mainLayoutwithBackGrounVideo.removeAllViews();
		manager.setSwitchActivity(true);
		startActivity(i);
		finish();
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
	protected void onPause() {
		super.onPause();
		if(isPopupShowen){
			popUploading.dismiss();
			isPopupShowen=false;
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
		
		if(!manager.isSwitchActivity()){
			manager.pausePlayBackgroundMusic(this);
			manager.pauseVideo();
		}
	}
	
	@SuppressLint("NewApi")
	public void getScreenSize(){
			final DisplayMetrics metrics = new DisplayMetrics(); 
		    Display display = getWindowManager().getDefaultDisplay();     
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
	
	private class FlagArrayAdapter extends ArrayAdapter<FlagData> {


		 ArrayList<FlagData> list;
	    @SuppressLint("NewApi")
		public FlagArrayAdapter(Context context,ArrayList<FlagData> list) {
	        super(context, android.R.layout.simple_spinner_item);
	        this.list=list;
	        addAll(list);
	    }

	    @Override
	    public View getView(final int position, View convertView, ViewGroup parent) {
	    	
	    	
	    	
	    	final TextView label = (TextView) super.getDropDownView(position, convertView, parent);
	    	label.setBackgroundColor(Color.RED);
	    	label.setId(position);
	    	label.setBackgroundColor(Color.WHITE);
	    	label.getBackground().setAlpha(80);
	        label.setText(list.get(position).getCountryName());
	        label.setTextColor(Color.WHITE);
	        label.setCompoundDrawablesWithIntrinsicBounds(0, 0,list.get(position).getImageSRC(), 0);
	        return label;
	    }

	    @Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent) {

	    	return getView(position,convertView,parent);
	    }
	}

}


