package com.android.app.planet

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.children
import com.android.core.utils.dip
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** 从这个角度开始画View ，可以调整 */
private const val START_ANGLE = 270f

/** px 转化为angle的比例
 * ps:一定要给设置一个转换，不然旋转的太欢了
 */
private const val SCALE_PX_ANGLE = 0.2f

/** 自动旋转角度，16ms（一帧）旋转的角度，值越大转的越快 */
private const val AUTO_SWEEP_ANGLE = 0.1f

/** 行星和太阳的父容器 */
class PlanetGroupView : FrameLayout {

    /** 旋转的角度 */
    private var sweepAngle = 0f
    private var pathRadius = 0f

    /** 容器距离左上右下的距离 */
    private var padding: Int = context.dip(80)

    /** 滑动结束后的动画 */
    private val velocityAnim = ValueAnimator()

    /** 手势处理 */
    private var downX = 0f

    /** 手指按下时的角度 */
    private var downAngle = sweepAngle

    /** 速度追踪器 */
    private val velocity = VelocityTracker.obtain()

    /** 自动滚动 */
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            sweepAngle += AUTO_SWEEP_ANGLE
            sweepAngle %= 360 // 取个模 防止sweepAngle爆表
            Log.d("jcy", "auto , sweepAngle == $sweepAngle")
            layoutChildren()
            postDelayed(this, 16)
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        // 通过isChildDrawingOrderEnable 动态改变子View的绘制顺序
        isChildrenDrawingOrderEnabled = true

        velocityAnim.apply {
            this.duration = 1000
            this.interpolator = DecelerateInterpolator()
            this.addUpdateListener {
                val value = it.animatedValue as Float
                sweepAngle += (value * SCALE_PX_ANGLE) // 乘以SCALE_PX_ANGLE是因为如果不乘 转得太欢了
                layoutChildren()
            }
        }
    }

    override fun onLayout(b: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        pathRadius = (measuredWidth / 2 - padding).toFloat()
        layoutChildren()
    }

    private fun layoutChildren() {
        val childCount = childCount
        if (childCount == 0) return
        val averageAngle = 360f / childCount
        // START_ANGLE° 开始画
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 弧度公式：1°=π/180°
            val angle = (START_ANGLE - averageAngle * index + sweepAngle).toDouble() * PI / 180
            val sin = sin(angle)
            val cos = cos(angle)
            val coordinateX = measuredWidth / 2 - pathRadius * cos
            val coordinateY =
                measuredHeight / 2 - pathRadius * sin * sin(PI / 9) // sin(PI/9)表示x轴方向倾斜的角度

            Log.e("jcy", "layoutChildren: angle=$angle sin=$sin cos=$cos")

            child.layout(
                (coordinateX - childWidth / 2).toInt(),
                (coordinateY - childHeight / 2).toInt(),
                (coordinateX + childWidth / 2).toInt(),
                (coordinateY + childHeight / 2).toInt()
            )

            // 假设view的最小缩放是原来的0.3倍，则缩放比例和角度的关系是
            val scale = (1 - 0.3f) / 2 * (1 - sin(angle)) + 0.3
            child.scaleX = scale.toFloat()
            child.scaleY = scale.toFloat()
        }
        changeZ()
//        testWatchDrawOrder()
    }

    /**
     * 改变子View的z值以改变子View的绘制优先级，z越大优先级越低（最后绘制）
     */
    private fun changeZ() {
        var order = 0.1f
        getSortChildFromScaleY().forEach {
            it.z = order
            order += 0.1f
        }
    }

    /** 根据Y轴缩放比，排序过后的子View */
    private fun getSortChildFromScaleY(): MutableList<View> {
        return children.sortedBy { it.scaleY }.toMutableList()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x ?: 0f
        velocity.addMovement(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downAngle = sweepAngle

                // 取消动画和自动旋转
                pause()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (downX - x) * SCALE_PX_ANGLE
                sweepAngle = (dx + downAngle)
                layoutChildren()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun pause() {
        velocityAnim.cancel()
        removeCallbacks(autoScrollRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        velocity.recycle()
        removeCallbacks(autoScrollRunnable)
    }

    private fun testWatchDrawOrder() {
        getSortChildFromScaleY().forEach {
            it.scaleX = 1f
            it.scaleY = 1f
        }
    }
}
