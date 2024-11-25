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

import java.lang.Double;

import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;

class Logic {
    private Context           mContext;
    private CalculatorDisplay mDisplay;
    private TextView          mAngleFormatIndicator;
    private TextView          mStorageIndicator;
    private PanelSwitcher     mPanelSwitcher;

    private static final int STATE_INPUT_MANTISSA_BEFORE_DOT = 0;
    private static final int STATE_INPUT_MANTISSA_AFTER_DOT  = 1;
    private static final int STATE_INPUT_EXPONENT            = 2;
    private static final int STATE_RESULT                    = 3;
    private static final int STATE_NO_STACKLIFT              = 4;
    private static final int STATE_STORE                     = 5;
    private static final int STATE_RECALL                    = 6;
    
    static final int LASTX = 0;

    static final int X     = 1;
    static final int Y     = 2;
    static final int Z     = 3;
    static final int T     = 4;

    static final int R0    = 5;
    static final int R1    = 6;
    static final int R2    = 7;
    static final int R3    = 8;
    static final int R4    = 9;
    static final int R5    = 10;
    static final int R6    = 11;
    static final int R7    = 12;
    static final int R8    = 13;
    static final int R9    = 14;
    
    private static final int SUM_X  = R4;
    private static final int SUM_XX = R5;
    private static final int SUM_Y  = R6;
    private static final int SUM_YY = R7;
    private static final int SUM_XY = R8;
    private static final int N      = R9;
    
