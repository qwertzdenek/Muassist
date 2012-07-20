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

import kiv.janecekz.ma.tone.Player;
import android.animation.AnimatorSet;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ToneFragment extends Fragment implements OnMyEvent {
    private Player pl;
    private ImageView circle;
    private AnimatorSet inAnim;
    private AnimatorSet outAnim;

    private TextView toneValue;
    private int freq = 440;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tone, container, false);
        v.setOnTouchListener(TouchControl.getInstance());

        circle = (ImageView) v.findViewById(R.id.circle);
        
        inAnim = TouchControl.getInAnim(circle);
        outAnim = TouchControl.getOutAnim(circle);
        
        toneValue = (TextView) v.findViewById(R.id.tone_value);
        // FIXME: Get value from the shared prefereces.
        toneValue.setText(Integer.toString(freq));

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        
        pl.interrupt();
    }

    @Override
    public void onResume() {
        pl = new Player();
        pl.setFreq(freq);
        pl.start();
        
        pl.setPlay();
        
        super.onResume();
    }

    public void onValueChange(TouchControl t, float val) {
        // TODO: editable speed
        freq = freq + (int) (val / 50);
        
        toneValue.setText(Integer.toString(freq));
        pl.setFreq(freq);
    }

    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            inAnim.start();
            break;
        case TouchControl.STATE_STOP:
            // TODO: Shared preferences
//            SharedPref.setPlay(getActivity(),
//                    !SharedPref.getPlay(getActivity()));
            pl.setPlay();
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

}
