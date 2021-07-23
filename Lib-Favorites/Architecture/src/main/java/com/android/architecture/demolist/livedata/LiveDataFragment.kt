package com.android.architecture.demolist.livedata

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class LiveDataFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 同一Activity下的fragment可通过此方式共享数据
        val viewModel = ViewModelProviders.of(requireActivity()).get(LiveDataTimerViewModel::class.java)
        viewModel.elapsedTime.observe(viewLifecycleOwner, {
            // ......
        })
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}