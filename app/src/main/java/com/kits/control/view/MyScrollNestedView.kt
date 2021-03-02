package com.kits.control.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Scroller
import kotlin.math.abs
import kotlin.math.max

/**
 * 惯性滑动：手指离开屏幕之后，控件会按照一定的速率继续滑动，优化了滑动的平滑性
 * 自定义滑动控件,此自定义控件支持惯性滑动
 * 平滑滑动：在滑动控件中，会有需求通过代码滑动到指定位置，如果使用接口 scrollTo/scrollBy 会有闪屏的现象
 * 因此这时候就需要使用平滑
 * 嵌套滑动，支持作为子控件角色和父控件角色
 * 1. 继承 LinearLayout
 * 2. 简单化处理，只处理垂直方向的布局
 * 3. 滑动的方向的前提是以手指的滑动作为参考点
 */
class MyScrollNestedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr){

    companion object{
        const val ANIMATED_SCROLL_GAP = 250*4
    }

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

    //<editor-fold desc="惯性或者平滑滑动相关属性">
    // 滑动辅助类，在惯性滑动，平滑滑动时使用
    private val mScroller = Scroller(context)
    // 速度模拟相关类
    private var mVelocityTracker: VelocityTracker?= null
    //</editor-fold>
    private var mLastScroll: Long = 0

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
                // 点击需要停止惯性滑动
                if(mScroller.isFinished){
                    mScroller.abortAnimation()
                }
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
        // 初始化速度控制器
        initVelocityTrackerIfNotExists()
        when(vtev.actionMasked){
            MotionEvent.ACTION_DOWN->{
                // 点击需要停止惯性滑动
                if(mScroller.isFinished){
                    mScroller.abortAnimation()
                    recycleVelocityTracker()
                }
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
                // 开始惯性滑动
                if(mIsBeingDragged){
                    mVelocityTracker?.let {
                        it.computeCurrentVelocity(1000)
                        // 判断是否支持惯性滑动，参考系统源码
                        println("惯性滑动 ${it.xVelocity};${it.xVelocity}; minFling =$minFling")
                        // 惯性滑动的速度大于系统最小的惯性滑动速度，执行惯性滑动
                        if(abs(it.yVelocity) > minFling){
                            val velocityY = -(it.yVelocity.toInt())
                            // 判断是否可以滑动
                            val canFling = (scrollY > 0 || velocityY > 0) &&
                                    (scrollY < getScrollRange() || velocityY < 0)
                            println("canFling == $canFling")
                            /**
                             * 参数 startX， startY 起始的滑动距离
                             * 参数 velocityX velocityY 滑动的速度
                             * 参数  minX minY  最小的滑动距离
                             * 参数  maxX maxY 最大的滑动就离
                             */
                            if(canFling){
                                mScroller.fling(0,scrollY,
                                        0,velocityY, 0,0, minFling, getScrollRange())
                                // 通知界面刷新 在onDraw 的时候调用 computeScroll 方法
                                invalidate()
                            }
                        }
                    }
                }
                // 设置为非拖动状态
                mIsBeingDragged = false
                // 记录触摸点坐标
                saveLocation(event.x.toInt(),event.y.toInt())
                recycleVelocityTracker()
            }
        }
        // 速度控制器添加事件
        mVelocityTracker?.addMovement(vtev)
        vtev.recycle()
        return true
    }


    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        println("onOverScrolled == $scrollX;$scrollY;$clampedX;$clampedY; Scroller.isFinished = ${mScroller.isFinished}")
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!mScroller.isFinished) {
            // 在Fling 的时候调用这里
            // 这里执行的是
            val oldX: Int = getScrollX()
            val oldY: Int = getScrollY()
            // 这里调用的是滑动
            setScrollX(scrollX)
            setScrollY(scrollY)
            onScrollChanged(getScrollX(), getScrollY(), oldX, oldY)
        } else {
            super.scrollTo(scrollX, scrollY)
        }
    }

    override fun computeScroll() {
        println("===computeScroll===")
        val ret = mScroller.computeScrollOffset()
        println("computeScroll == $ret")
        if(ret){
            val oldX: Int = scrollX
            val oldY: Int = scrollY
            val x = mScroller.currX
            val y = mScroller.currY

            if (oldX != x || oldY != y) {
                val range = getScrollRange()
                overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range,
                        0, mOverflingDistance, false)
                onScrollChanged(scrollX, scrollY, oldX, oldY)

            }
            // 通知界面刷新
            postInvalidate()
            return
        }
       // 已经滑动完成
        saveLocation(mScroller.finalX,mScroller.finalY)
    }
    //<editor-fold desc="私有方法">
    /**
     * 初始化速度控制器相关类
     */
    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    /**
     * 回收速度控制器
     */
    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }
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

    /**
     * @return 返回滑动范围
     */
    private fun getScrollRange():Int{
        return scrollRange
    }
    //</editor-fold>


    //<editor-fold desc="公共接口">
    /**
     * Like [View.scrollBy], but scroll smoothly instead of immediately.
     *
     * @param dx the number of pixels to scroll by on the X axis
     * @param dy the number of pixels to scroll by on the Y axis
     */
    fun smoothScrollBy(dx: Int, dy: Int) {
        var dy = dy
        if (childCount == 0) {
            // Nothing to do.
            return
        }
        val duration: Long = AnimationUtils.currentAnimationTimeMillis() - mLastScroll
        if (duration >ANIMATED_SCROLL_GAP) {
            val maxY = getScrollRange()
            val scrollY: Int = scrollY
            dy = Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY
            mScroller.startScroll(scrollY, scrollY, 0, dy,ANIMATED_SCROLL_GAP)
            postInvalidateOnAnimation()
        } else {
            if (!mScroller.isFinished) {
                mScroller.abortAnimation()
            }
            scrollBy(dx, dy)
        }
        mLastScroll = AnimationUtils.currentAnimationTimeMillis()
    }

    /**
     * Like [.scrollTo], but scroll smoothly instead of immediately.
     *
     * @param x the position where to scroll on the X axis
     * @param y the position where to scroll on the Y axis
     */
    fun smoothScrollTo(x: Int, y: Int) {
        smoothScrollBy(x - scrollX, y - scrollY)
    }
    //</editor-fold>


}