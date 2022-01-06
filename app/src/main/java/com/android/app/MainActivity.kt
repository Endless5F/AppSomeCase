package com.android.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager
import com.android.app.fragment.ComponentFragment
import com.android.app.fragment.FirstFragment
import com.android.app.fragment.SecondFragment
import com.android.app.fragment.ThirdFragment
import com.android.app.fragment.switchFragment
import com.android.core.restful.HiCallback
import com.android.core.restful.HiResponse
import com.android.core.restful.demo.ApiFactory
import com.android.core.restful.demo.TestApi
import com.android.kmmshared.Greeting

/** 首页效率在ViewPager中的位置  */
const val VISION_HOME_EFFICIENCY = 0
/** 首页默认在ViewPager中的位置  */
const val VISION_HOME_DEFAULT = 1
/** 首页娱乐在ViewPager中的位置  */
const val VISION_HOME_ENTERTAIN = 2

/**
 * 记录当前page的位置,正常取值:[0,2],无效值:-1
 */
private const val INVALID_POSITION = -1

private const val VISION_HOME_TAB_COUNT = 3

/** 首页ViewPager默认切换动效状态  */
private val PAGER_DEFAULT_ANIM_STATE = 0

/** 首页ViewPager左屏切换抽屉动效状态  */
private val PAGER_LEFT_ANIM_STATE = -1

