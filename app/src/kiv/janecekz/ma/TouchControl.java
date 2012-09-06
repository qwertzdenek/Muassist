/*
Musicians Assistant
    Copyright (C) 2012  Zdeněk Janeček <jan.zdenek@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kiv.janecekz.ma;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * I use this class to collect onTouch events and get final decision what to do
 * from that. It is necessary to obtain instance using the {@link getInstance()}
 * method. This class has to be singleton.
 * 
 * Register your {@link IControlable} class with registerOnMyEvent(IControlable
 * target).
 * 
 * @author Zdeněk Janeček
 */
public class TouchControl implements OnTouchListener {
    public static final int STATE_BEGIN = 0;
    public static final int STATE_STOP = 1;
    public static final int STATE_OUT = 2;

    private static TouchControl instance;

    private TouchControl() {
        vt = VelocityTracker.obtain();
    }

    /**
     * Use this to obtain instance.
     * @return Instance of TouchControl.
     */
    public static TouchControl getInstance() {
        if (instance == null) {
            instance = new TouchControl();
        }
        return instance;
    }

    private VelocityTracker vt;
    private IControlable target;

    /*
     * Touch control
     */
    // TODO: This should be editable.
    private final int DOUBLE_CLICK_DELAY = 500;
    private int biggestSpeed;
    private long startTime;
    private long stopTime;
    private boolean stopping = false;

    public boolean onTouch(View arg0, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (stopping
                    && ((event.getEventTime() - stopTime) < DOUBLE_CLICK_DELAY)) {

                target.onToggle(this, STATE_STOP);

                stopping = false;
            } else {
                target.onToggle(this, STATE_BEGIN);

                biggestSpeed = 0;
                startTime = event.getEventTime();
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            target.onPositionChange(this, event.getX(), event.getY());

            vt.addMovement(event);
            vt.computeCurrentVelocity(1000);

            int speed = (int) vt.getXVelocity();
            if (biggestSpeed < speed)
                biggestSpeed = speed;

            if (event.getEventTime() - startTime > DOUBLE_CLICK_DELAY) {
                if (Math.abs(speed) > 20) {
                    target.onValueChange(this, speed);
                }
            }

        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if ((Math.abs(biggestSpeed) < 150)
                    && ((event.getEventTime() - startTime) < DOUBLE_CLICK_DELAY)) {
                stopping = true;
                stopTime = event.getEventTime();
            }

            vt.clear();
            target.onToggle(this, STATE_OUT);
        }

        return true;
    }

    /**
     * Reassign controlling of touch events to new target.
     * @param target Usually controlled by the main Activity.
     */
    public void registerOnMyEvent(IControlable target) {
        this.target = target;
    }
}