    static final int RAD  = 0;
    static final int DEG  = 1;
    static final int GRAD = 2;
    
    
    static final String LOGIC_PREFS         = "LOGIC_PREFS";
    static final String PREF_REGISTER[]     = { "LASTX", "X", "Y", "Z", "T", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9"  };
    static final String PREF_STATE          = "STATE";
    static final String PREF_OPEN_STORE_OP  = "OPEN_STORE_OP";
    static final String PREF_MSIGN          = "MSIGN";
    static final String PREF_ESIGN          = "ESIGN";
    static final String PREF_MANTISSA       = "MANTISSA";
    static final String PREF_EXPONENT       = "EXPONENT";
    static final String PREF_DISPLAY_FORMAT = "DISPLAY_FORMAT";
    static final String PREF_DISPLAY_PLACES = "DISPLAY_PLACES";
    static final String PREF_ANGLE_FORMAT   = "ANGLE_FORMAT";
    
    static final String REGISTER = "REGISTER";
    
    private int             mState         = STATE_RESULT;
    private int             mOpenStoreOp   = 0; //0, R.id.*: plus, minus, mul, div
    private boolean         mMSign         = false;
    private boolean         mESign         = false;
    private StringBuilder   mMantissa      = new StringBuilder();
    private StringBuilder   mExponent      = new StringBuilder();
    private double          mRegister[]    = new double [4 + 1 + 10];
    private DoubleFormatter mFormatter;
    private int             mAngleFormat   = RAD;  // RAD, DEG, GRAD

    private double          m2RadFactor   = 1.;
    
    Logic(Context context, PanelSwitcher panel, CalculatorDisplay display, TextView angle, TextView storage) {
    	mContext              = context;
        mPanelSwitcher        = panel;
        mDisplay              = display;
        mAngleFormatIndicator = angle;
        mStorageIndicator     = storage;
        
        mDisplay.setLogic(this);
    }

    void setWidth(int pixelWidth, Paint paint) {
    	if ( mFormatter != null ) {
    		mFormatter.setWidth(pixelWidth, paint);
    		
    		if ( mState > STATE_INPUT_EXPONENT ) {
    			showResult();
    		}
    	}
    }

    /***
    boolean acceptInsert(String delta) {
    	/***
        String text = getText();
        return !mIsError &&
            (!mResult.equals(text) || 
             isOperator(delta) ||
             mDisplay.getSelectionStart() != text.length());
        *** /
    	//????
    	return mState == STATE_RESULT;
    }
	***/
    
    private void setStorageIndicator(String text) {
    	mStorageIndicator.setText(text);
    	mStorageIndicator.getParent().requestLayout();
    }
    
    public void onDelete() {
    	switch ( mState ) {
    	//...................................................................
    	case STATE_RESULT:
    	case STATE_NO_STACKLIFT:
    		onClear();
    		break;
    		
       	//...................................................................
    	case STATE_INPUT_MANTISSA_BEFORE_DOT:
    	case STATE_INPUT_MANTISSA_AFTER_DOT:
    		mMantissa.setLength(mMantissa.length() - 1);
    		
    		if ( mMantissa.indexOf(".") < 0 ) {
    			mState = STATE_INPUT_MANTISSA_BEFORE_DOT;
    		}
    		
    		if ( mMantissa.length() == 0 ) {
    			//
    			// all entered input is cleared 
    			// go back to state before input was made...
    			//
    			mMSign = false;
    			mState = STATE_RESULT; 
    			
    			showResult();     // show "old" X
    		} else {
    			updateDisplay();  // show entered digits etc.
    		}
    		
    		break;
    		
       	//...................................................................
    	case STATE_INPUT_EXPONENT:
    		mExponent.setLength(mExponent.length() - 1);
    		
    		if ( mExponent.length() == 0 ) {
    			mState = mMantissa.indexOf(".") < 0 ? STATE_INPUT_MANTISSA_BEFORE_DOT : STATE_INPUT_MANTISSA_AFTER_DOT;
    			mESign = false;
    		}
    		
    		updateDisplay();
    		break;
    		
       	//...................................................................
    	case STATE_STORE:
    	case STATE_RECALL:
    		mState = STATE_RESULT;
    		setStorageIndicator("");
    		break;
    	}
    }

	public void onClear() {
		mRegister[X] = 0.;
		showResult();
		
		mState = STATE_NO_STACKLIFT;  // Clx no stack lift !
    }

    public void onEnter() {
    	getXfromInput();
    	stackLift();
    	showResult();
    	
    	mState = STATE_NO_STACKLIFT;
    }

    public void onPlus() {
    	if ( mState == STATE_STORE || mState == STATE_RECALL ) {
    		mOpenStoreOp = R.id.plus;
    		setStorageIndicator("STO+");
    	} else {
    		getXfromInput();
    		twoOpResult(mRegister[Y] + mRegister[X]);
    	}
    }

    public void onMinus() {
    	if ( mState == STATE_STORE || mState == STATE_RECALL ) {
    		mOpenStoreOp = R.id.minus;
    		setStorageIndicator("STO-");
    	} else {
    		getXfromInput();
    		twoOpResult(mRegister[Y] - mRegister[X]);
    	}
    }
    
    public void onMul() {
    	if ( mState == STATE_STORE || mState == STATE_RECALL ) {
    		mOpenStoreOp = R.id.mul;
    		setStorageIndicator("STO*");
    	} else {
    		getXfromInput();
    		twoOpResult(mRegister[Y] * mRegister[X]);
    	}
    }

    public void onDiv() {
    	if ( mState == STATE_STORE || mState == STATE_RECALL ) {
    		mOpenStoreOp = R.id.div;
    		setStorageIndicator("STO/");
    	} else {
    		getXfromInput();
    		twoOpResult(mRegister[Y] / mRegister[X]);
    	}
    }
    
    public void onExp() {
    	getXfromInput();
    	oneOpResult(Math.exp(mRegister[X]));
    }
    
    public void onLn() {
    	getXfromInput();
    	oneOpResult(Math.log(mRegister[X]));
    }
    
    public void onPow10() {
    	getXfromInput();
    	oneOpResult(Math.pow(10., mRegister[X]));
    }
    
    public void onLog() {
    	getXfromInput();
    	oneOpResult(Math.log10(mRegister[X]));
    }
    
    public void onSin() {
    	getXfromInput();
    	oneOpResult(Math.sin(mRegister[X]*m2RadFactor));
    }
    
    public void onCos() {
    	getXfromInput();
    	oneOpResult(Math.cos(mRegister[X]*m2RadFactor));
    }
    
    public void onTan() {
    	getXfromInput();
    	oneOpResult(Math.tan(mRegister[X]*m2RadFactor));
    }
    
    public void onASin() {
    	getXfromInput();
    	oneOpResult(Math.asin(mRegister[X])/m2RadFactor);
    }
    
    public void onACos() {
    	getXfromInput();
    	oneOpResult(Math.acos(mRegister[X])/m2RadFactor);
    }
    
    public void onATan() {
    	getXfromInput();
    	oneOpResult(Math.atan(mRegister[X])/m2RadFactor);
    }
    
    public void onPi() {
		getXfromInput();
    	noOpResult(Math.PI);
    }
	
    public void onPercent() {
		getXfromInput();
    	oneOpResult(mRegister[X] / 100. * mRegister[Y]);
    }
    
    public void onDeltaPercent() {
		getXfromInput();
    	oneOpResult(((mRegister[X] - mRegister[Y]) / mRegister[Y]) * 100.);
    }
    
    public void onSquare() {
		getXfromInput();
    	oneOpResult(mRegister[X] * mRegister[X]);
    }
    
    public void onSqrt() {
		getXfromInput();
    	oneOpResult(Math.sqrt(mRegister[X]));
    }
    
    public void onPower() {
		getXfromInput();
    	twoOpResult(Math.pow(mRegister[Y], mRegister[X]));
    }
    
    public void onReciprocal() {
		getXfromInput();
    	oneOpResult(1. / mRegister[X]);
    }
     
    public void onInt() {
		getXfromInput();
    	oneOpResult(mRegister[X] >= 0. ? Math.floor(mRegister[X]) : Math.ceil(mRegister[X]));
    }
    
    public void onFrac() {
		getXfromInput();
    	oneOpResult(mRegister[X] - (mRegister[X] >= 0. ? Math.floor(mRegister[X]) : Math.ceil(mRegister[X])));
    }
    
    /*public void onAbs() {
		getXfromInput();
    	oneOpResult(Math.abs(mRegister[X]));
    }*/
    
    /*public void onRnd() {
		getXfromInput();
    	noOpResult(Math.random());
    }*/
    
    private void twoOpResult(double res) {
      mRegister[Y] = mRegister[Z];
      mRegister[Z] = mRegister[T];
      oneOpResult(res);
    }
    
    private void oneOpResult(double res) {
    	mRegister[LASTX] = mRegister[X];
    	mRegister[X] = res;
    	showResult();
    }
    
    private void noOpResult(double res) {
    	if ( mState != STATE_NO_STACKLIFT )
    		stackLift();

    	mRegister[X] = res;
    	showResult();
    }
    
    public void onSto() {
    	getXfromInput();
    	showResult();
    	
    	mState       = STATE_STORE;
    	mOpenStoreOp = 0;
    	setStorageIndicator("STO");
    	mPanelSwitcher.moveTo(PanelSwitcher.DIGITS);
    }
    
    public void onRcl() {
    	getXfromInput();
    	showResult();
    	
    	mState = STATE_RECALL;
    	setStorageIndicator("RCL");
    	mPanelSwitcher.moveTo(PanelSwitcher.DIGITS);
    }
    
    public void onLastX() {
		getXfromInput();
    	noOpResult(mRegister[LASTX]);
	}

	public void onClearStat() {
		for (int i = R4; i <= R9; i++ ) {
			mRegister[i] = 0.;
		}
	}

	public void onStatAdd() {
    	getXfromInput();
		mRegister[N]      ++;
		mRegister[SUM_X]  += mRegister[X];
		mRegister[SUM_XX] += mRegister[X] * mRegister[X];
		mRegister[SUM_Y]  += mRegister[Y];
		mRegister[SUM_YY] += mRegister[Y] * mRegister[Y];
		mRegister[SUM_XY] += mRegister[X] * mRegister[Y];
		  
		mRegister[X] = mRegister[N];
		showResult();
		mPanelSwitcher.moveTo(PanelSwitcher.DIGITS);
		mState = STATE_NO_STACKLIFT;
	}

	public void onStatSub() {
    	getXfromInput();
		mRegister[N]      --;
		mRegister[SUM_X]  -= mRegister[X];
		mRegister[SUM_XX] -= mRegister[X] * mRegister[X];
		mRegister[SUM_Y]  -= mRegister[Y];
		mRegister[SUM_YY] -= mRegister[Y] * mRegister[Y];
		mRegister[SUM_XY] -= mRegister[X] * mRegister[Y];
		  
		mRegister[X] = mRegister[N];
		showResult();
		mPanelSwitcher.moveTo(PanelSwitcher.DIGITS);
		mState = STATE_NO_STACKLIFT;
	}

	public void onMean() {
    	getXfromInput();
		mRegister[Y] = mRegister[SUM_Y] / mRegister[N];
		mRegister[X] = mRegister[SUM_X] / mRegister[N];
		showResult();
	}

	public void onStandardDeviation() {
    	getXfromInput();
		mRegister[Y] = Math.sqrt((mRegister[SUM_YY] - mRegister[SUM_Y] * mRegister[SUM_Y] / mRegister[N]) / (mRegister[N] - 1));
		mRegister[X] = Math.sqrt((mRegister[SUM_XX] - mRegister[SUM_X] * mRegister[SUM_X] / mRegister[N]) / (mRegister[N] - 1));
		showResult();
	}

	public void onFactorial() {
    	getXfromInput();

		double f;
		
		if ( Double.isNaN(mRegister[X]) ) {
			f = Double.NaN;
		} else if ( mRegister[X] > 170. ) {
    		f = Double.POSITIVE_INFINITY;
    	} else if ( mRegister[X] < 0. ) {
    		f = Double.NaN;
    	} else {
    		double n = Math.floor(mRegister[X]);
    		
    		for ( f = 1.;
    		      n > 1.;
    		      n--
    		    ) {
    			f *= n;
    		}
    	}
    	
		oneOpResult(f);
	}

	public void onPermutation() {
    	getXfromInput();

    	double n = Math.floor(Math.abs(mRegister[Y]));
    	double k = Math.floor(Math.abs(mRegister[X]));
    	double p;
    	
    	if ( k <= 0. ) {
    		p = 1.;
    	} else if ( n <= Math.pow(Double.MAX_VALUE, 1./k) ) {
    		for ( p = 1.;
    			  k > 0.;
    			  k--, n--
    			) {
    			p *= n;
    		}
    	} else {
    		p = Double.POSITIVE_INFINITY;
    	}
		
		twoOpResult(p);
	}

	public void onCombination() {
    	getXfromInput();
		
    	double n = Math.floor(Math.abs(mRegister[Y]));
    	double k = Math.floor(Math.abs(mRegister[X]));
    	double c;
    	
    	if ( k <= 0. ) {
    		c = 1.;
    	} else if ( n <= Math.pow(Double.MAX_VALUE, 1./k)*Math.pow(k, k/2. + 1.) ) {
    		for ( c = 1.;
    			  k > 0.;
    		      k--, n--
    		    ) {
    				c /= k;
    				c *= n;
    		}
    	} else {
    		c = Double.POSITIVE_INFINITY;
    	}
    	
		twoOpResult(c);
	}

	public void onMod() {
		getXfromInput();
		
		double mod = Math.IEEEremainder(mRegister[Y], mRegister[X]);
		
		if (   (mod < 0. && mRegister[X] > 0.)
			|| (mod > 0. && mRegister[X] < 0.)
		   ) {
			mod += mRegister[X];
		}
		
    	twoOpResult(mod);
	}

	public void onToRect() {
		getXfromInput();
		
		double    x  = mRegister[X] *  Math.cos(mRegister[Y] * m2RadFactor);
		mRegister[Y] = mRegister[X] *  Math.sin(mRegister[Y] * m2RadFactor);
		mRegister[X] = x;
		
		showResult();
	}

	public void onToPolar() {
		getXfromInput();
		
		double    r  = Math.hypot(mRegister[Y], mRegister[X]);
        mRegister[Y] = Math.atan2(mRegister[Y], mRegister[X]) / m2RadFactor;
        mRegister[X] = r;
        
        showResult();
	}

	public void onToHms() {
		getXfromInput();

		boolean sign;
		double  v;
		
		if ( mRegister[X] < 0. ) {
			sign = true;
			v    = -mRegister[X];
		} else {
			sign = false;
			v    = mRegister[X];
		}
		
		double h = Math.floor(v);
		       v = 60.*(v - h + 2e-14);
		double m = Math.floor(v);
		       v = 60.*(v - m - 60.*2e-14);
		
		h += m/100. + v/10000.;
		
		if ( sign ) {
			h = -h;
		}
		
		oneOpResult(h);
	}

	public void onToHr() {
		getXfromInput();
		
		boolean sign;
		double  v;
		
		if ( mRegister[X] < 0. ) {
			sign = true;
			v    = -mRegister[X];
		} else {
			sign = false;
			v    = mRegister[X];
		}
		
		double h = Math.floor(v);
		       v = 100.*(v - h + 2e-14);
		double m = Math.floor(v);
		       v = 100.*(v - m - 100.*2e-14);
		
		h += m/60. + v/3600.;
		
		if ( sign ) {
			h = -h;
		}
		
		oneOpResult(h);
	}

	public void onSwap() {
		getXfromInput();

		double  y = mRegister[X];
		mRegister[X] = mRegister[Y];
		mRegister[Y] = y;
		
		showResult();
	}

	public void onRollDown() {
		getXfromInput();

		double  h = mRegister[X];
		mRegister[X] = mRegister[Y];
		mRegister[Y] = mRegister[Z];
		mRegister[Z] = mRegister[T];
		mRegister[T] = h;
		
		showResult();
	}

	public void onRollUp() {
		getXfromInput();

		double  h = mRegister[T];
		mRegister[T] = mRegister[Z];
		mRegister[Z] = mRegister[Y];
		mRegister[Y] = mRegister[X];
		mRegister[X] = h;
		
		showResult();
	}

	public void onPrimeFactor() {
		getXfromInput();
		
		double dx, dp, dn;
		
		if ( Math.abs(mRegister[X]) < 1. ) {
			dx = 0.;
			dp = 0.;
			dn = 1.;
		} else if ( mRegister[X] > 1e10 ) {
			dx = mRegister[X];
			dp = Double.NaN;
			dn = Double.NaN;
		} else {
			long x   = new Double(mRegister[X]).longValue();
			long p;
			long n;
			
			if ( Math.abs(x) == 1 ) {
				p = 1;
			} else if ( (x & 1) == 0 ) {
				p = 2;
			} else if ( (x % 3) == 0 ) {
				p = 3;
			} else {
				long max = new Double(Math.sqrt(Math.abs(mRegister[X]))).longValue();

				p = x;
				
				for ( long q = 5, s = 4;
				      q <= max;
				      q += (s = 6 -s)
					) {
					if ( (x % q) == 0 ) {
						p = q;
						break;  //==========================================v
					}
				}	
			}
			
			if ( p > 1 ) {
				for ( n = 0;
			          (x % p) == 0;
			          n++, x /= p
			    	);
			} else {
				n = 1;
			}
			
			dx = x;
			dp = p;
			dn = n;
		}
		
		mRegister[T] = mRegister[Y];
		mRegister[Z] = dx;
		mRegister[Y] = dp;
		mRegister[X] = dn;
		
		showResult();
	}

	private long gcd(long n1, long n2) {
		n1 = Math.abs(n1);
		n2 = Math.abs(n2);
		
		if ( n1 < n2 ) {
			long h = n1;
			n1 = n2;
			n2 = h;
		}
		
		if ( n2 > 0 ) {
			long n3;
		
			while ( (n3 = n1 % n2) > 0 ) {
				n1 = n2;
				n2 = n3;
			}
		}
		
		return n2;
	}
	
	public void onGCD() {
		getXfromInput();
		
		long   n1 = new Double(mRegister[X]).longValue();
		long   n2 = new Double(mRegister[Y]).longValue();
		double res;
		
		if ( n1 == 0 || n2 == 0 ) {
			res = Double.NaN;
		} else {
			res = (double) gcd(n1, n2);
		}
		
		twoOpResult(res);
	}
	
	public void onLCM() {
		getXfromInput();
		
		long   n1 = new Double(mRegister[X]).longValue();
		long   n2 = new Double(mRegister[Y]).longValue();
		double res;
		
		if ( n1 == 0 || n2 == 0 ) {
			res = 0.;
		} else {
			res = (double) n1 / gcd(n1, n2) * n2;
		}
		
		twoOpResult(res);
	}
    
	public void onDigit(char digit) {
		switch ( mState ) {
		case STATE_RESULT:
			stackLift();
			//vvvvvvvvvvvvvvvvvvvvvvvvvvvv
			
		case STATE_NO_STACKLIFT:
			initInput();
			mState = STATE_INPUT_MANTISSA_BEFORE_DOT;
			//vvvvvvvvvvvvvvvvvvvvvvvvvvvv
			
		case STATE_INPUT_MANTISSA_BEFORE_DOT:
		case STATE_INPUT_MANTISSA_AFTER_DOT:
			mMantissa.append(digit);
			updateDisplay();
			break;

		case STATE_INPUT_EXPONENT:
			mExponent.append(digit);
			updateDisplay();
			break;
			
		case STATE_STORE:
			onStore(digit - '0');
			break;
			
		case STATE_RECALL:
			onRecall(digit - '0');
			break;
		} 
		
	}
	
	public void onDigit0() { onDigit('0'); }
	public void onDigit1() { onDigit('1'); }
	public void onDigit2() { onDigit('2'); }
	public void onDigit3() { onDigit('3'); }
	public void onDigit4() { onDigit('4'); }
	public void onDigit5() { onDigit('5'); }
	public void onDigit6() { onDigit('6'); }	
	public void onDigit7() { onDigit('7'); }
	public void onDigit8() { onDigit('8'); }
	public void onDigit9() { onDigit('9'); }
	
	public void onDot() {
		switch ( mState ) {
		case STATE_RESULT:
			stackLift();
			//vvvvvvvvvvvvvvvvvvvvvvvvvvvv
			
		case STATE_NO_STACKLIFT:
			initInput();
			//mState = STATE_INPUT_MANTISSA_BEFORE_DOT;
			//vvvvvvvvvvvvvvvvvvvvvvvvvvvv
			
		case STATE_INPUT_MANTISSA_BEFORE_DOT:
			mMantissa.append('.');
			mState = STATE_INPUT_MANTISSA_AFTER_DOT;
			break;
		}

		updateDisplay();
	}
	
	
	private void stackLift() {
		mRegister[T] = mRegister[Z];
		mRegister[Z] = mRegister[Y];
		mRegister[Y] = mRegister[X];
		//mRegister[X] = 0.;
	}
	
	private void initInput() {
		//mState = STATE_NO_STACKLIFT;
		mMSign = mESign = false;
		mMantissa.setLength(0);
		mExponent.setLength(0);
	}

	public void onChangeSign() {
		switch ( mState ) {
		case STATE_INPUT_MANTISSA_BEFORE_DOT:
		case STATE_INPUT_MANTISSA_AFTER_DOT:
			mMSign = !mMSign;
			updateDisplay();
			break;

		case STATE_INPUT_EXPONENT:
			mESign = !mESign;
			updateDisplay();
			break;
			
		default:
			oneOpResult(-mRegister[X]);  // change sign operator
			break;
		} 
	}

	public void onExponent() {
		switch ( mState ) {
		case STATE_RESULT:
		case STATE_STORE:
		case STATE_RECALL:
			stackLift();
			//vvvvvvvvvvvvvvvvvvvvvvvvvvv
			
		case STATE_NO_STACKLIFT:
			initInput();
			break;
		}
		
		if ( mMantissa.length() == 0 ) {
			mMantissa.append('1');
		} else {
			double mantissa = new Double(mMantissa.toString());
		
			if (  mantissa == 0. ) {
				mMantissa.setLength(0);
				mMantissa.append('1');
			}
		}
		
		mState = STATE_INPUT_EXPONENT;
		
		updateDisplay();
	}

	public void onStore(int index) {
		index = R0 + Math.max(0, Math.min(9, index));
		
		switch ( mOpenStoreOp ) {
		case R.id.plus:
			mRegister[index] += mRegister[X];
			break;
			
		case R.id.minus:
			mRegister[index] -= mRegister[X];
			break;
			
		case R.id.mul:
			mRegister[index] *= mRegister[X];
			break;
			
		case R.id.div:
			mRegister[index] /= mRegister[X];
			break;
			
		default:
			mRegister[index] = mRegister[X];
			break;
		}
		
		showResult();
	}
	
	public void onRecall(int index) {
		getXfromInput();
		noOpResult(mRegister[R0 + Math.max(0, Math.min(9, index))]);
	}

	private void updateDisplay() {
		StringBuilder number = new StringBuilder(mMantissa);
		
		DoubleFormatter.formatMantissa(number);
		
		if ( mMSign ) {
			number.insert(0, '-');
		}
		
		if ( mState == STATE_INPUT_EXPONENT ) {
			number.append('e');
			
			if ( mESign ) {
				number.append('-');
			}
			
			number.append(mExponent);
		}
		
		mDisplay.setText(number.toString());
	}

	private void getXfromInput() {
		switch ( mState ) {
		case STATE_INPUT_MANTISSA_BEFORE_DOT:
		case STATE_INPUT_MANTISSA_AFTER_DOT:
		case STATE_INPUT_EXPONENT:
			StringBuilder number = new StringBuilder(mMSign ? "-" : "");
		
			number.append(mMantissa);
		
			if ( mExponent.length() > 0 ) {
				number.append("e");
				number.append(mESign ? "-" : "");
				number.append(mExponent);
			}
		
			mRegister[X] = new Double(number.toString());
			initInput();  // not necessary but save space
			mState = STATE_RESULT;
		}
	}
	
	private void showResult() {
		mState = STATE_RESULT;

		setStorageIndicator("");
		mDisplay.setText(mFormatter.format(mRegister[X]));
	}
	
	private void initRadFactor() {
		switch ( mAngleFormat ) {
		default:
			mAngleFormat = RAD;
			//vvvvvvvvvvvvvvvvvvvvvv
			
		case RAD:
			m2RadFactor = 1.;
			break;

		case DEG:
			m2RadFactor = Math.PI / 180.;
			break;

		case GRAD:
			m2RadFactor = Math.PI / 200.;
			break;

		}
	}
	
	public void load(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(LOGIC_PREFS, Context.MODE_PRIVATE);
		long              zero  = Double.doubleToLongBits(0.);
		
		for ( int i = 0; i < mRegister.length; i++ ) {
			mRegister[i] = Double.longBitsToDouble(prefs.getLong(PREF_REGISTER[i], zero));
		}
		
		mState         = prefs.getInt(PREF_STATE,          STATE_RESULT);
		mOpenStoreOp   = prefs.getInt(PREF_OPEN_STORE_OP,  0);
		int fmt        = prefs.getInt(PREF_DISPLAY_FORMAT, DoubleFormatter.FIX);
		int places     = prefs.getInt(PREF_DISPLAY_PLACES,   2);
		mAngleFormat   = prefs.getInt(PREF_ANGLE_FORMAT,   RAD);
		
		mMSign         = prefs.getBoolean(PREF_MSIGN, false);
		mESign         = prefs.getBoolean(PREF_ESIGN, false);
		
		mMantissa.setLength(0);
		mMantissa.append(prefs.getString(PREF_MANTISSA, ""));

		mExponent.setLength(0);
		mExponent.append(prefs.getString(PREF_EXPONENT, ""));
		
		mFormatter = new DoubleFormatter(fmt, places, context);
		
		initRadFactor();
		
		mAngleFormatIndicator.setText(context.getResources().getStringArray(R.array.angle_formats)[mAngleFormat]);
		mAngleFormatIndicator.getParent().requestLayout();
		
		if ( mState <= STATE_INPUT_EXPONENT ) {
			setStorageIndicator("");
			updateDisplay();
		} else {
			showResult();
		}
	}

	public void save(Context context) {
		SharedPreferences prefs  = context.getSharedPreferences(LOGIC_PREFS, Context.MODE_PRIVATE);
        Editor            editor = prefs.edit();		

        for ( int i = 0; i < mRegister.length; i++ ) {
			editor.putLong(PREF_REGISTER[i], Double.doubleToLongBits(mRegister[i]));
			
		}
		
		editor.putInt(PREF_STATE,          mState);
		editor.putInt(PREF_OPEN_STORE_OP,  mOpenStoreOp);
		editor.putInt(PREF_DISPLAY_FORMAT, mFormatter.getDisplayFormat());
		editor.putInt(PREF_DISPLAY_PLACES, mFormatter.getDisplayPlaces());
		editor.putInt(PREF_ANGLE_FORMAT,   mAngleFormat);
		
		editor.putBoolean(PREF_MSIGN, mMSign);
		editor.putBoolean(PREF_ESIGN, mESign);
		
		editor.putString(PREF_MANTISSA, mMantissa.toString());
		editor.putString(PREF_EXPONENT, mExponent.toString());
		
		editor.commit();
	}
	
	public Bundle getBundle() {
		Bundle bundle = new Bundle();
		
		bundle.putDoubleArray(REGISTER, mRegister);
		
		return bundle;
	}

	public void ShowOptions() {
    	Intent intent = new Intent(mContext, EditOptions.class);
    	intent.putExtras(getBundle());
    	
        mContext.startActivity(intent);
	}
}
