package com.android.core.widget.banner2.indicator;

import android.view.View;

import androidx.annotation.NonNull;

import com.android.core.widget.banner2.config.IndicatorConfig;
import com.android.core.widget.banner2.listener.OnPageChangeListener;

public interface Indicator extends OnPageChangeListener {
    @NonNull
    View getIndicatorView();

    IndicatorConfig getIndicatorConfig();

    void onPageChanged(int count, int currentPosition);

}
