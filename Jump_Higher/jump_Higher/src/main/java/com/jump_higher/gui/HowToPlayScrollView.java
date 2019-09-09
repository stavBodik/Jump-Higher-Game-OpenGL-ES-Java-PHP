package com.jump_higher.gui;

import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * @author Stav Bodik
 * This class used inside Game guide How to play Popup window.
 */
public class HowToPlayScrollView extends ScrollView {

	private ApplicationManager manager;
	// display dimensions
	int screenW,screenH;
	int textSize;
	
	public HowToPlayScrollView(Context context) {
		super(context);

		manager = ApplicationManager.getInstance();
		// display dimensions
		screenW = manager.getScreenW();
		screenH = manager.getScreenH();
		textSize=screenW/30;

		LinearLayout mainLayOut = new LinearLayout(context);
		mainLayOut.setOrientation(LinearLayout.VERTICAL);

		TutorialStepLayOut intruductionGameGuide = new TutorialStepLayOut(context,(int)(screenH*0.5f));
		intruductionGameGuide.setText(context.getString(R.string.tutoriallevelup));
		intruductionGameGuide.setImage(ContextCompat.getDrawable(context,R.drawable.tutorialhowtolevelup));
		mainLayOut.addView(intruductionGameGuide);

		mainLayOut.addView(new LinearLayout(context),new LayoutParams((int)(screenW*0.9f),4));

		TutorialStepLayOut sphereJump = new TutorialStepLayOut(context,(int)(screenH*0.5f));
		sphereJump.setText(context.getString(R.string.tutorialjump));
		sphereJump.setImage(ContextCompat.getDrawable(context,R.drawable.tutorialhowtojump));
		mainLayOut.addView(sphereJump);

		mainLayOut.addView(new LinearLayout(context),new LayoutParams((int)(screenW*0.9f),4));

		TutorialStepLayOut sphereSurface = new TutorialStepLayOut(context,(int)(screenH*0.5f));
		sphereSurface.setText(context.getString(R.string.tutorialrotate));
		sphereSurface.setImage(ContextCompat.getDrawable(context,R.drawable.tutorialhowtorotate));
		mainLayOut.addView(sphereSurface);

		addView(mainLayOut);
	
	}

	/**
	 * @author Stav Bodik
	 * Single page for tutorial.
	 */
	private class 	TutorialStepLayOut extends LinearLayout{

		private TextView text;
		private ImageView image;
		
		public TutorialStepLayOut(Context context,int height) {
			super(context);

		setOrientation(LinearLayout.HORIZONTAL);
		
		text = new TextView(context);
		text.setTextColor(Color.BLACK);
		text.setGravity(Gravity.CENTER_VERTICAL);
		text.setPadding(2,2,2,2);
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
		text.setBackgroundColor(Color.WHITE);
		text.getBackground().setAlpha(180);
			
		LinearLayout imageViewContainer = new LinearLayout(context);
		//imageViewContainer.setBackgroundColor(Color.parseColor("#036be0"));
		imageViewContainer.setBackgroundColor(Color.parseColor("#000000"));

		imageViewContainer.getBackground().setAlpha(180);
		imageViewContainer.setGravity(Gravity.CENTER);
		image = new ImageView(context);
		//image.setAdjustViewBounds(true);
		//image.setScaleType(ImageView.ScaleType.CENTER_CROP);
		//image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.tutorialhowtojump));
		image.setAdjustViewBounds(true);
		imageViewContainer.addView(image);
		
		addView(text,new LayoutParams((int)(screenW*0.55f),height));
		addView(imageViewContainer,new LayoutParams((int)(screenW*0.4f),height));

		
		}
		
		public void setImage(Drawable d) {
			image.setImageDrawable(d);
		}
		
		public void setText(String text) {
			this.text.setText(Html.fromHtml(text));
		}
	}
	
}
