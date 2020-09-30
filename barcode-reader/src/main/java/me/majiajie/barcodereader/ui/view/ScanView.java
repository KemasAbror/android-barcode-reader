package me.majiajie.barcodereader.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * 扫描框UI
 */
public class ScanView extends View implements Animatable {

    private ScanDrawable mDrawable;

    private Rect mRect;// 扫描框在相对于当前视图的位置

    public ScanView(Context context) {
        super(context);
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 扫描框的长宽(200dp)
        int lenght = (int) (200 * getContext().getResources().getDisplayMetrics().density);
        // 扫描框的外边距
        float startX = Math.max(0,(w - lenght)/2f);
        float startY = Math.max(0,(h - lenght)/2f);

        mRect = new Rect((int) startX, (int) startY, (int)(startX + lenght),(int)(startY + lenght));

        //创建扫描视图
        mDrawable = new ScanDrawable(getContext(), w, h, lenght);
        mDrawable.setCallback(this);

        //开始扫描动画
        start();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        super.invalidateDrawable(drawable);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawable.draw(canvas);
    }

    @Override
    public void start() {
        if (mDrawable != null) {
            mDrawable.start();
        }
    }

    @Override
    public void stop() {
        if (mDrawable != null) {
            mDrawable.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return mDrawable != null && mDrawable.isRunning();
    }

    /**
     * 获取预览视图的扫描范围，通过预览图和真实视图宽高的比例差值计算得出
     * @param previewWidth  预览图的宽度
     * @param previewHeight 预览图的高度
     * @param isVertical    判断视图是否垂直
     */
    public Rect getFramingRect(int previewWidth,int previewHeight,boolean isVertical) {
        int tmpWidth;
        int tmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isVertical) {
            tmpWidth = previewHeight;
            tmpHeight = previewWidth;
        } else {
            tmpWidth = previewWidth;
            tmpHeight = previewHeight;
        }

        // 按比例缩放
        int left = mRect.left * tmpWidth / getWidth();
        int right = mRect.right * tmpWidth / getWidth();
        int top = mRect.top * tmpHeight / getHeight();
        int bottom = mRect.bottom * tmpHeight / getHeight();

        Rect rect = new Rect();
        if (isVertical) {
            // 视图为垂直时要旋转90度，因为预览图是横向的，
            rect.set(top, tmpWidth - right, bottom, tmpWidth - left);
        } else {
            rect.set(left,top,right,bottom);
        }
        return rect;
    }
}
