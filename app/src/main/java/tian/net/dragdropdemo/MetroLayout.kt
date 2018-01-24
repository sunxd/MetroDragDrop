package tian.net.dragdropdemo

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

/**
 * <br/>
 * sunxd<br/>
 * sunxd14@gmail.com<br/>
 * 2018/1/16 下午3:01<br/>
 */
class MetroLayout: ViewGroup {

    val TAG = "sunxd"

    private var hDivider: Int = 0
    private var vDivider: Int = 0
    private val colsCount = 3

    private val viewPosition = mutableListOf<Rect>()
    private var layoutRect: Rect = Rect()

    private lateinit var mDragHelper: ViewDragHelper
    private lateinit var tempDragHelper: ViewDragHelper
    private lateinit var tempDragHelper2: ViewDragHelper
    private var scaleView :View? = null
    private var scaleView2 :View? = null

    private val mDuration = 200L
    private val mScale = .95F

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        init(context, attr, 0)
    }


    constructor(context: Context, attr: AttributeSet, attrType: Int) : super(context, attr, attrType) {
        init(context, attr, attrType)
    }

    private fun init(context: Context, attr: AttributeSet?, attrType: Int) {
        hDivider = 10
        vDivider = 10

        mDragHelper = ViewDragHelper.create(this, object : ViewDragHelper.Callback(){

            override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
                if(tempDragHelper.continueSettling(false) ||
                        mDragHelper.continueSettling(false)) {
                    return false
                }
                return true
            }

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                super.onViewCaptured(capturedChild, activePointerId)
                capturedChild.tag = Point(capturedChild.left, capturedChild.top)
                bringChildToFront(capturedChild)
                invalidate()
            }

            override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
                if(child is IMetroView) {
                    if(child.getMode() == 1) {
                        return child?.left!!
                    }
                }
                return left
            }

            override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
                if(child is IMetroView) {
                    if(child.getMode() == 1) {
                        return top
                    }
                }
                return child?.top!!
                //return top
            }

            override fun getViewHorizontalDragRange(child: View?): Int = 1

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                super.onViewPositionChanged(changedView, left, top, dx, dy)
                var child = findChildViewUnder(changedView, left, top)

                //放大卡至正常大小
                if(child == null && scaleView!= null) {
                    zoomView(scaleView)
                    scaleView = null
                    zoomView(scaleView2)
                    scaleView2 = null
                }

                if(child != null) {

                    if(child is IMetroView && changedView is IMetroView) {

                        //收缩卡片
                        if(scaleView == null) {
                            scaleView = child
                            shrinkView(child)
                            Log.d(TAG, "大卡片-小卡片  上 = " + child.toString())
                        }

                        if(changedView.getMode() == 0 && child.getMode() == 0) {
                            val childCenter = (child.left + child.right)/2
                            if(!tempDragHelper.continueSettling(false)) {
                                if((left > child.left && left < childCenter)
                                        || changedView.right > childCenter && changedView.right < child.right) {

                                    val sourcePoint = changedView.tag as Point
                                    val toLeft = sourcePoint.x
                                    val toTop = sourcePoint.y
                                    changedView.tag = Point(child.left, child.top)
                                    tempDragHelper.smoothSlideViewTo(child, toLeft, toTop)
                                    invalidate()
                                }
                            }
                        }else if(changedView.getMode() == 1 && child.getMode() == 0 ) {
                            //小卡片， 大卡片
                        }else if(changedView.getMode() == 0 && child.getMode() == 1) {
                            //大卡片   小卡片
                            if(scaleView2 == null) {
                                val view = findChildViewUnder(changedView, left, top, true)
                                Log.d(TAG, "大卡片-小卡片  下 = " + view.toString())
                                if(view != null) {
                                    scaleView2 = view
                                    shrinkView(view)
                                }
                            }
                            val childCenter = (child.left + child.right)/2
                            if(!tempDragHelper.continueSettling(false)) {
                                if((left > child.left && left < childCenter)
                                        || changedView.right > childCenter && changedView.right < child.right) {

                                    val sourcePoint = changedView.tag as Point
                                    val toLeft = sourcePoint.x
                                    val toTop = sourcePoint.y
                                    changedView.tag = Point(child.left, child.top)

                                    tempDragHelper.smoothSlideViewTo(child, toLeft, toTop)
                                    tempDragHelper2.smoothSlideViewTo(scaleView2, toLeft, scaleView2?.top!!)
                                    invalidate()
                                }
                            }
                        }else if(changedView.getMode() == 1 && child.getMode() == 1) {
                            //小卡片   小卡片
                            val childCenter = (child.top + child.bottom)/2
                            if(!tempDragHelper.continueSettling(false)) {
                                if((top > child.top && top < childCenter)
                                        || changedView.bottom > childCenter && changedView.bottom < child.bottom) {

                                    val sourcePoint = changedView.tag as Point
                                    val toLeft = sourcePoint.x
                                    val toTop = sourcePoint.y
                                    changedView.tag = Point(child.left, child.top)
                                    tempDragHelper.smoothSlideViewTo(child, toLeft, toTop)
                                    invalidate()
                                }
                            }
                        }
                    }


                }
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
                val point = releasedChild.tag as Point
                mDragHelper.settleCapturedViewAt(point.x, point.y)
                invalidate()
            }
        })

        tempDragHelper = ViewDragHelper.create(this, object : ViewDragHelper.Callback(){
            override fun tryCaptureView(child: View?, pointerId: Int): Boolean = false
        })

        tempDragHelper2 = ViewDragHelper.create(this, object : ViewDragHelper.Callback(){
            override fun tryCaptureView(child: View?, pointerId: Int): Boolean = false
        })
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val dw = w - 4 * vDivider
        val dh = h - 2 * hDivider
        val childW = dw/3
        val childH = (dh - hDivider)/2
        //3列，每列最多两行
        viewPosition.clear()
        for(i in 0 until colsCount) {
            viewPosition.add(Rect(i * (childW + hDivider) + hDivider, hDivider, (i+1) * (childW + hDivider), hDivider + childH))
            viewPosition.add(Rect(i * childW + (i + 1) * hDivider, hDivider * 2 + childH , (i+1) * (childW + hDivider), h-vDivider))
        }
        for(i in 0 until childCount) {
            getChildAt(i).tag = null
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        //measure child
        val W = right - left - 4 * vDivider
        val H = bottom - top - 2 * hDivider
        val childW = W/3
        val childH = (H - hDivider)/2
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if(child is IMetroView) {
                val category = child.getMode()
                when(category) {
                    0 -> {
                        child.measure(makeMeasureSpec(childW, EXACTLY), makeMeasureSpec(H, EXACTLY))
                    }
                    1 -> {
                        child.measure(makeMeasureSpec(childW, EXACTLY), makeMeasureSpec(childH, EXACTLY))
                    }
                }
            }
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for(i in 0 until childCount) {
            val child = getChildAt(i)
            if(child is IMetroView) {
                val mode = child.getMode()
                layoutRect.set(0, 0, 0, 0)
                when(mode) {
                    0 -> {
                        for(rect in viewPosition) {
                            if(!hasChildViewUnder(rect.left, rect.top)) {
                                layoutRect.set(rect.left, rect.top, rect.right, rect.bottom*2)
                                break
                            }
                        }
                    }
                    1 -> {
                        for(rect in viewPosition) {
                            if(!hasChildViewUnder(rect.left, rect.top)) {
                                layoutRect.set(rect.left, rect.top, rect.right, rect.bottom)
                                break
                            }
                        }
                    }
                }
                if(layoutRect.left == layoutRect.right) {
                    layoutRect.set(child.left, child.top, child.right, child.bottom)
                }
                child.layout(layoutRect.left, layoutRect.top, layoutRect.right, layoutRect.bottom)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return mDragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if(mDragHelper.continueSettling(true)) {
            invalidate()
        }
        if(tempDragHelper.continueSettling(false)) {
            invalidate()
        }
        if(tempDragHelper2.continueSettling(false)) {
            invalidate()
        }
    }

    private fun hasChildViewUnder(x: Int, y: Int): Boolean {
        for(i in 0 until childCount) {
            var child = getChildAt(i)
            if(x >= child.left && x<= child.right
                    && y >= child.top && y <= child.bottom) {
                return true
            }
        }
        return false
    }

    private fun findChildViewUnder(view: View, x: Int, y: Int, reverse: Boolean = false):View? {
        val count = childCount - 1
        for(i in count downTo 0) {
            var child = getChildAt(i)
            if(child != view) {
                val childRect = Rect(child.left, child.top, child.right, child.bottom)
                if(reverse) {
                    //先下后上
                    if(contains(childRect, view.left, view.bottom)) return child
                    if(contains(childRect, view.right, view.bottom)) return child
                    /*if(contains(childRect, view.left, view.top)) return child
                    if(contains(childRect, view.right, view.top)) return child*/
                }else {
                    //先上后下
                    if(childRect.contains(view.left, view.top)) return child
                    if(childRect.contains(view.right, view.top)) return child
                    if(childRect.contains(view.left, view.bottom)) return child
                    if(childRect.contains(view.right, view.bottom)) return child
                }
            }
        }
        return null
    }

    /**
     * 缩小View
     */
    private fun shrinkView(view: View) {
        val scaleAnimation = ScaleAnimation(1f, mScale, 1f, mScale,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.duration = mDuration
        scaleAnimation.fillAfter = true
        view.startAnimation(scaleAnimation)
        view.invalidate()
    }

    /**
     * 放大View
     */
    private fun zoomView(view: View?) {
        val scaleAnimation = ScaleAnimation(mScale, 1f, mScale, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        //3秒完成动画
        scaleAnimation.duration = mDuration
        scaleAnimation.fillAfter = true
        view?.startAnimation(scaleAnimation)
        view?.invalidate()
    }

    private fun contains(rect: Rect, x: Int, y: Int): Boolean {
        return (rect.left < rect.right && rect.top < rect.bottom
                && x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom)
    }
}