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

import kiv.janecekz.ma.prefs.SharedPref;
import kiv.janecekz.ma.tone.Player;
import android.app.Fragment;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ToneFragment extends Fragment implements IControlable,
        OnEditorActionListener, OnClickListener {
    private static final float[] freqCoefs = new float[] { 0.594613636f,
            0.667409091f, 0.74825f, 0.793704545f, 0.890909091f, 1f, 1.122454545f };

    private ImageView circle;
    private AlphaAnimation inAnim;
    private AlphaAnimation outAnim;
    
    private Player pl;
    private EditText input;
    private TextView actualFreqView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.tone, container, false);
        root.setOnTouchListener(TouchControl.getInstance());

        circle = (ImageView) root.findViewById(R.id.circle);

        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);

        input = (EditText) root.findViewById(R.id.tone_value_edit);
        input.setOnEditorActionListener(this);

        ViewGroup defList = (ViewGroup) root.findViewById(R.id.tone_list);
        for (int i = 0; i < defList.getChildCount(); i++) {
            TextView v = (TextView) defList.getChildAt(i);
            v.setOnClickListener(this);
        }
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        pl.interrupt();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (actualFreqView != null) {
            actualFreqView.setTextColor(actualFreqView.getResources().getColor(
                    android.R.color.holo_blue_light));
        }
        // Defaulting to the 440 Hz.
        actualFreqView = ((TextView) getView().findViewById(R.id.toneA));
        actualFreqView.setTextColor(actualFreqView.getResources().getColor(
                android.R.color.holo_red_light));

        pl = new Player();

        pl.setFreq(SharedPref.getBaseFreq(getActivity()));
        pl.start();

        input.setText(Integer.toString(SharedPref.getBaseFreq(getActivity())));

        getView().setBackgroundResource(
                ((MainActivity) getActivity()).getBgRes());
    }

    public void onValueChange(TouchControl t, int val) {
        // nothing to do
    }

    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            circle.setVisibility(View.VISIBLE);
            circle.startAnimation(inAnim);
            break;
        case TouchControl.STATE_STOP:
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

    public void onPositionChange(TouchControl t, float x, float y) {
        circle.setX(x - circle.getWidth() / 2);
        circle.setY(y - circle.getHeight() / 2 - 80);
    }

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
                    actualFreqView.setTextColor(actualFreqView.getResources()
                            .getColor(android.R.color.holo_blue_light));
                    actualFreqView = null;
                }
            }
        }
        return true;
    }

    public void onClick(View arg0) {
        TextView v = (TextView) arg0;

        if (actualFreqView != null) {
            actualFreqView.setTextColor(actualFreqView.getResources().getColor(
                    android.R.color.holo_blue_light));
        }

        actualFreqView = v;

        int freqCoefPosition = 5;
        switch (v.getId()) {
        case R.id.toneC:
            freqCoefPosition = 0;
            break;
        case R.id.toneD:
            freqCoefPosition = 1;
            break;
        case R.id.toneE:
            freqCoefPosition = 2;
            break;
        case R.id.toneF:
            freqCoefPosition = 3;
            break;
        case R.id.toneG:
            freqCoefPosition = 4;
            break;
        case R.id.toneA:
            freqCoefPosition = 5;
            break;
        case R.id.toneB:
            freqCoefPosition = 6;
            break;

        default:
            break;
        }
        
        int baseFreq = SharedPref.getBaseFreq(getActivity());
        float freq = baseFreq * freqCoefs[freqCoefPosition];

        v.setTextColor(v.getResources()
                .getColor(android.R.color.holo_red_light));
        AnimationSet push = (AnimationSet) AnimationUtils.loadAnimation(
                v.getContext(), R.anim.push);
        v.startAnimation(push);

        input.setText(Float.toString(freq));
        pl.setFreq(freq);
        if (!pl.isPlay()) {
            pl.togglePlay();
        }
    }
}
