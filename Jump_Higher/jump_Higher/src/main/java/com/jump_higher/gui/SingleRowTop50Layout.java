package com.jump_higher.gui;

import com.jump_higher.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Stav bodik
 * This class used in TOPActivity class to show each user as single row in the top list . 
 */
public class SingleRowTop50Layout extends LinearLayout {

	// GUI Components
	private ImageView flag;
	private TextView userName;
	private TextView userRecord;
    private ImageView profileIV;
    private TextView rowNumberTV;    
    private RelativeLayout singleRowLayOut;
    private int profilImageSize;

    
	public SingleRowTop50Layout(Context context, int screenWith, int screenHeight) {
		super(context);
		int textSize=screenWith/38;
		int flagW=10*3;
		int flagH=6*3;
	    profilImageSize= (int) (screenHeight*0.05);

		//int likeIconSize=(int) ((32/1080f)*screenWith);
		

		
		// main transparent layout over the openGLview
        singleRowLayOut = new RelativeLayout(context);
    	singleRowLayOut.setGravity(Gravity.CENTER_VERTICAL);
    	singleRowLayOut.setId(0); 
    
 	    Typeface typeFace=Typeface.createFromAsset(context.getAssets(),"CONSOLA.TTF");

    	
    	rowNumberTV = new TextView(context);
    	rowNumberTV.setTypeface(typeFace);
    	rowNumberTV.setId(11);
    	rowNumberTV.setTextColor(Color.WHITE);
    	rowNumberTV.setText("1.");
    	rowNumberTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
    	
        RelativeLayout.LayoutParams rowNumberTVParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));  
        rowNumberTVParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rowNumberTVParams.leftMargin = 5;

    	
    	singleRowLayOut.addView(rowNumberTV,rowNumberTVParams);
    	
    	
    	profileIV = new ImageView(context);
    	profileIV.setId(1);
    	profileIV.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.anonymprofil));
    	//profileIV.setAdjustViewBounds(true);
    	profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams profilImageParams = new RelativeLayout.LayoutParams(new LayoutParams(profilImageSize,profilImageSize));  
        profilImageParams.addRule(RelativeLayout.RIGHT_OF, rowNumberTV.getId());
       // profilImageParams.addRule(RelativeLayout.ALIGN_BOTTOM, rowNumberTV.getId());
        //profilImageParams.bottomMargin = 2;

        singleRowLayOut.addView(profileIV,profilImageParams);

        
        flag = new ImageView(context);
        flag.setId(2);
        RelativeLayout.LayoutParams flagParams = new RelativeLayout.LayoutParams(new LayoutParams(flagW,flagH));  
        flagParams.addRule(RelativeLayout.RIGHT_OF, profileIV.getId());
        flagParams.addRule(RelativeLayout.CENTER_VERTICAL, profileIV.getId());
        flagParams.leftMargin = 15;
        singleRowLayOut.addView(flag,flagParams);
        

        userName = new TextView(context);
        userName.setId(3);
        userName.setTextColor(Color.WHITE);
        userName.setGravity(Gravity.BOTTOM);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
        userName.setGravity(Gravity.CENTER_HORIZONTAL);
        // dont forget to substring here
 	    userName.setTypeface(typeFace);
        RelativeLayout.LayoutParams  userNameParams = new RelativeLayout.LayoutParams(new LayoutParams(textSize*8,LayoutParams.WRAP_CONTENT));  
        userNameParams.addRule(RelativeLayout.RIGHT_OF, flag.getId());
        userNameParams.addRule(RelativeLayout.ALIGN_BOTTOM, flag.getId());

        userNameParams.leftMargin = 10;
        singleRowLayOut.addView(userName,userNameParams);
        
        
        
        userRecord = new TextView(context);
        userRecord.setId(4);
        userRecord.setTextColor(Color.WHITE);
        userRecord.setGravity(Gravity.BOTTOM);
        userRecord.setGravity(Gravity.CENTER);

        userRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
        // dont forget to substring here
 	    userRecord.setTypeface(typeFace);
        RelativeLayout.LayoutParams userInformationParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));  
        userInformationParams.addRule(RelativeLayout.RIGHT_OF, userName.getId());
        userInformationParams.addRule(RelativeLayout.ALIGN_BOTTOM, userName.getId());

        userInformationParams.leftMargin = 2;
        singleRowLayOut.addView(userRecord,userInformationParams);
        

        /*
       RelativeLayout rightLayout = new RelativeLayout(context);
	   rightLayout.setGravity(Gravity.CENTER);
	   rightLayout.setId(5); 
       RelativeLayout.LayoutParams rightLayoutParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));  

       rightLayoutParams.addRule(RelativeLayout.RIGHT_OF, userRecord.getId());
       rightLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, userRecord.getId());
       rightLayoutParams.leftMargin=20;
	   
	   Button likeBT = new Button(context);
       likeBT.setBackgroundResource(R.drawable.like);
	   likeBT.setGravity(Gravity.BOTTOM);
	   likeBT.setId(6);
       RelativeLayout.LayoutParams likeBTParams = new RelativeLayout.LayoutParams(new LayoutParams(likeSize,likeSize));  
	   
	  
	   rightLayout.addView(likeBT,likeBTParams);
	   leftLayOut.addView(rightLayout,rightLayoutParams);*/
		   
	    this.addView(singleRowLayOut,new LayoutParams((int) (screenWith*0.9f), LayoutParams.MATCH_PARENT));

		
	}
	
	public void setRowNumber(String number) {
		this.rowNumberTV.setText(number);
	}

	public ImageView getProfileIV() {
		return profileIV;
	}
	
	public void setUserRecordText(String text){
		userRecord.setText(text);
	}
	
	public void setFlagIcon(Bitmap b,Context context) {
    	flag.setImageDrawable(new BitmapDrawable(context.getResources(),b));
	}
	
	public void setUserNameText(String text){
		userName.setText(text);
	}

	public void setProfileImage(Bitmap b,Context context){
		Bitmap scaledBmp=Bitmap.createScaledBitmap(b, profilImageSize, profilImageSize, true);
    	profileIV.setImageDrawable(new BitmapDrawable(context.getResources(),scaledBmp));
	}
	
	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    bm.recycle();
	    return resizedBitmap;
	}
	
	public Bitmap getProfileImage(){
		return ((BitmapDrawable)profileIV.getDrawable()).getBitmap();
	}
}
