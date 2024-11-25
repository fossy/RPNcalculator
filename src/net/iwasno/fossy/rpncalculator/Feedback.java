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
import android.os.Vibrator;
import android.provider.Settings;


/**
 * Feedback
 */
class Feedback extends Object {
    static Vibrator sVibrator      = null;
    static long     sVibrateNormal = 30;
    static long     sVibrateLong   = 50;
   
    public Feedback(Context context) {
    	if (   sVibrator == null
        	&& Settings.System.getInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0
           ) {
    		sVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public void feedback() {
    	if ( sVibrator != null && sVibrateNormal > 0 ) {
    		sVibrator.vibrate(sVibrateNormal);
    	}
    }

    public void normalFeedback() {
    	feedback();
    }
    
    public void longFeedback() {
    	if ( sVibrator != null && sVibrateLong > 0 ) {
    		sVibrator.vibrate(sVibrateLong);
    	}
    }
}
