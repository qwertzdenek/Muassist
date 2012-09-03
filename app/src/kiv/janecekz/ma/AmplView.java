package kiv.janecekz.ma;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AmplView extends ImageView {
    private short ampl;
    private Paint p;

    public AmplView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        
        p = new Paint();
        p.setColor(ctx.getResources().getColor(android.R.color.holo_red_light));
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        p.setStrokeWidth(widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.drawColor(getResources().getColor(android.R.color.transparent));
        canvas.drawLine(canvas.getWidth()/2, canvas.getHeight(), canvas.getWidth()/2, canvas.getHeight()-ampl, p);
    }
    
    public void setAmpl(short arg0) {
        ampl = arg0;
    }
}
