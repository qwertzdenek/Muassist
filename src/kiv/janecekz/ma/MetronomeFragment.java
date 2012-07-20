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

import java.util.Observable;
import java.util.Observer;

import kiv.janecekz.ma.metronome.Operator;
import kiv.janecekz.ma.metronome.Peeper;
import kiv.janecekz.ma.metronome.TempoControl;
import kiv.janecekz.ma.prefs.SharedPref;
import android.animation.AnimatorSet;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

public class MetronomeFragment extends Fragment implements OnMyEvent,
        OnValueChangeListener, Observer {

    private NumberPicker beatPicker;
    private NumberPicker bpmPicker;

    private TempoControl tc;
    private Operator op;
    private Peeper peeper;

    private ImageView circle;
    private AnimatorSet inAnim;
    private AnimatorSet outAnim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tc = new TempoControl();
        tc.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.metronome, container, false);
        v.setOnTouchListener(TouchControl.getInstance());

        peeper = new Peeper((ImageView) v.findViewById(R.id.sun));

        beatPicker = (NumberPicker) v.findViewById(R.id.beatCount);
        beatPicker.setMinValue(1);
        beatPicker.setMaxValue(4);
        beatPicker.setOnValueChangedListener(this);

        bpmPicker = (NumberPicker) v.findViewById(R.id.bpmCount);
        bpmPicker.setMinValue(30);
        bpmPicker.setMaxValue(200);
        bpmPicker.setOnValueChangedListener(this);

        circle = (ImageView) v.findViewById(R.id.circle);

        inAnim = TouchControl.getInAnim(circle);
        outAnim = TouchControl.getOutAnim(circle);
        
        return v;
    }

    @Override
    public void onPause() {
        SharedPref.setBPM(getActivity(), tc.getBPM());

        op.interrupt();
        tc.deleteObserver(op);
        super.onPause();
    }

    @Override
    public void onResume() {
        peeper.setSound((byte) SharedPref.getSound(getActivity()));
        peeper.setTime(SharedPref.getTime(getActivity()));
        
        op = new Operator(peeper);
        tc.addObserver(op);

        tc.setBPM(SharedPref.getBPM(getActivity()));
        tc.refreshObservers();

        op.start();
        if (SharedPref.getPlay(getActivity())) {
            op.setPlay(true);
        }
        super.onResume();
    }

    public void onValueChange(TouchControl t, float val) {
//        Log.d(MainActivity.TAG, "get val: "+val);
        tc.setBPM((int) (tc.getBPM() + val / 100));
    }

    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            inAnim.start();
            break;
        case TouchControl.STATE_STOP:
            SharedPref.setPlay(getActivity(),
                    !SharedPref.getPlay(getActivity()));
            op.setPlay(SharedPref.getPlay(getActivity()));
            break;
        case TouchControl.STATE_OUT:
            if (inAnim.isRunning())
                inAnim.cancel();
            outAnim.start();
            break;
        default:
            break;
        }
    }

    public void onPositionChange(TouchControl t, float x, float y) {
        circle.setX(x - circle.getWidth() / 2);
        circle.setY(y - circle.getHeight() / 2);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (picker.equals(beatPicker)) {
            peeper.setTime(newVal);
        }
        if (picker.equals(bpmPicker)) {
            tc.setBPM(newVal);
        }
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        bpmPicker.setValue(((TempoControl) arg0).getBPM());
    }
}
// metoda OnToggle()
// if (!wl.isHeld()) {
// wl.acquire();
// } else {
// wl.release();
