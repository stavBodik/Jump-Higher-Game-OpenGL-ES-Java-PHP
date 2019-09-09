package com.jump_higher.gui;

import com.jump_higher.R;
import com.jump_higher.classes.ApplicationManager;
import com.jump_higher.opengl.Constants;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * @author stav bodik
 * This class used to show timer and current level at game play activity .
 */
public class LevelinformationLayout extends LinearLayout {

	//GUI components
	private TextView levelView;
	private TextView timerView;

	private int level;
	private String time;
	
	private int screenW;
	private int screenH;

	public LevelinformationLayout(final Context context) {
		super(context);

		// display dimensions
		ApplicationManager m = ApplicationManager.getInstance();
		screenW = m.getScreenW();
		screenH = m.getScreenW();


		LinearLayout mainLayOut = new LinearLayout(context);
		mainLayOut.setOrientation(LinearLayout.VERTICAL);
		mainLayOut.setId(0); 


		LinearLayout levelLayOut = new LinearLayout(context);
		levelLayOut.setGravity(Gravity.CENTER_HORIZONTAL);
		RelativeLayout.LayoutParams  levelLayOutParams = new RelativeLayout.LayoutParams(screenW, LayoutParams.WRAP_CONTENT);  


		levelView = new TextView(context);
		levelView.setTextColor(Color.WHITE);
		levelView.setId(1);
		levelView.setTextSize(TypedValue.COMPLEX_UNIT_PX,screenW/20f);
		RelativeLayout.LayoutParams levelViewParams = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,(int) (screenH*0.1)));  
		levelLayOut.addView(levelView,levelViewParams);

		mainLayOut.addView(levelLayOut,levelLayOutParams);

		LinearLayout timerLayOut = new LinearLayout(context);
		timerLayOut.setGravity(Gravity.CENTER_HORIZONTAL);
		RelativeLayout.LayoutParams  timerLayoutLParams = new RelativeLayout.LayoutParams(screenW, (int) (screenH*0.2));  

		timerView = new TextView(context);
		timerView.setTextColor(Color.WHITE);
		timerView.setTextSize(TypedValue.COMPLEX_UNIT_PX,screenW/5f);
		Typeface typeFace=Typeface.createFromAsset(context.getAssets(),"d_7_mono.ttf");
		timerView.setTypeface(typeFace);
		timerLayOut.addView(timerView);

		mainLayOut.addView(timerLayOut,timerLayoutLParams);


		this.addView(mainLayOut,new LayoutParams(screenW, LayoutParams.MATCH_PARENT));

		setLevel(Constants.START_LEVEL);
		setTimer("00:00:00");
   
	}
	
	public void setLevel(int level) {
		this.level=level;
		levelView.post(new Runnable() {
		    public void run() {
				levelView.setText(getResources().getString(R.string.level)+" "+LevelinformationLayout.this.level);
		    }
		});
		
	}
	
	public void restart(){
		   setLevel(Constants.START_LEVEL);
		   setTimer(Constants.START_TIMER);
	}
	
	public void setTimer(String time){
		    this.time=time.substring(0, 8);
			ApplicationManager.getInstance().setReachedTime(time);
			timerView.post(new Runnable() {
			    public void run() {
			    	timerView.setText(LevelinformationLayout.this.time);
			    }
			});
	}
	
}
