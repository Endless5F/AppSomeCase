package com.android.core.common.recyclerview.link

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * 联动列表适配器
 */
class LinkAdapter : RecyclerView.Adapter<LinkAdapter.LinkHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkHolder {
        return LinkHolder(LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })
    }

    override fun onBindViewHolder(holder: LinkHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class LinkHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}