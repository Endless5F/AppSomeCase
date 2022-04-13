package com.android.core.common.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 支持水平滑动回弹效果的容器
 */
public class StickyHorizontalScrollLayout extends LinearLayout implements NestedScrollingParent {

    private static final int DRAG = 2;

    /**
     * 嵌套滑动辅助类
     **/
    private NestedScrollingParentHelper mParentHelper;
    /**
     * 左侧回弹容器
     **/
    private FrameLayout mHeaderContainer;
    /**
     * 右侧回弹容器
     **/
    private FrameLayout mFooterContainer;
    /**
     * 列表组件
     **/
    private RecyclerView mChildView;
    /**
     * 是否正在进行回弹动效
     **/
    private boolean isRunAnim;
    /**
     * 两侧回弹的最大宽度
     **/
    public int maxWidth;
    /**
     * 回弹滚动监听
     */
    private StickyScrollListener mListener;

    public StickyHorizontalScrollLayout(Context context) {
        super(context);
        init(context);
    }

    public StickyHorizontalScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickyHorizontalScrollLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mHeaderContainer = new FrameLayout(context);
        mHeaderContainer.setBackgroundColor(Color.parseColor("#00000000"));
        mFooterContainer = new FrameLayout(context);
        mFooterContainer.setBackgroundColor(Color.parseColor("#00000000"));
        maxWidth = dp2Px(context, 60);
        mParentHelper = new NestedScrollingParentHelper(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOrientation(LinearLayout.HORIZONTAL);
        if (getChildAt(0) instanceof RecyclerView) {
            mChildView = (RecyclerView) getChildAt(0);
            LayoutParams layoutParams = new LayoutParams(maxWidth, LayoutParams.MATCH_PARENT);
            addView(mHeaderContainer, 0, layoutParams);
            addView(mFooterContainer, layoutParams);
            // 左移
            scrollBy(maxWidth, 0);
            // 保证动画状态中 子view不能滑动
            mChildView.setOnTouchListener((v, event) -> isRunAnim);
        } else {
            throw new IllegalStateException("第一个Child必须为RecyclerView");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mChildView != null && mChildView.getLayoutParams() != null) {
            ViewGroup.LayoutParams params = mChildView.getLayoutParams();
            params.width = getMeasuredWidth();
        }
    }

    /**
     * 必须要复写 onStartNestedScroll后调用
     */
    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        if (mParentHelper != null) {
            mParentHelper.onNestedScrollAccepted(child, target, axes);
        }
    }

    /**
     * 返回true代表处理本次事件
     * 在执行动画时间里不能处理本次事件
     */
    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return target instanceof RecyclerView && !isRunAnim;
    }

    /**
     * 复位初始位置
     * scrollTo 移动到指定坐标
     * scrollBy 在原有坐标上面移动
     */
    @Override
    public void onStopNestedScroll(@NonNull View target) {
        if (mParentHelper != null) {
            mParentHelper.onStopNestedScroll(target);
        }
        // 如果不在RecyclerView滑动范围内
        if (maxWidth != getScrollX()) {
            startAnimation(new StickyAnimation());
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {

    }

    /**
     * @param dx       水平滑动距离
     * @param dy       垂直滑动距离
     * @param consumed 父类消耗掉的距离
     */
    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        getParent().requestDisallowInterceptTouchEvent(true);
        // dx>0 往左滑动 dx<0往右滑动；
        boolean canScrollLeft = target.canScrollHorizontally(-1);
        boolean canScrollRight = target.canScrollHorizontally(1);
        boolean hiddenLeft = dx > 0 && getScrollX() < maxWidth && !canScrollLeft;
        boolean showLeft = dx < 0 && !canScrollLeft;
        boolean hiddenRight = dx < 0 && getScrollX() > maxWidth && !canScrollRight;
        boolean showRight = dx > 0 && !canScrollRight;
        if (hiddenLeft || showLeft || hiddenRight || showRight) {
            scrollBy(dx / DRAG, 0);
            consumed[0] = dx;
        }
        // 限制错位问题
        if (dx > 0 && getScrollX() > maxWidth && !canScrollLeft) {
            scrollTo(maxWidth, 0);
        }
        if (dx < 0 && getScrollX() < maxWidth && !canScrollRight) {
            scrollTo(maxWidth, 0);
        }

        if (mListener != null) {
            if (hiddenLeft || showLeft) {
                mListener.leftScroll(dx / DRAG);
            }
            if (hiddenRight || showRight) {
                mListener.rightScroll(dx / DRAG);
            }
        }
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    /**
     * 子view是否可以有惯性 解决右滑时快速左滑显示错位问题
     *
     * @return true不可以  false可以
     */
    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        // 当RecyclerView在界面之内交给它自己惯性滑动
        return getScrollX() != maxWidth;
    }

    @Override
    public int getNestedScrollAxes() {
        return 0;
    }

    /**
     * 限制滑动 移动x轴不能超出最大范围
     */
    @Override
    public void scrollTo(int x, int y) {
        if (x < 0) {
            x = 0;
        } else if (x > maxWidth * 2) {
            x = maxWidth * 2;
        }
        super.scrollTo(x, y);
    }

    public void setOnScrollListener(StickyScrollListener listener) {
        mListener = listener;
    }

    public void addHeaderView(View view) {
        if (view != null && view.getParent() == null) {
            mHeaderContainer.addView(view);
        }
    }

    public void addFooterView(View view) {
        if (view != null && view.getParent() == null) {
            mFooterContainer.addView(view);
        }
    }

    /**
     * 回弹动画
     */
    private class StickyAnimation extends Animation {

        private StickyAnimation() {
            isRunAnim = true;
            setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isRunAnim = false;
                    if (mListener != null) {
                        mListener.stickScrollEnd();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            setDuration(150);
            setInterpolator(new AccelerateInterpolator());
            if (mListener != null) {
                mListener.stickScrollStart();
            }
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            scrollBy((int) ((maxWidth - getScrollX()) * interpolatedTime), 0);
        }
    }

    public interface StickyScrollListener {

        void leftScroll(int dx);

        void rightScroll(int dx);

        void stickScrollStart();

        void stickScrollEnd();
    }

    private int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
