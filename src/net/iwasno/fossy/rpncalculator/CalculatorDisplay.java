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

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.graphics.Rect;
import android.graphics.Paint;

/**
 * Provides vertical scrolling for the input/result EditText.
 */
class CalculatorDisplay extends TextView {
    private static final int HVGA_WIDTH_PIXELS  = 320;
    private static final int MAJOR_MOVE_X = 60;
    private static final int MAJOR_MOVE_Y = 20;
    
    private Logic           mLogic;
    private GestureDetector mGestureDetector;
    private Feedback        mFeedback;
    
    public CalculatorDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int     dx  = (int) (e2.getX() - e1.getX());
                int     dy  = (int) (e2.getY() - e1.getY());
                boolean res = false;
                
                // don't accept the fling if it's too short
                // as it may conflict with a button push
                if ( Math.abs(dx) > MAJOR_MOVE_X && Math.abs(velocityX) > Math.abs(velocityY) ) {
                    /*if (velocityX > 0) {
                        mLogic.onSwap();
                    } else {*/
                        mLogic.onSwap();
                    //}
                    res = true;
                } else if ( Math.abs(dy) > MAJOR_MOVE_Y && Math.abs(velocityY) > Math.abs(velocityX) ) {
                	if ( velocityY > 0 ) {
                		mLogic.onRollDown();
                	} else {
                		mLogic.onRollUp();
                	}
                	
                	res = true;
                }
                
                if ( res ) {
                	mFeedback.feedback();
                }
                
                return res;
            }
        });
        
        mFeedback = new Feedback(context);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //RpnCalculator calc = (RpnCalculator) getContext();
        //calc.adjustFontSize((TextView)getChildAt(0));
        //calc.adjustFontSize((TextView)getChildAt(1));
        //calc.adjustFontSize(this);
        
        
        float   fontPixelSize = getTextSize();
        Display display       = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        int     h             = Math.min(display.getWidth(), display.getHeight());
        float   ratio         = (float)h/HVGA_WIDTH_PIXELS;
        
        setTextSize(TypedValue.COMPLEX_UNIT_PX, fontPixelSize*ratio);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        /***
        if ( !mComputedLineLength ) {
            mLogic.setLineLength(getNumberFittingDigits(this));
            mComputedLineLength = true;
        }
        ***/
        
        if ( mLogic != null ) {
            Paint paint  = getPaint();

            mLogic.setWidth(right - left, paint);
        }
    }

    // compute the maximum number of digits that fit in the
    // calculator display without scrolling.
    /***
    private int getNumberFittingDigits(TextView display) {
        int available    = display.getWidth()
                         - display.getTotalPaddingLeft() 
                         - display.getTotalPaddingRight();
        Paint paint      = display.getPaint();
        float digitWidth = paint.measureText("0123456789") / 10.f;
        
        return (int) (available / digitWidth);
    }
    ***/

    protected void setLogic(Logic logic) {
        mLogic = logic;
        /*NumberKeyListener calculatorKeyListener =
            new NumberKeyListener() {
                public int getInputType() {
                    // Don't display soft keyboard.
                    return InputType.TYPE_NULL;
                }
            
                protected char[] getAcceptedChars() {
                    return ACCEPTED_CHARS;
                }

                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {
                    /* the EditText should still accept letters (eg. 'sin')
                       coming from the on-screen touch buttons, so don't filter anything.
                    * /
                    return null;
                }
            };

        Editable.Factory factory = new CalculatorEditable.Factory(logic);
        for (int i = 0; i < 2; ++i) {
            EditText text = (EditText) getChildAt(i);
            text.setBackgroundDrawable(null);
            text.setEditableFactory(factory);
            text.setKeyListener(calculatorKeyListener);
        } */
        
        setBackgroundDrawable(null);
        //setKeyListener(calculatorKeyListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    /*@Override
    public void setOnKeyListener(OnKeyListener l) {
        //getChildAt(0).setOnKeyListener(l);
        //getChildAt(1).setOnKeyListener(l);
    	
    } */

    /*
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        inAnimUp = new TranslateAnimation(0, 0, h, 0);
        inAnimUp.setDuration(ANIM_DURATION);
        outAnimUp = new TranslateAnimation(0, 0, 0, -h);
        outAnimUp.setDuration(ANIM_DURATION);

        inAnimDown = new TranslateAnimation(0, 0, -h, 0);
        inAnimDown.setDuration(ANIM_DURATION);
        outAnimDown = new TranslateAnimation(0, 0, 0, h);
        outAnimDown.setDuration(ANIM_DURATION);
    }

    void insert(String delta) {
        EditText editor = (EditText) getCurrentView();
        int cursor = editor.getSelectionStart();
        editor.getText().insert(cursor, delta);
    } */

    /*
    EditText getEditText() {
        return (EditText) getCurrentView();
    } */
        
    /*
    Editable getText() {
        EditText text = (EditText) getCurrentView();
        return text.getText();
    } */
    
    /*void setText(CharSequence text, Scroll dir) {
        if (getText().length() == 0) {
            dir = Scroll.NONE;
        }
        
        if (dir == Scroll.UP) {
            setInAnimation(inAnimUp);
            setOutAnimation(outAnimUp);            
        } else if (dir == Scroll.DOWN) {
            setInAnimation(inAnimDown);
            setOutAnimation(outAnimDown);            
        } else { // Scroll.NONE
            setInAnimation(null);
            setOutAnimation(null);
        }
        
        EditText editText = (EditText) getNextView();
        editText.setText(text);
        //Calculator.log("selection to " + text.length() + "; " + text);
        editText.setSelection(text.length());
        showNext();
    }*/
    
    /*void setSelection(int i) {
        EditText text = (EditText) getCurrentView();
        text.setSelection(i);
    }*/
    
    /*int getSelectionStart() {
        EditText text = (EditText) getCurrentView();
        return text.getSelectionStart();
    }*/
    
    @Override
    protected void onFocusChanged(boolean gain, int direction, Rect prev) {
        //Calculator.log("focus " + gain + "; " + direction + "; " + prev);
        if (!gain) {
            requestFocus();
        }
    }
}
