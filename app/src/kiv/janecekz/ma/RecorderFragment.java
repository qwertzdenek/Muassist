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

import java.io.File;

import kiv.janecekz.ma.rec.ExtAudioRecorder;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RecorderFragment extends Fragment implements IControlable,
        IAplitudeShowListener {
    private final String[] dots = { " .", " . .", " . . ." };

    private ImageView circle;
    private AnimationSet inAnim;
    private AnimationSet outAnim;
    private ExtAudioRecorder ear;
    private TextView recTitleText;
    private TextView recStatusText;
    private AmplView spectr;
    private String lastRecorded = "N/A";
    private Handler mHandler = new Handler();
    private int dotCount = 0;

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            String dot = dots[dotCount];

            recStatusText.setText(getResources().getText(R.string.recording)
                    + dot);
            dotCount = (dotCount + 1) % dots.length;
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.recorder, container, false);
        v.setOnTouchListener(TouchControl.getInstance());

        circle = (ImageView) v.findViewById(R.id.circle);

        inAnim = (AnimationSet) AnimationUtils.loadAnimation(getActivity(),
                R.anim.nav_in);
        outAnim = (AnimationSet) AnimationUtils.loadAnimation(getActivity(),
                R.anim.nav_out);

        recTitleText = (TextView) v.findViewById(R.id.rec_title);
        recStatusText = (TextView) v.findViewById(R.id.rec_status);

        spectr = (AmplView) v.findViewById(R.id.amplView);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        ear.release();
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setBackgroundResource(
                ((MainActivity) getActivity()).getBgRes());

        ear = ExtAudioRecorder.getInstance(false);
        lastRecorded = getNextFile().getAbsolutePath();
        ear.setOutputFile(lastRecorded);
        ear.prepare();
        ear.setOnUpdateListener(this);
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
            ExtAudioRecorder.State status = ear.getState();
            if (status == ExtAudioRecorder.State.RECORDING) {
                Toast.makeText(getActivity(), lastRecorded, Toast.LENGTH_SHORT)
                        .show();
                ear.stop();
                recTitleText.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(mUpdateTimeTask);
                recStatusText.setText(getResources()
                        .getString(R.string.stopped));
            } else if (status == ExtAudioRecorder.State.READY) {
                ear.start();
                recTitleText.setVisibility(View.INVISIBLE);
                recStatusText.setText(getResources().getString(
                        R.string.recording));
                mHandler.postDelayed(mUpdateTimeTask, 1000);
            } else if ((status == ExtAudioRecorder.State.STOPPED)
                    || (status == ExtAudioRecorder.State.ERROR)) {
                ear.reset();
                lastRecorded = getNextFile().getAbsolutePath();
                ear.setOutputFile(lastRecorded);
                ear.prepare();
                ear.start();
                recTitleText.setVisibility(View.INVISIBLE);
                recStatusText.setText(getResources().getString(
                        R.string.recording));
                mHandler.postDelayed(mUpdateTimeTask, 1000);
            }

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

    private File getNextFile() {
        File f = new File(Environment.getExternalStorageDirectory().getPath()
                + "/rec1.wav");
        int i = 1;
        while (true) {
            if (f.exists()) {
                f = new File(Environment.getExternalStorageDirectory()
                        .getPath() + "/rec" + (++i) + ".wav");
            } else
                break;
        }
        return f;
    }

    @Override
    public void onUpdate(ExtAudioRecorder ear) {
        short ampl = (short) ear.getMaxAmplitude();
        short stopY = (short) ((((double) ampl) / Short.MAX_VALUE) * 300);
        spectr.setAmpl(stopY);
        spectr.invalidate();
    }
}
