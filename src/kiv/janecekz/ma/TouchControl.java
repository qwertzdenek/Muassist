package kiv.janecekz.ma;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * I use this class to collect onTouch events and get final decision what to do
 * from that.
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

    public static TouchControl getInstance() {
        if (instance == null) {
            instance = new TouchControl();
        }
        return instance;
    }

    private VelocityTracker vt;
    private OnMyEvent target;

    /*
     * Touch control
     */
    // TODO: This should be editable.
    private final int DOUBLE_CLICK_DELAY = 500;
    private float biggestSpeed;
    private long startTime;
    private long stopTime;
    private boolean stopping = false;

    @Override
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

            float speed = vt.getXVelocity();
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

    public void registerOnMyEvent(OnMyEvent target) {
        this.target = target;
    }
}
