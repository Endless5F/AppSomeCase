package com.android.app.custom

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
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
 * 1. 星球环的绘制
 *    椭圆公式rotateAngele
 *    用户视角调整cameraDistance，涉及每个角度星球大小和每个星球所在角度的计算
 * 2. 星球单个切换过渡动画逻辑
 * 3. 星球滑动(随手仅x轴)过渡动画逻辑
 * 4. 点击星球多个切换过渡动画逻辑
 * 5. 抽取每个星球静置状态下固定角度集
 * 6. 单独处理选中(最前方)星球单独缩放规则(选中星球最大)
 * 7. 点击切换和触摸抬起切换 事件冲突问题处理？
 * 8. 星球字体渐变色处理？
 * 9. 选中星球文本隐藏？
 *
 * ios 方案：
 * CATransform3D：https://www.kancloud.cn/manual/ios/97786
 * CATransformLayer：https://www.kancloud.cn/manual/ios/97792
 * 缺点：点击事件有问题(需动态计算)
 */
class StarGroupView : FrameLayout {
    private var centerX = 0
    private var centerY = 0

    /** 绘制轨道 */
    private val paint = Paint()

    /** 开始绘制的角度 */
    private val startAngle = 270f
    /** 开始位置放大比例 */
    private val startScala = 2f

    /** 平面正圆绕x轴旋转的角度，控制远近效果 */
    private val rotateAngela = 60f

    /** 相机(用户视角)距离 */
    private val cameraDistance = dip(130)

    /** 椭圆X轴半径 */
    private var ovalXRadius = 0f

    /** 椭圆Y轴半径 */
    private var ovalYRadius = 0f

    /** padding值 防止缩放超过边界 */
    private val padding = dip(50)

    /** 数据源 */
    private val dataArray = arrayListOf<StarItemModel>()

    /** 当前选中位置(以数据源中index为主) */
    private var currentSelected = 0

    /** 本地切换动画切换次数 */
    private var currentToggleNum = 1

    /** 是否允许滑动 */
    private var swipeEnable = false

    /** 随手滑动动画方向，是否向右滑动 */
    private var isSwipeRight = false

    /** 动画是否正在执行 */
    private var isAnimRunning = false

    /** 动画方向 */
    private var animDirection = ANIM_DIRECTION_NONE

    /** 动画滑动过程中触发切换的阈值方 */
    private var swipeToggleDistance = dip(100).toFloat()

    /** 滑动后的动画集 */
    private val transitionAnimList = arrayListOf<ValueAnimator>()

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
                    changeItemSelect()
                    isAnimRunning = false
                    animDirection = ANIM_DIRECTION_NONE
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
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
        setWillNotDraw(false)

