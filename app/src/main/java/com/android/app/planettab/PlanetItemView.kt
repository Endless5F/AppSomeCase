package com.android.app.planettab

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.android.app.R
import com.android.core.utils.dip
import kotlin.math.abs

/**
 * @author jiaochengyun@baidu.com
 * @since 3.0.0
 */
class PlanetItemView : FrameLayout {
    private val minScale = 1f
    private val maxScale = 6f
    private val finalScale = 5f

    private var nameView: TextView? = null
    private var iconView: ImageView? = null
    private var popupPointView: ImageView? = null
    private var popupIconView: ImageView? = null
    private var iconSelectView: ImageView? = null
    private var planetBean: PlanetItemData? = null

    var clickListener: ((PlanetItemData?) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        clipChildren = false
        LayoutInflater.from(context).inflate(R.layout.layout_planet_view, this)
        nameView = findViewById(R.id.planet_name)
        iconView = findViewById(R.id.planet_icon)
        popupPointView = findViewById(R.id.planet_pop_point)
        popupIconView = findViewById(R.id.planet_pop_icon)
        iconSelectView = findViewById(R.id.planet_icon_select)

        popupIconView?.let {
            it.z = 1f
            it.alpha = 0f
            it.rotation = 15f
        }
//        playPopAnim()
        setPlanetBean(PlanetItemData())
    }

    fun setPlanetBean(planetBean: PlanetItemData) {
        this.planetBean = planetBean
        planetBean.let {
            nameView?.text = it.name
            iconView?.setImageResource(it.picture)
            iconSelectView?.alpha = 0f
            iconSelectView?.setImageResource(it.pictureSelect)
        }
    }

    fun getSelectedIcon(): View? {
        return iconSelectView
    }

    fun setItemViewAlpha(alpha: Float) {
        // 2倍速渐变：1f - (1f - alpha) * 2，(1f - alpha) * 2 属于0..1f
        val diff2x = (1f - alpha) * 2
        val alpha2x = 1f - if (diff2x < 0) 0f else if (diff2x > 1f) 1f else diff2x

        nameView?.alpha = alpha2x
        iconView?.alpha = alpha2x
        popupPointView?.alpha = alpha2x
        iconSelectView?.alpha = 1f - alpha
    }

    private var initPivotX = 0f
    private var initPivotY = 0f

    fun playPopAnim() {
        popupIconView?.let {
            if (initPivotX == 0f) {
                initPivotX = it.pivotX
                initPivotY = it.pivotY
            }

            if (it.height == 0) {
                it.pivotX = initPivotX
                it.pivotY = initPivotY
                it.translationX = dip(-5).toFloat()
                it.translationY = dip(15).toFloat()
            } else {
                it.pivotX = 0f
                it.pivotY = it.height.toFloat()
                it.translationX = dip(-5) - it.width * finalScale / 2f
                it.translationY = dip(5) + it.height * finalScale / 2f
            }
            val scaleX1 = ObjectAnimator.ofFloat(it, "scaleX", minScale, finalScale).setDuration(200L)
            val scaleY1 = ObjectAnimator.ofFloat(it, "scaleY", minScale, finalScale).setDuration(200L)
            val scaleX2 = ObjectAnimator.ofFloat(it, "scaleX", finalScale, maxScale).setDuration(150L)
            val scaleY2 = ObjectAnimator.ofFloat(it, "scaleY", finalScale, maxScale).setDuration(150L)
            val scaleX3 = ObjectAnimator.ofFloat(it, "scaleX", maxScale, finalScale).setDuration(150L)
            val scaleY3 = ObjectAnimator.ofFloat(it, "scaleY", maxScale, finalScale).setDuration(150L)
            it.pivotX = 0f
            it.pivotY = it.measuredHeight.toFloat()
            val rotation1 = ObjectAnimator.ofFloat(it, "rotation", 15f, 5f).apply {
                startDelay = 400L
                duration = 150L
            }
            val rotation2 = ObjectAnimator.ofFloat(it, "rotation", 5f, 20f).apply {
                startDelay = 550L
                duration = 150L
            }
            val rotation3 = ObjectAnimator.ofFloat(it, "rotation", 20f, 5f).apply {
                startDelay = 700L
                duration = 150L
            }
            val rotation4 = ObjectAnimator.ofFloat(it, "rotation", 5f, 15f).apply {
                startDelay = 850L
                duration = 150L
            }
            val scaleX4 = ObjectAnimator.ofFloat(it, "scaleX", 5f, 1f).apply {
                startDelay = 1000L
                duration = 300L
            }
            val scaleY4 = ObjectAnimator.ofFloat(it, "scaleY", 5f, 1f).apply {
                startDelay = 1000L
                duration = 300L
            }
            val translationX = ObjectAnimator.ofFloat(it, "translationX", 0f).apply {
                startDelay = 1000L
                duration = 300L
            }
            val translationY = ObjectAnimator.ofFloat(it, "translationY", 0f).apply {
                startDelay = 1000L
                duration = 300L
            }

            val animationSet = AnimatorSet().apply {
                playSequentially(scaleX1, scaleX2, scaleX3)
                playSequentially(scaleY1, scaleY2, scaleY3)
                playTogether(rotation1, rotation2, rotation3, rotation4)
                playTogether(scaleX4, scaleY4, translationX, translationY)
            }
            animationSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    popupIconView?.alpha = 1f
                    popupPointView?.alpha = 0f
                }

                override fun onAnimationEnd(animation: Animator?) {
                    popupIconView?.alpha = 0f
                    popupPointView?.alpha = 1f
                }

                override fun onAnimationCancel(animation: Animator?) {
                    popupIconView?.alpha = 0f
                    popupPointView?.alpha = 1f
                }

                override fun onAnimationRepeat(animation: Animator?) {

                }

            })
            animationSet.start()
        }
    }

    fun stopPopAnim() {
        popupIconView?.alpha = 0f
        popupPointView?.alpha = 1f
    }

    fun hidePopup() {
        popupIconView?.alpha = 0f
        popupPointView?.alpha = 0f
    }

    /** 手势处理 */
    private var downX = 0f
    private var downY = 0f

    /** 系统所认为的最小滑动距离TouchSlop */
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x ?: 0f
        val y = event?.y ?: 0f
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = downX - x
                val dy = downY - y
                if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                    return super.onTouchEvent(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                val dx = downX - x
                val dy = downY - y
                if (abs(dx) < touchSlop && abs(dy) < touchSlop) {
                    clickListener?.invoke(planetBean)
                }
            }
        }
        return true
    }

    override fun toString(): String {
        return planetBean?.name ?: ""
    }
}
