package com.jump_higher.gui;

import java.util.Timer;
import java.util.TimerTask;

import com.jump_higher.classes.ApplicationManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author stav bodik
 * This class used to draw loading progress bar with white dots while doing tasks such as connecting server, loading images etc .
 */
@SuppressLint("HandlerLeak")
public class DotsProgressBar extends LinearLayout{

	//GUI components
	private TextView  dotsTV;
	private Timer timer; // used to schedule timer for drawing dots each sec 
	private  String dotsString="";
	private  int screenW;
	private  boolean stopAnimation=true;
	ApplicationManager m ;
	
	@SuppressLint("NewApi")
	public DotsProgressBar(Context context,int screenW,int screenH) {
		super(context);
		
        this.screenW=screenW;
        
		timer = new Timer();

		// display dimensions
        m = ApplicationManager.getInstance();
        	   
	   dotsTV = new TextView(context);
	   dotsTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,m.getTVtextSize());
	   dotsTV.setTextColor(Color.WHITE);
	   setGravity(Gravity.CENTER);
	   setBackgroundColor(Color.BLACK);
	   getBackground().setAlpha(0);
	   addView(dotsTV);
	   

	}
	
	private  void startTimer() {
		dotsString="";
		dotsTV.setText("");
		
		timer = new Timer();
	   
		timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {	
	        	if(!stopAnimation)mHandler.obtainMessage(1).sendToTarget();
	            
	        	if(dotsTV.getWidth()>=screenW-10){
	            	dotsString="";
	            	dotsTV.post(new  Runnable() {
						public void run() {
			            	dotsTV.setText("");
						}
					});
	            }
	            	            
	        }
	    }, 0, 100);
	};
	
	private Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			if(!stopAnimation){
			dotsString=dotsString+".";
			dotsTV.setText(dotsString);
			}
	    }
	};
	
	public void setStopAnimation(boolean stopAnimation) {
		this.stopAnimation = stopAnimation;
		if(stopAnimation){
			timer.cancel();
			dotsString="";
			dotsTV.setText("");
		}
		if(stopAnimation==false){
			   dotsString="";
			   dotsTV.setText("");
			   dotsTV.setTextColor(Color.WHITE);
			   dotsTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,m.getTVtextSize());
			   startTimer();

		}
	}
	
	public void setText(String text){
		dotsTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,m.getTVtextSize());
		dotsTV.setText(text);
	}
	
	public void setTextColor(int color){
		   dotsTV.setTextColor(color);
	}
	

}
