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

import java.util.Observable;
import java.util.Observer;

import kiv.janecekz.ma.metronome.Operator;
import kiv.janecekz.ma.metronome.Peeper;
import kiv.janecekz.ma.metronome.TempoControl;
import kiv.janecekz.ma.prefs.SharedPref;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.michaelnovakjr.numberpicker.NumberPicker.OnChangedListener;

@SuppressLint("NewApi")
public class MetronomeFragment extends Fragment implements IControlable,
		Observer {

	private android.widget.NumberPicker beatPicker;
	private android.widget.NumberPicker bpmPicker;
	private com.michaelnovakjr.numberpicker.NumberPicker beatPickerOld;
	private com.michaelnovakjr.numberpicker.NumberPicker bpmPickerOld;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.metronome, container, false);
		RelativeLayout space = (RelativeLayout) v.findViewById(R.id.sunspace);
		space.setOnTouchListener(TouchControl.getInstance());

		peeper = new Peeper((byte) 0, (ImageView) v.findViewById(R.id.sun));
		peeper.setTime(SharedPref.getTime(getActivity()));

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			beatPickerOld = (com.michaelnovakjr.numberpicker.NumberPicker) v
					.findViewById(R.id.beatCount);
			beatPickerOld.setRange(1, 4);
			beatPickerOld.setCurrent(SharedPref.getTime(getActivity()));
			beatPickerOld.setOnChangeListener(new OnChangedListener() {
				@Override
				public void onChanged(com.michaelnovakjr.numberpicker.NumberPicker picker,
						int oldVal, int newVal) {
					peeper.setTime(newVal);
				}
			});

			bpmPickerOld = (com.michaelnovakjr.numberpicker.NumberPicker) v
					.findViewById(R.id.bpmCount);
			bpmPickerOld.setRange(30, 220);
			bpmPickerOld.setCurrent(SharedPref.getBPM(getActivity()));
			bpmPickerOld.setOnChangeListener(new OnChangedListener() {
				@Override
				public void onChanged(com.michaelnovakjr.numberpicker.NumberPicker picker,
						int oldVal, int newVal) {
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
					peeper.setTime(newVal);
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
		RelativeLayout.LayoutParams pars = (LayoutParams) circle.getLayoutParams();
		pars.setMargins(x - circle.getWidth() / 2, y - circle.getHeight() / 2, 0, 0);
		
		circle.setLayoutParams(pars);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			bpmPickerOld.setCurrent(((TempoControl) arg0).getBPM());
		} else {
			bpmPicker.setValue(((TempoControl) arg0).getBPM());
		}
	}
}
