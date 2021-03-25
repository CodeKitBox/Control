package com.kits.control.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class UiUtils {
    private static final String TAG = "UiUtils";
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context  上下文
     * @param dipValue dip的值
     * @return px的值
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param context 上下文
     * @param pxValue px的值
     * @return dip的值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp值换算成px
     *
     * @param context 上下文
     * @param spValue sp的值
     * @return px的值
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param context 上下文
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return sp的值
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static void debugMeasureMode(int widthMeasureSpec, int heightMeasureSpec){
        if(View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.AT_MOST){
            Log.d(TAG,"width mode == AT_MOST");
        }
        if(View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY){
            Log.d(TAG,"width mode == EXACTLY");
        }
        if(View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST){
            Log.d(TAG,"height mode == AT_MOST");
        }
        if(View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY){
            Log.d(TAG,"height mode == EXACTLY");
        }
    }


}
