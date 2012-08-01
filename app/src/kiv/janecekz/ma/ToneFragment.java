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
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ToneFragment extends Fragment implements IControlable {
    private Player pl;
    private ImageView circle;
    private Animation inAnim;
    private Animation outAnim;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.tone, container, false);
        root.setOnTouchListener(TouchControl.getInstance());

        circle = (ImageView) root.findViewById(R.id.circle);

        inAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.nav_in);
        outAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.nav_out);

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
        pl = new Player((ViewGroup) getView().findViewById(R.id.tone_list));

        pl.start();

        getView().setBackgroundResource(((MainActivity) getActivity()).getBgRes());
    }

    public void onValueChange(TouchControl t, int val) {
        // nothing to do
    }

    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            circle.startAnimation(inAnim);
            break;
        case TouchControl.STATE_STOP:
            pl.togglePlay();
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
