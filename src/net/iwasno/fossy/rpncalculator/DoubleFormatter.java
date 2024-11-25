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

import java.util.Formatter;
import java.util.Locale;

import android.content.Context;
import android.graphics.Paint;

class DoubleFormatter {
    static final int FIX = 0;
    static final int SCI = 1;
    static final int ENG = 2;
    
    static final int MAX_TOTAL_DIGITS = 15;

    private int    mDisplayFormat;	// FIX, SCI, ENG
    private int    mDisplayPlaces;	// digits behind point '.' (exception: ENG)
    private String mFormat;			// printf format
    private double mFixMin;			// smallest number for FIX
    private double mFixMax;			// greatest number for FIX (with mDisplayPlaces)
    private int    mNumDigits;      // number of possible digit with FIX (before and behind dot)

    static private String mNan;
    static private String mInfinity;
    
	DoubleFormatter(int fmt, int places, Context context) {
		init(fmt, places, context/*, -1*/);
	}

	private void init(int fmt, int places, Context context/*, int numChar*/) {
		mDisplayFormat = fmt;
		mDisplayPlaces = places;
		
		char c = 'f';
		
		StringBuilder format = new StringBuilder("%.");
		
		switch ( mDisplayFormat ) {
		default:
			fmt = FIX;
			//vvvvvvvvvvvvvvvvvvvv
			
		case FIX:
			mFixMin = Math.pow(10., -mDisplayPlaces);
			//mFixMax = Math.pow(10., MAX_TOTAL_DIGITS - mDisplayPlaces);
			break;
			
		case ENG:
			places = Math.min(9, places + 2);
			//vvvvvvvvvvvvvvvvvvvv
			
		case SCI:
			c = 'e';
			break;
		}
		
		mFormat = format.append(places).append(c).toString();
		
		setWidth(-1, null);
		
		if ( mNan == null || mInfinity == null ) {
          mNan      = context.getResources().getString(R.string.nan);
          mInfinity = context.getResources().getString(R.string.infinity);
		}
	}
	
	public void setWidth(int pixelWidth, Paint paint) {
		
		int numInt = MAX_TOTAL_DIGITS - mDisplayPlaces;
		
		if ( mDisplayFormat == FIX && pixelWidth > 0 && paint != null ) {
			float digitWidth = paint.measureText("0123456789") / 10f;
			float signWidth  = paint.measureText("+\u2212")    / 2f;
			float dotWidth   = paint.measureText(".");
			float spaceWidth = paint.measureText(" ");
		
			//
			// -12 345 678.1234    FIX4
			// 
			// pixel = signWidth + numInt*digitWidth + floor((numInt-1)/3)*spaceWidth
			//       + dotWidth + mDisplayPlaces*digitWidth
			//
        
			int fixWidth = (int) Math.ceil(signWidth + dotWidth + mDisplayPlaces*digitWidth);
			
			numInt = 1 + (int) (  (pixelWidth - fixWidth)
			       	            / (spaceWidth/3f + digitWidth)
							   );
			while ( Math.floor((numInt - 1)/3f)*spaceWidth + numInt*digitWidth + fixWidth > pixelWidth ) {
				numInt--;
			}
		}
		
		mNumDigits = Math.min( MAX_TOTAL_DIGITS, numInt + mDisplayPlaces);
		mFixMax    = Math.pow(10., numInt);
	}
	
	public int getDisplayFormat() {
		return mDisplayFormat;
	}

	public int getDisplayPlaces() {
		return mDisplayPlaces;
	}

	public static void formatMantissa(StringBuilder number) {
		if ( number.length() > 0 ) {
			// insert blanks every 3 digits
			int start = number.charAt(0) == '-' ? 1 : 0;
			int end   = number.indexOf(".");
		
			if ( end < 0 ) end = number.length();
		
			int n = end - start;

			int i = start + (n - 1) % 3 + 1;
		
			while ( i < end ) {
				number.insert(i, ' ');
				i += 4;
				end++;
			}
		}
	}
	
	public String format(double x) {
		StringBuilder result = new StringBuilder();
		
		if ( Double.isNaN(x) ) {
			result.append(mNan);
		} else if ( Double.isInfinite(x) ) {
			if ( x < 0. ) {
				result.append('-');
			}
			
			result.append(mInfinity);
		} else {
			int    fmt  = mDisplayFormat;
			String mask = mFormat;
			double absX = Math.abs(x);
			
			if ( fmt == FIX && x != 0. ) {
				if ( absX >= 1e10 || absX <= mFixMin ) {
					fmt  = SCI;
					mask = new String("%.9e");
				} else if ( absX >= mFixMax ) {
					mask = new StringBuilder("%.").append(mNumDigits - 1 - (int) Math.log10(absX)).append('f').toString();
				}
			} 
			
			if ( x < 0. ) {
				result.append("\u2212");
			}
			
			result.append(new Formatter(new Locale("us")).format(mask, absX).toString());
			
			if ( fmt == FIX ) {
				formatMantissa(result);
			} else {
				int idxOfE   = result.indexOf("e");
				int exponent = new Integer(result.substring(idxOfE + 2));
				
				if ( result.charAt(idxOfE + 1) == '-' ) {
					exponent = -exponent;
				}
				
				if ( fmt == ENG ) {
					int offset = exponent % 3;
				
					if ( offset < 0 ) {
						offset += 3;
					}
				
					if ( offset > 0 ) {
						int dot = result.indexOf(".");
					
						result.deleteCharAt(dot);
						result.insert(dot + offset, '.');
					
						exponent -= offset;
					}
				}
				
				result.setLength(idxOfE);
				result.append(exponent < 0 ? "\u2212" : "+")
				      .append(new Formatter(new Locale("us")).format("%03d", Math.abs(exponent))
				      .toString())
				      ;
			}
		}
		
		return result.toString();
	}
}