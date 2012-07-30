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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class RecorderFragment extends Fragment implements IControlable {
    private ImageView circle;
    private AnimationSet inAnim;
    private AnimationSet outAnim;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View v = inflater.inflate(R.layout.recorder, container, false);
        v.setOnTouchListener(TouchControl.getInstance());
        
        circle = (ImageView) v.findViewById(R.id.circle);
        
        inAnim = (AnimationSet) AnimationUtils.loadAnimation(getActivity(), R.anim.nav_in);
        outAnim = (AnimationSet) AnimationUtils.loadAnimation(getActivity(), R.anim.nav_out);
        
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setBackgroundResource(((MainActivity) getActivity()).getBgRes());
    }

    public void onValueChange(TouchControl t, int val) {
        // Not used
    }

    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            circle.startAnimation(inAnim);
            break;
        case TouchControl.STATE_STOP:
            // TODO control here
            break;
        case TouchControl.STATE_OUT:
            if (!inAnim.hasEnded())
                inAnim.cancel();
            circle.startAnimation(outAnim);
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
