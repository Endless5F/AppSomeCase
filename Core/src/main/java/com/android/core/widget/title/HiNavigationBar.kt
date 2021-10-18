package com.android.core.widget.title

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.android.core.R
import com.android.core.util.dip
import com.android.core.widget.ImageTextButton

/**
 * 公共Bar
 * @author jiaochengyun
 * @version
 * @since 2021/10/18
 */
class HiNavigationBar(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var navAttrs: Attrs

    private var titleView: View? = null
    private var titleTextView: TextView? = null
    private var leftLastViewId = View.NO_ID
    private var rightLastViewId = View.NO_ID
    private val leftViewList = arrayListOf<View>()
    private val rightViewList = arrayListOf<View>()

    init {
        navAttrs = parseNavAttrs(context, attrs, defStyleAttr)

        if (!TextUtils.isEmpty(navAttrs.navTitle)) {
            setTitle(navAttrs.navTitle)
        }
    }

    fun setNavListener(listener: OnClickListener) {
        if (navAttrs.navBackIcon != null) {
            addLeftImageButton(navAttrs.navBackIcon!!, R.id.id_nav_left_back_view).setOnClickListener(listener)
        }
    }

    fun addLeftImageButton(res: Drawable, viewId: Int = View.generateViewId()): View {
        return generateLeftButton(res, viewId)
    }

    private fun generateLeftButton(res: Drawable, viewId: Int): ImageTextButton {
        val button = ImageTextButton(context).apply {
            addImageButton(res, viewId, navAttrs.btnTextSize, navAttrs.btnTextSize)
        }
        if (leftViewList.isEmpty()) {
            button.setPadding(navAttrs.horPadding * 2, 0, navAttrs.horPadding, 0)
        } else {
            button.setPadding(navAttrs.horPadding, 0, navAttrs.horPadding, 0)
        }
        addLeftView(button, generateImageTextButtonLayoutParams())
        return button
    }

    fun addLeftView(view: View, params: LayoutParams) {
        val viewId = view.id
        if (viewId == View.NO_ID) {
            throw IllegalStateException("左侧View必须有一个有效的ID")
        }
        if (leftLastViewId == View.NO_ID) {
            params.addRule(ALIGN_PARENT_LEFT, viewId)
        } else {
            params.addRule(RIGHT_OF, leftLastViewId)
        }
        leftLastViewId = viewId
        params.alignWithParent = true
        leftViewList.add(view)
        addView(view, params)
    }

    fun addRightImageButton(res: Drawable, viewId: Int = View.generateViewId()): View {
        return generateRightButton(res, viewId)
    }

    private fun generateRightButton(res: Drawable, viewId: Int): ImageTextButton {
        val button = ImageTextButton(context).apply {
            addImageButton(res, viewId, navAttrs.btnTextSize, navAttrs.btnTextSize)
        }
        if (rightViewList.isEmpty()) {
            button.setPadding(navAttrs.horPadding, 0, navAttrs.horPadding * 2, 0)
        } else {
            button.setPadding(navAttrs.horPadding, 0, navAttrs.horPadding, 0)
        }
        addRightView(button, generateImageTextButtonLayoutParams())
        return button
    }

    fun addRightView(view: View, params: LayoutParams) {
        val viewId = view.id
        if (viewId == View.NO_ID) {
            throw IllegalStateException("右侧View必须有一个有效的ID")
        }
        if (rightLastViewId == View.NO_ID) {
            params.addRule(ALIGN_PARENT_RIGHT, viewId)
        } else {
            params.addRule(LEFT_OF, rightLastViewId)
        }
        rightLastViewId = viewId
        params.alignWithParent = true
        rightViewList.add(view)
        addView(view, params)
    }

    /**
     * @param isBold 是否加粗
     */
    fun setTitle(title: String?, isBold: Boolean = true) {
        if (titleView != null) removeView(titleView)
        titleView = null
        if (titleTextView == null) {
            titleTextView = TextView(context).apply {
                gravity = Gravity.CENTER
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(navAttrs.titleTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, navAttrs.titleTextSize)
                if (isBold) typeface = Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    addRule(CENTER_IN_PARENT)
                }
            }
            addView(titleTextView)
        }
        titleTextView?.text = title
        titleTextView?.visibility = if (TextUtils.isEmpty(title)) GONE else VISIBLE
    }

    fun setTitleView(view: View) {
        if (titleTextView != null) removeView(titleTextView)
        titleTextView = null
        if (titleView == null) {
            titleView = view
            addView(titleView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT)
            })
        }
    }

    private fun generateImageTextButtonLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if ((titleView != null) || (titleTextView != null)) {
            // 计算标题 左侧已占用的空间
            var leftUseSpace = paddingLeft
            leftViewList.forEach {
                leftUseSpace += it.measuredWidth
            }
            // 计算标题 右侧已占用的空间
            var rightUseSpace = paddingRight
            rightViewList.forEach {
                rightUseSpace += it.measuredWidth
            }

            val titleContainerWidth: Int = if (titleView == null) {
                if (titleTextView == null) 0 else titleTextView!!.measuredWidth
            } else titleView!!.measuredWidth

            val remainingSpace = measuredWidth - leftUseSpace.coerceAtLeast(rightUseSpace)
            if (remainingSpace < titleContainerWidth) {
                val size = MeasureSpec.makeMeasureSpec(remainingSpace, MeasureSpec.EXACTLY)
                titleView?.measure(size, heightMeasureSpec)
                titleTextView?.measure(size, heightMeasureSpec)
            }
        }
    }

    private fun parseNavAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int): Attrs {
        val array = context.obtainStyledAttributes(attrs, R.styleable.HiNavigationBar,defStyleAttr, R.style.hiSearchViewStyle)
        val navBackIcon = array.getDrawable(R.styleable.HiNavigationBar_nav_back_icon)
        val navTitle = array.getString(R.styleable.HiNavigationBar_nav_title)
        val horPadding = array.getDimensionPixelSize(R.styleable.HiNavigationBar_hor_padding, 0)
        val btnTextSize = array.getDimensionPixelSize(R.styleable.HiNavigationBar_text_btn_size, context.dip(16))
        val titleTextSize = array.getDimensionPixelSize(R.styleable.HiNavigationBar_title_text_size, context.dip(18))
        val titleTextColor = array.getColor(R.styleable.HiNavigationBar_title_text_color, ContextCompat.getColor(context, R.color.color_000))

        array.recycle()

        return Attrs(navBackIcon, navTitle, horPadding, btnTextSize, titleTextSize.toFloat(), titleTextColor)
    }

    class Attrs(
        val navBackIcon: Drawable?,
        val navTitle: String?,
        val horPadding: Int,
        val btnTextSize: Int,
        val titleTextSize: Float,
        val titleTextColor: Int
    )
}


