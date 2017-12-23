package me.majiajie.barcodereader.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * 扫码框前景图像
 */
public class ScanDrawable extends Drawable implements Animatable {

    //扫描动画移动一次的时间
    private final int ANIM_TIME = 3_000;
    //扫描匡以外颜色
    private final int COLOR_OUTSIDE_BACKGROUND = 0x66000000;
    //扫描线条的颜色
    private final int COLOR_LINE_BACKGROUND = 0xFFFFFFFF;

    //扫描匡长度
    private int mScanViewLenght;

    //总的宽高
    private int mWidth;
    private int mHeight;

    private Paint mPaintLine, mPaintBackground;

    private Path mPathLine, mPathFrame, mPathBackground;

    private ValueAnimator mAnimator;

    private float mStartX, mStartY, mEndX, mEndY;

    ScanDrawable(Context context, int width, int height, int scanViewLenght) {
        mWidth = width;
        mHeight = height;
        mScanViewLenght = scanViewLenght;

        // 画背景色的笔
        mPaintBackground = new Paint();
        mPaintBackground.setColor(COLOR_OUTSIDE_BACKGROUND);
        mPaintBackground.setAntiAlias(true);

        // 背景
        mPathBackground = new Path();
        RectF rectF = new RectF((mWidth - mScanViewLenght) / 2,(mHeight - mScanViewLenght) / 2,mWidth - (mWidth - mScanViewLenght) / 2,mHeight - (mHeight - mScanViewLenght) / 2);
        mPathBackground.addRect(new RectF(0,0,mWidth,mHeight), Path.Direction.CW);
        mPathBackground.addRect(rectF, Path.Direction.CCW);

        float one_dp = 1 * context.getResources().getDisplayMetrics().density;

        // 画扫描框的笔
        mPaintLine = new Paint();
        mPaintLine.setColor(COLOR_LINE_BACKGROUND);
        mPaintLine.setStrokeWidth(one_dp * 2);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setAntiAlias(true);//抗锯齿
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);//直线头尾圆滑
        mPaintLine.setStrokeJoin(Paint.Join.ROUND);//直线交界处圆滑处理

        // 上下移动的线
        mPathLine = new Path();
        mStartX = (mWidth - mScanViewLenght) / 2 + one_dp;
        mStartY = mEndY = (mHeight - mScanViewLenght) / 2f;
        mEndX = mStartX + mScanViewLenght - one_dp;
        mPathLine.moveTo(mStartX, mStartY);
        mPathLine.lineTo(mEndX, mEndY);

        // 画四个角
        float eight_dp = 8 * context.getResources().getDisplayMetrics().density;
        float padding = eight_dp * 2;
        mPathFrame = new Path();

        mPathFrame.moveTo((mWidth - mScanViewLenght) / 2 + padding,(mHeight - mScanViewLenght) / 2f + padding + eight_dp);
        mPathFrame.rLineTo(0,-eight_dp);
        mPathFrame.rLineTo(eight_dp,0);

        mPathFrame.rMoveTo(mScanViewLenght - padding * 2 - eight_dp *2,0);
        mPathFrame.rLineTo(eight_dp,0);
        mPathFrame.rLineTo(0,eight_dp);

        mPathFrame.rMoveTo(0,mScanViewLenght - padding * 2 - eight_dp *2);
        mPathFrame.rLineTo(0,eight_dp);
        mPathFrame.rLineTo(-eight_dp,0);

        mPathFrame.rMoveTo(-(mScanViewLenght - padding * 2 - eight_dp *2),0);
        mPathFrame.rLineTo(-eight_dp,0);
        mPathFrame.rLineTo(0,-eight_dp);

        setupAnimators();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(mPathBackground, mPaintBackground);
        canvas.drawPath(mPathLine, mPaintLine);
        canvas.drawPath(mPathFrame, mPaintLine);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaintLine.setAlpha(alpha);
        mPaintBackground.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaintLine.setColorFilter(colorFilter);
        mPaintBackground.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void start() {
        mAnimator.start();
    }

    @Override
    public void stop() {
        mAnimator.end();
    }

    @Override
    public boolean isRunning() {
        return mAnimator.isRunning();
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    private void setupAnimators() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(ANIM_TIME);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float n = (float) animation.getAnimatedValue();

                Path path = new Path();
                mStartY = mEndY = (mHeight - mScanViewLenght) / 2f + mScanViewLenght * n;
                path.moveTo(mStartX, mStartY);
                path.lineTo(mEndX, mEndY);
                mPathLine = path;

                invalidateSelf();
            }
        });

        mAnimator = valueAnimator;
    }
}
