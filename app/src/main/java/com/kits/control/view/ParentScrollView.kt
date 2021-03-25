package com.kits.control.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView


/**
 * ScrollView 优先滑动的
 */
class ParentScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        ScrollView(context, attrs, defStyleAttr){

    private val TAG = "ParentScrollView"

    override fun onNestedPreScroll(target: View?, dx: Int, dy: Int,  consumed: IntArray?) {
        // ViewGroup 的默认方法是分发给自己的父控件消费，自己不消费
        // 如果作为父控件需要消费距离，就需要重写
        // 获取控件的坐标
        Log.d(TAG,"===onNestedPreScroll=== $x ; $y; $scrollY")
        if(target is RecyclerView){
            Log.d(TAG,"RecyclerView ${target.x}; ${target.y}")
            // RecyclerView 没有滑动到顶部，父控件消费距离
            if(scrollY < target.y){
                val maxConsumeY = kotlin.math.abs(target.y - scrollY)
                val consumeY = if(dy < maxConsumeY) dy else maxConsumeY.toInt()
                scrollBy(0, consumeY)
                consumed?.let {
                    consumed[0] = 0
                    consumed[1] = consumeY
                }
                return
            }
        }
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onNestedPreFling(target: View?, velocityX: Float, velocityY: Float): Boolean {
        if(target is RecyclerView){
            if(scrollY < target.y){
                // 父控件消费所有的惯性滑动
                val maxConsumeY = kotlin.math.abs(target.y - scrollY)
                val consumeY = if(velocityX < maxConsumeY) velocityX else maxConsumeY
                flingWithNestedDispatch(consumeY.toInt())
                return true
            }
        }
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun flingWithNestedDispatch(velocityY: Int) {
        Log.d(TAG,"===flingWithNestedDispatch=== ")
        val canFling = (scrollY > 0 || velocityY > 0) &&
                (scrollY < getScrollRange() || velocityY < 0)
        if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
            dispatchNestedFling(0f, velocityY.toFloat(), canFling)
            if (canFling) {
                fling(velocityY)
            }
        }
    }

    private fun getScrollRange(): Int {
        Log.d(TAG,"===getScrollRange=== ")
        var scrollRange = 0
        if (childCount > 0) {
            val child = getChildAt(0)
            scrollRange = Math.max(0,
                    child.height - (height - paddingBottom - paddingTop))
        }
        return scrollRange
    }

}