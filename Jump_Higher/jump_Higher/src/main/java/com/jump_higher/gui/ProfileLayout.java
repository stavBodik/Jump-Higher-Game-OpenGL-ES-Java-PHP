package com.jump_higher.gui;


import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.classes.User;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * @author stav bodik
 * This class used to show user account profile details such as profile image, current rank , best record . 
 */
public class ProfileLayout extends LinearLayout {

	// GUI Components
	private TextView fullName;
	private ImageView flag;
	private TextView rank;
    private User loggedInUser;
    private ImageView profileIV;
    private TextView bestRecord;
    private RelativeLayout leftLayOut;
    private int screenW,screenH;
    
	public ProfileLayout(Context context) {
		super(context);
		
		// display dominations
        ApplicationManager m = ApplicationManager.getInstance();
        screenW = m.getScreenW();
        screenH = m.getScreenH();
		
		this.loggedInUser = ApplicationManager.getInstance().getLoggedInUser();
		int profilImageSize= (int) (screenH*0.3f*0.4f);
		int leftImageOffest=20;
		int flagW=16*3;
		int flagH=11*3;
		

		
		// main transparent layout over the openGLview
        leftLayOut = new RelativeLayout(context);
    	leftLayOut.setGravity(Gravity.BOTTOM);
    	leftLayOut.setId(0); 
    
    	profileIV = new ImageView(context);
    	profileIV.setId(1);
    	profileIV.setImageDrawable(new BitmapDrawable(context.getResources(),loggedInUser.getProfileImage()));
    	profileIV.setAdjustViewBounds(true);
    	profileIV.setBackgroundColor(Color.WHITE);  
    	profileIV.setPadding(2,2,2,2);
        RelativeLayout.LayoutParams profilImageParams = new RelativeLayout.LayoutParams(new LayoutParams(profilImageSize,profilImageSize));  

        profilImageParams.addRule(RelativeLayout.ALIGN_LEFT);
        profilImageParams.leftMargin = leftImageOffest;
        profilImageParams.bottomMargin = 5;

        leftLayOut.addView(profileIV,profilImageParams);


        rank = new TextView(context);
        rank.setId(2);
        //rank.setBackgroundColor(Color.GREEN);
        rank.setTextColor(Color.WHITE);
        rank.setGravity(Gravity.BOTTOM);
        rank.setTextSize(TypedValue.COMPLEX_UNIT_PX,m.getTVtextSize());
        rank.setText(getResources().getString(R.string.rank)+" "+loggedInUser.getRank()+" of "+ApplicationManager.getInstance().getNumberOfRegistartedUsers());
        RelativeLayout.LayoutParams rankParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));  
        rankParams.addRule(RelativeLayout.RIGHT_OF, profileIV.getId());
        rankParams.addRule(RelativeLayout.ALIGN_BOTTOM, profileIV.getId());

        rankParams.leftMargin = 5;
        leftLayOut.addView(rank,rankParams);

        
        
        
        fullName = new TextView(context);
        fullName.setId(3);
        fullName.setTextColor(Color.WHITE);
        fullName.setTextSize(TypedValue.COMPLEX_UNIT_PX,m.getTVtextSize());
        fullName.setText(loggedInUser.getUserName());
        RelativeLayout.LayoutParams fullNameParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));  
        fullNameParams.addRule(RelativeLayout.ABOVE, rank.getId());
        fullNameParams.addRule(RelativeLayout.RIGHT_OF, profileIV.getId());

        fullNameParams.leftMargin = 5;
        leftLayOut.addView(fullName,fullNameParams);
        
        flag = new ImageView(context);
        flag.setId(4);
        flag.setImageResource(loggedInUser.getFlagData().getImageSRC());
        RelativeLayout.LayoutParams flagParams = new RelativeLayout.LayoutParams(new LayoutParams(flagW,flagH));  
        flagParams.addRule(RelativeLayout.RIGHT_OF, fullName.getId());
        flagParams.addRule(RelativeLayout.ALIGN_BOTTOM, fullName.getId());

        flagParams.leftMargin = 15;

        leftLayOut.addView(flag,flagParams);
        
        

	    this.addView(leftLayOut,new LayoutParams((int) (screenW*(3/4f)), LayoutParams.MATCH_PARENT));
	    
       RelativeLayout rightLayout = new RelativeLayout(context);
	   rightLayout.setGravity(Gravity.BOTTOM);
	   rightLayout.setId(5); 
	   
	   bestRecord = new TextView(context);
	   bestRecord.setTextColor(Color.WHITE);
	   bestRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX,m.getTVtextSize());
	   bestRecord.setText(context.getString(R.string.bestrecord)
	   		            + "\n"+context.getString(R.string.level)+loggedInUser.getBestLevel()
	   		            + "\n"+context.getString(R.string.time)+loggedInUser.getBestTime());
	   
	   rightLayout.addView(bestRecord);
		   
	   this.addView(rightLayout,new LayoutParams((int) (screenW*(1/4f)), LayoutParams.MATCH_PARENT));

		
	}
	public void updateRankViews(){
        rank.setText(getResources().getString(R.string.rank)+" "+loggedInUser.getRank()+" of "+ApplicationManager.getInstance().getNumberOfRegistartedUsers());
	}
	public void updateBestRecordView(Context context){
		bestRecord.setText(context.getString(R.string.bestrecord)
		            + "\n"+context.getString(R.string.level)+loggedInUser.getBestLevel()
		            + "\n"+context.getString(R.string.time)+loggedInUser.getBestTime());

	}
	public ImageView getProfileIV() {
		return profileIV;
	}
	public Bitmap getProfileImage(){
		return ((BitmapDrawable)profileIV.getDrawable()).getBitmap();
	}
}
