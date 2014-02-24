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
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;

public class MetronomeFragment extends Fragment implements IControlable,
        OnValueChangeListener, Observer {

    private NumberPicker beatPicker;
    private NumberPicker bpmPicker;

    private TempoControl tc;
    private Operator op;
    private Peeper peeper;

    private ImageView circle;
    private AlphaAnimation inAnim;
    private AlphaAnimation outAnim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tc = new TempoControl();
        tc.addObserver(this);

        peeper = new Peeper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.metronome, container, false);
        v.setOnTouchListener(TouchControl.getInstance());

        peeper.setSun((ImageView) v.findViewById(R.id.sun));
        peeper.setTime(SharedPref.getTime(getActivity()));

        beatPicker = (NumberPicker) v.findViewById(R.id.beatCount);
        beatPicker.setMinValue(1);
        beatPicker.setMaxValue(4);
        beatPicker.setOnValueChangedListener(this);
        beatPicker.setValue(SharedPref.getTime(getActivity()));

        bpmPicker = (NumberPicker) v.findViewById(R.id.bpmCount);
        bpmPicker.setMinValue(30);
        bpmPicker.setMaxValue(200);
        bpmPicker.setOnValueChangedListener(this);

        circle = (ImageView) v.findViewById(R.id.circle);

        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);

        op = new Operator(peeper);
        tc.addObserver(op);

        tc.setBPM(SharedPref.getBPM(getActivity()));
        tc.refreshObservers();
        
        op.start();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setBackgroundResource(
                ((MainActivity) getActivity()).getBgRes());
        peeper.setSound((byte) SharedPref.getSound(getActivity()));
    }

    @Override
    public void onPause() {
        SharedPref.setBPM(getActivity(), tc.getBPM());
        SharedPref.setTime(getActivity(), peeper.getTime());

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        op.finish();
        tc.deleteObserver(op);
        tc.deleteObserver(this);
    }

    @Override
    public void onValueChange(TouchControl t, int val) {
        int speed = 2 * SharedPref.getSpeed(getActivity());

        tc.setBPM(tc.getBPM() + val / speed);
    }

    @Override
    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            circle.setVisibility(View.VISIBLE);
            circle.startAnimation(inAnim);
            break;
        case TouchControl.STATE_TOGGLE:
            op.togglePlay();
            break;
        case TouchControl.STATE_OUT:
            if (!inAnim.hasEnded())
                return;
            circle.startAnimation(outAnim);
            circle.setVisibility(View.INVISIBLE);
            break;
        default:
            break;
        }
    }

    @Override
    public void onPositionChange(TouchControl t, float x, float y) {
//        circle.setX(x - circle.getWidth() / 2);
//        circle.setY(y - circle.getHeight() / 2 - 80);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        bpmPicker.setValue(((TempoControl) arg0).getBPM());
    }

	@Override
	public void onValueChange(net.simonvt.numberpicker.NumberPicker picker,
			int oldVal, int newVal) {
		if (picker.equals(beatPicker)) {
            peeper.setTime(newVal);
        }
        if (picker.equals(bpmPicker)) {
            tc.setBPM(newVal);
        }
	}
}
