package com.android.core.widget.search

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import com.android.core.R
import com.android.core.util.dip

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/10/15
 */
class HiSearchView constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
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

    private var keywordTv: TextView? = null
    private var keywordClearIcon: ImageView? = null
    private var keywordContainer: LinearLayout? = null

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
            addView(clearIcon)
        }
    }

    fun setKeyword(keyword: String?, listener: OnClickListener) {
        ensureKeywordContainer()
        toggleSearchViewVisibility(true)

        editText?.text = null
        keywordTv?.text = keyword
        keywordClearIcon?.setOnClickListener {
            toggleSearchViewVisibility(false)
            listener.onClick(it)
        }
    }

    fun setCLearIconClickListener(listener: OnClickListener) {
        clearIcon?.setOnClickListener {
            editText?.text = null
            clearIcon?.visibility = GONE
            searchIcon?.visibility = VISIBLE
            hintTv?.visibility = VISIBLE
            searchIconHintContainer?.visibility = VISIBLE
        }
    }

    fun setHintText(hintText: String) {
        hintTv?.text = hintText
    }

    private fun ensureKeywordContainer() {
        if (keywordContainer != null) return
        if (viewAttrs.keywordClearIcon != null) {
            keywordClearIcon = ImageView(context).apply {
                setImageDrawable(viewAttrs.keywordClearIcon)
                val padding = viewAttrs.iconPadding
                setPadding(padding, padding / 2, padding, padding / 2)
                id = R.id.id_search_keyword_clear_icon

                layoutParams =
                    LayoutParams(viewAttrs.keywordClearIconSize, viewAttrs.keywordClearIconSize)
            }
        }

        keywordTv = TextView(context).apply {
            isSingleLine = true
            includeFontPadding = false
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(viewAttrs.keywordColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, viewAttrs.keywordSize)
            id = R.id.id_search_keyword_text_view
            val padding = viewAttrs.iconPadding
            setPadding(padding, padding / 2, 0, padding / 2)
            filters = arrayOf(InputFilter.LengthFilter(viewAttrs.keywordMaxLen))
        }

        keywordContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = viewAttrs.keywordBackground

            addView(
                keywordTv,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            if (keywordClearIcon != null) {
                addView(keywordClearIcon)
            }

            layoutParams =
                LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(CENTER_VERTICAL)
                    addRule(ALIGN_PARENT_LEFT)
                    leftMargin = viewAttrs.iconPadding
                    rightMargin = viewAttrs.iconPadding
                }
        }
        addView(keywordContainer)
    }

    private fun toggleSearchViewVisibility(showKeyword: Boolean) {
        clearIcon?.visibility = GONE
        editText?.visibility = if (showKeyword) GONE else VISIBLE
        searchIconHintContainer?.visibility = if (showKeyword) GONE else VISIBLE
        searchIcon?.visibility = if (showKeyword) GONE else VISIBLE
        hintTv?.visibility = if (showKeyword) GONE else VISIBLE

        keywordContainer?.visibility = if (showKeyword) VISIBLE else GONE
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

            layoutParams = LayoutParams(viewAttrs.searchIconSize, viewAttrs.searchIconSize)
        }

        searchIconHintContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            addView(
                hintTv,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            addView(searchIcon)

            layoutParams =
                LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(CENTER_VERTICAL)

                    when (viewAttrs.hintGravity) {
                        CENTER -> addRule(CENTER_IN_PARENT)
                        else -> addRule(ALIGN_PARENT_LEFT)
                    }
                }
        }
        addView(searchIconHintContainer)
    }

    fun demo() {
        val searchView = HiSearchView(context).apply {
            layoutParams = ViewGroup.LayoutParams(-1, context.dip(40))
            setHintText("搜索你想要的视频？")
            postDelayed({
                this@HiSearchView.setKeyword("android") {

                }
            }, 2000)
        }
    }
}