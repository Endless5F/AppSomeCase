package com.android.app.planettab

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.android.app.R
import kotlin.math.abs


class PlanetAfxView : FrameLayout {
    private var playCount = 0
    private var playPath = "afx.mp4"

    @DrawableRes
    private var playDefaultIcon = R.drawable.icon_planet_selected

    /** 点击已选中的星球Tab 回调 */
    var clickCallback: (() -> Unit)? = null

    /** 滑动后选中星球的AFX特效动画 */
//    private val alphaVideo by lazy {
//        AlphaVideo(context).apply {
//            setDarkFilter(0f)
//            setLooping(false)
//            setKeepLastFrame(true)
//
//            // 设置开始播放监听器
//            setOnVideoStartedListener {
//                imageView.visibility = INVISIBLE
//            }
//            // 设置播放结束监听器
//            setOnVideoEndedListener {
//                if (playCount == 0) {
//                    playCount++
//                    // 再播放一遍
//                    play()
//                }
//            }
//            // 设置播放错误监听器
//            setOnVideoErrorListener {
//                afxPlayError()
//                true
//            }
//        }
//    }

    /** 滑动后选中星球的AFX特效动画第一帧静置图 */
    private val imageView by lazy {
        ImageView(context)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        addView(
            imageView,
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
//        addView(
//            alphaVideo,
//            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        )
    }

    fun setAfxPath(path: String, @DrawableRes defaultIcon: Int) {
        this.playPath = path
        this.playDefaultIcon = defaultIcon

        visibility = VISIBLE
        imageView.visibility = VISIBLE
//        alphaVideo.visibility = VISIBLE
        imageView.setImageResource(defaultIcon)
//        try {
//            alphaVideo.stop()
//            playCount = 0
//            alphaVideo.setSourceAssets(path)
//            alphaVideo.play()
//        } catch (e: Exception) {
//            afxPlayError()
//        }
    }

    fun afxStop() {
        visibility = INVISIBLE
//        alphaVideo.stop()
    }

    private fun afxPlayError() {
        imageView.visibility = VISIBLE
//        alphaVideo.visibility = INVISIBLE
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        imageView.layout(0, 0, right - left, bottom - top)
//        alphaVideo.layout(0, 0, right - left, bottom - top)
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
                    clickCallback?.invoke()
                }
            }
        }
        return true
    }
}