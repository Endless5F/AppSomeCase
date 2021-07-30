package com.android.customwidget.widget.form

import android.content.Context
import com.android.customwidget.ext.dp

class VerticalFormView(context: Context) : AbsFormView<FormItemEntity>(context) {

    override fun getIconWidth(): Int {
        return 45.dp
    }

    override fun getSpanCount(): Int {
        return 3
    }

    override fun getGravityType(): Int {
        return horizontalRight
    }

    override fun getCurrentIcon(index: Int): String {
        return dataList[index].icon
    }

    override fun getCurrentText(index: Int): String {
        return dataList[index].display_name
    }

    override fun onItemClickListener(index: Int) {

    }
}