package com.kits.control.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Scroller
import androidx.annotation.RequiresApi
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParent
import androidx.core.view.NestedScrollingParentHelper
import kotlin.math.abs
import kotlin.math.max

/**
 * 惯性滑动：手指离开屏幕之后，控件会按照一定的速率继续滑动，优化了滑动的平滑性
 * 自定义滑动控件,此自定义控件支持惯性滑动
 * 平滑滑动：在滑动控件中，会有需求通过代码滑动到指定位置，如果使用接口 scrollTo/scrollBy 会有闪屏的现象
 * 因此这时候就需要使用平滑
 * 嵌套滑动作为子控件支持嵌套滑动，区别与联系
 * 1. 继承 LinearLayout
 * 2. 简单化处理，只处理垂直方向的布局
 * 3. 滑动的方向的前提是以手指的滑动作为参考点
 * 4. 内嵌滑动View本身的方法 和 实现 NestedScrollingChild
 * 5. 控件本身也支持作为嵌套滑动的父控件
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

    //<editor-fold desc="内嵌滑动相关属性">
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mNestedYOffset = 0
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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
                mNestedYOffset = 0
                /**
                 * 1. 判断是否支持内嵌滑动
                 * 2. 遍历获取距离最近的支持内嵌滑动的父控件，调用父控件的 onStartNestedScroll来判断，
                 * 接口  onStartNestedScroll 用于父控件根据入参决定 是否支持内嵌滑动，true 表面支持
                 * 3. 调用支持内嵌滑动父控件的onNestedScrollAccepted接口，onNestedScrollAccepted 父控件
                 * 支持内嵌滑动的时候，进行一些初始化工作
                 */
                startNestedScroll(View.SCROLL_AXIS_VERTICAL)
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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        println("onTouchEvent")
        // 这里的 主要也是处理滑动事件，点击事件由系统处理
        val vtev = MotionEvent.obtain(event)
        val actionMasked = vtev.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0
        }
        vtev.offsetLocation(0f, mNestedYOffset.toFloat())
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
                startNestedScroll(View.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_MOVE->{
                var dy = differY(event.y.toInt())
                println("dy == $dy ; touchSlop = $touchSlop ; mIsBeingDragged=$mIsBeingDragged ")
                if(isNestedScrollingEnabled){
                    // 判断是否父控件优先滑动
                    /**
                     * 接口  dispatchNestedPreScroll
                     * 参数 dx dy 子控件在x 轴,y 轴的偏移
                     * 参数 consumed 是数组，是父控件消耗了子控件偏移的数据
                     * 参数 mScrollOffset 也是数组，是父控件滑动了，这时候子控件也要应得滑动
                     */
                    if (dispatchNestedPreScroll(0, dy.toInt(), mScrollConsumed, mScrollOffset)) {
                        println("父控件优先滑动")
                        // 调整事件坐标
                        dy -= mScrollConsumed[1]
                        vtev.offsetLocation(0f, mScrollOffset[1].toFloat())
                        mNestedYOffset += mScrollOffset[1]
                    }else{
                        println("子控件优先滑动")
                        // 子控件优先 不用调整
                    }
                }
                // 通过系统分发到这里，判断是否可以滑动
                if(abs(dy) > touchSlop && !mIsBeingDragged){
                    mIsBeingDragged = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (mIsBeingDragged){
                    // 内嵌滑动下，子控件滑动
                    // 调用View的接口判断滑动
                    // 参数  deltaX ，deltaY 指的是滑动的偏移量
                    // 参数 scrollX scrollY 指的是已经滑动的距离
                    // 参数 scrollRangeX scrollRangeY 指的是滑动的范围
                    // 参见 maxOverScrollX  maxOverScrollY 指的是越界滑动的距离
                    //  isTouchEvent 系统中此参数没有使用
                    // 返回值 true 标识达到了最大越界，在惯性滑动中使用
                    // 调用onOverScrolled 实现真正的滑动
                    //println("dy = $dy ;scrollY = $scrollY ")
                    // 记录上一次滑动得距离
                    val oldY = scrollY
                    overScrollBy(0,dy,0,scrollY,0,scrollRange,0,0,true)
                    // 子控件滑动完成，需要判断dy 被完全消费，如果没有完全消费，通知父控件进行一定得滑动
                    val scrolledDeltaY: Int = scrollY - oldY
                    val unconsumedY: Int = dy - scrolledDeltaY
                    if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
                        dy -= mScrollOffset[1]
                        vtev.offsetLocation(0f, mScrollOffset[1].toFloat())
                        mNestedYOffset += mScrollOffset[1]
                    }
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
                        fling(-(it.yVelocity.toInt()))
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

    /**
     * 封装函数，方便多次处调用
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun fling(velocityY:Int){
        // 判断是否可以滑动
        val canFling = (scrollY > 0 || velocityY > 0) &&
                (scrollY < getScrollRange() || velocityY < 0)
        println("canFling == $canFling")
        // dispatchNestedPreFling 返回true 表明父控件完全消费子控件的惯性滑动
        if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
            println("子控件消费控件惯性滑动 ")
            // 父控件滑动，这里子控件和父控件（如果支持）本质上是做了一的滑动，没有办法父控件滑动一部分，子控件滑动一部分
            // 这部分的优化在 NestedScrollingChild2 NestedScrollingParent2 中实现了，需要参考RecyclerView的源码
            val ret = dispatchNestedFling(0f, velocityY.toFloat(), canFling)
            println("dispatchNestedFling ret == $ret")
            if (canFling) {
                /**
                 * 参数 startX， startY 起始的滑动距离
                 * 参数 velocityX velocityY 滑动的速度
                 * 参数  minX minY  最小的滑动距离
                 * 参数  maxX maxY 最大的滑动就离
                 */
                mScroller.fling(0, scrollY, 0, velocityY,
                        0, 0,
                        0, getScrollRange())
                // 通知界面刷新
                invalidate()
            }
        }else{

            println("父控件消费惯性滑动")
        }
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
                        0, mOverflingDistance, true)
                onScrollChanged(scrollX, scrollY, oldX, oldY)
            }
            // 通知界面刷新
            postInvalidate()
            return
        }
       // 已经滑动完成
        //saveLocation(mScroller.finalX,mScroller.finalY)
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

    /*******
     *  NestedScrollingChild NestedScrollingChildHelper 和 View本身的区别
     *  1. View 实现了 NestedScrollingChild 所有接口，且实现方法和  NestedScrollingChildHelper 是一样的
     *  2. 通过View 本身的方法来实现比较简单，可以针对想要修改的方法进行重写，而不需要实现NestedScrollingChild 的所有方法
     */
    //<editor-fold desc="View自身的方法和通过接口实现的区别和联系">
    class MyNestedScrollingChild(view:View) : NestedScrollingChild {
        private val  childHelper = NestedScrollingChildHelper(view)

        /**
         * 判断是否有父控件支持内嵌滑动
         * View 也有相应的接口，通过View的属性 mNestedScrollingParent 来判断
         * @return true 有支持嵌套滑动的父控件
         */
        override fun hasNestedScrollingParent(): Boolean {
            return childHelper.hasNestedScrollingParent()
        }

        /**
         * 设置 控件本身是否支持内嵌滑动
         */
        override fun setNestedScrollingEnabled(enabled: Boolean) {
           childHelper.isNestedScrollingEnabled = enabled
        }
        /**
         * 判断控件是否支持内嵌滑动，
         * View 通过接口setNestedScrollingEnabled 来设置，同时也有接口来获取
         * @return控件本身是否支持嵌套滑动
         */
        override fun isNestedScrollingEnabled(): Boolean {
            return childHelper.isNestedScrollingEnabled

        }

        /**
         * 通知父控件开始内嵌滑动
         * View 的实现和  NestedScrollingChildHelper 的实现逻辑是一样的
         * 子控件startNestedScroll 通过helper 调用 父控件onStartNestedScroll 和 onNestedScrollAccepted
         * @param axes 滑动方向
         * @return 父控件是否支持此方向的内嵌滑动
         *
         */
        override fun startNestedScroll(axes: Int): Boolean {
            return childHelper.startNestedScroll(axes)
        }

        /**
         * 在子控件滑动之前,通知给父控件，由父控件决定是否消费滑动距离。本质是调用嵌套滑动的父控件的onNestedPreScroll接口。
         * View的实现和 NestedScrollingChildHelper 是一样的
         * 子控件调用dispatchNestedPreScroll ，通过helper 调用父类的 onNestedPreScroll
         * @param dx 子控件在 x 轴方向的滑动距离
         * @param dy 子控件在 y 轴方向的滑动距离
         * @param consumed 数组 父控件消费的距离，索引0 表示的X轴；索引1 表示的是y轴
         * @param offsetInWindow 获取父控件的偏移
         * @return true 父控件需要消费此偏移，false 父控件不消费项偏移
         */
        override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
            return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
        }

        /**
         * 在子控件滑动之后，通知父控件是否需要消费剩余的移动距离。View的实现和 NestedScrollingChildHelper 是一样的
         * @param dxConsumed 子控件消费X轴的滑动距离
         * @param dyConsumed 子控件消费Y轴的滑动距离
         * @param dxUnconsumed 未消费的X轴滑动距离
         * @param dyUnconsumed 未消费的Y轴滑动距离
         * @param offsetInWindow 偏移量
         * @return true 父控件消费了剩余的距离
         */
        override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
            return childHelper.dispatchNestedScroll(dxConsumed,dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
        }

        /**
         * 在子控件开始惯性滑动前判断父控件是否完全消费惯性滑动距离
         * @param velocityX X 轴的速度
         * @param velocityY y 轴的速度
         * @return true 父控件消费完全惯性滑动距离，子控件不需要进行惯性滑动
         * false 父控件不消费惯性滑动，或者不完全消费惯性滑动，需要 dispatchNestedFling 传入滑动距离
         */

        override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
            return childHelper.dispatchNestedPreFling(velocityX, velocityY)
        }

        /**
         * 当 dispatchNestedPreFling 返回未false时调用
         * 通过系统源码，调用  dispatchNestedFling 和 子控件进行惯性滑动时一起的，说明如果进行惯性滑动
         * 父控件和子控件时一起滑动的。无法实现子控件滑动一段距离之后父控件再滑动一段距离
         */
        override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
            return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
        }

        /**
         * 停止内嵌滑动
         */
        override fun stopNestedScroll() {
            return childHelper.stopNestedScroll()
        }

    }

    //<editor-fold desc="处理作为父控件的嵌套滑动">
    /**
     * ViewGroup 默认返回false
     */
    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        // 测试条件，直接子控件和内嵌滑动子控件是同一个View，且滑动方向是垂直方向是支持内嵌滑动
        println("===onStartNestedScroll===")
        if (child?.equals(target) == true && (View.SCROLL_AXIS_VERTICAL == nestedScrollAxes)){
            println("控件本身支持内部子控件内嵌滑动")
            return true
        }
        return false
      // return super.onStartNestedScroll(child, target, nestedScrollAxes)
    }

    /**
     *  当控件 onStartNestedScroll 返回true 的时候通过 此接口进行一些嵌套滑动的初始化操作
     */
    override fun onNestedScrollAccepted(child: View?, target: View?, axes: Int) {
        // 保存了支持的嵌套滑动方向
        println("===onNestedScrollAccepted===")
       super.onNestedScrollAccepted(child, target, axes)
    }

    /**
     * 返回支持的嵌套滑动方向
     */
    override fun getNestedScrollAxes(): Int {
        //
        return super.getNestedScrollAxes()
    }

    /**
     * 在子控件滑动之前，优先消费滑动距离
     */
    override fun onNestedPreScroll(target: View?, dx: Int, dy: Int, consumed: IntArray?) {
        // ViewGroup 的默认方法是分发给自己的父控件消费，自己不消费
        // 如果作为父控件需要消费距离，就需要重写
        // 获取控件的坐标

        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    /**
     * 子控件滑动之后，分发给父控件消费
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onNestedScroll(target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        // 默认是分发给自己的父控件去处理
        //super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        // 控件本身需要处理，需要自行处理
        // 1. 记录已滑动的距离
        val oldY = scrollY
        //2. 滑动未消费的距离
        overScrollBy(0,dyConsumed,0,scrollY,0,scrollRange,0,0,true)
        // 计算已消费的距离
        val myConsumed: Int = scrollY - oldY
        // 计算未消费的距离
        val myUnconsumed = dyUnconsumed - myConsumed
        // 继续分发给上一层父控件处理
        dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null)

    }
    /**
     * 子控件滑动之后，分发给父控件消费
     */
    override fun onNestedPreFling(target: View?, velocityX: Float, velocityY: Float): Boolean {
        // 默认是分发给自己的父控件去处理
        return super.onNestedPreFling(target, velocityX, velocityY)
        // 控件本身需要处理，需要自行处理
    }

    override fun onNestedFling(target: View?, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        // 默认是分发给自己的父控件去处理
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun flingWithNestedDispatch(velocityY: Int) {
        val canFling = (scrollY > 0 || velocityY > 0) &&
                (scrollY < getScrollRange() || velocityY < 0)
        if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
            dispatchNestedFling(0f, velocityY.toFloat(), canFling)
            if (canFling) {
                fling(velocityY)
            }
        }
    }

    /**
     * 停止内嵌滑动
     */
    override fun onStopNestedScroll(child: View?) {
        super.onStopNestedScroll(child)
    }
    //</editor-fold>

    /***
     *  控件作为父控件支持内嵌滑动
     */
    class MyNestedScrollingParent(viewgroup: ViewGroup)  : NestedScrollingParent{
        private val  parentHelper = NestedScrollingParentHelper(viewgroup)

        /**
         * 子控件startNestedScroll 通过helper 调用 父控件onStartNestedScroll 和 onNestedScrollAccepted
         * @param child 控件的直接子View
         * @param target 内嵌滑动的子View
         * @param axes 滑动方向
         * @return true 是否支持内嵌滑动
         */
        override fun onStartNestedScroll(child: View, target: View, axes: Int): Boolean {
            // 有实现的方法来决定是否支持内嵌滑动，
            return true
        }

        /**
         * 子控件startNestedScroll 通过helper 调用 父控件onStartNestedScroll 和 onNestedScrollAccepted
         */
        override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
            // 对私有属性进行赋值，保存支持的嵌套滑动方向
            parentHelper.onNestedScrollAccepted(child,target, axes)
        }

        /**
         * 返回支持的嵌套滑动方向
         */
        override fun getNestedScrollAxes(): Int {
            return parentHelper.nestedScrollAxes
        }
        /**
         * 子控件调用dispatchNestedPreScroll ，通过helper 调用父类的 onNestedPreScroll
         * 当 consumed的数据元素存在一个不为0 返回true,否则为false
         */
        override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
            //helper 没有对应的实现，需要自行实现
        }

        /**
         * 子控件滑动之后调用 dispatchNestedScroll 通过 helper 调用到父类的 onNestedScroll
         */
        override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
            //helper 没有对应的实现，需要自行实现
        }

        /**
         * 实现的情景和 onNestedPreScroll 类似，不同的是处理惯性滑动
         */
        override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
            //helper 没有对应的实现，需要自行实现
            return false
        }

        /**
         * 实现的情景和 onNestedScroll 类似，不同的是处理惯性滑动
         */
        override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
            //helper 没有对应的实现，需要自行实现
            return false
        }

        override fun onStopNestedScroll(target: View) {
            parentHelper.onStopNestedScroll(target)
        }

    }

}