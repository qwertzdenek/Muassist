package kiv.janecekz.ma;

import android.app.*;
import android.os.*;
import android.view.*;
import kiv.janecekz.ma.*;
import android.widget.*;

public class RecorderFragment extends Fragment
 {
	public View OnCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
								 
	View v = inflater.inflate(R.layout.recorder, container, false);
	return v;  
  }
  
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        
	}
}
