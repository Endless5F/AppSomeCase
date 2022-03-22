package com.android.app.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface AbstractMenuAdapter {

    int getCount();

    View onCreateView(LayoutInflater mInflater, ViewGroup viewGroup);

    void onViewBinder(View v, int i);

    void upDateView(View child, double mStartAngle, int i);
}
