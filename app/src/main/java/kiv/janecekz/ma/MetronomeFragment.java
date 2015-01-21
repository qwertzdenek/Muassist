/*
Musicians Assistant
    Copyright (C) 2012-2014  Zdeněk Janeček <jan.zdenek@gmail.com>

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

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import java.util.Observable;
import java.util.Observer;

import kiv.janecekz.ma.metronome.Operator;
import kiv.janecekz.ma.metronome.Peeper;
import kiv.janecekz.ma.metronome.TempoControl;
import kiv.janecekz.ma.prefs.SharedPref;

public class MetronomeFragment extends Fragment implements IControlable,
        Observer, OnClickListener {

    private android.widget.NumberPicker beatPicker;
    private android.widget.NumberPicker bpmPicker;
    private net.simonvt.numberpicker.NumberPicker beatPickerOld;
    private net.simonvt.numberpicker.NumberPicker bpmPickerOld;

    private TempoControl tc;
    private Operator op;
    private Peeper peeper;

    private ImageView circle;
    private AlphaAnimation inAnim;
    private AlphaAnimation outAnim;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.metronome, container, false);
        RelativeLayout space = (RelativeLayout) v.findViewById(R.id.sunspace);
        space.setOnTouchListener(TouchControl.getInstance());

        peeper = new Peeper((byte) 0, (ImageView) v.findViewById(R.id.pend));
        peeper.setBeats(SharedPref.getTime(getActivity()));

        tc = new TempoControl();
        tc.addObserver(this);

        v.findViewById(R.id.split1).setOnClickListener(this);
        v.findViewById(R.id.split2).setOnClickListener(this);
        v.findViewById(R.id.split3).setOnClickListener(this);
        v.findViewById(R.id.split4).setOnClickListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            beatPickerOld = (net.simonvt.numberpicker.NumberPicker) v
                    .findViewById(R.id.beatCount);
            beatPickerOld.setMinValue(1);
            beatPickerOld.setMinValue(4);
            beatPickerOld.setValue(SharedPref.getTime(getActivity()));
            beatPickerOld.setOnValueChangedListener(new net.simonvt.numberpicker.NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(net.simonvt.numberpicker.NumberPicker picker, int oldVal, int newVal) {
                    peeper.setBeats(newVal);
                }
            });

            bpmPickerOld = (net.simonvt.numberpicker.NumberPicker) v
                    .findViewById(R.id.beatCount);
            bpmPickerOld.setMinValue(30);
            bpmPickerOld.setMinValue(220);
            bpmPickerOld.setValue(SharedPref.getBPM(getActivity()));
            bpmPickerOld.setOnValueChangedListener(new net.simonvt.numberpicker.NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(net.simonvt.numberpicker.NumberPicker picker, int oldVal, int newVal) {
                    tc.setBPM(newVal);
                }
            });
        } else {
            beatPicker = (NumberPicker) v.findViewById(R.id.beatCount);
            beatPicker.setMinValue(1);
            beatPicker.setMaxValue(4);
            beatPicker.setValue(SharedPref.getTime(getActivity()));
            beatPicker.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal,
                                          int newVal) {
                    peeper.setBeats(newVal);
                }
            });

            bpmPicker = (NumberPicker) v.findViewById(R.id.bpmCount);
            bpmPicker.setMinValue(30);
            bpmPicker.setMaxValue(200);
            bpmPicker.setValue(SharedPref.getBPM(getActivity()));
            bpmPicker.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal,
                                          int newVal) {
                    tc.setBPM(newVal);
                }
            });
        }

        circle = (ImageView) v.findViewById(R.id.circle);

        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN, 300);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT, 300);

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
        SharedPref.setTime(getActivity(), peeper.getBeats());

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDetach();

        op.finish();
        tc.deleteObserver(op);
        tc.deleteObserver(this);
        peeper.cleanup();
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
                    inAnim.cancel();
                circle.startAnimation(outAnim);
                circle.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPositionChange(TouchControl t, int x, int y) {
        RelativeLayout.LayoutParams pars = (LayoutParams) circle
                .getLayoutParams();
        pars.setMargins(x - circle.getWidth() / 2, y - circle.getHeight() / 2,
                0, 0);

        circle.setLayoutParams(pars);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            bpmPickerOld.setValue(((TempoControl) arg0).getBPM());
        } else {
            bpmPicker.setValue(((TempoControl) arg0).getBPM());
        }
    }

    @Override
    public void onClick(View arg0) {
        if (!(arg0 instanceof Button))
            return;

        int split = 0;

        switch (arg0.getId()) {
            case R.id.split1:
                split = 0;
                break;
            case R.id.split2:
                split = 1;
                break;
            case R.id.split3:
                split = 2;
                break;
            case R.id.split4:
                split = 3;
                break;
            default:
                break;
        }

        peeper.setSplit(split);
        op.setSplit(split);
    }
}
