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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

public class EditOptions extends Activity {
	
	private Bundle mBundle;
	
	private DoubleFormatter mFormatter;
	
	private Spinner mDisplayFormatSpinner;
	private Spinner mDisplayPlacesSpinner;
	private Spinner mAngleFormatSpinner;
	
	private TextView mRegisterView[] = new TextView [4 + 1 + 10];
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.edit_options);
        
        mBundle = getIntent().getExtras();
        
        mDisplayFormatSpinner = (Spinner) findViewById(R.id.display_format);
        mDisplayPlacesSpinner = (Spinner) findViewById(R.id.display_places);
        mAngleFormatSpinner   = (Spinner) findViewById(R.id.angle_format);
    
        mRegisterView[Logic.LASTX] = (TextView) findViewById(R.id.last_x);
        mRegisterView[Logic.X]     = (TextView) findViewById(R.id.stack_x);
        mRegisterView[Logic.Y]     = (TextView) findViewById(R.id.stack_y);
        mRegisterView[Logic.Z]     = (TextView) findViewById(R.id.stack_z);
        mRegisterView[Logic.T]     = (TextView) findViewById(R.id.stack_t);
        mRegisterView[Logic.R0]    = (TextView) findViewById(R.id.register_0);
        mRegisterView[Logic.R1]    = (TextView) findViewById(R.id.register_1);
        mRegisterView[Logic.R2]    = (TextView) findViewById(R.id.register_2);
        mRegisterView[Logic.R3]    = (TextView) findViewById(R.id.register_3);
        mRegisterView[Logic.R4]    = (TextView) findViewById(R.id.register_4);
        mRegisterView[Logic.R5]    = (TextView) findViewById(R.id.register_5);
        mRegisterView[Logic.R6]    = (TextView) findViewById(R.id.register_6);
        mRegisterView[Logic.R7]    = (TextView) findViewById(R.id.register_7);
        mRegisterView[Logic.R8]    = (TextView) findViewById(R.id.register_8);
        mRegisterView[Logic.R9]    = (TextView) findViewById(R.id.register_9);
        
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener()
			{

        		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        			setValuesFromSpinner();
        		}

        		public void onNothingSelected(AdapterView<?> arg0) {
        			// TODO Auto-generated method stub
				
        		}
        	};
        	
        mDisplayFormatSpinner.setOnItemSelectedListener(listener);
        mDisplayPlacesSpinner.setOnItemSelectedListener(listener);
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    	SharedPreferences prefs = getSharedPreferences(Logic.LOGIC_PREFS, Context.MODE_PRIVATE);
    
        mDisplayFormatSpinner.setSelection(prefs.getInt(Logic.PREF_DISPLAY_FORMAT, DoubleFormatter.FIX));
        mDisplayPlacesSpinner.setSelection(prefs.getInt(Logic.PREF_DISPLAY_PLACES, 2));
		mAngleFormatSpinner.  setSelection(prefs.getInt(Logic.PREF_ANGLE_FORMAT,   Logic.RAD));

		setValuesFromSpinner();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        SharedPreferences prefs  = getSharedPreferences(Logic.LOGIC_PREFS, Context.MODE_PRIVATE);
        Editor            editor = prefs.edit();
        
        editor.putInt(Logic.PREF_DISPLAY_FORMAT, mDisplayFormatSpinner.getSelectedItemPosition());
        editor.putInt(Logic.PREF_DISPLAY_PLACES, mDisplayPlacesSpinner.getSelectedItemPosition());
        editor.putInt(Logic.PREF_ANGLE_FORMAT,   mAngleFormatSpinner.  getSelectedItemPosition());
        
        editor.commit();
    }
    
    private void setValuesFromSpinner() {
    	int fmt    = mDisplayFormatSpinner.getSelectedItemPosition();
    	int places = mDisplayPlacesSpinner.getSelectedItemPosition();
    		
    	if (   mFormatter == null
    		|| mFormatter.getDisplayFormat() != fmt
    		|| mFormatter.getDisplayPlaces() != places
    	   ) {
    		mFormatter = new DoubleFormatter(fmt, places, getApplicationContext());
    	
    		int    i;
    		double d[];
		
    		d = mBundle.getDoubleArray(Logic.REGISTER);
		
    		for ( i = 0; i < mRegisterView.length; i++ ) {
    			mRegisterView[i].setText(mFormatter.format(d[i]));
    		}
    		
    		getWindow().getDecorView().invalidate();
		}
    }
}