        // 初始化数据
        initData(arrayListOf())
    }

    fun initData(data: List<StarItemModel>) {
        dataArray.add(StarItemModel("地球", R.drawable.planet_diqiu_activated))
        dataArray.add(StarItemModel("水星", R.drawable.planet_shuixing_activated))
        dataArray.add(StarItemModel("金星", R.drawable.planet_jinxing_activated))
        dataArray.add(StarItemModel("土星", R.drawable.planet_tuxing_activated))
        dataArray.add(StarItemModel("木星", R.drawable.planet_muxing_activated))
        dataArray.add(StarItemModel("火星", R.drawable.planet_huoxing_activated))

        currentSelected = 0
        dataArray[0].isSelected = true
        val all = dataArray.size
        val middle = all / 2f
        for (i in dataArray.indices) {
            val item = dataArray[i]
            item.initIndex(i)
            val child = StarItemView(context).apply {
                setPlanetBean(item)
                clickListener = { isSelected, currentIndex ->
                    if (!isSelected) {
                        val count = if (currentIndex > middle) {
                            // 左半边星球
                            isSwipeRight = true
                            all - currentIndex
                        } else {
                            // 右半边星球
                            isSwipeRight = false
                            currentIndex
                        }
                        executeToggleTransitionAnimation(count)
                    }
                }
            }
            addView(child, LayoutParams(-2, -2))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        ovalXRadius = (measuredWidth - padding * 2).toFloat() / 2
        ovalYRadius = (ovalXRadius * cos(rotateAngela * PI / 180)).toFloat()
        swipeToggleDistance = ovalXRadius

        // 计算各个星球位置对应角度
        StarItemModel.calculateAngleArray(startAngle, dataArray.size) { index, average ->
            val currentAngle = getCurrentItemAngle(index, average)
            dataArray[index].currentAngle = currentAngle
            currentAngle
        }

        setMeasuredDimension(measuredWidth, (ovalYRadius * 2 + padding * 2).toInt())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        centerX = measuredWidth / 2
        centerY = measuredHeight / 2

        for (index in 0 until childCount) {
            layoutItemChildren(index)
        }
    }

    private fun layoutItemChildren(index: Int) {
        val child = getChildAt(index)
        val childWidth = child.measuredWidth
        val childHeight = child.measuredHeight
        // 度数
        val angle = dataArray[index].currentAngle
        // 弧度
        val radian = angle.toDouble() * PI / 180
        val sin = sin(radian)
        val cos = cos(radian)
        val coordinateX = centerX + ovalXRadius * cos
        val coordinateY = centerY - ovalYRadius * sin

        val x1 = (coordinateX - childWidth / 2).toInt()
        val y1 = (coordinateY - childHeight / 2).toInt()
        val x2 = (coordinateX + childWidth / 2).toInt()
        val y2 = (coordinateY + childHeight / 2).toInt()
        child.layout(x1, y1, x2, y2)

        val scale = calculateScale(angle)

        child.scaleX = scale
        child.scaleY = scale
    }

    /** 计算缩放比例 */
    private fun calculateScale(angle: Float): Float {
        val angleAbs = (angle + 360) % 360
        Log.e("jcy", "calculateScale: angle=$angle angleAbs=$angleAbs")
        val radian = angle.toDouble() * PI / 180
        val isRange = StarItemModel.isSelectAngleRange(angleAbs)
        return if (isRange) {
            calculateSelectScaleRule(angleAbs)
        } else {
            // 缩放比例和角度的关系：保证270度时最大，90度时最小，并且最小为0.3，最大为1
            //        val scale = (1 - sin(angle)) / 2 + 0.3
            // 物体离你的距离增加一倍，视觉上，物体缩小为原来的1/2。
            // 缩放比例 = (相机(用户视角)距离-绕x轴旋转距离) / 相机(用户视角)距离
            (((cameraDistance - ovalYRadius * sin(radian)) / cameraDistance)).toFloat()
        }
    }

    /** 计算选中(最前方)星球单独缩放规则 */
    private fun calculateSelectScaleRule(angle: Float): Float {
        val firstNextAngle = StarItemModel.firstNextAngle
        val diff = angle - startAngle
        val firstRadian = firstNextAngle.toDouble() * PI / 180
        val firstNextRadian = firstNextAngle.toDouble() * PI / 180
        val firstScala =
            (((cameraDistance - ovalYRadius * sin(firstRadian)) / cameraDistance) * startScala).toFloat()
        val firstNextScala =
            (((cameraDistance - ovalYRadius * sin(firstNextRadian)) / cameraDistance)).toFloat()
        Log.e("jcy", "calculateSelectScaleRule: diff=$diff ${firstNextAngle - startAngle}")
        return (1 - abs(diff) / (abs(firstNextAngle - startAngle))) * (firstScala - firstNextScala) + firstNextScala
    }

//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        paint.isAntiAlias = true
//        paint.color = Color.BLUE
//        paint.style = Paint.Style.FILL
//        paint.strokeWidth = 1f
//        paint.style = Paint.Style.STROKE
//
//        val a = 1
//        val startX = a * padding.toFloat()
//        val startY = a * padding.toFloat()
//        val endX = (measuredWidth - padding * a).toFloat()
//        val endY = (measuredHeight - bottomPadding - padding * a).toFloat()
//
//        canvas.drawOval(startX, startY, endX, endY, paint)
//    }

    /** 手势处理 */
    private var downX = 0f

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
                if (abs(dx) > touchSlop || lastSwipeProgress != 0f) {
                    // 执行切换动画
                    executeToggleTransitionAnimation(1)
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
                    it.resetCurrentIndex(currentToggleNum)
                }
                ANIM_DIRECTION_LEFT -> {
                    it.resetCurrentIndex(-currentToggleNum)
                }
                else -> {}
            }
        }
    }

    /** 改变所有星球的角度 */
    private fun changeItemsAngle() {
        dataArray.forEach {
            it.currentAngle = it.currentAngle()
        }
    }

    /** 改变所有星球的选中态 */
    private fun changeItemSelect() {
        dataArray.forEachIndexed { index, model ->
            val isSelected = model.currentAngle == startAngle
            if (isSelected) {
                currentSelected = index
            }
            model.isSelected = isSelected
            getChildAt(model.originalIndex).isSelected = isSelected
        }
    }

    /** 获取当前星球角度 */
    private fun getCurrentItemAngle(index: Int, average: Float): Float {
        val angle = (startAngle + average * index) % 360
        // 角度缩放比
        val scale = ovalYRadius * cos(angle * PI / 180) / cameraDistance
        val scaleAngle = average * scale.toFloat()
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
            var fromAngle = item.currentAngle()
            val endAngle: Float
            val toAngle = if (isSwipeRight) {
                // 右滑：current --> next (小 --> 大)
                val nextAngle = item.nextAngle(1)
                endAngle = if (fromAngle > nextAngle) nextAngle + 360 else nextAngle
                fromAngle += (endAngle - fromAngle) * fromProgress
                val toAngle = (endAngle - fromAngle) * toProgress + fromAngle
                toAngle
            } else {
                // 左滑：current --> previous (大 --> 小)
                val previousAngle = item.previousAngle(1)
                endAngle = if (fromAngle < previousAngle) previousAngle - 360 else previousAngle
                fromAngle -= (fromAngle - endAngle) * fromProgress
                val toAngle = fromAngle - (fromAngle - endAngle) * toProgress
                toAngle
            }
            item.currentAngle = toAngle
            layoutItemChildren(item.originalIndex)
        }
    }

    /**
     * 执行切换动画
     * @param count 切换个数
     */
    private fun executeToggleTransitionAnimation(count: Int) {
        if (isAnimRunning) return
        currentToggleNum = count
        initTransitionAnimList()
        animDirection = if (isSwipeRight) ANIM_DIRECTION_RIGHT else ANIM_DIRECTION_LEFT
        var build: AnimatorSet.Builder? = null
        dataArray.forEachIndexed { index, item ->
            val fromAngle = item.currentAngle
            val toAngle = if (isSwipeRight) {
                // 右滑：current --> next (小 --> 大)
                val nextAngle = item.nextAngle(count)
                if (fromAngle > nextAngle) nextAngle + 360 else nextAngle
            } else {
                // 左滑：current --> previous (大 --> 小)
                val previousAngle = item.previousAngle(count)
                if (fromAngle < previousAngle) previousAngle - 360 else previousAngle
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