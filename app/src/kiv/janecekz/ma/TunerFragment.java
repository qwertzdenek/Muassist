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

import java.util.concurrent.ExecutionException;

import kiv.janecekz.ma.prefs.SharedPref;
import kiv.janecekz.ma.tuner.Analyzer;
import kiv.janecekz.ma.tuner.AnalyzerACF;
import kiv.janecekz.ma.tuner.AnalyzerAMDF;
import kiv.janecekz.ma.tuner.Recorder;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class TunerFragment extends Fragment implements IControlable {
	private static final int METHOD_AMDF = 0;
	private static final int METHOD_ACF = 1;

    private Recorder recorder;
    private Analyzer analyzer;

    private Short[] recs;

    private ImageView circle;
    private TextView tunerText;
    private AlphaAnimation inAnim;
    private AlphaAnimation outAnim;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tuner, container, false);
        v.setOnTouchListener(TouchControl.getInstance());

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        recorder.end();

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
        recs = new Short[recorder.getFrameSize()];
        recorder.start();

        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);
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
            // TODO: toggle play

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

    public synchronized void postAnalyzed(Double freq) {
        //this.notify();
        
        tunerText.setText(String.format("%.2f", freq));
    }

    public synchronized void postRec(Short[] recorded) {
    	System.arraycopy(recorded, 0, recs, 0, recorded.length);
    	int method = SharedPref.getAnlMethod(getActivity().getApplicationContext());
        
        if (analyzer == null
                || (analyzer != null && (analyzer.getStatus() != AsyncTask.Status.RUNNING))) {
        	
            // TODO: do it parralel
            
        	if (method == METHOD_AMDF)
        		analyzer = new AnalyzerAMDF(this, recorder.getSampleFreq());
        	else if (method == METHOD_ACF)
        		analyzer = new AnalyzerACF(this, recorder.getSampleFreq());
        	
            analyzer.execute(recs);
        } else
            try {
                // Log.d(MainActivity.TAG,
                // "Waiting to finish analyze; from "+Thread.currentThread());
                analyzer.get();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
}
