package com.android.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class OvalMenuLayout extends FrameLayout {
    private static final double OVAL_A = 440;

    private static final double OVAL_B = 240;

    private int mStartAngle = 270;
    private int mRadiusX;

    private int mRadiusY;
    /**
     * 该容器内child item的默认尺寸
     */
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 3f;

    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private float mPadding;

    /**
     * 每个菜单的间隔角度
     */
    private float angleDelay;
    private AbstractMenuAdapter mMenuAdapter;
    private float mTmpAngle;
    private long mDownTime;

    public OvalMenuLayout(Context context) {
        this(context, null);
    }

    public OvalMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OvalMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPadding(0, 0, 0, 0);
        setClickable(true);
        setClipChildren(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth;
        int resHeight;

        /*
         * 根据传入的参数，分别获取测量模式和测量值
         */
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);

        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        /*
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != View.MeasureSpec.EXACTLY
                || heightMode != View.MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            // 如果都设置为精确值，则直接取小值；
            resWidth = width;
            resHeight = height;
        }

        setMeasuredDimension(resWidth, resHeight);

        // 获得半径
        mRadiusX = getMeasuredWidth();

        mRadiusY = getMeasuredHeight();

        // menu item数量
        final int count = getChildCount();
        // menu item尺寸
        int childSize = (int) (Math.min(mRadiusX, mRadiusY) * RADIO_DEFAULT_CHILD_DIMENSION);
        // menu item测量模式
        int childMode = MeasureSpec.EXACTLY;

        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeasureSpec = -1;

            makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
                    childMode);

            child.measure(makeMeasureSpec, makeMeasureSpec);
        }
        mPadding = RADIO_PADDING_LAYOUT * mRadiusX;
    }

    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutRadius = Math.min(mRadiusX, mRadiusY);

        // Laying out the child views
        final int childCount = getChildCount();

        int left, top;
        // menu item 的尺寸
        int cWidth = (int) (layoutRadius * RADIO_DEFAULT_CHILD_DIMENSION);

        // 根据menu item的个数，计算角度
        angleDelay = 360f / childCount;

        // 遍历去设置menuitem的位置
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            mStartAngle %= 360;

            // tmp cosa 即menu item中心点的横坐标
            left = (int) (mRadiusX / 2 + Math.ceil(getXInOval(mStartAngle)) - 1 / 2f * cWidth);
            // tmp sina 即menu item的纵坐标
            top = (int) (mRadiusY / 2 - Math.ceil(getYInOval(mStartAngle)) - 1 / 2f * cWidth);

            child.layout(left, top, left + cWidth, top + cWidth);

            if (mMenuAdapter != null) {
                mMenuAdapter.upDateView(child, mStartAngle, i);
            }
            // 叠加尺寸
            mStartAngle += angleDelay;
        }
    }

    private double getYInOval(double degress) {
        double a = OVAL_A;
        double b = OVAL_B;
        double y = (a * b) / (Math.sqrt((Math.pow(b, 2)
                * Math.pow(Math.tan(Math.toRadians(degress)), 2) + Math.pow(a, 2)
        )));
        if (degress > 90 && degress < 270) {
            y = -y;
        }
        return y;
    }

    private double getXInOval(double degress) {
        double a = OVAL_A;
        double b = OVAL_B;
        double x = (a * b) / (Math.sqrt((Math.pow(a, 2)
                / Math.pow(Math.tan(Math.toRadians(degress)), 2) + Math.pow(b, 2))));
        if (degress < 360 && degress > 180) {
            x = -x;
        }
        return x;
    }


    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private static final int FLINGABLE_VALUE = 300;

    /**
     * 如果移动角度达到该值，则屏蔽点击
     */
    private static final int NOCLICK_VALUE = 3;

    private float mLastX, mLastY;
    private Runnable mRunnable;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        /*
         * 当每秒移动角度达到该值时，认为是快速移动
         */
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();

                removeCallbacks(mRunnable);

                break;
            case MotionEvent.ACTION_MOVE:

                /*
                 * 获得开始的角度
                 */
                float start = getAngle(mLastX, mLastY);
                /*
                 * 获得当前的角度
                 */
                float end = getAngle(x, y);

                // 如果是一、四象限，则直接end-start，角度值都是正值
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mStartAngle += end - start;
                    mTmpAngle += end - start;
                } else { // 二、三象限，色角度值是付值
                    mStartAngle += start - end;
                    mTmpAngle += start - end;
                }
                // 重新布局
                requestLayout();

                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:

                // 计算，每秒移动的角度
                float anglePerSecond = mTmpAngle * 1000
                        / (System.currentTimeMillis() - mDownTime);


                if (Math.abs(anglePerSecond) > FLINGABLE_VALUE) {
                    // // TODO: 2018/9/18  快速滚动 post一个任务，不断减速滚动到指定位置
                    Log.d("LyjLog", "  快速滚动:" + anglePerSecond);
                    post(mRunnable = new AutoFlingRunnable(anglePerSecond));
                    return true;
                } else {
                    Log.d("LyjLog", "  缓慢滚动:" + anglePerSecond);
                    // // TODO: 2018/9/18  缓慢滚动 去自动滚动到指定位置
                    //                    mRunnable = new AutoFlingRunnable(mStartAngle);
                    //                    post(mRunnable);
                    rollToNearPosition(mStartAngle, anglePerSecond > 0);
                }

                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) {
                    return true;
                }

                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private float getAngle(float x, float y) {
        double tmpx = x - mRadiusX / 2d;
        double tmpy = y - mRadiusY / 2d;

        return (float) (Math.asin(tmpy / Math.hypot(tmpx, tmpy)) * 180 / Math.PI);
    }

    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - mRadiusX / 2);
        int tmpY = (int) (y - mRadiusY / 2);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }
    }

    private void rollToNearPosition(double velocity, boolean upOrDown) {

        mStartAngle %= 360;
        Log.d("LyjLog", "  rollToNearPosition: mStartAngle= " + mStartAngle);
        float targetAngle = getNearAngle(velocity, upOrDown);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat((float) mStartAngle, targetAngle);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            float d = (float) animation.getAnimatedValue();
            mStartAngle = (int) d;
            requestLayout();
            Log.d("LyjLog", "  rollToNearPosition: targetAngle= " + mStartAngle);
        });
        valueAnimator.start();
    }

    private float getNearAngle(double velocity, boolean upOrDown) {
        velocity %= 360;
        float per = 1 / 3;
        float targetAngle = (float) mStartAngle;
        float[] childPositions = new float[getChildCount()];
        for (int i = 0; i < childPositions.length; i++) {
            childPositions[i] = i * angleDelay;
        }
        if (upOrDown) {
            //向前旋转
            if (velocity > childPositions[childPositions.length - 1] + angleDelay * per) {
                targetAngle = 360;
            } else {
                for (int i = 0; i < childPositions.length; i++) {
                    if (velocity < childPositions[i] + angleDelay * per) {
                        targetAngle = childPositions[i];
                        break;
                    }
                }
            }
        } else {
            //向后旋转
            if (velocity < childPositions[0] + angleDelay * (1 - per)) {
                targetAngle = 0;
            } else {
                for (int i = childPositions.length - 1; i >= 0; i--) {
                    if (velocity > childPositions[i] + angleDelay * (1 - per)) {
                        targetAngle = childPositions[i] + angleDelay;
                        break;
                    }
                }
            }
        }
        return targetAngle;
    }

    private class AutoFlingRunnable implements Runnable {
        private double angelPerSecond;
        /**
         * 默认每秒旋转角度
         */
        private final double DEFAULT_ANGLE = 80f;
        /**
         * 刷新间隔，一秒60帧
         */
        private final long DURATION = 1000 / 70;
        /**
         * 加速度
         */
        private final float DECELERATION = 3;


        public AutoFlingRunnable(float velocity) {
            this.angelPerSecond = velocity;

        }

        @Override
        public void run() {

            if (Math.abs(angelPerSecond) < DEFAULT_ANGLE) {
                rollToNearPosition(mStartAngle, angelPerSecond > 0);
                removeCallbacks(mRunnable);
                return;
            } else {
                //当前每秒旋转角度大于默认旋转角度
                mStartAngle += angelPerSecond / DURATION;
                if (angelPerSecond > 0) {
                    angelPerSecond -= DECELERATION;
                } else {
                    angelPerSecond += DECELERATION;
                }
            }
            Log.d("LyjLog", "angelPerSecond:" + angelPerSecond + "  mStartAngle:" + mStartAngle);
            requestLayout();
            postDelayed(this, DURATION);
        }
    }


    public void setMenuAdapter(AbstractMenuAdapter menuAdapter) {
        this.mMenuAdapter = menuAdapter;
        if (menuAdapter != null) {
            addMenuItems();
        }
    }

    private void addMenuItems() {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        /*
         * 菜单的个数
         */
        int mMenuItemCount = mMenuAdapter.getCount();
        /*
         * 根据用户设置的参数，初始化view
         */
        for (int i = 0; i < mMenuItemCount; i++) {
            View v = mMenuAdapter.onCreateView(mInflater, this);

            mMenuAdapter.onViewBinder(v, i);

            // 添加view到容器中
            addView(v);
        }
    }
}
