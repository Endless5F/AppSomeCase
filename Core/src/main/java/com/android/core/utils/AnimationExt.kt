package com.android.core.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import com.android.core.interpolator.CubicBezierInterpolator

private const val ANIMATION_DURATION = 200L

/**
 * 从底部上移显示动画
 */
fun getBottomInAnimation(): AnimationSet {
    val animationSet = AnimationSet(true)
    var animation: Animation = TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 0.0f
    )
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animation = AlphaAnimation(0f, 1f)
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animationSet.interpolator = AccelerateInterpolator()
    return animationSet
}

/**
 * 从底部下移消失动画
 */
fun getBottomOutAnimation(): AnimationSet {
    val animationSet = AnimationSet(true)
    var animation: Animation = TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 2.0f
    )
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animation = AlphaAnimation(1f, 0f)
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animationSet.interpolator = AccelerateInterpolator()
    return animationSet
}

/**
 * 从右侧左移显示动画
 */
fun getRightInAnimation(): AnimationSet {
    val animationSet = AnimationSet(true)
    var animation: Animation = TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
    )
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animation = AlphaAnimation(0f, 1f)
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animationSet.interpolator = AccelerateInterpolator()
    return animationSet
}

/**
 * 从右侧右移消失动画
 */
fun getRightOutAnimation(): AnimationSet {
    val animationSet = AnimationSet(true)
    var animation: Animation = TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 2.0f,
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
    )
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animation = AlphaAnimation(1f, 0f)
    animation.duration = ANIMATION_DURATION
    animationSet.addAnimation(animation)
    animationSet.interpolator = AccelerateInterpolator()
    return animationSet
}

/**
 * 缩放动画
 */
fun View.startScaleAnim(fromValue: Float, toValue: Float, duration: Long = 300L) {
    val mScaleAnimator = AnimatorSet()
    val scaleXAnim =
        ObjectAnimator.ofFloat(this, "scaleX", fromValue, toValue)
    val scaleYAnim =
        ObjectAnimator.ofFloat(this, "scaleY", fromValue, toValue)
    mScaleAnimator.playTogether(scaleXAnim, scaleYAnim)
    mScaleAnimator.duration = duration

    mScaleAnimator.start()
}

/**
 * 抖一下动画，上下抖动
 * @param mask 和调用此抖动动画同一FrameLayout层同样大小的蒙层，防止位移上去后底部空白
 * @param duration 动画时长
 */
fun View.startShakeAnim(mask: View, duration: Long = 320L) {
    val mShowAnimator = AnimatorSet()
    alpha = 0.0f
    mask.alpha = 0.0f
    translationY = height.toFloat()

    val alphaMask = ObjectAnimator.ofFloat(mask, "alpha", 1.0f)
    alphaMask.duration = duration
    alphaMask.interpolator = LinearInterpolator()

    val value = context?.dip(17)?.toFloat() ?: 0f
    val upTranslationY = ObjectAnimator.ofFloat(this, "translationY", -value)
    upTranslationY.duration = duration
    upTranslationY.interpolator = CubicBezierInterpolator(0.42f, 0.0f, 0.58f, 1.0f)

    val downTranslationY: ObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f)
    downTranslationY.duration = 200
    downTranslationY.interpolator = CubicBezierInterpolator(0.42f, 0.0f, 0.58f, 1.0f)

    mShowAnimator.play(alphaMask).with(upTranslationY).before(downTranslationY)
    mShowAnimator.addListener(object : Animator.AnimatorListener{
        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            alpha = 1f
        }

        override fun onAnimationCancel(animation: Animator?) {
            alpha = 1f
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    })

    post {
        mShowAnimator.start()
    }
}