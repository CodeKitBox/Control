package com.kits.control.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.Display
import android.view.WindowManager


object ScreenUtils {
    fun getScreenHeight(context: Context):Int{
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            windowManager.defaultDisplay
        }

        val outPoint = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            display?.let {
                display.getRealSize(outPoint)
            }
        }else{
            display?.let {
                display.getSize(outPoint)
            }
        }
        return outPoint.y
    }
}