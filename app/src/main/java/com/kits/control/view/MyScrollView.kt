package com.kits.control.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.LinearLayout
import kotlin.math.abs
import kotlin.math.max

/**
 * 自定义滑动控件
 * 1. 继承 LinearLayout
 * 2. 简单化处理，只处理垂直方向的布局
 * 3. 滑动的方向的前提是以手指的滑动作为参考点
 */
class MyScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr){

    //<editor-fold desc="基本相关属性">
    private val minFling = ViewConfiguration.get(getContext()).scaledMinimumFlingVelocity
    private val maxFling = ViewConfiguration.get(getContext()).scaledMaximumFlingVelocity
    // 滑动阈值
    private var touchSlop:Int = ViewConfiguration.get(getContext()).scaledTouchSlop
    private val mOverscrollDistance = ViewConfiguration.get(getContext()).scaledOverscrollDistance;
    private val mOverflingDistance = ViewConfiguration.get(getContext()).scaledOverflingDistance;
    // 当前的触摸点坐标
    private var touchX = 0
    private var touchY = 0
    //</editor-fold>

    //<editor-fold desc="滑动相关属性">
    // 滑动范围
    private var scrollRange = 0
    // 标志手指是否在屏幕拖动过程
    private var mIsBeingDragged = false
    //</editor-fold>

    /**
     * LinearLayout 布局对于超过容器尺寸的，不调用子控件的布局接口，因此子类重写
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //获取父控件给的宽度
        val selfWidth = MeasureSpec.getSize(widthMeasureSpec)
        //获取父控件给的宽度
        val selfHeight = MeasureSpec.getSize(heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // 测量子控件获取 需要的宽度和高度
        var needWidth = 0
        var needHeight = 0

        for (i in 0 until childCount){
            val childView = getChildAt(i)
            val lp = childView.layoutParams
            var measuredWidth = 0
            var measuredHeight = 0

            if (lp is MarginLayoutParams){
                measureChildWithMargins(childView,widthMeasureSpec,0,heightMeasureSpec,0)
                measuredWidth = childView.measuredWidth + lp.leftMargin+lp.rightMargin
                measuredHeight = childView.measuredHeight + lp.topMargin+lp.bottomMargin
            }else{
                measureChild(childView,widthMeasureSpec,heightMeasureSpec)
                measuredWidth = childView.measuredWidth
                measuredHeight = childView.measuredHeight
            }
            needWidth = max(needWidth,measuredWidth)
            // 需要换行
            if(measuredWidth >= selfWidth){
                needHeight+=measuredHeight
            }
        }
        var containerWidth = when(widthMode){
            MeasureSpec.EXACTLY->selfWidth
            else -> needWidth
        }
        var containerHeight = when(heightMode){
            MeasureSpec.EXACTLY->selfHeight
            else -> needHeight
        }
        val maxLength = max(containerHeight,needHeight)
        // 计算滑动的范围
        scrollRange = if(maxLength - containerHeight > 0) maxLength - containerHeight else maxLength
        // 设置容器的宽度和高度
        setMeasuredDimension(containerWidth,containerHeight)
    }

    //<editor-fold desc="分发触摸事件相关接口">
    /** 触摸事件可以分为：单击事件，长按事件，滑动事件
     * @param ev 触摸事件
     * @return true: 控件消费触摸事件 false: 控件不消费触摸事件
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val superConsume = super.dispatchTouchEvent(ev)
        // println("superConsume = $superConsume")
        // 控件需要处理触摸事件因此返回true
        return true
    }

    /**
     * @param ev 触摸事件
     * @return true 事件在控件本身处理，由 onTouchEvent 函数进行处理
     *         false 事件通过系统分发到子控件 及其 控件本身的 onTouchEvent
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        // 判断是否拦截事件的标准 ： 如果是滑动事件在自定义的控件中处理
        val action = ev.action
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true
        }

        val vtev = MotionEvent.obtain(ev)
        when(vtev.actionMasked){
            MotionEvent.ACTION_DOWN->{
                // 设置为非拖动状态
                mIsBeingDragged = false
                // 记录触摸点坐标
                saveLocation(ev.x.toInt(),ev.y.toInt())
            }
            MotionEvent.ACTION_MOVE->{
                val dy = differY(ev.y.toInt())
                println("onInterceptTouchEvent dy $dy; touchSlop $touchSlop")
                if (abs(dy) > touchSlop){
                    // 控件在滑动，通知父控件不要拦截次事件
                    mIsBeingDragged = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->mIsBeingDragged = false
        }

        vtev.recycle()
        return mIsBeingDragged || super.onInterceptTouchEvent(ev)
    }

    /**
     * @param event 触摸事件
     * 1. 当控件本身的 onInterceptTouchEvent 函数位返回值是调用这个函数，
     * 2. 当控件内的子控件不消费事件的时候，调用 onTouchEvent 来消费
     * @return true 消费次事件，触摸事件不在分发给其他控件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        println("onTouchEvent")
        // 这里的 主要也是处理滑动事件，点击事件由系统处理
        val vtev = MotionEvent.obtain(event)
        when(vtev.actionMasked){
            MotionEvent.ACTION_DOWN->{
                // 设置为非拖动状态
                mIsBeingDragged = false
                // 记录触摸点坐标
                saveLocation(event.x.toInt(),event.y.toInt())
            }
            MotionEvent.ACTION_MOVE->{
                val dy = differY(event.y.toInt())
                println("dy == $dy ; touchSlop = $touchSlop ; mIsBeingDragged=$mIsBeingDragged ")
                // 通过系统分发到这里，判断是否可以滑动
                if(abs(dy) > touchSlop && !mIsBeingDragged){
                    mIsBeingDragged = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (mIsBeingDragged){
                    // 调用View的接口判断滑动
                    // 参数  deltaX ，deltaY 指的是滑动的偏移量
                    // 参数 scrollX scrollY 指的是已经滑动的距离
                    // 参数 scrollRangeX scrollRangeY 指的是滑动的范围
                    // 参见 maxOverScrollX  maxOverScrollY 指的是越界滑动的距离
                    //  isTouchEvent 系统中此参数没有使用
                    // 返回值 true 标识达到了最大越界，在惯性滑动中使用
                    // 调用onOverScrolled 实现真正的滑动
                    //println("dy = $dy ;scrollY = $scrollY ")
                    overScrollBy(0,dy,0,scrollY,0,scrollRange,0,0,true)
                    // 记录触摸点坐标
                    saveLocation(event.x.toInt(),event.y.toInt())
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{
                // 设置为非拖动状态
                mIsBeingDragged = false
                // 记录触摸点坐标
                saveLocation(event.x.toInt(),event.y.toInt())
            }
        }

        vtev.recycle()
        return true
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        println("onOverScrolled == $scrollX;$scrollY;$clampedX;$clampedY")
        // 在ScrollView 中对  clampedX == true clampedY == true 进行了处理
        // 滑动到指定坐标
        super.scrollTo(scrollX, scrollY)

    }
    //<editor-fold desc="私有方法">
    /**
     * 计算Y轴的偏移
     * 计算公式： 上一个点的坐标 键 当前坐标
     * @return 大于0 手指上滑 小于0 手指下滑
     */
    private fun differY(currentY:Int):Int{
        return touchY - currentY
    }

    /**
     * 保存当前的坐标
     * @param curX 当前的X轴坐标
     * @param curY  当前的Y轴坐标
     */
    private fun saveLocation(curX: Int,curY:Int){
        touchX = curX
        touchY = curY
    }
    //</editor-fold>
}