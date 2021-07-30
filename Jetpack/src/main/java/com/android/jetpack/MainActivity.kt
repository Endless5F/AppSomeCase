package com.android.jetpack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.jetpack.ui.Home
import com.android.jetpack.ui.theme.WeTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Log.e("jcy", "onCreate: setContent")
            val viewModel: WeViewModel = viewModel()
            WeTheme(viewModel.theme) {
                Home()
            }
        }
        Log.e("jcy", "onCreate")
    }

    override fun onBackPressed() {
        val viewModel: WeViewModel by viewModels()
        if (viewModel.chatting) {
            viewModel.endChat()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        Log.e("jcy", "onResume")
        super.onResume()
        Log.e("jcy", "onResume super.onResume")
        // 打印view层级
        Handler(Looper.getMainLooper()).post {
            depthFirst(window.decorView)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e("jcy", "onAttachedToWindow")
    }
    /**
     * 普通DecorView结构:
     * --DecorView == extends FrameLayout
     * ------View (4.4在这自定义个伪状态栏)
     * ------LinearLayout
     * ----------ViewStub
     * ----------FrameLayout
     * --------------FitWindowsLinearLayout(style设置了不带toolbar)
     * ------------------ViewStubCompat
     * ------------------ContentFrameLayout == ContentView
     * --------------ActionBarOverlayLayout(style设置了带toolbar)
     * ------------------ContentFrameLayout == ContentView
     * ------------------ActionBarContainer
     *
     * Jetpack Compose DecorView结构:
     * - com.android.internal.policy.DecorView
     * -- android.widget.LinearLayout
     * --- android.widget.FrameLayout
     * ---- androidx.appcompat.widget.FitWindowsLinearLayout
     * ----- androidx.appcompat.widget.ContentFrameLayout
     * ------ androidx.compose.ui.platform.ComposeView
     * ------- androidx.compose.ui.platform.AndroidComposeView
     * ----- androidx.appcompat.widget.ViewStubCompat
     * --- android.view.ViewStub
     */
    private fun depthFirst(root: View) {
        val viewDeque = LinkedList<ViewIndex>()
        var viewIndex = ViewIndex(0, root)
        viewDeque.push(viewIndex)
        while (!viewDeque.isEmpty()) {
            viewIndex = viewDeque.pop()
            printView(viewIndex)
            if (viewIndex.view is ViewGroup) {
                for (childIndex in 0 until (viewIndex.view as ViewGroup).childCount) {
                    val childView = (viewIndex.view as ViewGroup).getChildAt(childIndex)
                    viewDeque.push(ViewIndex(viewIndex.index + 1, childView))
                }
            }
        }
    }

    private fun printView(root: ViewIndex) {
        val divider = StringBuilder()
        for (i in 0..root.index) {
            divider.append("-")
        }
        Log.e("jcy", "printView: $divider ${root.view.javaClass.name}")
    }


    class ViewIndex(val index: Int, val view: View)
}