/** 首页ViewPager右屏切换抽屉动效状态  */
private val PAGER_RIGHT_ANIM_STATE = 1


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val HOME_TAB_COUNT = 3
    private var one: TextView? = null
    private var two: TextView? = null
    private var three: TextView? = null
    private val fm = supportFragmentManager
    private var viewPager: ViewPager? = null
    private var mCurrentPosition = INVALID_POSITION
    private val mFragmentCaches = arrayOfNulls<Fragment>(HOME_TAB_COUNT)

    /** 记录Fragment滑动动效状态，-1:左侧负一屏抽屉动效，1:右侧负一屏抽屉动效，0：默认动效  */
    private var mPagerTransformerState: Int = PAGER_DEFAULT_ANIM_STATE

    /** Fragment滑动动效，抽屉效果  */
    private val mPageTransformer: ViewPager.PageTransformer =
        ViewPager.PageTransformer { page, position ->
            val pageWidth = page.width.toFloat()
            when (mPagerTransformerState) {
                PAGER_RIGHT_ANIM_STATE -> {
                    page.translationX =
                        if (-1 < position && position <= 0) pageWidth * -position else 0f
                }
                PAGER_LEFT_ANIM_STATE -> {
                    page.translationX = if (0 <= position && position < 1) pageWidth * -position else 0f
                }
                else -> {
                    page.translationX = 0f
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.view_pager)
        initViewPager()
        initBottomBar()
    }

    private fun initViewPager() {
        viewPager?.adapter =
            object : FragmentPagerAdapter(supportFragmentManager) {
                override fun getCount(): Int {
                    return Int.MAX_VALUE
                }

                override fun getItem(position: Int): Fragment {
                    return getFragment(position)
                }

                override fun instantiateItem(container: ViewGroup, position: Int): Any {
                    val transaction: FragmentTransaction = fm.beginTransaction()
                    val itemId = getItemId(position)
                    val name: String = makeFragmentName(container.id, itemId)
                    var fragment: Fragment? = fm.findFragmentByTag(name)
                    if (fragment != null) {
                        transaction.attach(fragment)
                    } else {
                        fragment = getItem(position)
                        transaction.add(
                            container.id,
                            fragment,
                            makeFragmentName(container.id, itemId)
                        )
                    }
                    if (position != viewPager?.currentItem) {
                        transaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                    }
                    transaction.commitAllowingStateLoss()
                    return fragment
                }

                override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
                    super.setPrimaryItem(container, position, `object`)
                    if (`object` !is Fragment) {
                        return
                    }
                    val transaction = fm.beginTransaction()
                    transaction.setMaxLifecycle(`object`, Lifecycle.State.RESUMED)
                    for (f in fm.fragments) {
                        if (`object` !== f) {
                            transaction.setMaxLifecycle(f, Lifecycle.State.STARTED)
                        }
                    }
                    transaction.commitAllowingStateLoss()
                }

                override fun getItemId(position: Int): Long {
                    return (position % HOME_TAB_COUNT).toLong()
                }

                override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                    super.destroyItem(container, position % HOME_TAB_COUNT, `object`)
                }
            }
        // 默认显示中间Fragment
        viewPager?.setCurrentItem(Int.MAX_VALUE / 2 + 1, false)

        addOnPageChangeListener()
    }

    private fun initBottomBar() {
        one = findViewById(R.id.tv_one)
        two = findViewById(R.id.tv_two)
        three = findViewById(R.id.tv_three)
        one?.setOnClickListener(this)
        two?.setOnClickListener(this)
        three?.setOnClickListener(this)
    }

    private fun addOnPageChangeListener() {
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            /** 记录当前scroll状态  */
            private var mCurrentState = -1

            /** 记录页面切换蒙层状态,无效值:-1  */
            private var mPagerTransformerMaskState = -1
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (viewPager == null) {
                    return
                }
                if (mCurrentPosition == INVALID_POSITION && positionOffset != 0f) {
                    mCurrentPosition =
                        position % VISION_HOME_TAB_COUNT
                    onPageScrollDirectionWithAnim(
                        mCurrentState,
                        mCurrentPosition,
                        viewPager!!.currentItem % VISION_HOME_TAB_COUNT,
                        false
                    )
                } else {
                    // 手势滑动左右切换两个page时，中间态重置标记，重新判断滑动方向，如：HOME->娱乐->效率
                    if (mCurrentState == ViewPager.SCROLL_STATE_DRAGGING
                        && mCurrentPosition != position % VISION_HOME_TAB_COUNT
                    ) {
                        mCurrentPosition =
                            INVALID_POSITION
                    }
                }
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {
                mCurrentState = state
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mCurrentPosition = INVALID_POSITION
                }
            }

            /**
             * 通过state/position/currentPage总结出来判断三个page切换方向的离散点，根据不同page切换实现不同切换动效
             *
             * @param fromState   从onPageScrollStateChanged获取的scroll起始状态
             * @param position    onPageScrolled中的position
             * @param currentItem mViewPager.getCurrentItem()获取的当前page位置
             * @param isOnlyReadState 只读取状态，主要为 mPagerTransformerMaskState 重新设置真实状态
             * @return 三个page切换定值，记录离散取值（HOME->娱乐，娱乐->效率等）
             */
            private fun onPageScrollDirectionWithAnim(
                fromState: Int, position: Int,
                currentItem: Int, isOnlyReadState: Boolean
            ): HomePageScrollDirection {
                // 将要改变的状态
                var pageAnimState: Int = PAGER_DEFAULT_ANIM_STATE
                var direction: HomePageScrollDirection = HomePageScrollDirection.INVALIDATE
                if (fromState == ViewPager.SCROLL_STATE_DRAGGING) {
                    // 手动滑动切换page
                    if (position == VISION_HOME_EFFICIENCY && currentItem == VISION_HOME_DEFAULT) {
                        // HOME->效率
                        pageAnimState =
                            PAGER_LEFT_ANIM_STATE
                        direction = HomePageScrollDirection.HOME_TO_EFFICIENCY
                    } else if (position == VISION_HOME_EFFICIENCY && currentItem == VISION_HOME_EFFICIENCY) {
                        // 效率->HOME
                        pageAnimState =
                            PAGER_LEFT_ANIM_STATE
                        direction = HomePageScrollDirection.EFFICIENCY_TO_HOME
                    } else if (position == VISION_HOME_DEFAULT && currentItem == VISION_HOME_DEFAULT) {
                        // HOME->娱乐
                        pageAnimState =
                            PAGER_RIGHT_ANIM_STATE
                        direction = HomePageScrollDirection.HOME_TO_ENTERTAIN
                    } else if (position == VISION_HOME_DEFAULT && currentItem == VISION_HOME_ENTERTAIN) {
                        // 娱乐->HOME
                        pageAnimState =
                            PAGER_RIGHT_ANIM_STATE
                        direction = HomePageScrollDirection.ENTERTAIN_TO_HOME
                    } else if (position == VISION_HOME_ENTERTAIN && currentItem == VISION_HOME_EFFICIENCY) {
                        // 效率->娱乐
                        direction = HomePageScrollDirection.EFFICIENCY_TO_ENTERTAIN
                    } else if (position == VISION_HOME_ENTERTAIN && currentItem == VISION_HOME_ENTERTAIN) {
                        // 娱乐->效率
                        direction = HomePageScrollDirection.ENTERTAIN_TO_EFFICIENCY
                    }
                    if (!isOnlyReadState) {
                        setDirection(pageAnimState)
                    }
                } else if (fromState == ViewPager.SCROLL_STATE_SETTLING) {
                    // 点击底部工具栏切换page
                    if (position == VISION_HOME_EFFICIENCY && currentItem == VISION_HOME_EFFICIENCY) {
                        // HOME->效率
                        pageAnimState =
                            PAGER_LEFT_ANIM_STATE
                        direction = HomePageScrollDirection.HOME_TO_EFFICIENCY
                    } else if (position == VISION_HOME_EFFICIENCY && currentItem == VISION_HOME_DEFAULT) {
                        // 效率->HOME
                        pageAnimState =
                            PAGER_LEFT_ANIM_STATE
                        direction = HomePageScrollDirection.EFFICIENCY_TO_HOME
                    } else if (position == VISION_HOME_DEFAULT && currentItem == VISION_HOME_ENTERTAIN) {
                        // HOME->娱乐
                        pageAnimState =
                            PAGER_RIGHT_ANIM_STATE
                        direction = HomePageScrollDirection.HOME_TO_ENTERTAIN
                    } else if (position == VISION_HOME_DEFAULT && currentItem == VISION_HOME_DEFAULT) {
                        // 娱乐->HOME
                        pageAnimState =
                            PAGER_RIGHT_ANIM_STATE
                        direction = HomePageScrollDirection.ENTERTAIN_TO_HOME
                    } else if (position == VISION_HOME_ENTERTAIN && currentItem == VISION_HOME_ENTERTAIN) {
                        // 效率->娱乐
                        direction = HomePageScrollDirection.EFFICIENCY_TO_ENTERTAIN
                    } else if (position == VISION_HOME_ENTERTAIN && currentItem == VISION_HOME_EFFICIENCY) {
                        // 娱乐->效率
                        direction = HomePageScrollDirection.ENTERTAIN_TO_EFFICIENCY
                    }
                    if (!isOnlyReadState) {
                        setDirection(pageAnimState)
                    }
                }
                mPagerTransformerMaskState = pageAnimState
                return direction
            }
        })
    }

    /**
     * 设置抽屉动效
     *
     * @param pagerTransformerState -1:左侧滑动抽屉动效，1:右侧滑动抽屉动效，0:默认平移动效
     */
    fun setDirection(pagerTransformerState: Int) {
        if (mPagerTransformerState == pagerTransformerState || viewPager == null) {
            return
        }
        mPagerTransformerState = pagerTransformerState
        viewPager?.setPageTransformer(
            mPagerTransformerState == PAGER_LEFT_ANIM_STATE,
            mPageTransformer
        )
    }

    override fun onClick(v: View?) {
        when(v) {
            one -> {
//                viewPager?.currentItem = 1
            }
            two -> {
//                viewPager?.currentItem = 2
                switchFragment(R.id.rootView, ComponentFragment())
            }
            three -> {
//                viewPager?.currentItem = 3
            }
        }
    }

    private fun getFragment(position: Int): Fragment {
        val pos: Int = position % HOME_TAB_COUNT
        val name: String = makeFragmentName(0, pos.toLong())
        var fragment = fm.findFragmentByTag(name)
        if (fragment != null) {
            return fragment
        }
        fragment = mFragmentCaches[pos]
        if (fragment == null) {
            fragment = when(pos) {
                1-> SecondFragment()
                2-> ThirdFragment()
                else -> FirstFragment()
            }
            mFragmentCaches[pos] = fragment
        }
        return fragment
    }

    private fun makeFragmentName(viewId: Int, id: Long): String {
        return "android:switcher::$id"
    }

    private fun testApiFactory() {
        ApiFactory.create(TestApi::class.java).getArticleList(0)
            .enqueue(object : HiCallback<String> {
                override fun onSuccess(response: HiResponse<String>) {
                    Log.e("jcy", "onSuccess: ${response.data}")
                }

                override fun onFailed(throwable: Throwable) {
                    Log.e("jcy", "onFailed: ${throwable.message}")
                }
            })

        Greeting().greeting()
    }
}

/** 首页page滑动方向 */
enum class HomePageScrollDirection {

    /** HOME->效率 */
    HOME_TO_EFFICIENCY,
    /** 效率->HOME */
    EFFICIENCY_TO_HOME,
    /** HOME->娱乐 */
    HOME_TO_ENTERTAIN,
    /** 娱乐->HOME */
    ENTERTAIN_TO_HOME,
    /** 娱乐->效率 */
    ENTERTAIN_TO_EFFICIENCY,
    /** 效率->娱乐 */
    EFFICIENCY_TO_ENTERTAIN,
    /** 无效 */
    INVALIDATE
}