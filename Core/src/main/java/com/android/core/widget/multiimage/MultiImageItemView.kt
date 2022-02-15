package com.android.core.widget.multiimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.android.core.AppRuntime
import com.android.core.R
import com.android.core.utils.createRectDrawable
import com.android.core.utils.dip
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.fresco.animation.drawable.AnimatedDrawable2
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder

/**
 * 多图片item控件
 */
class MultiImageItemView : FrameLayout {

    /** gif状态——未下载  */
    private val GIF_STATUS_NO_DOWNLOAD = 0

    /** gif状态——下载中  */
    private val GIF_STATUS_DOWNLOADING = 1

    /** gif状态——已下载到磁盘  */
    private val GIF_STATUS_IN_DISK = 2

    /** gif状态——在缓存中  */
    private val GIF_STATUS_IN_CACHE = 3

    private val tagDefaultWidth = ViewGroup.LayoutParams.WRAP_CONTENT
    private val tagDefaultHeight = ViewGroup.LayoutParams.WRAP_CONTENT

    private var url: String? = null
    private var tag: String? = null
    private var type: String? = null
    private var originUrl: String? = null

    private var textTagView: TextView? = null
    private var imageView: SimpleDraweeView? = null

    /** gif图  */
    private var mGifImage: SimpleDraweeView? = null

    /** 当前gif图的状态  */
    private var mGifStatus: Int = GIF_STATUS_NO_DOWNLOAD

    /** gif图下载完毕后是否需要自动播放  */
    private var mNeedPlayAfterDownload = false

    /** gif图对应的drawable  */
    private var mGifDrawable: AnimatedDrawable2? = null

    /** gif 播放的handler  */
    private var mGifPlayHandler: Handler? = null

    private var mGifControllerListener: GifControllerListener? = null

    constructor(context: Context) : super(context) {
        setupViews()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupViews()
    }

