package com.android.core.widget.multiimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.android.core.R
import com.android.core.utils.dip
import com.android.core.utils.imageResource
import kotlin.math.max
import kotlin.math.min

const val REC_MAX_IMG = 9

/** gif图播放结束消息  */
const val MSG_GIF_POST_PLAY = 1

/** gif图下载结束后进行播放  */
const val MSG_GIF_START_AFTER_DOWNLOAD = 2

@SuppressLint("ResourceAsColor")
class RecMultiImageLayout : CardView {

    private var imageCount = 0

    /**
     * 图片多余3个时是否显示 +xxx
     */
    var isShowImgCount = true

    /** 默认可点击  */
    var isItemClickable = true

    /** gif图正在播放的index  */
    private var gifPlayIndex = -1

    /** gif播放相关的handler  */
    private var gifPlayHandler: Handler? = null

    /** 是否点击查看了图片查看：用于判断gifPlayIndex变量是否重置(从图片查看器跳回也会触发update)  */
    private var isPictureBrowser = false

    // 图片数量视图，图片总数超过3则展示-3
    private var imgCountView: TextView? = null

    private var playIconView: ImageView? = null

    private var images: List<ImageEntity>? = null

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
        setCardBackgroundColor(context.getColor(R.color.color_fff))

        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        initHandler()
    }

    /**
     * 初始化handler
     */
    private fun initHandler() {
        gifPlayHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    // 当前gif播放完毕后，播放下一张gif图
                    MSG_GIF_POST_PLAY -> {
                        if (gifPlayIndex < 0 || imageViews.size < gifPlayIndex) {
                            return
                        }
                        val nextGifIndex = findNextGifIndex(gifPlayIndex)
                        if (nextGifIndex < 0 || nextGifIndex == gifPlayIndex) {
                            // 没有gif图/当前仅有一张gif图
                            return
                        }
                        val baseImg = imageViews[gifPlayIndex]

                        baseImg.stopPlayGif()
                        startPlayGif(nextGifIndex)
                    }
                    // 下载完毕后开始自动播放（从第一张开始）
                    MSG_GIF_START_AFTER_DOWNLOAD -> {
                        startPlayGif(findFirstGifIndex())
                    }
                }
            }
        }
    }

    fun setMultiImageStrategy(strategy: MultiImageStrategy) {
        this.strategy = strategy
    }

    /**
     * @param marginArray 此margin 为整个Layout距离屏幕两边的间距
     */
    fun setMarginArray(marginArray: IntArray = intArrayOf(0, 0)) {
        strategy.setMarginArray(marginArray)
    }

    /**
     * @param marginArray 0: left，1：right；
     */
    @SuppressLint("SetTextI18n")
    fun showImages(images: List<ImageEntity>?) {
        images ?: return
        if (images.isEmpty()) {
            return
        }
        this.images = images
        val targetCount: Int = min(images.size, REC_MAX_IMG)
        if (targetCount == 1) {
            val itemImage = images[0]
            strategy.setShowPercent(itemImage.heightWidthRatio)
        }
        strategy.setImageCount(targetCount)
        // 1张图片样式较多，需要重新设置高宽，避免复用上一个样式
        setImageCountAndStyle(targetCount)
        for (i in 0 until targetCount) {
            val v = imageViews[i]
            images.getOrNull(i)?.let {
                v.showTextTagView(it.textTag)
                v.showImageWithUrl(it.imageUrl, it.imgOrigin, it.type)
            }
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

    fun showVideo(videoCoverUrl: String?, duration: String) {
        strategy.setShowPercent(9f / 16, true)
        strategy.setImageCount(1)

        setImageCountAndStyle(1)

        val v = imageViews.getOrNull(0)
        v?.showImageWithUrl(videoCoverUrl)
        v?.showTextTagView(duration)

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
                currentImageView.setGifPlayHandler(gifPlayHandler)
                if (isItemClickable) {
                    currentImageView.setOnClickListener {
                        stopPlay()
                        // 如果当前点击的图片为gif图, 则下次返回从当前gif图开始播放
                        if (currentImageView.isGifType()) {
                            isPictureBrowser = true
                            gifPlayIndex = i
                        } else {
                            gifPlayIndex = -1
                        }

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
                layoutParams = LayoutParams(rect.width(), rect.height())
            }

            layoutParams.width = rect.width()
            layoutParams.height = rect.height()
            layoutParams.marginStart = rect.left
            layoutParams.topMargin = rect.top

            currentImageView.layoutParams = layoutParams
        }
    }

    /**---------------------------------GIF start---------------------------------------*/

    fun onViewResume() {
        if (!isPictureBrowser) {
            gifPlayIndex = -1
        } else {
            // 如果是图片浏览器返回到广场页触发的update，播放动画要从当前位置开始
            isPictureBrowser = false
            if (gifPlayIndex < 0 || gifPlayIndex >= imageViews.size
                || !imageViews[gifPlayIndex].isGifResourceReady()
            ) {
                // 保证返回的gif图为已加载完毕的
                gifPlayIndex = findFirstGifIndex()
            }
        }
    }

    fun startPlay() {
        if (gifPlayIndex < 0) {
            gifPlayIndex = findFirstGifIndex()
            if (gifPlayIndex < 0) {
                return
            }
        }

        if (gifPlayIndex < imageViews.size && imageViews[gifPlayIndex].isGifRunning()) {
            return
        }
        startPlayGif(gifPlayIndex)
    }

    fun needStopPlay(): Boolean {
        return false
    }

    fun stopPlay() {
        if (gifPlayIndex < 0) {
            return
        }
        for (baseImg in imageViews) {
            baseImg.stopPlayGif()
        }
        gifPlayHandler?.removeMessages(MSG_GIF_POST_PLAY)
        gifPlayHandler?.removeMessages(MSG_GIF_START_AFTER_DOWNLOAD)
    }

    /**
     * 开始播放指定[index]位置的gif图
     */
    private fun startPlayGif(index: Int) {
        // 校验index合理性
        if (index < 0 || index >= imageViews.size) {
            return
        }

        val baseImg = imageViews[index]
        if (baseImg.isGifResourceReady()) {
            // 如果gif图加载完毕后，开始进行播放
            gifPlayIndex = index
            baseImg.startPlayGif(images?.getOrNull(gifPlayIndex)?.type)
        } else {
            val firstGifIndex = findFirstGifIndex()
            val url = images?.getOrNull(firstGifIndex)?.imgOrigin
            val type = images?.getOrNull(firstGifIndex)?.type
            if (index == firstGifIndex) {
                // 如果是第一张gif图，则需要等待gif下载好后触发播放任务
                baseImg.downloadGifResource(true, url, type)
            } else {
                // 资源未ready，开始进行gif图下载
                baseImg.downloadGifResource(false, url, type)
                // 同时开始播放第一张gif图
                startPlayGif(firstGifIndex)
            }
        }

        // 开启nextGif的资源下载
        val nextGifIndex = findNextGifIndex(index)
        if (nextGifIndex >= 0 && nextGifIndex != gifPlayIndex && nextGifIndex < imageViews.size) {
            val nextImage = imageViews[nextGifIndex]
            if (!nextImage.isGifResourceReady()) {
                val url = images?.getOrNull(nextGifIndex)?.imgOrigin
                val type = images?.getOrNull(nextGifIndex)?.type
                nextImage.downloadGifResource(false, url, type)
            }
        }
    }

    /**
     * 返回第一个gif图资源
     */
    private fun findFirstGifIndex(): Int {
        return findNextGifIndex(-1)
    }

    /**
     * 根据当前播放的gif[curIndex]获取下一个gif图资源
     */
    private fun findNextGifIndex(curIndex: Int): Int {
        images?.let {
            if (it.isEmpty()) {
                return -1
            }
            // 从当前正在播放的gif图后面的一张开始找
            var beginIndex = 0.coerceAtLeast(curIndex + 1)
            // 如果下一张gif图index超出展示的最大图片数量,返回第一张gif资源的index
            if (beginIndex >= REC_MAX_IMG || beginIndex >= it.size) {
                return findFirstGifIndex()
            }
            var imageIndex: Int
            for (i in it.indices) {
                // beginIndex可能会超过image列表的size
                imageIndex = beginIndex % it.size
                if (TextUtils.equals(it[imageIndex].type, STYLE_MOTIVE)) {
                    return imageIndex
                }
                beginIndex++
            }
        }
        // 没有gif资源/gif资源没有准备好
        return -1
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPlay()
    }
/**---------------------------------GIF end---------------------------------------*/
}