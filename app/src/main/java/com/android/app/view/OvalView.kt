package com.android.app.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import com.android.core.utils.dip
import java.lang.Math.PI
import kotlin.math.abs
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

/**
 * 3d旋转点坐标计算公式:
 * 1. 绕Z轴旋转a度
 *    x1=x*cos（a）-y*sin（a）;
 *    y1=y*cos（a）+x*sin（a）;
 *    z1=z;
 * 2. 绕X轴旋转a度
 *    x1=x;
 *    y1=y*cos（a）-z*sin（a）;
 *    z1=z*cos（a）+y*sin（a）;
 * 3. 绕Y轴旋转a度
 *    x1=x*cos（a）-z*sin（a）;
 *    y1=y;
 *    z1=z*cos（a）+x*sin（a）;
 *
 * 椭圆公式：x^2/a^2 + y^2/b^2 = 1
 */
class OvalView : FrameLayout {
    private val paint = Paint()
    private val padding = dip(50)
    private var ovalA = 0f
    private var ovalB = 0f

    private val startAngle = 270f

    /** 滑动结束后的动画 */
    private val velocityAnim = ValueAnimator()

    /** 手势处理 */
    private var downX = 0f

    private var sweepAngle = 0f

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
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        // ViewGroup 需要主动开启 dispatchDraw() 以外的绘制
        setWillNotDraw(false)
        // 通过isChildDrawingOrderEnable 动态改变子View的绘制顺序
        isChildrenDrawingOrderEnabled = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        ovalA = (measuredWidth - padding - padding).toFloat() / 2
        ovalB = (measuredHeight - padding - padding).toFloat() / 2
        layoutChildren()
    }

    private fun layoutChildren() {
        val childCount = childCount
        if (childCount == 0) return
        val centerX = measuredWidth / 2
        val centerY = measuredHeight / 2
        val averageAngle = 360f / childCount
        val middle = childCount / 2f
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            val diff = index - middle
            val proportion = (diff % middle) / middle

            val du = startAngle + averageAngle * index + + sweepAngle - averageAngle * proportion
            Log.e("jcy", "layoutChildren: index=$index diff=$diff proportion=$proportion du=$du")
            val angle = du.toDouble() * PI / 180
            val sin = sin(angle)
            val cos = cos(angle)
            val coordinateX = centerX + ovalA * cos
            val coordinateY = centerY - ovalB * sin

            val x1 = (coordinateX - childWidth / 2).toInt()
            val y1 = (coordinateY - childHeight / 2).toInt()
            val x2 = (coordinateX + childWidth / 2).toInt()
            val y2 = (coordinateY + childHeight / 2).toInt()
            child.layout(x1, y1, x2, y2)

            // 缩放比例和角度的关系：保证270度时最大，90度时最小，并且最小为0.3，最大为1
            val scale = (1 - sin(angle)) / 2 + 0.3
            child.scaleX = scale.toFloat()
            child.scaleY = scale.toFloat()
        }
        changeZ()
    }

    private fun getIndexAngle() {
        val childCount = childCount
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

    override fun onDraw(canvas: Canvas?) {
        paint.isAntiAlias = true
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE

        val startX = padding.toFloat()
        val startY = padding.toFloat()
        val endX = (measuredWidth - padding).toFloat()
        val endY = (measuredHeight - padding).toFloat()

        canvas?.drawOval(startX, startY, endX, endY, paint)
        super.onDraw(canvas)
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
}