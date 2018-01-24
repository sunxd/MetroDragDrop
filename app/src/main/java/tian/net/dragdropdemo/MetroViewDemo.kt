package tian.net.dragdropdemo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.animation.ValueAnimator
import android.widget.Toast
import android.view.animation.Animation
import android.view.animation.ScaleAnimation


/**
 * <br/>
 * sunxd<br/>
 * sunxd14@gmail.com<br/>
 * 2018/1/3 下午5:54<br/>
 */
class MetroViewDemo : FrameLayout {

    private val TAG = "sunxd"

    private lateinit var mDragHelper: ViewDragHelper
    private lateinit var tempDragHelper: ViewDragHelper

    private var mSourceLeft = 0
    private var mSourceTop = 0
    private var mLastLeft = 0
    private var mLastTop = 0
    private var mTargetLeft = 0
    private var mTargetTop = 0

    //private var smoothTo = false
    private var scaleView :View? = null

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

    fun init(context: Context, attr: AttributeSet?, attrType: Int) {
        View.inflate(context, R.layout.metro_view, this)
        val pading = 8
        setPadding(pading, pading, pading, pading)

        //val view = getChildAt(0)
        for(i in 0 until childCount) {
            getChildAt(i).setOnClickListener {
                Toast.makeText(context, "我是view_$i", Toast.LENGTH_SHORT).show()
            }
        }


        mDragHelper = ViewDragHelper.create(this, object : ViewDragHelper.Callback(){

            override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
                if(tempDragHelper.continueSettling(false)) {
                    return false
                }
                Log.d(TAG, "tryCaptureView  left = " +  child?.left)
                return true
            }

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                super.onViewCaptured(capturedChild, activePointerId)
                mSourceLeft = capturedChild.left
                mSourceTop = capturedChild.top
                //capturedChild.bringToFront()
                bringChildToFront(capturedChild)
                invalidate()
                Log.d(TAG, "onViewCaptured  left = " +  mSourceLeft)
            }

            override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
                return left
            }

            override fun getViewHorizontalDragRange(child: View?): Int {
                return 1
            }

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                super.onViewPositionChanged(changedView, left, top, dx, dy)
                mLastLeft = left
                mLastTop = top
                var child = findChildViewUnder(changedView, mLastLeft, mLastTop)

                if(child == null && scaleView!= null) {
                    val scaleAnimation = ScaleAnimation(mScale, 1f, mScale, 1f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    //3秒完成动画
                    scaleAnimation.duration = mDuration
                    scaleAnimation.fillAfter = true
                    scaleView?.startAnimation(scaleAnimation)
                    scaleView?.invalidate()
                    scaleView = null
                }

                if(child != null) {
                    if(scaleView == null) {
                        scaleView = child
                        val scaleAnimation = ScaleAnimation(1f, mScale, 1f, mScale,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                        scaleAnimation.duration = mDuration
                        scaleAnimation.fillAfter = true
                        child.startAnimation(scaleAnimation)
                        child.invalidate()
                    }

                    val childCenter = (child.left + child.right)/2
                    if(!tempDragHelper.continueSettling(false)) {
                        if((left > child.left && left < childCenter)
                                || changedView.right > childCenter && changedView.right < child.right) {

                            val toLeft = mSourceLeft
                            val toTop = mSourceTop
                            mSourceLeft = child.left
                            mSourceTop = child.top

                            Log.d(TAG, "smoothSlideViewTo('"+ mSourceLeft +"', " + mSourceTop +  ")")
                            tempDragHelper.smoothSlideViewTo(child, toLeft, toTop)
                            invalidate()
                        }
                    }
                }
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
                mDragHelper.settleCapturedViewAt(mSourceLeft, mSourceTop)
                invalidate()
            }
        })

        tempDragHelper = ViewDragHelper.create(this, object : ViewDragHelper.Callback(){
            override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
                return false
            }
        })
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
            //Log.d(TAG, "tempDragHelper.continueSettling =  true")
            invalidate()
        }
    }

    private fun findChildViewUnder(view: View, x: Int, y: Int):View? {
        val count = childCount - 1
        for(i in count downTo 0) {
            var child = getChildAt(i)
            if(child != view) {
                /*if (x >= child.left && x <= (child.left + child.right)/2) {
                    return child
                }
                val dx = view.right
                if(dx >= (child.left + child.right)/2 && dx <= child.right) {
                    return child
                }*/
                //左边框
                if (x > child.left && x < child.right) {
                    return child
                }
                //右边框
                if (view.right > child.left && view.right < child.right) {
                    return child
                }
            }
        }
        return null
    }

    private fun smoothSlideViewTo(child: View, left: Int, top: Int) {
        val animator = ValueAnimator.ofFloat(child.left.toFloat(), left.toFloat())

        animator.addUpdateListener { animation -> child.x = animation.animatedValue as Float }
        animator.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                //child.left = child.x.toInt()
                //child.translationX = 0F
                //invalidate()
                Log.d(TAG, "left = ${child.left} x = ${child.x}  translateX = ${child.translationX}" )
            }
        })
        animator.start()
    }
}