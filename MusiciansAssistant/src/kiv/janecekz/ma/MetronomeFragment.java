package kiv.janecekz.ma;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.NumberPicker;

public class MetronomeFragment extends Fragment implements OnTouchListener {

    private static NumberPicker beatCount;
    private static NumberPicker bpmCount;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d("Metronome", "Creating Fragment");
        
        View v = inflater.inflate(R.layout.metronome, container, false);
        v.setOnTouchListener(this);
        
        beatCount = (NumberPicker) v.findViewById(R.id.beatCount);
        beatCount.setMinValue(1);
        beatCount.setMaxValue(4);
        
        bpmCount = (NumberPicker) v.findViewById(R.id.bpmCount);
        bpmCount.setMinValue(30);
        bpmCount.setMaxValue(200);
        
        return v;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

}
