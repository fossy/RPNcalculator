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

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Button with click-animation effect.
 */
class ColorButton extends Button implements OnClickListener {
    
    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        RpnCalculator calc = (RpnCalculator) context;
        
        setOnClickListener(calc.mListener);
    }
    
	public void onClick(DialogInterface dialog, int which) {
	}

	/***
    @Override
    protected void onDraw (Canvas canvas) {
    	RpnCalculator.log("ColorButton.onDraw id=" + getId());
    	
    	super.onDraw(canvas);
    }
    ***/
}
