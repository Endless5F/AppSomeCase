package com.android.core.search

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import com.android.core.R

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/10/15
 */
class HiSearchView constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int) :
    RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        val LEFT = 1
        val CENTER = 0
    }

    private var editText: EditText? = null

    // 搜索小图标 和 迷人提示语，以及 container
    private var searchIcon: ImageView? = null
    private var hintTv: TextView? = null
    private var searchIconHintContainer: LinearLayout? = null

    // 右侧清除小图标
    private var clearIcon: ImageView? = null

    private val viewAttrs = AttrParse.parseSearchViewAttrs(context, attrs, defStyleAttr)

    init {
        // 初始化editText
        initEditText()
        // 初始化右键侧一键清除按钮
        initClearIcon()
        // 初始化 默认的提示语 和 searchIcon
        initSearchIconHintContainer()

        background = viewAttrs.searchBackground
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val hasContent = s?.length ?: 0 > 0
                clearIcon?.visibility = if (hasContent) VISIBLE else GONE
                searchIconHintContainer?.visibility = if (hasContent) GONE else VISIBLE
            }
        })
    }

    private fun initEditText() {
        editText = EditText(context).apply {
            setTextColor(viewAttrs.searchTextColor)
            setBackgroundColor(Color.TRANSPARENT)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, viewAttrs.searchTextSize)
            setPadding(viewAttrs.iconPadding, 0, viewAttrs.iconPadding, 0)
            id = R.id.id_search_edit_view

            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                    addRule(CENTER_VERTICAL)
                }
        }
        addView(editText)
    }

    private fun initClearIcon() {
        if (viewAttrs.clearIcon != null) {
            clearIcon = ImageView(context).apply {
                setImageDrawable(viewAttrs.clearIcon)
                val padding = viewAttrs.iconPadding
                setPadding(padding, padding, padding, padding)
                id = R.id.id_search_clear_icon

                layoutParams =
                    LayoutParams(viewAttrs.clearIconSize, viewAttrs.clearIconSize).apply {
                        addRule(CENTER_VERTICAL)
                    }
                visibility = GONE
            }
        }
        addView(clearIcon)
    }

    private fun initSearchIconHintContainer() {
        hintTv = TextView(context).apply {
            text = viewAttrs.hintText
            isSingleLine = true
            setTextColor(viewAttrs.hintTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, viewAttrs.hintTextSize)
            id = R.id.id_search_hint_view
        }

        searchIcon = ImageView(context).apply {
            setImageDrawable(viewAttrs.searchIcon)
            val padding = viewAttrs.iconPadding
            setPadding(0, 0, padding / 2, 0)
            id = R.id.id_search_icon
        }

        searchIconHintContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            addView(hintTv)
            addView(searchIcon)
            layoutParams =
                LayoutParams(viewAttrs.clearIconSize, viewAttrs.clearIconSize).apply {
                    addRule(CENTER_VERTICAL)

                    when(viewAttrs.hintGravity) {
                        CENTER -> addRule(CENTER_IN_PARENT)
                        else -> addRule(ALIGN_PARENT_LEFT)
                    }
                }
        }
        addView(searchIconHintContainer)
    }
}