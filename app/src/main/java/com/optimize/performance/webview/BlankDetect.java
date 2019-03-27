package com.optimize.performance.webview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.view.View;

import java.util.Arrays;

/**
 * WebView白屏检测
 */
public class BlankDetect {

    /**
     * 判断Bitmap是否都是一个颜色
     * @param bitmap
     * @return
     */
    public static boolean isBlank(View view) {
        Bitmap bitmap = getBitmapFromView(view);

        if (bitmap == null) {
            return true;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > 0 && height > 0) {
            int originPix = bitmap.getPixel(0, 0);
            int[] target = new int[width];
            Arrays.fill(target, originPix);
            int[] source = new int[width];
            boolean isWhiteScreen = true;
            for (int col = 0; col < height; col++) {
                bitmap.getPixels(source, 0, width, 0, col, width, 1);
                if (!Arrays.equals(target, source)) {
                    isWhiteScreen = false;
                    break;
                }
            }
            return isWhiteScreen;
        }
        return false;
    }

    /**
     * 从View获取转换到的Bitmap
     * @param view
     * @return
     */
    private static Bitmap getBitmapFromView(View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (Build.VERSION.SDK_INT >= 11) {
            view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
            view.layout((int) view.getX(), (int) view.getY(), (int) view.getX() + view.getMeasuredWidth(), (int) view.getY() + view.getMeasuredHeight());
        } else {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        view.draw(canvas);
        return bitmap;
    }


}
