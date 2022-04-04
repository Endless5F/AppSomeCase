package com.android.app.planettab

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.android.app.R
import com.android.app.planettab.VibrateUtil.startVibrate
import com.android.core.utils.dip
import com.android.core.utils.getScreenWidth
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 动画方向
 */
private const val ANIM_DIRECTION_NONE = 0
private const val ANIM_DIRECTION_LEFT = -1
private const val ANIM_DIRECTION_RIGHT = 1

/**
 * @author jiaochengyun@baidu.com
 * @since 3.0.0
 *
 * 星球Tab
 * 1. 星球环的绘制
 *    椭圆公式rotateAngele
 *    用户视角调整cameraDistance，涉及每个角度星球大小和每个星球所在角度的计算
 * 2. 星球单个切换过渡动画逻辑
 * 3. 星球滑动(随手仅x轴)过渡动画逻辑
 * 4. 点击星球多个切换过渡动画逻辑
 * 5. 抽取每个星球静置状态下固定角度集
 * 6. 单独处理选中(最前方)星球单独缩放规则(选中星球最大)
 * 7. 点击切换和触摸抬起切换 事件冲突问题处理
 * 8. 星球字体渐变色处理？
 * 9. 选中星球文本隐藏
 * 10. 横竖滑动事件冲突
 * 11. 初始化根据选中星球调整数据源的真实顺序和显示角度
 * 12. 星球Tab整体宽高计算，星球放大缩小后 真实或近似(至少显示完整)的高度值计算
 * 13. afx接入，afx选中状态的位置计算
 * 14. 选中星球默认图和afx静置图动画切换过程中2倍速渐变(即：动画执行一半时默认图和文本透明度变为0)
 * 15. 只处理选中星球特殊缩放以及切换动画马上只有一个角度转换时afx动画静置图和默认图片才显示渐变效果
 *
 * 注：子View 尽量不要使用View.GONE等导致父View重新测量和布局的属性，由于动画是在layout布局中实现，会导致动画reset。
 *
 * ios 方案：
 * CATransform3D：https://www.kancloud.cn/manual/ios/97786
 * CATransformLayer：https://www.kancloud.cn/manual/ios/97792
 * 缺点：点击事件有问题(需动态计算)
 */
class PlanetTabView : FrameLayout {
    private var centerX = 0
    private var centerY = 0
    private var viewWidth = 0
    private var viewHeight = 0

    private var currentSelectedView: View? = null

    /** 绘制轨道 */
    private val paint = Paint()

    /** 开始绘制的角度 */
    private val startAngle = 270f

    /** 开始位置放大比例 */
    private val startScala = 2.5f

    /** 平面正圆绕x轴旋转的角度，控制远近效果 */
    private val rotateAngela = 60f

    /** 角度缩放，可设置非选中星球的间距 */
    private val angleScalaRatio = 1f

    /** 相机(用户视角)距离 */
    private val cameraDistance = dip(100)

    /** 椭圆X轴半径 */
    private var ovalXRadius = 0f

    /** 椭圆Y轴半径 */
    private var ovalYRadius = 0f

    /** padding值 防止缩放超过边界 */
    private val padding = dip(40)

    /** 当前选中位置(以数据源中index为主) */
    private var currentSelectedIndex = 0

    /** 是否允许滑动 */
    private var swipeEnable = false

    /** 本地切换动画切换次数 */
    private var currentToggleNum = 1

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

    /** 数据源 */
    private var dataArray: List<PlanetItemData>? = null

    /** 切换Tab 回调 */
    var switchTabCallback: ((PlanetItemData, direction: Int) -> Unit)? = null

    /** 滑动后选中星球的AFX特效动画 */
    private val alphaVideo by lazy {
        View(context)
    }
    /** item中仅星球的大小和顶部间距 */
    private val planetTopSpace = resources.getDimension(R.dimen.planet_top_space)
    private val planetOriginalWidth = resources.getDimension(R.dimen.planet_width)
    private val planetOriginalHeight = resources.getDimension(R.dimen.planet_height)

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
        viewWidth = context.getScreenWidth()

