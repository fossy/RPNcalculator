/*
 * Copyright (c) 2010 J.Fossy Weinzinger <fossy at iwasno dot net>
 *
 * This file is part of RPNcalculator.
 *
 * RPNcalculator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPNcalculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPNcalculator.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.iwasno.fossy.rpncalculator;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;


public class PanelSwitcher extends FrameLayout {
    private static final float MAJOR_MOVE_FACTOR     = 0.20f;  // 20   %
    private static final float START_DRAGGING_FACTOR = 0.075f; //  7.5 %
    private static final int   ANIM_DURATION         = 750;    // [ms]

    private int mCurrentViewIndex;
    private int mNewViewIndex;
    
    private View mChildren[];

    private int     mWidth = 9999;
    private int     mMajorMove;
    private int     mStartDragging;
    private boolean mDragging;
    
	private float mDownX;
	//private float mDownY;
	
    private TextView mLeftKbIndicator   = null;
    private TextView mActualKbIndicator = null;
    private TextView mRightKbIndicator  = null;

	private String[] mKbIndicators;
	
	private AnimationListener mAnimationListener;
	
    public static final int DIGITS     = 0;
    public static final int FUNCTIONS  = 1;
    public static final int STATISTICS = 2;
    
    public PanelSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mCurrentViewIndex = DIGITS;
        
        mKbIndicators = context.getResources().getStringArray(R.array.kb_indicators);
        
        mAnimationListener = new AnimationListener() {

            public void onAnimationStart(Animation animation) {
            // do nothing       
            }

            public void onAnimationRepeat(Animation animation) {
            // do nothing       
            }

            public void onAnimationEnd(Animation animation) {
            	RpnCalculator.log("animation ended");
            	setViewIndex(mNewViewIndex);
            	//offsetChildren(0, true); //done by setViewIndex
            }
        };
    }

    protected boolean otherIsRightOfCurrent(int dx, int dir) {
    	return  dx < 0 || (dx == 0 && dir > 0);
    }
    
    protected int otherIndex(boolean otherIsRightOfCurrent) {
    	return  otherIsRightOfCurrent
    		  ? (mCurrentViewIndex + 1) % mChildren.length
    		  : (mCurrentViewIndex > 0 ? mCurrentViewIndex - 1 : mChildren.length - 1)
    		  ;
    }

    protected int otherIndex(int dx, int dir) {
    	return otherIndex(otherIsRightOfCurrent(dx, dir));
    }
    
    protected int offsetChildren(int dx, int dir) {
    	boolean oiroc     = otherIsRightOfCurrent(dx, dir);
    	int     otherIdx  = otherIndex(oiroc); 
    	
    	for ( int i = 0;
    	      i < mChildren.length;
    	      i++
    	    ) {
    		int left;
   		
    		if ( i == mCurrentViewIndex ) {
    			left = dx;
    		} else if ( i == otherIdx ) {
    			left = dx + (oiroc ? mWidth : -mWidth);
    		} else {
    			left = -mWidth;
    		}
    		
    		int h = mChildren[i].getLeft();
        	mChildren[i].offsetLeftAndRight(left - mChildren[i].getLeft());
        	RpnCalculator.log("offsetChildren: child=" + i + " left-before=" + h + " set-left=" + left + " now-left=" + mChildren[i].getLeft());
    	}
    	
    	return otherIdx;
    }
    
    public void setIndicators(TextView left, TextView actual, TextView right) {
		mLeftKbIndicator   = left;
		mActualKbIndicator = actual;
		mRightKbIndicator  = right;
    }
    
    public void setViewIndex(int idx) {
    	if (   mChildren != null
    		&& idx >= 0
    		&& idx <  mChildren.length
    	   ) {
    			mCurrentViewIndex = idx;
    			offsetChildren(0, 0);

    			if (   mLeftKbIndicator   != null
    			    && mActualKbIndicator != null
    			    && mRightKbIndicator  != null
    		       ) {
    				mLeftKbIndicator  .setText(mKbIndicators[otherIndex(false)]); // isRightOfCurrent == false
    				mActualKbIndicator.setText(mKbIndicators[mCurrentViewIndex]);
    				mRightKbIndicator .setText(mKbIndicators[otherIndex(true)]);  // isRightOfCurrent == true
    		
    				mActualKbIndicator.getParent().requestLayout();
    			}
    			
    		invalidate();
    	}
    }
    
    @Override 
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        mWidth         = w;
        mMajorMove     = (int) Math.round(MAJOR_MOVE_FACTOR     * mWidth);
        mStartDragging = (int) Math.round(START_DRAGGING_FACTOR * mWidth);
        
        RpnCalculator.log("onSizeChanged: w=" + w + " h=" + h);
        
        //offsetChildren(0, 0);
        invalidate(); //???
        
        /*if ( mChildren != null ) {
        	for ( int i = 0 ; i < mChildren.length; i++ ) {
        		mChildren[i].postInvalidate();
        	}
        }*/
    }

    protected void onFinishInflate() {
        RpnCalculator.log("onFinishInflate:");
        
        mChildren = new View[getChildCount()];
        
        for ( int i = 0;
              i < mChildren.length;
              ++i
            ) {
            mChildren[i] = getChildAt(i);
        }
        
        offsetChildren(0, 0);
        invalidate();

        super.onFinishInflate();
    }
    
    /*protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        offsetChildren(0, 0);
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }*/
	
    private boolean onDownAction(MotionEvent e) {
		RpnCalculator.log("onDownAction: x=" + e.getX());
		mDownX    = e.getX();
		//mDownY    = e.getY();
		mDragging = false;
		
		return false;
	}

    private boolean onScrollAction(MotionEvent e) {
    	int dx = (int) (e.getX() - mDownX);

    	RpnCalculator.log("onScroll: dx=" + dx);
    	
    	if ( Math.abs(dx) > mStartDragging || mDragging ) {
    	    offsetChildren(dx, 0);
    	    invalidate();
    		
    		mDragging = true;
    	}
    	
    	return mDragging;
    }

    private boolean onFlingAction(MotionEvent e) {
    	int dx = (int) (e.getX() - mDownX);

    	RpnCalculator.log("onFlying: dx=" + dx);
    	
    	// don't accept the flying if it's too short
    	// as it may conflict with a button push
    	if ( Math.abs(dx) > mMajorMove ) {
    		animateFromTo(dx, dx > 0 ? mWidth : -mWidth);
    		
    		return true;  //===================================>
	   	} 
    	// else
	   	if ( mDragging ) {
	   		animateFromTo(dx, 0);
    		
    		return true;  //===================================>
	   	}
	   		
	   	return false;
	 }

    private void animateFromTo(int startX, int endX) {
    	//
    	// animate actual child from actual offset position <startX> to offset position <endX>
    	// after the animation, the framework will show the position the child had have at the
    	// beginning from the animation (?)
    	// so, we offset now to the end position (but without invalidate it)
    	//
    	int otherIdx = offsetChildren(endX, endX - startX);
        
        RpnCalculator.log("animateFromTo:\nstartX=" + startX + " endX=" + endX + "\n idx=" + mCurrentViewIndex + " other=" + otherIdx + "\n idx-left=" + mChildren[mCurrentViewIndex].getLeft() + " other-left=" + mChildren[otherIdx].getLeft());
        
        TranslateAnimation current = new TranslateAnimation(startX - endX, 0, 0, 0);        
    	TranslateAnimation other   = new TranslateAnimation(startX - endX, 0, 0, 0);
    	
        mNewViewIndex = endX == 0 ? mCurrentViewIndex : otherIdx;

        current.setDuration(ANIM_DURATION);
        other  .setDuration(ANIM_DURATION);
    	
        long startTime = AnimationUtils.currentAnimationTimeMillis();
        current.setStartTime(startTime);
        other  .setStartTime(startTime);
       
        other  .setAnimationListener(mAnimationListener);
        
        mChildren[mCurrentViewIndex].startAnimation(current);
        mChildren[otherIdx]         .startAnimation(other);
    }
    
    private boolean onMotionAction(MotionEvent event) {
    	switch ( event.getAction() ) {
    	case MotionEvent.ACTION_DOWN:
    		return onDownAction(event);  //================================>
    		
    	case MotionEvent.ACTION_MOVE:
    		return onScrollAction(event);  //==============================>
    		
    	case MotionEvent.ACTION_UP:
    		return onFlingAction(event);  //===============================>
    	}
    	
    	return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	RpnCalculator.log("onTouchEvent: action=" + event.getAction());
    	return onMotionAction(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	RpnCalculator.log("onInterceptTouchEvent: action=" + event.getAction());
    	return onMotionAction(event);
    }

    void moveLeft() {
    	//
        //  <--
        //
    	animateFromTo(0, -mWidth);
    }

    void moveRight() {
    	//
        //  -->
        //
    	animateFromTo(0, mWidth);
    }

    /***
    void moveTo(int idx) {
    	if ( mCurrentViewIndex == idx )
    		return;  //=====================================================>
    	
    	int stepsRight;
    	int stepsLeft;
    	
    	if ( idx > mCurrentViewIndex ) {
    		stepsLeft  = idx - mCurrentViewIndex;
    		stepsRight = mChildren.length - idx + mCurrentViewIndex;
    	} else {
    		stepsRight = mCurrentViewIndex - idx;
    		stepsLeft  = mChildren.length + idx - mCurrentViewIndex;
    	}
    	
    	if ( stepsRight < stepsLeft ) {
    		while ( stepsRight > 0  ) {
    			moveRight();  // -1
    			stepsRight--;
    		}
    	} else {
    		while ( stepsLeft > 0 ) {
    			moveLeft();   // +1
    			stepsLeft--;
    		}
    	}
    }
    ***/

    void moveTo(int idx) {
    	if ( mCurrentViewIndex == idx )
    		return;  //=====================================================>
    	
    	int stepsLeft = idx > mCurrentViewIndex
    	                  ? idx - mCurrentViewIndex
    	                  : mChildren.length + idx - mCurrentViewIndex
    	                  ;
    	
    	if ( stepsLeft > 1 ) {
    		moveRight();
    	} else {
    		moveLeft();
    	}
    }    
        
    int getCurrentIndex() {
        return mCurrentViewIndex;
    }
}
