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

import kiv.janecekz.ma.common.Tones;
import kiv.janecekz.ma.prefs.SharedPref;
import kiv.janecekz.ma.tone.Player;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ToneFragment extends Fragment implements IControlable,
		OnEditorActionListener, OnClickListener {

	private ImageView circle;
	private AlphaAnimation inAnim;
	private AlphaAnimation outAnim;

	private Player pl;
	private EditText input;
	private TextView actualFreqView;

	private boolean sharp = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.tone, container, false);
		RelativeLayout space = (RelativeLayout) root.findViewById(R.id.sunspace);
		space.setOnTouchListener(TouchControl.getInstance());

		circle = (ImageView) root.findViewById(R.id.circle);

		inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN, 300);
		outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT, 300);

		input = (EditText) root.findViewById(R.id.tone_value_edit);
		input.setOnEditorActionListener(this);

		TextView sharp = (TextView) root.findViewById(R.id.sharp);
		sharp.setOnClickListener(this);

		ViewGroup defList = (ViewGroup) root.findViewById(R.id.tone_list);
		for (int i = 0; i < defList.getChildCount(); i++) {
			TextView v = (TextView) defList.getChildAt(i);
			v.setOnClickListener(this);
		}
		return root;
	}

	@Override
	public void onPause() {
		pl.interrupt();

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (actualFreqView != null) {
			actualFreqView.setTextColor(getView().getResources().getColor(R.color.blue));
		}
		// Defaulting to the 440 Hz.
		actualFreqView = ((TextView) getView().findViewById(R.id.toneA));
		actualFreqView.setTextColor(Color.RED);

		pl = new Player(getActivity().getApplicationContext());

		pl.setFreq(SharedPref.getBaseFreq(getActivity()));
		pl.start();

		input.setText(Integer.toString(SharedPref.getBaseFreq(getActivity())));

		getView().setBackgroundResource(
				((MainActivity) getActivity()).getBgRes());
	}

	@Override
	public void onValueChange(TouchControl t, int val) {
		// nothing to do
	}

	@Override
	public void onToggle(TouchControl t, int state) {
		switch (state) {
		case TouchControl.STATE_BEGIN:
			circle.setVisibility(View.VISIBLE);
			circle.startAnimation(inAnim);
			break;
		case TouchControl.STATE_TOGGLE:
			pl.togglePlay();
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
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		if (arg0.equals(input) && (arg1 == EditorInfo.IME_ACTION_DONE)) {
			boolean b = pl.setFreq(Float
					.parseFloat(((SpannableStringBuilder) arg0.getText())
							.toString()));
			if (!b) {
				arg0.setText(Float.toString(pl.getFreq()));
			} else {
				pl.togglePlay();
				// FIXME: What if only push the enter?
				if (actualFreqView != null) {
					actualFreqView.setTextColor(getView().getResources().getColor(R.color.blue));
					actualFreqView = null;
				}
			}
		}
		return true;
	}

	@Override
	public void onClick(View arg0) {
		TextView v = (TextView) arg0;

		if (v.getId() == R.id.sharp) {
			sharp = !sharp;
			int pos = 9;
			if (actualFreqView != null && !sharp) {
				pos = getTone(actualFreqView.getId()).getTonePos();
				v.setTextColor(getView().getResources().getColor(R.color.blue));
			} else if (actualFreqView != null && sharp) {
				// FIXME: Use only one method to get pos.

				pos = (getTone(actualFreqView.getId()).getTonePos() + 1) % 12;
				v.setTextColor(Color.RED);
			} else if (actualFreqView == null && sharp) {
				v.setTextColor(Color.RED);
			} else if (actualFreqView == null && !sharp) {
				v.setTextColor(getView().getResources().getColor(R.color.blue));
			}

			int baseFreq = SharedPref.getBaseFreq(getActivity());
			float freq = baseFreq * Tones.freqCoefs[pos];
			input.setText(String.format("%.2f", freq));

			pl.setFreq(freq);
		} else {
			if (actualFreqView != null) {
				actualFreqView.setTextColor(getView().getResources().getColor(R.color.blue));
			}

			actualFreqView = v;

			// Set frequency coefficient accordingly.
			int freqCoefPosition = getTone(v.getId()).getTonePos();
			freqCoefPosition = sharp ? (freqCoefPosition + 1) % 12
					: freqCoefPosition;

			int baseFreq = SharedPref.getBaseFreq(getActivity());
			float freq = baseFreq * Tones.freqCoefs[freqCoefPosition];

			input.setText(String.format("%.2f", freq));
			pl.setFreq(freq);
			if (!pl.isPlay()) {
				pl.togglePlay();
			}

			v.setTextColor(Color.RED);
		}

		AnimationSet push = (AnimationSet) AnimationUtils.loadAnimation(
				v.getContext(), R.anim.push);
		v.startAnimation(push);
	}

	private Tones getTone(int id) {
		Tones res = null;

		switch (id) {
		case R.id.toneC:
			res = Tones.C;
			break;
		case R.id.toneD:
			res = Tones.D;
			break;
		case R.id.toneE:
			res = Tones.E;
			break;
		case R.id.toneF:
			res = Tones.F;
			break;
		case R.id.toneG:
			res = Tones.G;
			break;
		case R.id.toneA:
			res = Tones.A;
			break;
		case R.id.toneB:
			res = Tones.B;
			break;
		default:
			break;
		}

		return res;
	}
}
