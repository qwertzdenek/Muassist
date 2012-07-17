package kiv.janecekz.ma;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecorderFragment extends Fragment implements OnMyEvent {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        return inflater.inflate(R.layout.recorder, container, false);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onValueChange(TouchControl t, float val) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onToggle(TouchControl t, int state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPositionChange(TouchControl t, float x, float y) {
        // TODO Auto-generated method stub

    }
}
