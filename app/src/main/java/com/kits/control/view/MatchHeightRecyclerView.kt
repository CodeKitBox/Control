package com.kits.control.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kits.control.utils.ScreenUtils

/**
 * 作为容器的RecyclerView,特点是：高度跟屏幕一样
 */
class MatchHeightRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        RecyclerView(context, attrs, defStyleAttr){

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val params = layoutParams
        params.height = ScreenUtils.getScreenHeight(context)
        super.onMeasure(widthSpec, heightSpec)
    }
}