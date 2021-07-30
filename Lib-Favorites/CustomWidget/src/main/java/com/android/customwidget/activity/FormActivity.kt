package com.android.customwidget.activity

import android.os.Bundle
import com.android.customwidget.R
import com.android.customwidget.widget.form.FormItemEntity

class FormActivity : BaseActivity() {

    var formData = ArrayList<FormItemEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
//        val formView = VerticalFormView(this)
//        formData.apply {
//            add(FormItemEntity("数据1数据", ""))
//            add(FormItemEntity("数据2数据", ""))
//            add(FormItemEntity("数据3数据", ""))
//            add(FormItemEntity("数据4数据", ""))
//            add(FormItemEntity("数据5数据", ""))
//            add(FormItemEntity("数据6数据", ""))
//            add(FormItemEntity("数据7数据", ""))
//            add(FormItemEntity("数据8数据", ""))
//            add(FormItemEntity("数据9数据", ""))
//        }
//        formView.apply {
//            setItemsData(formData)
//        }
//
//        rootView.addView(formView)
    }
}


