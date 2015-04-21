package sg.edu.nus.photopix;

/**
 * Created by linxiuqing on 7/4/15.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BorderImage extends ImageView {
    private static final int PADDING = 8;
    private static final float STROKE_WIDTH = 8.0f;
    private Paint borderPaint;

    public BorderImage(Context context) {
        this(context, null);
    }

    public BorderImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setPadding(PADDING, PADDING, PADDING, PADDING);
    }
    public BorderImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initBorderPaint();
    }
    private void initBorderPaint() {
        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(STROKE_WIDTH);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(PADDING, PADDING, getWidth() - PADDING, getHeight() - PADDING, borderPaint);
    }
}