package com.android.core.widget.multiimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.android.core.R
import com.android.core.util.dip
import com.android.core.util.imageResource
import kotlin.math.max
import kotlin.math.min

const val REC_MAX_IMG = 9

@SuppressLint("ResourceAsColor")
class RecMultiImageLayout : CardView {

    private var imageCount = 0

    /**
     * 图片多余3个时是否显示 +xxx
     */
    var isShowImgCount = true

    var isItemClickable = false

    // 图片数量视图，图片总数超过3则展示-3
    private var imgCountView: TextView? = null

    private var playIconView: ImageView? = null

    private val imageViews = mutableListOf<MultiImageItemView>()

    var onImageItemClickCallback: ((positionInList: Int) -> Unit)? = null

    /**
     * 多图显示策略，默认为均分
     */
    private var strategy: MultiImageStrategy = MultiImageAverageStrategy()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        radius = context.dip(10).toFloat()
        elevation = 0f
        setCardBackgroundColor(R.color.color_00a)
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun setMultiImageStrategy(strategy: MultiImageStrategy) {
        this.strategy = strategy
    }

    /**
     * @param marginArray 0: left，1：right；
     */
    @SuppressLint("SetTextI18n")
    fun showImages(images: List<ImageEntity>?, marginArray: IntArray = intArrayOf(0, 0)) {
        images ?: return
        if (images.isEmpty()) {
            return
        }
        val targetCount: Int = min(images.size, REC_MAX_IMG)
        if (targetCount == 1) {
            val itemImage = images[0]
            strategy.setImageSize(itemImage.width, itemImage.height)
        }
        strategy.setMarginArray(marginArray)
        strategy.setImageCount(targetCount)
        // 1张图片样式较多，需要重新设置高宽，避免复用上一个样式
        setImageCountAndStyle(targetCount)
        for (i in 0 until targetCount) {
            val v = imageViews[i]
            v.showImageWithUrl(images.getOrNull(i)?.imageUrl)
            v.showTextTagView("第$i")
        }
        var showCount = false
        // 设置最后一张 显示 +N 的剩余张数标识
        if (images.size > REC_MAX_IMG && isShowImgCount) {
            showCount = true
            strategy.imageRectList.last().run {
                if (imgCountView == null) {
                    val lp = LayoutParams(this.width(), this.height())
                    imgCountView = TextView(context)
                    imgCountView?.gravity = Gravity.CENTER
                    imgCountView?.setTextColor(Color.parseColor("#FFFFFF"))
                    imgCountView?.textSize = context.dip(10).toFloat()
                    imgCountView?.setBackgroundColor(Color.parseColor("#4D000000"))
                    this@RecMultiImageLayout.addView(imgCountView, lp)
                }
                var iconLayoutParam = imgCountView?.layoutParams as? LayoutParams
                if (iconLayoutParam == null) {
                    iconLayoutParam = LayoutParams(this.width(), this.height())
                }
                iconLayoutParam.topMargin = this.top
                iconLayoutParam.leftMargin = this.left
                imgCountView?.layoutParams = iconLayoutParam
                imgCountView?.text = "+ ${(images.size - REC_MAX_IMG)}"
            }
        }
        imgCountView?.visibility = if (showCount) View.VISIBLE else View.GONE
        imgCountView?.bringToFront()
        playIconView?.visibility = View.GONE
    }

    fun showVideo(videoCoverUrl: String?, marginArray: IntArray = intArrayOf(0, 0)) {
        strategy.setShowPercent(16f / 9)
        strategy.setMarginArray(marginArray)
        strategy.setImageCount(1)

        setImageCountAndStyle(1)

        val v = imageViews[0]
        v.showImageWithUrl(videoCoverUrl)

        val iconSize = context.dip(35)
        if (playIconView == null) {
            val lp = LayoutParams(iconSize, iconSize)
            playIconView = ImageView(context)
            playIconView?.scaleType = ImageView.ScaleType.CENTER
            playIconView?.imageResource = R.drawable.icon_video_pause
            this@RecMultiImageLayout.addView(playIconView, lp)
        }

        var iconLayoutParam = playIconView?.layoutParams as? LayoutParams
        if (iconLayoutParam == null) {
            iconLayoutParam = LayoutParams(iconSize, iconSize)
        }
        val rect = strategy.imageRectList[0]
        iconLayoutParam.topMargin = (rect.height() - iconSize) / 2
        iconLayoutParam.leftMargin = (rect.width() - iconSize) / 2
        playIconView?.layoutParams = iconLayoutParam
        playIconView?.visibility = View.VISIBLE
        imgCountView?.visibility = View.GONE
    }

    private fun setImageCountAndStyle(imageCount: Int) {
        this.imageCount = when {
            imageCount < 0 -> 0
            imageCount > REC_MAX_IMG -> REC_MAX_IMG
            else -> imageCount
        }
        if (this.imageCount == 0) return

        val targetLoopCount = max(imageViews.size, this.imageCount)
        for (i in 0 until targetLoopCount) {
            // 当前循环已经超出了_imageCount的范围，需要从父View上移除
            if (i + 1 > this.imageCount) {
                val v = imageViews[i]
                this.removeView(v)
                continue
            }
            // ImageView池中的View不足，需要创建一个
            var currentImageView: MultiImageItemView
            if (imageViews.size < i + 1) {
                currentImageView = MultiImageItemView(context)
                if (isItemClickable) {
                    currentImageView.setOnClickListener {
                        onImageItemClickCallback?.invoke(i)
                    }
                }
                imageViews.add(currentImageView)
            } else {
                currentImageView = imageViews[i]
            }
            if (currentImageView.parent == null) {
                this.addView(currentImageView)
            }
            var layoutParams = currentImageView.layoutParams as? LayoutParams
            val rect = strategy.imageRectList[i]
            if (layoutParams == null) {
                layoutParams = LayoutParams(context, null)
            }

            layoutParams.width = rect.width()
            layoutParams.height = rect.height()
            layoutParams.marginStart = rect.left
            layoutParams.topMargin = rect.top

            Log.e("jcy", "setImageCountAndStyle: $i  ${rect.width()}")
            currentImageView.layoutParams = layoutParams
        }
    }
}