package com.android.architecture.demolist.lifecycle

import androidx.fragment.app.Fragment

class LifecycleFragment : Fragment() {
    init {
        lifecycle.addObserver(StudyLifecyleObserver(requireContext()))
    }



}