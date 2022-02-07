package com.android.core.widget.multiimage

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.android.core.util.createRectDrawable
import com.android.core.util.dip
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder

/**
 * 多图片item控件
 */
class MultiImageItemView : FrameLayout {

    var needResize = false

    private val defaultSize = context.dip(108)
    private val tagDefaultWidth = context.dip(35)
    private val tagDefaultHeight = context.dip(18)

    private var textTagView: TextView? = null
    private var imageView: SimpleDraweeView? = null

    constructor(context: Context) : super(context) {
        setupViews()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupViews()
    }

    private fun setupViews() {
        imageView = SimpleDraweeView(context).apply {
            hierarchy = GenericDraweeHierarchyBuilder(resources)
//                .setRoundingParams(
//                    RoundingParams.asCircle()
//                        .setBorder(Color.parseColor("#FFFFFF"), dip(1).toFloat())
//                )
                .setPlaceholderImage(PlaceHolderDrawable.rectangleLogo(context))
                .setPlaceholderImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .build()
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        addView(imageView)
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun showImageWithUrl(url: String?) {
        url ?: return
        if (needResize) {
            // 设置前确保为正方形，或者适配比例
            var targetWidth = width / 2
            var targetHeight = height / 2
            if (targetWidth <= 0 || targetWidth > defaultSize) {
                targetWidth = defaultSize
            }
            if (targetHeight <= 0 || targetHeight > defaultSize) {
                targetHeight = defaultSize
            }
            val uri = Uri.parse(url)
            val request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(ResizeOptions(targetWidth, targetHeight)) // 解决加载多图导致的卡顿问题
                .build()
            val controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imageView?.controller)
                .setImageRequest(request)
                .build()
            imageView?.controller = controller
        } else {
            imageView?.setImageURI(url, null)
        }
    }

    fun showTextTagView(text: String?) {
        text?.let {
            createTextTagView()
            textTagView?.text = it
            setTagViewVisibility(true)
        } ?: setTagViewVisibility(false)
    }

    private fun setTagViewVisibility(isVisibility: Boolean) {
        textTagView?.visibility = if (isVisibility) VISIBLE else GONE
    }

    private fun createTextTagView() {
        if (textTagView == null) {
            textTagView = TextView(context).apply {
                gravity = Gravity.CENTER
                layoutParams = createTagBottomRightLayoutParams()
                background =
                    createRectDrawable(Color.parseColor("#66000000"), context.dip(8).toFloat())
            }
            addView(textTagView)
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
}