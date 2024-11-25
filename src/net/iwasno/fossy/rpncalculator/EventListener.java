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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import android.view.KeyEvent;
import android.view.View;

class EventListener implements View.OnKeyListener, 
                               View.OnClickListener, 
                               View.OnLongClickListener {
	
    Logic                      mLogic;
    PanelSwitcher              mPanelSwitcher;
    Hashtable<Integer, Method> mId2Logic;
    Hashtable<Integer, Method> mKey2Logic;

    private Feedback mFeedback;
   
    EventListener() {
    	//mFeedback = new Feedback(RpnCalculator);
    }
    
    private void addHash(Hashtable<Integer, Method> ht, int id, String s, Class<? extends Logic> c) {
    	try {
    		ht.put(id, c.getMethod(s));
    	} catch (NullPointerException e) {
    		e.printStackTrace();
    	} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
    }
    
    private void InitHashTables() {
    	Class<? extends Logic> logicClass = mLogic.getClass();
    	
    	mId2Logic = new Hashtable<Integer, Method>(100);
    	
		addHash(mId2Logic, R.id.digit0,     "onDigit0",            logicClass);
		addHash(mId2Logic, R.id.digit1,     "onDigit1",            logicClass);
		addHash(mId2Logic, R.id.digit2,     "onDigit2",            logicClass);
		addHash(mId2Logic, R.id.digit3,     "onDigit3",            logicClass);
		addHash(mId2Logic, R.id.digit4,     "onDigit4",            logicClass);
		addHash(mId2Logic, R.id.digit5,     "onDigit5",            logicClass);
		addHash(mId2Logic, R.id.digit6,     "onDigit6",            logicClass);
		addHash(mId2Logic, R.id.digit7,     "onDigit7",            logicClass);
		addHash(mId2Logic, R.id.digit8,     "onDigit8",            logicClass);
		addHash(mId2Logic, R.id.digit9,     "onDigit9",            logicClass);
		addHash(mId2Logic, R.id.dot,        "onDot",               logicClass);
    	addHash(mId2Logic, R.id.chs,        "onChangeSign",        logicClass);
    	addHash(mId2Logic, R.id.eex,        "onExponent",          logicClass);
    	addHash(mId2Logic, R.id.del,        "onDelete",            logicClass);
    	addHash(mId2Logic, R.id.enter,      "onEnter",             logicClass);
    	addHash(mId2Logic, R.id.plus,       "onPlus",              logicClass);
    	addHash(mId2Logic, R.id.minus,      "onMinus",             logicClass);
    	addHash(mId2Logic, R.id.mul,        "onMul",               logicClass);
    	addHash(mId2Logic, R.id.div,        "onDiv",               logicClass);

    	addHash(mId2Logic, R.id.exp,        "onExp",               logicClass);
    	addHash(mId2Logic, R.id.ln,         "onLn",                logicClass);
    	addHash(mId2Logic, R.id.pow10,      "onPow10",             logicClass);
    	addHash(mId2Logic, R.id.log,        "onLog",               logicClass);
    	addHash(mId2Logic, R.id.sin,        "onSin",               logicClass);
    	addHash(mId2Logic, R.id.cos,        "onCos",               logicClass);
    	addHash(mId2Logic, R.id.tan,        "onTan",               logicClass);
    	addHash(mId2Logic, R.id.asin,       "onASin",              logicClass);
    	addHash(mId2Logic, R.id.acos,       "onACos",              logicClass);
    	addHash(mId2Logic, R.id.atan,       "onATan",              logicClass);
    	addHash(mId2Logic, R.id.pi,         "onPi",                logicClass);
    	addHash(mId2Logic, R.id.percent,    "onPercent",           logicClass);
    	addHash(mId2Logic, R.id.square,     "onSquare",            logicClass);
    	addHash(mId2Logic, R.id.sqrt,       "onSqrt",              logicClass);
    	addHash(mId2Logic, R.id.power,      "onPower",             logicClass);
    	addHash(mId2Logic, R.id.reciprocal, "onReciprocal",        logicClass);
    	addHash(mId2Logic, R.id.integer,    "onInt",               logicClass);
    	addHash(mId2Logic, R.id.frac,       "onFrac",              logicClass);
    	//addHash(mId2Logic, R.id.rnd,        "onRnd",               logicClass);
    	addHash(mId2Logic, R.id.delta_percent, "onDeltaPercent",               logicClass);
    
    	addHash(mId2Logic, R.id.lastx,      "onLastX",             logicClass);
    	addHash(mId2Logic, R.id.sto,        "onSto",               logicClass);
    	addHash(mId2Logic, R.id.rcl,        "onRcl",               logicClass);
    	addHash(mId2Logic, R.id.clstat,     "onClearStat",         logicClass);
    	addHash(mId2Logic, R.id.statadd,    "onStatAdd",           logicClass);
    	addHash(mId2Logic, R.id.statsub,    "onStatSub",           logicClass);
    	addHash(mId2Logic, R.id.mean,       "onMean",              logicClass);
    	addHash(mId2Logic, R.id.stddev,     "onStandardDeviation", logicClass);
    	addHash(mId2Logic, R.id.factorial,  "onFactorial",         logicClass);
    	addHash(mId2Logic, R.id.perm ,      "onPermutation",       logicClass);
    	addHash(mId2Logic, R.id.comb,       "onCombination",       logicClass);
    	addHash(mId2Logic, R.id.mod,        "onMod",               logicClass);
    	addHash(mId2Logic, R.id.rect,       "onToRect",            logicClass);
    	addHash(mId2Logic, R.id.polar,      "onToPolar",           logicClass);
    	addHash(mId2Logic, R.id.hms,        "onToHms",             logicClass);
    	addHash(mId2Logic, R.id.hr,         "onToHr",              logicClass);
    	addHash(mId2Logic, R.id.swap,       "onSwap",              logicClass);
    	addHash(mId2Logic, R.id.down,       "onRollDown",          logicClass);
    	//addHash(mId2Logic, R.id.up,         "onRollUp",            logicClass);
    	addHash(mId2Logic, R.id.prime_factor, "onPrimeFactor",              logicClass);
    	addHash(mId2Logic, R.id.gcd,          "onGCD",              logicClass);
    	addHash(mId2Logic, R.id.lcm,          "onLCM",              logicClass);

    	mKey2Logic = new Hashtable<Integer, Method>(100);
    	
		addHash(mKey2Logic, KeyEvent.KEYCODE_0,      "onDigit0",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_1,      "onDigit1",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_2,      "onDigit2",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_3,      "onDigit3",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_4,      "onDigit4",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_5,      "onDigit5",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_6,      "onDigit6",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_7,      "onDigit7",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_8,      "onDigit8",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_9,      "onDigit9",            logicClass);
		addHash(mKey2Logic, KeyEvent.KEYCODE_PERIOD, "onDot",               logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_C,      "onChangeSign",        logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_E,      "onExponent",          logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_BACK,   "onDelete",            logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_DEL,    "onDelete",            logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_ENTER,  "onEnter",             logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_PLUS,   "onPlus",              logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_MINUS,  "onMinus",             logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_STAR,   "onMul",               logicClass);
    	addHash(mKey2Logic, KeyEvent.KEYCODE_SLASH,  "onDiv",               logicClass);
    }
    
    void setHandler(Logic logic, PanelSwitcher panelSwitcher, RpnCalculator calc) {
        mLogic         = logic;
        mPanelSwitcher = panelSwitcher;
        mFeedback      = new Feedback(calc);
        
        InitHashTables();
    }
    
    //@Override
    public void onClick(View view) {
        int id = view.getId();

        if ( mFeedback != null ) {
        	mFeedback.feedback();
        }
        
        if ( mId2Logic != null ) {
        	Method method = mId2Logic.get(id);
        
        	if ( method != null) {
        		try {
        			method.invoke(mLogic);
        		} catch (IllegalArgumentException e) {
        			e.printStackTrace();
        		} catch (IllegalAccessException e) {
        			e.printStackTrace();
        		} catch (InvocationTargetException e) {
        			e.printStackTrace();
        		}
        	} else {
        		RpnCalculator.log("unknown id [".concat(new Integer(id).toString()).concat("]"));
        	}
        }
    }

    //@Override
    public boolean onLongClick(View view) {
    	if ( mFeedback != null ) {
    		mFeedback.longFeedback();
    	}
        
        switch ( view.getId() ) {
        //..................................................................
        case R.id.del:
        	mLogic.onClear();
            return true;
            
        //..................................................................
        case R.id.rcl:
        	mLogic.ShowOptions();
        	return true;
        }
        
        return false;
    }
    

    //@Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        
        if ( action == KeyEvent.ACTION_MULTIPLE && keyCode == KeyEvent.KEYCODE_UNKNOWN ) {
            return true; // eat it
        }
        
        if ( action == KeyEvent.ACTION_UP ) {
            if ( mKey2Logic != null ) {
            	Method method = mKey2Logic.get(keyCode);
            
            	if ( method != null ) {
            		try {
            			method.invoke(mLogic);
            			return true;  //====================================>
            		} catch (IllegalArgumentException e) {
            			e.printStackTrace();
            		} catch (IllegalAccessException e) {
            			e.printStackTrace();
            		} catch (InvocationTargetException e) {
            			e.printStackTrace();
            		}
            	}
            }
            
            switch ( keyCode ) {
            //...............................................................
            case KeyEvent.KEYCODE_DPAD_LEFT:
            	mPanelSwitcher.moveRight();
                return true;  //============================================>
            	
            //...............................................................
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            	mPanelSwitcher.moveLeft();
                return true;  //============================================>
            }
        }
        
        return false;
    }
}
