package com.android.core.widget.multiimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.android.core.R
import java.lang.ref.WeakReference

/**
 * 带logo占位图
 * v1：以宽为准，该比例计算logo高如果超过视图高，则比例再计算一次确保logo高也能展示全
 */
class PlaceHolderDrawable private constructor(context: Context, private val proportion: Float) : Drawable() {

    companion object {

        private var mLogoBitmap: WeakReference<Bitmap?>? = null

        /**
         * @param proportion logo默认占视图比例
         */
        fun rectangleLogo(context: Context, proportion: Float = (1 / 3.toFloat())): Drawable {
            return PlaceHolderDrawable(context, proportion)
        }
    }

    init {
        init(context)
    }

    private var mContext: Context? = null
    private var mDestRect: RectF? = null
    private var mBgColor = 0
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun init(context: Context) {
        mContext = context
        try {
            if (mBgColor == 0) {
                mBgColor = Color.parseColor("#F5F5F5")
            }
            mLogoBitmap = ensureLogoBitmap(context)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun ensureLogoBitmap(context: Context): WeakReference<Bitmap?>? {
        if (mLogoBitmap == null || mLogoBitmap!!.get() == null || mLogoBitmap!!.get()!!.isRecycled) {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_round)
            mLogoBitmap = WeakReference(bitmap)
        }
        return mLogoBitmap
    }

    override fun draw(canvas: Canvas) {
        mPaint.style = Paint.Style.FILL
        mPaint.color = mBgColor
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        val bounds = bounds
        canvas.save()
        canvas.clipRect(bounds)
        canvas.drawColor(mBgColor)
        try {
            mContext?.let {
                val bitmap = ensureLogoBitmap(it)?.get()
                drawLogoBitmap(canvas, bounds, bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        canvas.restore()
    }

    private fun drawLogoBitmap(canvas: Canvas, bounds: Rect, logoBitmap: Bitmap?) {
        if (logoBitmap != null) {
            // 以宽为准，logo默认占比视图三分之二，该比例计算logo高如果超过视图高，则比例再计算一次确保logo高也能展示全
            val destW = bounds.width() * proportion
            val logoScaleW = destW / logoBitmap.width
            if (logoBitmap.height * logoScaleW > bounds.height()) {
                val logoScaleH = bounds.height() / (logoBitmap.height * logoScaleW)
                mDestRect = RectF(0.0f, 0.0f, logoBitmap.width * logoScaleW * logoScaleH,
                        logoBitmap.height * logoScaleW * logoScaleH)
                canvas.translate((bounds.width() - destW) / 2,
                        bounds.height() / 2 - logoBitmap.height * logoScaleW * logoScaleH / 2)
            } else {
                mDestRect = RectF(0.0f, 0.0f, logoBitmap.width * logoScaleW, logoBitmap.height * logoScaleW)
                canvas.translate((bounds.width() - destW) / 2, bounds.height() / 2 - logoBitmap.height * logoScaleW / 2)
            }
            canvas.drawBitmap(logoBitmap, null, mDestRect!!, null)
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}