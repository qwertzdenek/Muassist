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

import kiv.janecekz.ma.prefs.SharedPref;
import kiv.janecekz.ma.rec.ExtAudioRecorder;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RecorderFragment extends Fragment implements IControlable,
        IAplitudeShowListener {
    private final String[] dots = { " .", " . .", " . . ." };

    private ImageView circle;
    private AlphaAnimation inAnim;
    private AlphaAnimation outAnim;
    private ExtAudioRecorder ear;
    private TextView recTitleText;
    private TextView recStatusText;
    private AmplView spectr;
    private String lastRecorded = "N/A";
    private Handler mHandler = new Handler();
    private int dotCount = 0;

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            String dot = dots[dotCount];

            recStatusText.setText(getResources().getText(R.string.recording)
                    + dot);
            dotCount = (dotCount + 1) % dots.length;
            mHandler.postDelayed(this, 1000);
        }
    };

    private Runnable mUpdateAmplTask = new Runnable() {
        @Override
        public void run() {
            onUpdate(ear);
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.recorder, container, false);
        v.setOnTouchListener(TouchControl.getInstance());

        circle = (ImageView) v.findViewById(R.id.circle);

        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);

        recTitleText = (TextView) v.findViewById(R.id.rec_title);
        recStatusText = (TextView) v.findViewById(R.id.rec_status);

        spectr = (AmplView) v.findViewById(R.id.amplView);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ExtAudioRecorder.State status = ear.getState();
        if (status == ExtAudioRecorder.State.RECORDING)
            ear.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (SharedPref.getComp(getActivity()))
            mHandler.removeCallbacks(mUpdateAmplTask);
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setBackgroundResource(
                ((MainActivity) getActivity()).getBgRes());

        boolean comp = SharedPref.getComp(getActivity());

        // TODO: prepare file only when it is necessary!
        
        if (ear == null) {
            ear = ExtAudioRecorder.getInstance(comp);
            lastRecorded = getNextFile().getAbsolutePath();
            ear.setOutputFile(lastRecorded);
            ear.prepare();
            ear.setOnUpdateListener(this);
        } else {
            ExtAudioRecorder.State status = ear.getState();
            
            if (status == ExtAudioRecorder.State.RECORDING) {
                mHandler.postDelayed(mUpdateTimeTask, 1000);
                if (SharedPref.getComp(getActivity()))
                    mHandler.postDelayed(mUpdateAmplTask, 100);
            }
        }
    }
    
    @Override
    public void onValueChange(TouchControl t, int val) {
        // Not used
    }

    @Override
    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            circle.setVisibility(View.VISIBLE);
            circle.startAnimation(inAnim);
            break;
        case TouchControl.STATE_STOP:
            ExtAudioRecorder.State status = ear.getState();
            if (status == ExtAudioRecorder.State.RECORDING) {
                Toast.makeText(getActivity(), lastRecorded, Toast.LENGTH_SHORT)
                        .show();
                ear.stop();
                recTitleText.setVisibility(View.VISIBLE);
                recStatusText.setText(getResources()
                        .getString(R.string.stopped));
                mHandler.removeCallbacks(mUpdateTimeTask);
                if (SharedPref.getComp(getActivity()))
                    mHandler.removeCallbacks(mUpdateAmplTask);
            } else if (status == ExtAudioRecorder.State.READY) {
                ear.start();
                recTitleText.setVisibility(View.INVISIBLE);
                recStatusText.setText(getResources().getString(
                        R.string.recording));
                mHandler.postDelayed(mUpdateTimeTask, 1000);
                if (SharedPref.getComp(getActivity()))
                    mHandler.postDelayed(mUpdateAmplTask, 100);
            } else if ((status == ExtAudioRecorder.State.STOPPED)
                    || (status == ExtAudioRecorder.State.ERROR)) {
                ear.release();
                boolean comp = SharedPref.getComp(getActivity());
                ear = ExtAudioRecorder.getInstance(comp);
                lastRecorded = getNextFile().getAbsolutePath();
                ear.setOutputFile(lastRecorded);
                ear.prepare();
                ear.setOnUpdateListener(this);
                ear.start();
                recTitleText.setVisibility(View.INVISIBLE);
                recStatusText.setText(getResources().getString(
                        R.string.recording));
                mHandler.postDelayed(mUpdateTimeTask, 1000);
                if (SharedPref.getComp(getActivity()))
                    mHandler.postDelayed(mUpdateAmplTask, 100);
            }

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
    public void onPositionChange(TouchControl t, float x, float y) {
        circle.setX(x - circle.getWidth() / 2);
        circle.setY(y - circle.getHeight() / 2 - 80);
    }

    private File getNextFile() {
        String extension = null;
        if (SharedPref.getComp(getActivity()))
            extension = ".amr";
        else
            extension = ".wav";
        File f;
        int i = 1;
        while (true) {
            f = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/rec" + (i++) + extension);
            
            if (!f.exists())
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
