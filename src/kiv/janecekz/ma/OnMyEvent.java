package kiv.janecekz.ma;

public interface OnMyEvent {
    public void onValueChange(TouchControl t, float val);
    
    public void onToggle(TouchControl t, int state);
    
    public void onPositionChange(TouchControl t, float x, float y);
}
