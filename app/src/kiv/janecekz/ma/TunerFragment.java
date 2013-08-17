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

import java.util.LinkedList;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TunerFragment extends Fragment implements IControlable {
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;

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

        RelativeLayout layout = (RelativeLayout) getView().findViewById(
                R.id.tunerl);

        if (mChart == null) {
            initChart();
            mCurrentSeries.add(0, 1);
            mCurrentSeries.add(1, 3);
            mCurrentSeries.add(2, 2);
            mCurrentSeries.add(3, 4);
            mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset,
                    mRenderer, 0f);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            layout.addView(mChart, lp);
        } else {
            mChart.repaint();
        }

        recorder = new Recorder(this);
        recs = new Short[recorder.getFrameSize()];
        recorder.start();

        inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
        outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);
    }

    private void initChart() {
        mCurrentSeries = new XYSeries("AMDF+ACF result");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setColor(Color.YELLOW);
        mRenderer.addSeriesRenderer(mCurrentRenderer);
        // mRenderer.setYAxisMax(10.0);
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

    public synchronized void postAnalyzed(Double[] val) {
        this.notify();
        // Log.d(MainActivity.TAG,
        // "Drawing data; from "+Thread.currentThread());
        mCurrentSeries.clear();
        for (int i = 0; i < val.length; i++) {
            mCurrentSeries.add(i, val[i]);
        }
        mChart.repaint();

        // TODO: find top
        LinkedList<Integer> tops = new LinkedList<Integer>();
        int i = 0;
        while (i < val.length - 1) {
            // going top
            while ((i < val.length - 1) && (val[i] < val[i + 1])) {
                i++;
                if (val[i] > val[i + 1]) {
                    tops.add(i);
                    break;
                }
            }

            // going down
            while ((i < val.length - 1) && (val[i] >= val[i + 1]))
                i++;
        }
        
        int[] dists = new int[tops.size()];
        
        i = 0;
        int lastPeak = 0;
        for (int peak : tops) {
            dists[i++] = peak - lastPeak;
            lastPeak = peak;
        }
        
        LinkedList<LinkedList<Integer>> bucket = new LinkedList<LinkedList<Integer>>();
        
        boolean newVal = false;
        for (Integer dist : dists) {
            newVal = true;
            for (LinkedList<Integer> b : bucket) {
                if (Math.abs(b.getFirst() - dist) < 6) {
                    b.add(dist);
                    newVal = false;
                    break;
                }
            }
            
            if (newVal) {
                LinkedList<Integer> newList = new LinkedList<Integer>();
                newList.add(dist);
                bucket.add(newList);
            }
        }
        
        LinkedList<Integer> most = new LinkedList<Integer>();
        for (LinkedList<Integer> list : bucket) {
            if (list.size() > most.size())
                most = list;
        }

        int sum = 0;
        for (int d : most) {
            sum += d;
        }
        
        double freq;
        
        if (sum < 10)
            freq = 0.0;
        else
            freq = most.size() * recorder.getSampleFreq() / sum;
        
        /*
        // find median
        Arrays.sort(dists);
        double freq;
        if (dists.length == 0 || dists.length == 1)
            freq = 0.0;
        else if ((dists.length & 0x1) == 1) {
            freq = (double) recorder.getSampleFreq() / dists[dists.length / 2];
        } else {
            int between = (dists[dists.length / 2] + dists[dists.length / 2 - 1]) / 2;
            freq = (double) recorder.getSampleFreq() / between;
        }
        */
        
        
        tunerText.setText(String.format("%.2f", freq));
    }

    public synchronized void postRec(Short[] recorded) {
        System.arraycopy(recorded, 0, recs, 0, recorded.length);
        if (analyzer == null
                || (analyzer != null && (analyzer.getStatus() != AsyncTask.Status.RUNNING))) {
            analyzer = new Analyzer(this);
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
