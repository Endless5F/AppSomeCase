package com.android.core.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt

/**
 * 图片或者文本 按钮，非此即彼
 * @author jiaochengyun
 * @version
 * @since 2021/10/18
 */
class ImageTextButton(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var textButton: TextView? = null
    private var imageButton: ImageView? = null

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun addImageButton(
        img: Drawable,
        viewId: Int,
        width: Int = LayoutParams.WRAP_CONTENT,
        height: Int = LayoutParams.WRAP_CONTENT
    ) {
        textButton?.visibility = GONE
        imageButton = ImageView(context).apply {
            id = viewId
            setImageDrawable(img)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LayoutParams(width, height)
        }
        addView(imageButton)
    }

    fun addTextButton(
        content: String,
        @ColorInt color: Int,
        contentSize: Float,
        viewId: Int,
        width: Int = LayoutParams.WRAP_CONTENT,
        height: Int = LayoutParams.WRAP_CONTENT
    ) {
        imageButton?.visibility = GONE
        textButton = TextView(context).apply {
            id = viewId
            text = content
            setTextColor(color)
            isSingleLine = true
            gravity = Gravity.CENTER
            ellipsize = TextUtils.TruncateAt.END
            setTextSize(TypedValue.COMPLEX_UNIT_PX, contentSize)
            layoutParams = LayoutParams(width, height)
        }
        addView(imageButton)
    }
}