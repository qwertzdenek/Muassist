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

import kiv.janecekz.ma.tuner.Analyzer;
import kiv.janecekz.ma.tuner.Recorder;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class TunerFragment extends Fragment implements IControlable {
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    
    private Recorder recorder;
    private Analyzer analyzer;
    
    private ImageView circle;
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

        RelativeLayout layout = (RelativeLayout) getView().findViewById(R.id.tunerl);
        
        if (mChart == null) {
            initChart();
            mCurrentSeries.add(0, 1);
            mCurrentSeries.add(1, 3);
            mCurrentSeries.add(2, 2);
            mCurrentSeries.add(3, 4);
            mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset, mRenderer, 0f);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layout.addView(mChart, lp);
        } else {
            mChart.repaint();
        }
        
        
        recorder = new Recorder(this);
        recorder.start();
        
        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);
    }
    
    private void initChart() {
        mCurrentSeries = new XYSeries("AMDF+ACF result");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
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
    
    public synchronized void postAnalyzed(Short[] val) {
        this.notify();
        //Log.d(MainActivity.TAG, "Drawing data; from "+Thread.currentThread());
        mCurrentSeries.clear();
        for (int i = 1; i < val.length; i++) {
            mCurrentSeries.add(i, val[i]);
        }
        mChart.repaint();
    }

    public synchronized void postRec(Short[] recorded) {
        if (analyzer == null) {
            analyzer = new Analyzer(this);
            analyzer.execute(recorded);
        } else if (analyzer.getStatus() == AsyncTask.Status.RUNNING)
            try {
                //Log.d(MainActivity.TAG, "Waiting to finish analyze; from "+Thread.currentThread());
                analyzer.get();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else {
            //Log.d(MainActivity.TAG, "Analyzing data; from "+Thread.currentThread());
            analyzer = new Analyzer(this);
            analyzer.execute(recorded);
        }
    }
}
