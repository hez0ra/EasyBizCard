package volosyuk.easybizcard.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import androidx.annotation.Nullable;

public class ScalableImageView extends androidx.appcompat.widget.AppCompatImageView {
    private float scaleFactor = 1.0f; // Начальный масштаб (100%)
    private ScaleGestureDetector scaleGestureDetector;

    public ScalableImageView(Context context) {
        super(context);
        init(context);
    }

    public ScalableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScalableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();

                // Ограничиваем масштаб
                scaleFactor = Math.max(0.2f, Math.min(scaleFactor, 3.0f));

                // Масштабируем ImageView
                setScaleX(scaleFactor);
                setScaleY(scaleFactor);

                // Изменяем размеры самого элемента
                float newWidth = getDrawable().getIntrinsicWidth() * scaleFactor;
                float newHeight = getDrawable().getIntrinsicHeight() * scaleFactor;
                getLayoutParams().width = (int) newWidth;
                getLayoutParams().height = (int) newHeight;
                requestLayout();

                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
}
