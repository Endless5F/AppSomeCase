package com.android.app.custom

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.android.app.R
import com.android.core.utils.dip
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


/**
 * 动画方向
 */
private const val ANIM_DIRECTION_NONE = -1
private const val ANIM_DIRECTION_LEFT = 0
private const val ANIM_DIRECTION_RIGHT = 1

/**
 * 星球环
 *
 * ios 方案：
 * CATransform3D：https://www.kancloud.cn/manual/ios/97786
 * CATransformLayer：https://www.kancloud.cn/manual/ios/97792
 * 缺点：点击事件有问题
 */
class CustomPlanetGroup : FrameLayout {
    private var centerX = 0
    private var centerY = 0

    /** 绘制轨道 */
    private val paint = Paint()

    /** 开始绘制的角度 */
    private val startAngle = 270f

    /** 平面正圆绕x轴旋转的角度，控制远近效果 */
    private val rotateAngela = 50f

    /** 每个星球平均的角度 */
    private var averageAngle = 0f

    /** 相机(用户视角)距离 */
    private val cameraDistance = dip(150)

    /** 椭圆X轴半径 */
    private var ovalXRadius = 0f

    /** 椭圆Y轴半径 */
    private var ovalYRadius = 0f

    /** padding值 防止缩放超过边界 */
    private val padding = dip(50)
    private val bottomPadding = dip(0)

    /** 数据源 */
    private val dataArray = arrayListOf<PlanetItem>()

    /** 是否允许滑动 */
    private var swipeEnable = true
    private var isAnimRunning = false

    /** 动画方向 */
    private var animDirection = ANIM_DIRECTION_NONE

    /** 动画滑动过程中触发切换的阈值方 */
    private var swipeToggleDistance = dip(100).toFloat()

    /** 系统所认为的最小滑动距离TouchSlop */
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    /** 动画集 */
    private val animSet by lazy {
        AnimatorSet().apply {
            interpolator = DecelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    changeItemIndex()
                    changeItemsAngle()
                    isAnimRunning = false
                    animDirection = ANIM_DIRECTION_NONE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    changeItemIndex()
                    changeItemsAngle()
                    isAnimRunning = false
                    animDirection = ANIM_DIRECTION_NONE
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
        }
    }

    /** 滑动后的动画集 */
    private val transitionAnimList = arrayListOf<ValueAnimator>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        setWillNotDraw(false)

        dataArray.add(PlanetItem(0, "地球", R.drawable.planet_diqiu_activated))
        dataArray.add(PlanetItem(1, "水星", R.drawable.planet_shuixing_activated))
        dataArray.add(PlanetItem(2, "金星", R.drawable.planet_jinxing_activated))
        dataArray.add(PlanetItem(3, "火星", R.drawable.planet_huoxing_normal))
        dataArray.add(PlanetItem(4, "土星", R.drawable.planet_tuxing_activated))
        dataArray.add(PlanetItem(5, "木星", R.drawable.planet_tianwangxing_normal))
        dataArray.add(PlanetItem(6, "海王星", R.drawable.planet_haiwagnxing_activated))

        for (i in dataArray.indices) {
            val child = CustomPlanetView(context).apply {
                setPlanetBean(dataArray[i])
            }
            addView(child, LayoutParams(dip(80), dip(80)))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            measuredWidth,
            (measuredWidth * cos(rotateAngela * PI / 180) + bottomPadding).toInt()
        )

        ovalXRadius = (measuredWidth - padding * 2).toFloat() / 2
        ovalYRadius = (measuredHeight - padding * 2).toFloat() / 2

        swipeToggleDistance = ovalXRadius

        Log.e("jcy", "onMeasure: ovalXRadius=$ovalXRadius ovalYRadius=$ovalYRadius")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        centerX = measuredWidth / 2
        centerY = measuredHeight / 2

        averageAngle = 360f / childCount
        changeItemsAngle()

