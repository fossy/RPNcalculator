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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Config;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class RpnCalculator extends Activity {
    EventListener mListener = new EventListener();
    private CalculatorDisplay mDisplay;
    private TextView          mAngleFormatIndicator;
    private TextView          mStorageIndicator;
    private Logic             mLogic;
    private PanelSwitcher     mPanelSwitcher;

    private static final int CMD_DIGITS_PANEL     = 1;
    private static final int CMD_FUNCTIONS_PANEL  = 2;
    private static final int CMD_STATISTICS_PANEL = 3;
    private static final int CMD_OPTIONS          = 4;
    private static final int CMD_HELP             = 5;

    private static final String LOG_TAG = "RPNcalculator";
    private static final boolean DEBUG  = true;
    private static final boolean LOG_ENABLED = DEBUG ? Config.LOGD : Config.LOGV;

    private static final String PANEL_INDEX = "PANEL_INDEX";
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.main);

        mDisplay              = (CalculatorDisplay) findViewById(R.id.display);
        mAngleFormatIndicator = (TextView)          findViewById(R.id.angle_format_indicator);
        mStorageIndicator     = (TextView)          findViewById(R.id.storage_indicator);
        mPanelSwitcher        = (PanelSwitcher)     findViewById(R.id.panelswitch);

        int panel = PanelSwitcher.DIGITS;
        
        if ( icicle != null ) {
        	panel = icicle.getInt(PANEL_INDEX, PanelSwitcher.DIGITS);
        }

        mPanelSwitcher.setIndicators( (TextView) findViewById(R.id.leftkb_indicator),
		                              (TextView) findViewById(R.id.kb_indicator),
		                              (TextView) findViewById(R.id.rightkb_indicator)
        		);
    	mPanelSwitcher.setViewIndex(panel);

        mLogic = new Logic(this, mPanelSwitcher, mDisplay, mAngleFormatIndicator, mStorageIndicator);

        mListener.setHandler(mLogic, mPanelSwitcher, this);

        mDisplay.setOnKeyListener(mListener);

        View view;
        
        if ( (view = findViewById(R.id.del)) != null ) {
            view.setOnLongClickListener(mListener);
        }

        if ( (view = findViewById(R.id.rcl)) != null ) {
            view.setOnLongClickListener(mListener);
        }
    }

    /***
    @Override
    public void onStart() {
    	super.onStart();
    	mPanelSwitcher.getParent().requestLayout(); //.setViewIndex(mPanelSwitcher.getCurrentIndex());
    }
    ***/
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, CMD_DIGITS_PANEL,     0, R.string.digits_panel    ).setIcon(R.drawable.digits);
        menu.add(0, CMD_FUNCTIONS_PANEL,  0, R.string.functions_panel ).setIcon(R.drawable.functions);
        menu.add(0, CMD_STATISTICS_PANEL, 0, R.string.statistics_panel).setIcon(R.drawable.statistics);
        menu.add(0, CMD_OPTIONS,          0, R.string.options         ).setIcon(R.drawable.options);
        menu.add(0, CMD_HELP,             0, R.string.help            ).setIcon(R.drawable.help);

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(CMD_DIGITS_PANEL).setVisible(mPanelSwitcher != null && 
                          mPanelSwitcher.getCurrentIndex() != PanelSwitcher.DIGITS);
        
        menu.findItem(CMD_FUNCTIONS_PANEL).setVisible(mPanelSwitcher != null && 
                          mPanelSwitcher.getCurrentIndex() != PanelSwitcher.FUNCTIONS);
        
        menu.findItem(CMD_STATISTICS_PANEL).setVisible(mPanelSwitcher != null && 
                		  mPanelSwitcher.getCurrentIndex() != PanelSwitcher.STATISTICS);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CMD_DIGITS_PANEL:
            if ( mPanelSwitcher != null ) { 
                mPanelSwitcher.moveTo(PanelSwitcher.DIGITS);
            }
            break;

        case CMD_FUNCTIONS_PANEL:
            if ( mPanelSwitcher != null ) { 
                mPanelSwitcher.moveTo(PanelSwitcher.FUNCTIONS);
            }
            break;

        case CMD_STATISTICS_PANEL:
            if ( mPanelSwitcher != null ) { 
                mPanelSwitcher.moveTo(PanelSwitcher.STATISTICS);
            }
            break;

        case CMD_OPTIONS:
        	mLogic.ShowOptions();
        	//mPanelSwitcher.setViewIndex(mPanelSwitcher.getCurrentIndex());
            break;

        case CMD_HELP:
        	Uri    uri    = Uri.parse("http://fossy.iwasno.net/RPNcalculator/?p=help");
        	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        	startActivity(intent);
            break;
       }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
    	super.onSaveInstanceState(icicle);
    	
    	icicle.putInt(PANEL_INDEX, mPanelSwitcher.getCurrentIndex());
    }

    @Override
    public void onResume() {
    	super.onResume();
    	mLogic.load(this);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        mLogic.save(this);
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if ( keyCode == KeyEvent.KEYCODE_DPAD_LEFT ) {
            mPanelSwitcher.moveRight();
            return true;  //================================================>
        } else if ( keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ) {
            mPanelSwitcher.moveLeft();
            return true;  //================================================>
        } else {
            return super.onKeyDown(keyCode, keyEvent);  //==================>
        }
    }*/

    static void log(String message) {
        if ( LOG_ENABLED ) {
            Log.v(LOG_TAG, message);
        }
    }
}
