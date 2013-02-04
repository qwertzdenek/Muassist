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

import kiv.janecekz.ma.tuner.Analyzer;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TunerFragment extends Fragment implements IControlable, Informable {
    private TextView tuner_out;
    private Analyzer anl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.tuner, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setBackgroundResource(
                ((MainActivity) getActivity()).getBgRes());
        tuner_out = (TextView) getView().findViewById(R.id.tuner_out);

        anl = new Analyzer();
        anl.setOnMessageListener(this);
    }

    public void onValueChange(TouchControl t, int val) {
        // TODO Auto-generated method stub

    }

    public void onToggle(TouchControl t, int state) {
        // TODO Auto-generated method stub

    }

    public void onPositionChange(TouchControl t, float x, float y) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMessage(String msg) {
        tuner_out.setText(msg);
    }
}
