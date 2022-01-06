package com.android.app.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.android.app.R
import com.android.app.view.ComponentEditView
import com.android.core.interpolator.CubicBezierInterpolator


fun View.startAnim() {
    val mShowAnimator = AnimatorSet()
    alpha = 0f
    translationY = this.height.toFloat()
    val alpha = ObjectAnimator.ofFloat(this, "alpha", 1.0f)
    alpha.duration = 200
    alpha.interpolator = LinearInterpolator()

    val anim = ObjectAnimator.ofFloat(this, "translationY", 0f)
    anim.duration = 200
    anim.interpolator = CubicBezierInterpolator(0.42f, 0.0f, 0.58f, 1.0f)

    val oa: ObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", 51f)
    oa.duration = 300L
    oa.interpolator = CubicBezierInterpolator(0.42f, 0.0f, 0.58f, 1.0f)

    mShowAnimator.play(anim).with(alpha).before(oa)

    post {
        mShowAnimator.start()
    }
}

fun AppCompatActivity.switchFragment(layoutInt: Int, fragment: ComponentFragment) {
    val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.add(layoutInt, fragment)
    fragmentTransaction.commitAllowingStateLoss()
}

class ComponentFragment : LazyFragment() {
    override val layout: Int
        get() = R.layout.fragment_component

    override fun initView(view: View?) {
        if (view is ViewGroup) {
            view.addView(ComponentEditView(view.context))
        }
    }

    override fun onResume() {
        super.onResume()
        if (mRootView?.height == 0) {
            mRootView?.viewTreeObserver?.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mRootView?.startAnim()
                    mRootView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                }
            })
        } else {
            mRootView?.startAnim()
        }
    }
}