        ovalXRadius = (viewWidth - padding * 2).toFloat() / 2
        swipeToggleDistance = ovalXRadius
        // 椭圆公式rotateAngele
        ovalYRadius = ovalXRadius * 110 / 350 // x : y = 350 : 110
//        ovalYRadius = (ovalXRadius * cos(rotateAngela * PI / 180)).toFloat()
        viewHeight = (ovalYRadius * 2 + padding * 2).toInt()

        centerX = viewWidth / 2
        centerY = viewHeight / 2
    }

    fun initPlanetListData(data: List<PlanetItemData>) {
        resetAll()
        dataArray = data
        if (!dataArray.isNullOrEmpty()) {
            currentSelectedIndex = 0
            val size = dataArray!!.size
            val middle = size / 2f
            for (i in 0 until size) {
                val item = dataArray!![i]
                if (item.isSelected) {
                    currentSelectedIndex = i
                }
                val child = PlanetItemView(context).apply {
                    setPlanetBean(item)
                    clickListener = {
                        val isSelected = it?.isSelected ?: false
                        val currentIndex = it?.currentIndex ?: 0
                        if (!isSelected) {
                            val count = if (currentIndex > middle) {
                                // 左半边星球
                                isSwipeRight = true
                                size - currentIndex
                            } else {
                                // 右半边星球
                                isSwipeRight = false
                                currentIndex
                            }
                            // 点击星球多个切换过渡动画逻辑
                            executeToggleTransitionAnimation(count)
                        }
//                        it?.originalIndex?.let { selectedIndex -> currentSelectedIndex = selectedIndex }
                    }
                }
                addView(child, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }

            switchTabCallback?.invoke(dataArray!![currentSelectedIndex], ANIM_DIRECTION_NONE)
            calculateAngleArray(startAngle, size)

            addView(alphaVideo, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        requestLayout()
    }

    /** 选中星球的显示与隐藏 */
    fun setSelectedPlanetVisibility(visibility: Int) {
        val index = dataArray?.getOrNull(currentSelectedIndex)?.originalIndex ?: 0
        if (index in 0 until childCount) {
            getChildAt(index).visibility = visibility
        }
    }

    /** 选中星球的位置 */
    fun setSelectedPlanetLocation(visibility: Int): Rect {
        val index = dataArray?.getOrNull(currentSelectedIndex)?.originalIndex ?: 0
        val rect = Rect()
        if (index in 0 until childCount) {
            getChildAt(index).getGlobalVisibleRect(rect)
        }
        return rect
    }

    /** 重置view到初始 */
    private fun resetAll() {
        animSet.cancel()
        removeAllViews()
        transitionAnimList.clear()
        animDirection = ANIM_DIRECTION_NONE
    }

    /** 计算静置状态每个星球静置时的角度 */
    private fun calculateAngleArray(start: Float, size: Int) {
        // 抽取每个星球静置状态下固定角度集
        PlanetItemData.calculateAngleArray(start, size) { index, average ->
            getCurrentItemAngle(index, average)
        }
        // 计算完成角度后，对各个星球实例进行角度变量初始化
        changeItemsAngle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val startScala = calculateSelectScaleRule(startAngle)
        var viewHeight = this.viewHeight
        if (childCount > 0) {
            val startChild = getChildAt(0)
            // 通过对选中星球半径的计算，来显示全星球Tab
            viewHeight = (viewHeight + (startChild.measuredHeight * startScala / 2) - padding).toInt()
        }

        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (childCount > 0) {
            for (index in 0 until (childCount - 1)) {
                layoutItemChildren(index)
            }
            layoutSelectedAfx(getChildAt(childCount - 1))
        }
    }

    private fun layoutItemChildren(index: Int) {
        val child = getChildAt(index)
        // 度数
        val angle = dataArray?.getOrNull(index)?.currentAngle ?: startAngle
        // 弧度
        val radian = angle.toDouble() * PI / 180
        val rect = calculateItemLayoutRect(child, radian)
        child.layout(rect.left, rect.top, rect.right, rect.bottom)

        /* 计算缩放比例 */
        val angleAbs = (angle + 360) % 360
        val isRange = PlanetItemData.isSelectAngleRange(angleAbs)
        val scale = if (currentSelectedIndex == index && isRange) {
            calculateSelectScaleRule(angleAbs)
        } else {
            // 缩放比例和角度的关系：保证270度时最大，90度时最小，并且最小为0.3，最大为1
            //        val scale = (1 - sin(angle)) / 2 + 0.3
            // 物体离你的距离增加一倍，视觉上，物体缩小为原来的1/2。
            // 缩放比例 = (相机(用户视角)距离-绕x轴旋转距离) / 相机(用户视角)距离
            (((cameraDistance - ovalYRadius * sin(radian)) / cameraDistance)).toFloat()
        }
        if (child is PlanetItemView) {
            // 选中星球文本隐藏
            val yuzhi = (1 - sin(angle)) / 2 + 0.4f
            child.setNameAlpha(if (currentSelectedIndex == index && isRange) calculateSelectItemProportion(angleAbs) else 1f, yuzhi)
        }

        child.scaleX = scale
        child.scaleY = scale
    }

    /**
     * 布局选中状态的afx动画位置
     * 通过afx原始大小 和 选中状态的缩放比，以及选中状态星球layout_marginTop 值的缩放比计算最终位置
     */
    private fun layoutSelectedAfx(childAt: View?) {
        val afxRect = calculateSelectedPlanetAfx()
        val scale = calculateSelectScaleRule(startAngle)
        val afxWidth = planetOriginalWidth * scale
        val afxHeight = planetOriginalHeight * scale
        val diffWidth = afxWidth - planetOriginalWidth
        val diffHeight = afxHeight - planetOriginalHeight
        val scaleTopScale = planetTopSpace * scale / 2
        childAt?.layout(
            (afxRect.left - diffWidth / 2).toInt(),
            (afxRect.top - diffHeight / 2 + scaleTopScale).toInt(),
            (afxRect.right + diffWidth / 2).toInt(),
            (afxRect.bottom + diffHeight / 2 + scaleTopScale).toInt()
        )
    }

    /** 根据宽高和弧度计算layout的位置 */
    private fun calculateLayoutRect(width: Int, height: Int, radian: Double): Rect {
        val sin = sin(radian)
        val cos = cos(radian)
        val coordinateX = centerX + ovalXRadius * cos
        val coordinateY = centerY - ovalYRadius * sin

        val x1 = (coordinateX - width / 2).toInt()
        val y1 = (coordinateY - height / 2).toInt()
        val x2 = (coordinateX + width / 2).toInt()
        val y2 = (coordinateY + height / 2).toInt()

        return Rect(x1, y1, x2, y2)
    }

    /**
     * 计算当前itemView的layout位置
     * @param child 当前子View
     * @param radian 弧度，弧度 = 角度 * PI / 180
     */
    private fun calculateItemLayoutRect(child: View, radian: Double): Rect {
        return calculateLayoutRect(child.measuredWidth, child.measuredHeight, radian)
    }

    /** 计算选中星球afx位置 */
    private fun calculateSelectedPlanetAfx(): Rect {
        val radian = startAngle.toDouble() * PI / 180
        return calculateLayoutRect(planetOriginalWidth.toInt(), planetOriginalHeight.toInt(), radian)
    }

    /** 计算选中(最前方)星球单独缩放规则 */
    private fun calculateSelectScaleRule(angle: Float): Float {
        val firstNextAngle = PlanetItemData.firstNextAngle
        val diff = angle - startAngle
        val firstRadian = firstNextAngle.toDouble() * PI / 180
        val firstNextRadian = firstNextAngle.toDouble() * PI / 180
        val firstScala =
                (((cameraDistance - ovalYRadius * sin(firstRadian)) / cameraDistance) * startScala).toFloat()
        val firstNextScala =
                (((cameraDistance - ovalYRadius * sin(firstNextRadian)) / cameraDistance)).toFloat()
        return (1 - abs(diff) / (abs(firstNextAngle - startAngle))) * (firstScala - firstNextScala) + firstNextScala
    }

    /** 计算选中(最前方)星球当前角度比例 */
    private fun calculateSelectItemProportion(angle: Float): Float {
        val diff = angle - startAngle
        val firstNextAngle = PlanetItemData.firstNextAngle
        return abs(diff) / (abs(firstNextAngle - startAngle))
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
    private var downY = 0f

    /** 随手滑动动画进度 */
    private var swipeProgress = 0f
    private var lastSwipeProgress = 0f

    /** 系统所认为的最小滑动距离TouchSlop */
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x ?: 0f
        val y = event?.y ?: 0f
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                resetSwipeProgress()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = downX - x
                val dy = downY - y
                if (swipeEnable) {
                    // 执行滑动动画，星球滑动(随手仅x轴)过渡动画逻辑
                    swipeDirectionAndProgress(dx)
                    executeSwipeTransitionAnimation()
                }
                if (abs(dx) > abs(dy) && abs(dx) > touchSlop) {
                    // 解决横竖滑动事件冲突
                    requestDisallowInterceptTouchEvent(true)
                    // 产生滑动，则拦截事件，防止触发子item的点击事件
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                val dx = downX - x
                if (abs(dx) > touchSlop || lastSwipeProgress != 0f) {
                    isSwipeRight = dx <= 0
                    // 执行切换动画，星球单个切换过渡动画逻辑
                    executeToggleTransitionAnimation(1)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // 保证空白(非星球)区域可滑动切换星球
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /** 获取当前星球index */
    private fun changeItemIndex() {
        if (animDirection == ANIM_DIRECTION_NONE) return
        dataArray?.forEach {
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
        dataArray?.forEach {
            it.currentAngle = it.currentAngle()
        }
    }

    /** 改变所有星球的选中态 */
    private fun changeItemSelect() {
        var selectedItem: PlanetItemData? = null
        dataArray?.forEachIndexed { index, model ->
            val isSelected = model.currentAngle == startAngle
            if (isSelected) {
                selectedItem = model
            }
            model.isSelected = isSelected
//            getChildAt(model.originalIndex).isSelected = isSelected
        }
        startVibrate(context as Activity)
        selectedItem?.let {
            switchTabCallback?.invoke(it, animDirection)
        }

//        val index = dataArray?.getOrNull(currentSelectedIndex)?.originalIndex ?: 0
//        currentSelectedView = getChildAt(index)
//        currentSelectedView?.visibility = INVISIBLE
//        alphaVideo.visibility = View.VISIBLE
//        alphaVideo.stop()
//        alphaVideo.setSourceAssets("afx.mp4")
//        alphaVideo.setDarkFilter(0f)
//        alphaVideo.setLooping(true)
//        alphaVideo.play()
    }

    /** 获取当前星球角度 */
    private fun getCurrentItemAngle(index: Int, average: Float): Float {
        val angle = (startAngle + average * index) % 360
        // 角度缩放比。用户视角调整cameraDistance，涉及每个角度星球大小和每个星球所在角度的计算
        val scale = ovalYRadius * cos(angle * PI / 180) / cameraDistance
        val scaleAngle = average * scale.toFloat() * angleScalaRatio
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
//        alphaVideo.visibility = INVISIBLE
//        currentSelectedView?.visibility = VISIBLE
        val fromProgress = lastSwipeProgress
        val toProgress = swipeProgress
        lastSwipeProgress = toProgress
        dataArray?.forEach { item ->
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
//        alphaVideo.visibility = INVISIBLE
//        currentSelectedView?.visibility = VISIBLE
        currentToggleNum = count
        initTransitionAnimList()
        animDirection = if (isSwipeRight) ANIM_DIRECTION_RIGHT else ANIM_DIRECTION_LEFT
        currentSelectedIndex = if (isSwipeRight)  {
            (currentSelectedIndex - count + dataArray!!.size) % dataArray!!.size
        } else {
            (currentSelectedIndex + count + dataArray!!.size) % dataArray!!.size
        }
        var build: AnimatorSet.Builder? = null
        dataArray?.forEachIndexed { index, item ->
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
        animSet.duration = 600L
        animSet.start()
    }

    private fun initTransitionAnimList() {
        if (transitionAnimList.isEmpty() && !dataArray.isNullOrEmpty()) {
            for (i in 0 until dataArray!!.size) {
                transitionAnimList.add(createPlanetTransitionAnim(i))
            }
        }
    }

    private fun createPlanetTransitionAnim(index: Int): ValueAnimator {
        return ValueAnimator().apply {
            this.addUpdateListener {
                val value = it.animatedValue as Float
                dataArray?.getOrNull(index)?.currentAngle = value
                layoutItemChildren(index)
            }
        }
    }
}