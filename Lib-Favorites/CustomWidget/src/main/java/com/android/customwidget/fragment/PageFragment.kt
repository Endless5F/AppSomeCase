package com.android.customwidget.fragment

import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.customwidget.R
import android.view.ViewStub
import androidx.fragment.app.Fragment
import com.android.customwidget.fragment.PageFragment

class PageFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            @LayoutRes sampleLayoutRes: Int,
            @LayoutRes practiceLayoutRes: Int
        ): PageFragment {
            val fragment = PageFragment()
            val args = Bundle()
            args.putInt("sampleLayoutRes", sampleLayoutRes)
            args.putInt("practiceLayoutRes", practiceLayoutRes)
            fragment.arguments = args
            return fragment
        }
    }

    @LayoutRes
    var sampleLayoutRes = 0

    @LayoutRes
    var practiceLayoutRes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            sampleLayoutRes = args.getInt("sampleLayoutRes")
            practiceLayoutRes = args.getInt("practiceLayoutRes")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)
        val sampleStub = view.findViewById<View>(R.id.sampleStub) as ViewStub
        sampleStub.layoutResource = sampleLayoutRes
        sampleStub.inflate()
        val practiceStub = view.findViewById<View>(R.id.practiceStub) as ViewStub
        practiceStub.layoutResource = practiceLayoutRes
        practiceStub.inflate()
        return view
    }
}