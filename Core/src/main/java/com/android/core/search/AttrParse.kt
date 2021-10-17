package com.android.core.search

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.android.core.R
import com.android.core.util.dip

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/10/15
 */
object AttrParse {

    @SuppressLint("UseCompatLoadingForDrawables")
    fun parseSearchViewAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int): Attrs {
        // 解析apptheme是否有配置hiSearchViewStyle，否则使用默认
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.hiSearchViewStyle, value, true)
        val defStyleRes = if (value.resourceId != 0) value.resourceId else R.style.hiSearchViewStyle

        val array = context.obtainStyledAttributes(
            attrs,
            R.styleable.HiSearchView,
            defStyleAttr,
            defStyleRes
        )

        val searchBackground = array.getDrawable(R.styleable.HiSearchView_search_background)
            ?: ContextCompat.getDrawable(context, R.drawable.shape_search_view)

        val searchIcon = array.getDrawable(R.styleable.HiSearchView_search_icon)
        val searchIconSize =
            array.getDimensionPixelSize(R.styleable.HiSearchView_search_icon_size, context.dip(16))

        val iconPadding = array.getInteger(R.styleable.HiSearchView_icon_padding, context.dip(10))

        val clearIcon = array.getDrawable(R.styleable.HiSearchView_clear_icon)
        val clearIconSize =
            array.getDimensionPixelSize(R.styleable.HiSearchView_clear_icon_size, context.dip(16))

        val hintText = array.getString(R.styleable.HiSearchView_hint_text)
        val hintTextSize =
            array.getDimensionPixelSize(R.styleable.HiSearchView_hint_text_size, context.dip(16))
        val hintTextColor = array.getColor(
            R.styleable.HiSearchView_hint_text_color,
            ContextCompat.getColor(context, R.color.color_999)
        )
        val hintGravity = array.getInteger(R.styleable.HiSearchView_hint_gravity, 1)

        val searchTextSize =
            array.getDimensionPixelSize(R.styleable.HiSearchView_search_text_size, context.dip(16))
        val searchTextColor = array.getColor(
            R.styleable.HiSearchView_search_text_color,
            ContextCompat.getColor(context, R.color.color_000)
        )

        val keywordClearIconSize =
            array.getDimensionPixelSize(R.styleable.HiSearchView_key_word_clear_icon_size, context.dip(16))
        val keywordSize =
            array.getDimensionPixelSize(R.styleable.HiSearchView_key_word_size, context.dip(16))
        val keywordColor = array.getColor(
            R.styleable.HiSearchView_key_word_color,
            ContextCompat.getColor(context, R.color.color_999)
        )
        val keywordMaxLen = array.getInteger(R.styleable.HiSearchView_key_word_max_length, 10)
        val keywordBackground = array.getDrawable(R.styleable.HiSearchView_key_word_background)
        val keywordClearIcon = array.getDrawable(R.styleable.HiSearchView_key_word_clear_icon)
        val keywordPadding =
            array.getInteger(R.styleable.HiSearchView_key_word_padding, context.dip(4))

        array.recycle()

        return Attrs(
            searchBackground,
            searchIcon,
            searchIconSize,
            iconPadding,
            clearIcon,
            clearIconSize,
            hintText,
            hintTextSize.toFloat(),
            hintTextColor,
            hintGravity,
            searchTextSize.toFloat(),
            searchTextColor,
            keywordSize.toFloat(),
            keywordColor,
            keywordMaxLen,
            keywordBackground,
            keywordClearIcon,
            keywordClearIconSize,
            keywordPadding
        )
    }

    class Attrs(
        val searchBackground: Drawable?,
        val searchIcon: Drawable?,
        val searchIconSize: Int,
        val iconPadding: Int,
        val clearIcon: Drawable?,
        val clearIconSize: Int,
        val hintText: String?,
        val hintTextSize: Float,
        val hintTextColor: Int,
        val hintGravity: Int,
        val searchTextSize: Float,
        val searchTextColor: Int,
        val keywordSize: Float,
        val keywordColor: Int,
        val keywordMaxLen: Int,
        val keywordBackground: Drawable?,
        val keywordClearIcon: Drawable?,
        val keywordClearIconSize: Int,
        val keywordPadding: Int
    )
}
