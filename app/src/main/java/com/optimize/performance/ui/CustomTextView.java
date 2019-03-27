package com.optimize.performance.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.Executors;

@Deprecated
public class CustomTextView extends View {

    private String mText = "我是StaticLayout显示出来的文本";
    private TextPaint mTextPaint;
    private StaticLayout mStaticLayout;

    public CustomTextView(Context context) {
        super(context);
        initLabelView();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLabelView();
    }

    private void initLabelView() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(16 * getResources().getDisplayMetrics().density);
        mTextPaint.setColor(Color.BLACK);
        final int width = (int) mTextPaint.measureText(mText);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mStaticLayout = new StaticLayout(mText, mTextPaint, (int) width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
                postInvalidate();
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mStaticLayout != null){
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            mStaticLayout.draw(canvas);
            canvas.restore();
        }
    }
}