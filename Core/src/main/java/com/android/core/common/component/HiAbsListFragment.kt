package com.android.core.common.component

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.core.R
import com.android.core.widget.EmptyView
import com.android.core.widget.recycleview.HiRecyclerView
import com.android.core.widget.item.HiAdapter
import com.android.core.widget.item.HiDataItem
import com.android.core.widget.refresh.HiOverView
import com.android.core.widget.refresh.HiRefresh
import com.android.core.widget.refresh.HiRefreshLayout
import com.android.core.widget.refresh.HiTextOverView

open class HiAbsListFragment : HiBaseFragment(), HiRefresh.HiRefreshListener {
    var pageIndex: Int = 1
    private lateinit var hiAdapter: HiAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var refreshHeaderView: HiTextOverView
    private var loadingView: ContentLoadingProgressBar? = null
    private var emptyView: EmptyView? = null
    private var recyclerView: HiRecyclerView? = null
    private var refreshLayout: HiRefreshLayout? = null

    companion object {
        const val PREFETCH_SIZE = 5
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_list
    }


    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.refreshLayout = view.findViewById(R.id.refresh_layout)
        this.recyclerView = view.findViewById(R.id.recycler_view)
        this.emptyView = view.findViewById(R.id.empty_view)
        this.loadingView = view.findViewById(R.id.content_loading)

        refreshHeaderView = HiTextOverView(context)
        refreshLayout?.setRefreshOverView(refreshHeaderView)
        refreshLayout?.setRefreshListener(this)

        layoutManager = createLayoutManager()
        hiAdapter = HiAdapter(view.context)

        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = hiAdapter

        emptyView?.visibility = View.GONE
        emptyView?.setIcon(R.string.list_empty)
        emptyView?.setDesc(getString(R.string.list_empty_desc))
        emptyView?.setButton(getString(R.string.list_empty_action), View.OnClickListener {
            onRefresh()
        })

        loadingView?.visibility = View.VISIBLE
        pageIndex = 1
    }

    open fun finishRefresh(dataItems: List<HiDataItem<*, out RecyclerView.ViewHolder>>?) {
        val success = dataItems != null && dataItems.isNotEmpty()
        //光真么判断还是不行的，我们还需要别的措施。。。因为可能会出现 下拉单时候，有执行了删上拉分页
        val refresh = pageIndex == 1
        if (refresh) {
            loadingView?.visibility = View.GONE
            refreshLayout?.refreshFinished()
            if (success) {
                emptyView?.visibility = View.GONE
                hiAdapter.clearItems()
                hiAdapter.addItems(dataItems!!, true)
            } else {
                //此时就需要判断列表上是否已经有数据，如果么有，显示出空页面转态
                if (hiAdapter.itemCount <= 0) {
                    emptyView?.visibility = View.VISIBLE
                }
            }
        } else {
            if (success) {
                hiAdapter.addItems(dataItems!!, true)
            }
            recyclerView?.loadFinished(success)
        }
    }


    open fun enableLoadMore(callback: () -> Unit) {
        //这里可以直接这么写吗？
        //为了防止 同时 下拉刷新 和上拉分页的请求，这里就需要处理一把
        recyclerView?.enableLoadMore({
            if (refreshHeaderView.state == HiOverView.HiRefreshState.STATE_REFRESH) {
                //正处于刷新状态
                recyclerView?.loadFinished(false)
                return@enableLoadMore
            }
            pageIndex++
            callback()
        }, PREFETCH_SIZE)
    }

    open fun disableLoadMore() {
        recyclerView?.disableLoadMore()
    }

    open fun createLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun enableRefresh(): Boolean {
        return true
    }

    @CallSuper
    override fun onRefresh() {
        if (recyclerView?.isLoading() == true) {
            //正处于分页
            //复现场景,比较难以复现---》如果下执行上拉分页。。。快速返回  往下拉，松手。会出现一个bug: 转圈圈的停住不动了。
            //问题的原因在于 立刻调用 refreshFinished 时，refreshHeader的底部bottom值是超过了 它的height的。
            //refreshLayout#recover（dis） 方法中判定了，如果传递dis 参数 大于 header height ,dis =200,height =100,只能恢复到 刷新的位置。不能恢复到初始位置。
            //加了延迟之后，他会  等待 松手的动画做完，才去recover 。此时就能恢复最初状态了。
            refreshLayout?.post {
                refreshLayout?.refreshFinished()
            }
            return
        }
        pageIndex = 1
    }
}