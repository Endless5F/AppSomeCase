package com.android.app.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.app.R;

public class OvalMenuAdapter implements AbstractMenuAdapter {

    /**
     * 菜单项的图标
     */
    private int[] mItemImags;
    private int mItemCount = 0;


    public OvalMenuAdapter(int[] mItemImags) {
        // 参数检查
        this.mItemImags = mItemImags;
        mItemCount = mItemImags.length;
    }

    @Override
    public int getCount() {
        return mItemCount;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.circle_menu_item, container, false);
        return view;
    }

    @Override
    public void onViewBinder(View itemView, int position) {

        final int i = position;
        ImageView iv = itemView.findViewById(R.id.id_circle_menu_item_image);
        if (iv != null) {
            iv.setImageResource(mItemImags[i]);
            iv.setOnClickListener(v -> {
                        if (mOnMenuItemClickListener != null) {
                            mOnMenuItemClickListener.itemClick(v, i);
                        }
                    }
            );
        }
    }

    @Override
    public void upDateView(View v, double angle, int position) {
        /*
         * don't  see
         * 0.8*-abs(cos(1/2*(3.1415*(sin(1/2*x)))))+1.2
         */
        float d = (float) (0.8f * -Math.abs(Math.cos(0.5f * Math.PI * Math.sin(Math.toRadians(angle) / 2))) + 1.2-0.2);
        v.setScaleY(d);
        v.setScaleX(d);
        if (d > 1) {
            d = 1;
        }
        v.setAlpha(d);
    }


    public interface OnMenuItemClickListener {
        void itemClick(View view, int pos);
    }

    private OvalMenuAdapter.OnMenuItemClickListener mOnMenuItemClickListener;

    public void setOnItemClickListener(OvalMenuAdapter.OnMenuItemClickListener l) {
        this.mOnMenuItemClickListener = l;
    }
}
