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

import java.util.concurrent.Semaphore;

import kiv.janecekz.ma.prefs.SharedPref;
import kiv.janecekz.ma.tuner.Analyzer;
import kiv.janecekz.ma.tuner.AnalyzerACF;
import kiv.janecekz.ma.tuner.AnalyzerAMDF;
import kiv.janecekz.ma.tuner.Classificator;
import kiv.janecekz.ma.tuner.Classificator.Result;
import kiv.janecekz.ma.tuner.MedianFilter;
import kiv.janecekz.ma.tuner.Recorder;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TunerFragment extends Fragment implements IControlable {
	private static final int METHOD_AMDF = 0;
	private static final int METHOD_ACF = 1;

    private Recorder recorder;
    private Analyzer analyzer;
    private Classificator classify;
    private MedianFilter mf;

    private ImageView circle;
    private TextView tunerText;
    private ProgressBar leftBar;
    private ProgressBar rightBar;
    private AlphaAnimation inAnim;
    private AlphaAnimation outAnim;
    
    public Semaphore full = new Semaphore(0);
    public Semaphore free = new Semaphore(1);
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tuner, container, false);
        v.setOnTouchListener(TouchControl.getInstance());

        return v;
    }

    @Override
    public void onPause() {
        recorder.stop();
        analyzer.cancel(true);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setBackgroundResource(
                ((MainActivity) getActivity()).getBgRes());

        circle = (ImageView) getView().findViewById(R.id.circle);
        
        tunerText = (TextView) getView().findViewById(R.id.tuner_text);

        recorder = new Recorder(this);
        recorder.start();

        int method = SharedPref.getAnlMethod(getActivity().getApplicationContext());
        
        if (method == METHOD_AMDF)
            analyzer = new AnalyzerAMDF(this, recorder.getSampleFreq(), recorder.getBuffer());
        else if (method == METHOD_ACF)
            analyzer = new AnalyzerACF(this, recorder.getSampleFreq(), recorder.getBuffer());
        
        analyzer.execute();
        
        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);
        
        classify = new Classificator(440); // TODO: choose it on the fly
        
        leftBar = (ProgressBar) getView().findViewById(R.id.progressBarLeft);
        rightBar = (ProgressBar) getView().findViewById(R.id.progressBarRight);
        
        mf = new MedianFilter();
    }
    
    @Override
    public void onValueChange(TouchControl t, int val) {

    }

    @Override
    public void onToggle(TouchControl t, int state) {
        switch (state) {
        case TouchControl.STATE_BEGIN:
            circle.setVisibility(View.VISIBLE);
            circle.startAnimation(inAnim);
            break;
        case TouchControl.STATE_STOP:
            // not used

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

    /**
     * Updates screen with the new analyzed frequency.
     * @param freq new frequency to show
     */
    public void postAnalyzed(Double freq) {
        mf.addValue(classify.findTone(freq));
        Result r = mf.getMedian();
        
        tunerText.setText(String.format("%s  %f", r.getTone(), r.getFreq()));
        
        if (r.getError() >= 0) {
            leftBar.setProgress(0);
            rightBar.setProgress((int) Math.floor(100 * r.getError()));
        } else {
            leftBar.setProgress((int) Math.floor(-100 * r.getError()));
            rightBar.setProgress(0);
        }
    }
}
