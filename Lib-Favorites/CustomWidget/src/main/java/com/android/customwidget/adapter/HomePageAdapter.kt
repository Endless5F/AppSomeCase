package com.android.customwidget.adapter

import com.android.customwidget.data.HomeData
import androidx.recyclerview.widget.RecyclerView
import androidx.collection.SparseArrayCompat
import android.view.LayoutInflater
import com.android.customwidget.R
import android.content.Intent
import com.android.customwidget.exerciseList.hencoderpracticedraw1.DrawBasicActivity
import com.android.customwidget.exerciseList.hencoderpracticedraw2.PaintActivity
import com.android.customwidget.exerciseList.hencoderpracticedraw3.DrawTextActivity
import com.android.customwidget.exerciseList.hencoderpracticedraw4.ClipAndMatrixActivity
import com.android.customwidget.exerciseList.hencoderpracticedraw5.DrawOrderActivity
import com.android.customwidget.exerciseList.hencoderpracticedraw6.Animation1Activity
import com.android.customwidget.exerciseList.hencoderpracticedraw7.Animation2Activity
import com.android.customwidget.exerciseList.hencoderpracticelayout1.LayoutBasicActivity
import com.android.customwidget.activity.FormActivity
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.customwidget.activity.DynamicHeightActivity

class HomePageAdapter(
    private val mContext: Context,
    private val mItemData: List<HomeData.ItemView>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mHeaderViews = SparseArrayCompat<Int?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when {
            mHeaderViews[viewType] != null -> {
                val v = LayoutInflater.from(mContext).inflate(
                    mHeaderViews[viewType]!!, parent,
                    false
                )
                HeadViewHolder(v)
            }
            ITEM_TYPE_TITLE == viewType -> {
                val v = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_home_page_title,
                    parent, false
                )
                TitleViewHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_home_page_item,
                    parent, false
                )
                ItemViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        var pos = position
        val viewType = getItemViewType(pos)
        pos -= if (mHeaderViews[viewType] != null) {
            val headViewHolder = viewHolder as HeadViewHolder
            headViewHolder.scan.setOnClickListener { }
            return
        } else {
            headersCount
        }
        if (viewType == ITEM_TYPE_TITLE) {
            val titleViewHolder = viewHolder as TitleViewHolder
            titleViewHolder.title.text = mItemData[pos].desc
        } else {
            val itemViewHolder = viewHolder as ItemViewHolder
            itemViewHolder.name.text = mItemData[pos].desc
            itemViewHolder.name.setOnClickListener { v: View? ->
                when (pos) {
                    1 -> {
                        val intent1 = Intent(mContext, DrawBasicActivity::class.java)
                        mContext.startActivity(intent1)
                    }
                    2 -> {
                        val intent2 = Intent(mContext, PaintActivity::class.java)
                        mContext.startActivity(intent2)
                    }
                    3 -> {
                        val intent3 = Intent(mContext, DrawTextActivity::class.java)
                        mContext.startActivity(intent3)
                    }
                    4 -> {
                        val intent4 = Intent(mContext, ClipAndMatrixActivity::class.java)
                        mContext.startActivity(intent4)
                    }
                    5 -> {
                        val intent5 = Intent(mContext, DrawOrderActivity::class.java)
                        mContext.startActivity(intent5)
                    }
                    6 -> {
                        val intent6 = Intent(mContext, Animation1Activity::class.java)
                        mContext.startActivity(intent6)
                    }
                    7 -> {
                        val intent7 = Intent(mContext, Animation2Activity::class.java)
                        mContext.startActivity(intent7)
                    }
                    8 -> {
                        val intent8 = Intent(mContext, LayoutBasicActivity::class.java)
                        mContext.startActivity(intent8)
                    }
                    9 -> {
                        val intent9 = Intent(mContext, DynamicHeightActivity::class.java)
                        mContext.startActivity(intent9)
                    }
                    13 -> {
                        val intent13 = Intent(mContext, FormActivity::class.java)
                        mContext.startActivity(intent13)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isHeaderViewPos(position) -> {
                mHeaderViews.keyAt(position)
            }
            isTitle(position) -> {
                ITEM_TYPE_TITLE
            }
            else -> {
                ITEM_TYPE_SECOND
            }
        }
    }

    override fun getItemCount(): Int {
        return mItemData.size + headersCount
    }

    private fun isHeaderViewPos(position: Int): Boolean {
        return position < headersCount
    }

    private val headersCount: Int
        get() = mHeaderViews.size()

    fun addHeaderView(view: Int) {
        mHeaderViews.put(mHeaderViews.size() + ITEM_TYPE_HEADER, view)
    }

    fun removeHeaderView() {
        mHeaderViews.clear()
    }

    val isHaveHeaderView: Boolean
        get() = mHeaderViews.size() > 0

    private fun isTitle(position: Int): Boolean {
        return "" == mItemData[position - headersCount].icon
    }

    internal inner class ItemViewHolder @SuppressLint("WrongViewCast") constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.tv_item_name)
        var icon: TextView = itemView.findViewById(R.id.tv_item_icon)

    }

    internal inner class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.tv_title)

    }

    internal inner class HeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var scan: TextView = itemView.findViewById(R.id.tv_scan)
    }

    companion object {
        private const val ITEM_TYPE_HEADER = 100000
        private const val ITEM_TYPE_TITLE = 111110
        private const val ITEM_TYPE_SECOND = 111111
    }
}