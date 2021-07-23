package com.android.architecture.demolist.paging.viewholder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.architecture.R

class HeaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_paging_header, parent, false)) {

    fun bindsHeader() {
        // empty implementation
    }
}