    private fun setupViews() {
        imageView = SimpleDraweeView(context).apply {
            hierarchy = GenericDraweeHierarchyBuilder(resources)
                .setPlaceholderImage(PlaceHolderDrawable.rectangleLogo(context))
                .setPlaceholderImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setProgressBarImage(R.drawable.loading_flower)
                .build()
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        addView(imageView)

        textTagView = TextView(context).apply {
            gravity = Gravity.CENTER
            layoutParams = createTagBottomRightLayoutParams()
            val horizontal = context.dip(4)
            val vertical = context.dip(2)
            setPadding(horizontal, vertical, horizontal, vertical)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.dip(10).toFloat())
            setTextColor(context.getColor(R.color.color_fff))
            background =
                createRectDrawable(Color.parseColor("#66000000"), context.dip(8).toFloat())
        }
        addView(textTagView)
        setTagViewVisibility(false)

        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * @param url 静态图
     * @param originUrl 如果类型是gif的话。url 是静态链接，originUrl 是gif链接地址
     * @param type 图片类型 STYLE_MOTIVE：gif  STYLE_STATIC：普通
     */
    fun showImageWithUrl(url: String?, originUrl: String? = null, type: String = STYLE_STATIC) {
        url ?: return
        this.url = url
        this.type = type
        this.originUrl = originUrl

        if (type == STYLE_MOTIVE) {
            createGitImageView()
        }
        setGifViewVisibility(false)
        imageView?.setImageURI(url, null)
    }

    fun showTextTagView(text: String?) {
        this.tag = text
        if (!TextUtils.isEmpty(text)) {
            textTagView?.text = text
            setTagViewVisibility(true)
        } else {
            setTagViewVisibility(false)
        }
    }

    private fun setTagViewVisibility(isVisibility: Boolean) {
        if (TextUtils.isEmpty(tag)) {
            textTagView?.visibility = GONE
        } else {
            textTagView?.visibility = if (isVisibility) VISIBLE else GONE
        }
    }

    /**
     * 创建Tag右下角显示
     */
    private fun createTagBottomRightLayoutParams(): LayoutParams {
        val layoutParam = LayoutParams(tagDefaultWidth, tagDefaultHeight)
        layoutParam.bottomMargin = context.dip(5)
        layoutParam.marginEnd = context.dip(5)
        layoutParam.gravity = Gravity.END or Gravity.BOTTOM
        return layoutParam
    }

    /** --------------------------------- Gif 相关 start ------------------------------------- */

    private fun createGitImageView() {
        if (mGifImage == null) {
            mGifImage = SimpleDraweeView(context).apply {
                hierarchy = GenericDraweeHierarchyBuilder(resources)
                    .setProgressBarImage(R.drawable.loading_flower)
                    .build()
                layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            addView(mGifImage)
        }
    }

    /**
     * 设置gif播放的handler
     *
     * @param gifPlayHandler gif播放的handler
     */
    fun setGifPlayHandler(gifPlayHandler: Handler?) {
        mGifPlayHandler = gifPlayHandler
    }

    fun isGifType(): Boolean {
        return TextUtils.equals(type, STYLE_MOTIVE)
    }

    /**
     * 下载gif资源
     *
     * @param needPlayAfterDownload gif加载完毕后是否需要自动播放
     */
    fun downloadGifResource(needPlayAfterDownload: Boolean, url: String?, type: String?) {
        // 如果图片不为动图，则不需要开启下载
        if (!TextUtils.equals(type, STYLE_MOTIVE) || TextUtils.isEmpty(url)) {
            return
        }
        mNeedPlayAfterDownload = needPlayAfterDownload
        // 如果正在下载, 不需要重新触发下载任务
        if (mGifStatus != GIF_STATUS_NO_DOWNLOAD) {
            return
        }
        val imagePipeline = Fresco.getImagePipeline()
        val uri = Uri.parse(url)
        val request = ImageRequestBuilder
            .newBuilderWithSource(uri)
            .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
            .build()
        // 发起网络请求
        mGifStatus = GIF_STATUS_DOWNLOADING
        val dataSource = imagePipeline.fetchDecodedImage(
            request, AppRuntime.getAppContext()
        )
        dataSource.subscribe(object : BaseBitmapDataSubscriber() {
            override fun onNewResultImpl(bitmap: Bitmap?) {
                // 当前已下载到磁盘中
                mGifStatus = GIF_STATUS_IN_DISK
                // 下载完毕后需要进行gif播放
                if (mNeedPlayAfterDownload && mGifPlayHandler != null) {
                    mGifPlayHandler?.sendEmptyMessage(MSG_GIF_START_AFTER_DOWNLOAD)
                    mNeedPlayAfterDownload = false
                }
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage?>>) {
                mGifStatus = GIF_STATUS_NO_DOWNLOAD
            }
        }, CallerThreadExecutor.getInstance())
    }

    /**
     * gif资源是否准备完毕
     */
    fun isGifResourceReady(): Boolean {
        // 当前gif资源已加载至内存中
        if (mGifDrawable != null) {
            return true
        }
        // 当前gif资源已下载到本地
        return mGifStatus == GIF_STATUS_IN_DISK
        // 其余情况都被认为资源还未ready
    }

    /**
     * gif资源是否正在播放
     *
     * @return gif资源是否正在播放
     */
    fun isGifRunning(): Boolean {
        return mGifDrawable != null && mGifDrawable?.isRunning == true
    }

    /**
     * 开始播放gif图：显示gif图
     */
    fun startPlayGif(type: String?) {
        // 如果不是gif图，不需要关心
        if (!TextUtils.equals(type, STYLE_MOTIVE)) {
            return
        }
        // 先设置可见性，否则部分机型的GifController方法不会被触发
        mGifImage?.visibility = VISIBLE
        if (mGifDrawable != null) {
            // 如果正在播放，则不需要其他操作
            if (mGifDrawable?.isRunning == true) {
                return
            }
            setGifViewVisibility(true)
            // 直接用缓存中的drawable
            mGifImage?.setImageDrawable(mGifDrawable)
            mGifDrawable?.start()
            if (mGifPlayHandler != null) {
                mGifDrawable?.loopDurationMs?.let {
                    mGifPlayHandler?.sendEmptyMessageDelayed(MSG_GIF_POST_PLAY, it)
                }
            }
        } else {
            if (originUrl == null) {
                return
            }
            val draweeController = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(false)
                .setOldController(mGifImage?.controller)
                .setUri(Uri.parse(originUrl))
                .setControllerListener(getGifControllerListener())
                .build()
            mGifImage?.controller = draweeController
        }
    }

    /**
     * 停止播放gif图：显示静态图
     */
    fun stopPlayGif() {
        // 如果不是gif图，不需要关心
        if (!TextUtils.equals(type, STYLE_MOTIVE)) {
            return
        }
        setGifViewVisibility(false)
        mGifDrawable?.stop()
    }

    private fun setGifViewVisibility(isVisibility: Boolean) {
        if (isVisibility) {
            mGifImage?.visibility = VISIBLE
            imageView?.visibility = GONE
        } else {
            mGifImage?.visibility = GONE
            imageView?.visibility = VISIBLE
        }
        setTagViewVisibility(!isVisibility)
    }

    private fun getGifControllerListener(): GifControllerListener  {
        if (mGifControllerListener == null) {
            mGifControllerListener = GifControllerListener()
        }
        return mGifControllerListener!!
    }

    /**
     * gif图加载controller
     */
    private inner class GifControllerListener : BaseControllerListener<ImageInfo?>() {
        /** 加载后是否需要开始播放  */
        private var mShouldPlayAfterLoad = true
        override fun onFinalImageSet(id: String, imageInfo: ImageInfo?, animatable: Animatable?) {
            super.onFinalImageSet(id, imageInfo, animatable)
            // 保存当前的drawable
            if (animatable is AnimatedDrawable2) {
                mGifStatus = GIF_STATUS_IN_CACHE
                mGifDrawable = animatable

                // 落地页返回也会触发自动播放，会导致播放异常：仅第一次加载后进行播放
                if (mShouldPlayAfterLoad) {
                    createGitImageView()
                    setGifViewVisibility(true)
                    mGifDrawable?.start()
                    if (mGifPlayHandler != null) {
                        mGifDrawable?.loopDurationMs?.let {
                            mGifPlayHandler?.sendEmptyMessageDelayed(MSG_GIF_POST_PLAY, it)
                        }
                    }
                    mShouldPlayAfterLoad = false
                }
            }
        }
    }

/** --------------------------------- Gif 相关 end --------------------------------------- */
}