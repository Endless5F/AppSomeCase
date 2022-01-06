package com.android.app.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class LazyFragment : Fragment() {
    private val TAG = "lazy_fragment"
    protected var mRootView: View? = null
    private var viewCreated = false
    private var currentVisibleStatus = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mRootView == null) {
            mRootView = inflater.inflate(layout, container, false)
        }
        initView(mRootView)
        viewCreated = true
        Log.i(TAG, javaClass.simpleName + "====>onCreateView")
        if (userVisibleHint) {
            userVisibleHint = true
        }
        return mRootView
    }

    abstract val layout: Int
    abstract fun initView(view: View?)

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.i(TAG, javaClass.simpleName + "====>setUserVisibleHint")
        if (viewCreated) {
            if (!currentVisibleStatus && isVisibleToUser) {
                dispatchUserVisibleStatus(true)
            } else if (currentVisibleStatus && !isVisibleToUser) {
                dispatchUserVisibleStatus(false)
            }
        }
    }

    fun dispatchUserVisibleStatus(isUserVisibleStatus: Boolean) {
        currentVisibleStatus = isUserVisibleStatus
        if (isUserVisibleStatus) {
            onStartLoad()
        } else {
            onStopLoad()
        }
        //在嵌套模式下，让子类的fragment进行分发
        val fm = childFragmentManager
        val fragments = fm.fragments
        if (fragments.size > 0) {
            for (fragment in fragments) {
                if (fragment is LazyFragment) {
                    if (fragment.getUserVisibleHint()) {
                        fragment.dispatchUserVisibleStatus(true)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, javaClass.simpleName + "===>onResume")
        if (userVisibleHint && !currentVisibleStatus) {
            dispatchUserVisibleStatus(true)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, javaClass.simpleName + "===>onPause")
        if (userVisibleHint && currentVisibleStatus) {
            dispatchUserVisibleStatus(false)
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, javaClass.simpleName + "===>onDetach")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, javaClass.simpleName + "===>onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, javaClass.simpleName + "===>onDestroy")
    }

    /**
     * 子类重写该方法来实现开始加载数据
     */
    protected open fun onStartLoad() {
        Log.i(TAG, javaClass.simpleName + "====>开始加载数据onStartLoad")
    }

    /**
     * 子类重写该方法来实现暂停数据加载
     */
    protected open fun onStopLoad() {
        Log.i(TAG, javaClass.simpleName + "====>停止加载数据onStopLoad")
    }
}