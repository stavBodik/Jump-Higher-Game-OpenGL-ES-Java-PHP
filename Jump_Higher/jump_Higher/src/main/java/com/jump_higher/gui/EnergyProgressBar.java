package com.jump_higher.gui;

import com.jump_higher.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * @author Stav Bodik
 * This class used to draw sphere energy bar in game activity , 
 * when energy is reached to 100% the sphere jump energy increased
 */
public class EnergyProgressBar extends LinearLayout {

    ProgressBar mProgress;
    private int progressCount=0;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public EnergyProgressBar(Context context,int secreenHeight) {
		super(context);
  		
		 setGravity(Gravity.CENTER);
		 
		 mProgress = new ProgressBar(context,null, android.R.attr.progressBarStyleHorizontal); 
		 mProgress.setMax(100);
	     mProgress.setProgress(progressCount);
	     if(android.os.Build.VERSION.SDK_INT >= 21){
	   	     mProgress.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressvertical,context.getTheme()));
	    } else {
	   	     mProgress.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressvertical));
	    }
	     	     
	     addView(mProgress, new LayoutParams(LayoutParams.MATCH_PARENT,(int) (secreenHeight*0.4)));
	}
	
	public int getCurrentProgressColor(){
	     // start color rgb(0,210,255)
	     // end color   rgb(150,0,255)
		return Color.rgb((int) (150*(progressCount/100.0)), (int) (210*(1-(progressCount/100.0))), 255);
	}
	
	public void setProgressCount( final int progressCount) {
		
		
		mProgress.post(new Runnable() {
			public void run() {
				
				
				
				if(progressCount>=100){
					EnergyProgressBar.this.progressCount=0;
					ProgressBarAnimation anim = new ProgressBarAnimation(mProgress, 100, EnergyProgressBar.this.progressCount);
					anim.setDuration(500);
					mProgress.startAnimation(anim);
				}else{
					ProgressBarAnimation anim = new ProgressBarAnimation(mProgress, EnergyProgressBar.this.progressCount, progressCount);
					anim.setDuration(1000);
					mProgress.startAnimation(anim);
				}

				EnergyProgressBar.this.progressCount = progressCount;

				
				
			}
		});
		
		
	}

	public int getProgressCount() {
		return progressCount;
	}

	public class ProgressBarAnimation extends Animation{
	    private ProgressBar progressBar;
	    private float from;
	    private float  to;

	    public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
	        super();
	        this.progressBar = progressBar;
	        this.from = from;
	        this.to = to;
	    }

	    @Override
	    protected void applyTransformation(float interpolatedTime, Transformation t) {
	        super.applyTransformation(interpolatedTime, t);
	        float value = from + (to - from) * interpolatedTime;
	        progressBar.setProgress((int) value);
	    }

	}

}