        for (index in 0 until childCount) {
            layoutItemChildren(index)
        }
    }

    private fun layoutItemChildren(index: Int) {
        val child = getChildAt(index)
        val childWidth = child.measuredWidth
        val childHeight = child.measuredHeight
        // °
        val du = dataArray[index].currentAngle
        val angle = du.toDouble() * Math.PI / 180
        val sin = sin(angle)
        val cos = cos(angle)
        val coordinateX = centerX + ovalXRadius * cos
        val coordinateY = centerY - ovalYRadius * sin

        val x1 = (coordinateX - childWidth / 2).toInt()
        val y1 = (coordinateY - childHeight / 2).toInt()
        val x2 = (coordinateX + childWidth / 2).toInt()
        val y2 = (coordinateY + childHeight / 2).toInt()
        child.layout(x1, y1, x2, y2)

        // 缩放比例和角度的关系：保证270度时最大，90度时最小，并且最小为0.3，最大为1
//        val scale = (1 - sin(angle)) / 2 + 0.3
        // 物体离你的距离增加一倍，视觉上，物体缩小为原来的1/2。
        // 缩放比例 = (相机(用户视角)距离-绕x轴旋转距离) / 相机(用户视角)距离
        val scale = (cameraDistance - ovalYRadius * sin(angle)) / cameraDistance
        child.scaleX = scale.toFloat()
        child.scaleY = scale.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.isAntiAlias = true
        paint.color = Color.BLUE
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE

        val a = 1
        val startX = a * padding.toFloat()
        val startY = a * padding.toFloat()
        val endX = (measuredWidth - padding * a).toFloat()
        val endY = (measuredHeight - bottomPadding - padding * a).toFloat()

        canvas.drawOval(startX, startY, endX, endY, paint)
    }

    /** 手势处理 */
    private var downX = 0f

    /** 随手滑动动画方向，是否向右滑动 */
    private var isSwipeRight = false

    /** 随手滑动动画进度 */
    private var swipeProgress = 0f
    private var lastSwipeProgress = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x ?: 0f
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                resetSwipeProgress()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = downX - x
                if (swipeEnable) {
                    // 执行滑动动画
                    swipeDirectionAndProgress(dx)
                    executeSwipeTransitionAnimation()
                }
            }
            MotionEvent.ACTION_UP -> {
                val dx = downX - x
                isSwipeRight = dx <= 0
                if (abs(dx) > touchSlop || swipeEnable) {
                    // 执行切换动画
                    executeToggleTransitionAnimation()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /** 获取当前星球index */
    private fun changeItemIndex() {
        if (animDirection == ANIM_DIRECTION_NONE) return
        dataArray.forEach {
            when (animDirection) {
                ANIM_DIRECTION_RIGHT -> {
                    if (it.currentIndex == dataArray.size - 1) {
                        it.currentIndex = 0
                    } else {
                        it.currentIndex += 1
                    }
                }
                ANIM_DIRECTION_LEFT -> {
                    if (it.currentIndex == 0) {
                        it.currentIndex = dataArray.size - 1
                    } else {
                        it.currentIndex -= 1
                    }
                }
                else -> {}
            }
        }
    }

    /** 改变所有星球的角度 */
    private fun changeItemsAngle() {
        dataArray.forEach {
            it.currentAngle = getCurrentItemAngle(it.currentIndex)
            it.tempAngle = it.currentAngle
            val nextIndex = when (it.currentIndex) {
                dataArray.size - 1 -> 0
                else -> it.currentIndex + 1
            }
            it.nextAngle = getCurrentItemAngle(nextIndex)
            val previousIndex = when (it.currentIndex) {
                0 -> dataArray.size - 1
                else -> it.currentIndex - 1
            }
            it.previousAngle = getCurrentItemAngle(previousIndex)
        }
    }

    /** 获取当前星球角度 */
    private fun getCurrentItemAngle(index: Int): Float {
        val angle = (startAngle + averageAngle * index) % 360
        // 角度缩放比
        val scale = ovalYRadius * cos(angle * PI / 180) / cameraDistance
        val scaleAngle = averageAngle * scale.toFloat()
        // 二、三象限角度减小；一、四象限角度增大
        return if (angle == 90f || angle == 270f) angle else angle + scaleAngle
    }

    /** 重置随手滑动动画进度 */
    private fun resetSwipeProgress() {
        swipeProgress = 0f
        lastSwipeProgress = 0f
    }

    /** 计算滑动方向和进度 */
    private fun swipeDirectionAndProgress(dx: Float) {
        isSwipeRight = dx <= 0
        // 滑动进度
        swipeProgress = abs(dx) / swipeToggleDistance
        swipeProgress = if (swipeProgress > 1f) 1f else swipeProgress
    }

    /** 执行滑动动画 */
    private fun executeSwipeTransitionAnimation() {
        val fromProgress = lastSwipeProgress
        val toProgress = swipeProgress
        lastSwipeProgress = toProgress
        initTransitionAnimList()
        dataArray.forEach { item ->
            var fromAngle = item.tempAngle
            val endAngle: Float
            val toAngle = if (isSwipeRight) {
                // 右滑：current --> next (小 --> 大)
                endAngle =
                    if (fromAngle > item.nextAngle) item.nextAngle + 360f else item.nextAngle
                fromAngle += (endAngle - fromAngle) * fromProgress
                val toAngle = (endAngle - fromAngle) * toProgress + fromAngle
                toAngle
            } else {
                // 左滑：current --> previous (大 --> 小)
                endAngle =
                    if (fromAngle < item.previousAngle) item.previousAngle - 360f else item.previousAngle
                fromAngle -= (fromAngle - endAngle) * fromProgress
                val toAngle = fromAngle - (fromAngle - endAngle) * toProgress
                toAngle
            }
            item.currentAngle = toAngle
            layoutItemChildren(item.originalIndex)
        }
    }

    /** 执行切换动画 */
    private fun executeToggleTransitionAnimation() {
        // 滑动动画 需要实时更新
        animSet.cancel()
        lastSwipeProgress = 1f
        initTransitionAnimList()
        animDirection = if (isSwipeRight) ANIM_DIRECTION_RIGHT else ANIM_DIRECTION_LEFT
        var build: AnimatorSet.Builder? = null
        dataArray.forEachIndexed { index, item ->
            val fromAngle = item.currentAngle
            val toAngle = if (isSwipeRight) {
                // 右滑：current --> next (小 --> 大)
                if (fromAngle > item.nextAngle) item.nextAngle + 360f else item.nextAngle
            } else {
                // 左滑：current --> previous (大 --> 小)
                if (fromAngle < item.previousAngle) item.previousAngle - 360f else item.previousAngle
            }
            val animItemIndex = item.originalIndex
            transitionAnimList[animItemIndex].setFloatValues(fromAngle, toAngle)
            if (index == 0) {
                build = animSet.play(transitionAnimList[animItemIndex])
            } else {
                build?.with(transitionAnimList[animItemIndex])
            }
        }
        isAnimRunning = true
        animSet.duration = 300L
        animSet.start()
    }

    private fun initTransitionAnimList() {
        if (transitionAnimList.isEmpty()) {
            for (i in 0 until dataArray.size) {
                transitionAnimList.add(createPlanetTransitionAnim(i))
            }
        }
    }

    private fun createPlanetTransitionAnim(index: Int): ValueAnimator {
        return ValueAnimator().apply {
            this.addUpdateListener {
                val value = it.animatedValue as Float
                dataArray[index].currentAngle = value
                layoutItemChildren(index)
            }
        }
    }
}

