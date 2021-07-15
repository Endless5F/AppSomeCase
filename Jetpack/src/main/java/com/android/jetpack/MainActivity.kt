package com.android.jetpack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ActionBarOverlayLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.viewpager.widget.ViewPager
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Greeting("World")
        }
    }

    @Composable
    fun Greeting(name: String) {
        Column() {
            Text(text = "Hello $name!", Modifier.padding(2.dp))
            Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "图标")
        }

    }

    @Preview
    @Composable
    fun PreviewGreeting() {
        Greeting("Android")
    }

    override fun onResume() {
        super.onResume()
        val root = LinearLayout(this)
        root.addView(TextView(this))
        root.addView(TextView(this))
        val rl = RelativeLayout(this)
        rl.addView(ImageView(this))
        rl.addView(ViewPager(this))
        root.addView(rl)
        // 打印view层级
        Handler(Looper.getMainLooper()).post {
            depthFirst(window.decorView)
        }
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
        var tree = ""
        for (i in 0..root.index) {
            tree += "-"
        }
        Log.e("jcy", "printView: $tree ${root.view.javaClass.name}")
    }


    class ViewIndex(val index: Int, val view: View)
}