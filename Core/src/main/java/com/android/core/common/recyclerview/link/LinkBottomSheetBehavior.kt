package com.android.core.common.recyclerview.link

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.forEach
import androidx.customview.widget.ViewDragHelper
import com.google.android.material.R
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs
import kotlin.math.max

/** The bottom sheet is dragging. */
const val STATE_DRAGGING = 1

/** The bottom sheet is settling. */
const val STATE_SETTLING = 2

/** The bottom sheet is expanded. */
const val STATE_EXPANDED = 3

/** The bottom sheet is collapsed. */
const val STATE_COLLAPSED = 4

/** The bottom sheet is hidden. */
const val STATE_HIDDEN = 5

/** The bottom sheet is half-expanded (used when mFitToContents is false). */
const val STATE_HALF_EXPANDED = 6

/** Peek at the 16:9 ratio keyline of its parent. */
const val PEEK_HEIGHT_AUTO = -1

/** This flag will preserve the peekHeight int value on configuration change. */
const val SAVE_PEEK_HEIGHT = 0x1

/** This flag will preserve the fitToContents boolean value on configuration change. */
const val SAVE_FIT_TO_CONTENTS = 1 shl 1

/** This flag will preserve the hideable boolean value on configuration change. */
const val SAVE_HIDEABLE = 1 shl 2

/** This flag will preserve the skipCollapsed boolean value on configuration change. */
const val SAVE_SKIP_COLLAPSED = 1 shl 3

/** This flag will preserve all aforementioned values on configuration change. */
const val SAVE_ALL = -1

/** This flag will not preserve the aforementioned values set at runtime if the view is destroyed and recreated. */
const val SAVE_NONE = 0

private const val HIDE_THRESHOLD = 0.5f
private const val HIDE_FRICTION = 0.1f

/**
 * 具有和底层RecyclerView联动效果的Behavior
 * 参考案例：https://github.com/liuyak/NestedBehaviorDemo
 */
class LinkBottomSheetBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    /**
     * Callback for monitoring events about bottom sheets.
     */
    abstract class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState The new state.
         */
        abstract fun onStateChanged(bottomSheet: View, @State newState: Int)

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset increases
         * as this bottom sheet is moving upward. From 0 to 1 the sheet is between collapsed and
         * expanded states and from -1 to 0 it is between hidden and collapsed states.
         */
        abstract fun onSlide(bottomSheet: View, slideOffset: Float, slideByOutsideView: Boolean)
    }

    private var behaviorCallback: BottomSheetCallback? = null

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(
        STATE_EXPANDED,
        STATE_COLLAPSED,
        STATE_DRAGGING,
        STATE_SETTLING,
        STATE_HIDDEN,
        STATE_HALF_EXPANDED
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class State

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(
        flag = true,
        value = [SAVE_PEEK_HEIGHT, SAVE_FIT_TO_CONTENTS, SAVE_HIDEABLE, SAVE_SKIP_COLLAPSED, SAVE_ALL, SAVE_NONE]
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class SaveFlags

    @SaveFlags
    private var saveFlags = SAVE_NONE
    private var fitToContents = true
    private var maximumVelocity = 0f

    /** Peek height set by the user */
    private var peekHeight = 0

    /** Whether or not to use automatic peek height */
    private var peekHeightAuto = false

    /** Minimum peek height permitted */
    private var peekHeightMin = 0

    private var settleRunnable: SettleRunnable? = null
    private var flingRunnable: FlingRunnable? = null
    private var nestedScrollFlingRunnable: NestedScrollFlingRunnable? = null

    var expandedOffset = 0
    var fitToContentsOffset = 0
    var halfExpandedOffset = 0
    var halfExpandedRatio = 0.5f
    var collapsedOffset = 0
    var hideable = false
    private var skipCollapsed = false

    @State
    var state = STATE_COLLAPSED
    var viewDragHelper: ViewDragHelper? = null
    private var ignoreEvents = false
    private var lastNestedScrollDy = 0
    private var nestedScrolled = false
    var parentWidth = 0
    var parentHeight = 0
    var viewRef: WeakReference<V>? = null
    var nestedScrollingChildRef: WeakReference<View?>? = null
    private var velocityTracker: VelocityTracker? = null
    var activePointerId = 0
    private var initialY = 0
    var touchingScrollingChild = false
    private var slideByOutsideView = false
    private var snbNestedRecycleView: LinkRecycleView? = null
    private var slideOffset = 0
    private var overScroller: OverScroller? = null
    private var importantForAccessibilityMap: MutableMap<View, Int>? = null

    constructor()
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        overScroller = OverScroller(context)
        maximumVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()
    }

    fun setSlideByOutsideView(slideByOutsideView: Boolean) {
        this.slideByOutsideView = slideByOutsideView
        if (!slideByOutsideView) {
            updateState(STATE_COLLAPSED)
        }
    }

    fun setOutsideView(outsideView: LinkRecycleView) {
        snbNestedRecycleView = outsideView
    }

    fun setBehaviorCallback(callback: BottomSheetCallback) {
        this.behaviorCallback = callback
    }

    override fun onAttachedToLayoutParams(layoutParams: CoordinatorLayout.LayoutParams) {
        super.onAttachedToLayoutParams(layoutParams)
        // These may already be null, but just be safe, explicitly assign them. This lets us know the
        // first time we layout with this behavior by checking (viewRef == null).
        viewRef = null
        viewDragHelper = null
    }

    override fun onDetachedFromLayoutParams() {
        super.onDetachedFromLayoutParams()
        // Release references so we don't run unnecessary codepaths while not attached to a view.
        viewRef = null
        viewDragHelper = null
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            child.fitsSystemWindows = true
        }
        if (viewRef == null) {
            // First layout with this behavior.
            peekHeightMin =
                parent.resources.getDimensionPixelSize(R.dimen.design_bottom_sheet_peek_height_min)
            viewRef = WeakReference(child)
            updateAccessibilityActions()
            if (ViewCompat.getImportantForAccessibility(child) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                ViewCompat.setImportantForAccessibility(
                    child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
                )
            }
        }
        if (viewDragHelper == null) {
            viewDragHelper = ViewDragHelper.create(parent, dragCallback)
        }

        val savedTop = child.top
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection)
        // Offset the bottom sheet
        parentWidth = parent.width
        parentHeight = parent.height
        fitToContentsOffset = max(0, parentHeight - child.height)
        calculateHalfExpandedOffset()
        calculateCollapsedOffset()

        if (slideByOutsideView) {
            ViewCompat.offsetTopAndBottom(child, slideOffset)
            dispatchOnSlide(slideOffset)
        } else {
            if (state == STATE_EXPANDED) {
                ViewCompat.offsetTopAndBottom(child, getExpandOffset())
            } else if (state == STATE_HALF_EXPANDED) {
                ViewCompat.offsetTopAndBottom(child, halfExpandedOffset)
            } else if (hideable && state == STATE_HIDDEN) {
                ViewCompat.offsetTopAndBottom(child, parentHeight)
            } else if (state == STATE_COLLAPSED) {
                ViewCompat.offsetTopAndBottom(child, collapsedOffset)
            } else if (state == STATE_DRAGGING || state == STATE_SETTLING) {
                ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
            }
        }
        nestedScrollingChildRef = WeakReference(findScrollingChild(child))
        return true
    }

    private fun scrollOutsideView(dy: Int) {
        if (snbNestedRecycleView != null && slideByOutsideView) {
            snbNestedRecycleView?.scrollByOutsideView = true
            snbNestedRecycleView?.scrollBy(0, dy)
        }
    }

    fun setSlideHeight(slideHeight: Int) {
        var tempSlideHeight = slideHeight
        when {
            tempSlideHeight <= peekHeight -> {
                tempSlideHeight = peekHeight
                setStateInternal(STATE_COLLAPSED)
            }
            tempSlideHeight >= parentHeight -> {
                tempSlideHeight = parentHeight
                setStateInternal(STATE_EXPANDED)
            }
            else -> {
                setStateInternal(STATE_DRAGGING)
            }
        }

        slideOffset = parentHeight - tempSlideHeight
        viewRef?.get()?.requestLayout()
    }

    fun getSlideHeight(): Int {
        return parentHeight - slideOffset
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        if (!child.isShown) {
            ignoreEvents = true
            return false
        }
        val action = event.actionMasked
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchingScrollingChild = false
                activePointerId = MotionEvent.INVALID_POINTER_ID
                // Reset the ignore flag
                if (ignoreEvents) {
                    ignoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                // bugFix，修复华为机型上下抖动问题--start
                if (nestedScrollFling) {
                    overScroller?.abortAnimation()
                    nestedScrollFling = false
                }
                snbNestedRecycleView?.stopScroll()
                // bugFix，修复华为机型上下抖动问题--end
                val initialX = event.x.toInt()
                initialY = event.y.toInt()
                // Only intercept nested scrolling events here if the view not being moved by the
                // ViewDragHelper.
                if (state != STATE_SETTLING) {
                    val scroll = nestedScrollingChildRef?.get()
                    if (scroll != null && parent.isPointInChildBounds(scroll, initialX, initialY)) {
                        activePointerId = event.getPointerId(event.actionIndex)
                        touchingScrollingChild = true
                    }
                }
                ignoreEvents =
                    activePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(
                        child,
                        initialX,
                        initialY
                    )
            }
        }
        if (!ignoreEvents && viewDragHelper?.shouldInterceptTouchEvent(event) == true) {
            return true
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        val scroll = nestedScrollingChildRef?.get()
        return action == MotionEvent.ACTION_MOVE
                && scroll != null
                && !ignoreEvents
                && state != STATE_DRAGGING
                && !parent.isPointInChildBounds(scroll, event.x.toInt(), event.y.toInt())
                && viewDragHelper != null
                && abs(initialY - event.y) > viewDragHelper!!.touchSlop
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            return false
        }
        val action = event.actionMasked
        if (!slideByOutsideView && state == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }
        viewDragHelper?.processTouchEvent(event)
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !ignoreEvents) {
            if (abs(initialY - event.y) > (viewDragHelper?.touchSlop ?: 0)) {
                // 开始拖拽
                viewDragHelper?.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !ignoreEvents
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        lastNestedScrollDy = 0
        nestedScrolled = false
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            // Ignore fling here. The ViewDragHelper handles it.
            return
        }
        if (target != nestedScrollingChildRef?.get()) {
            return
        }

        val currentTop = child.top
        val newTop = currentTop - dy
        if (dy > 0) { // Upward
            if (newTop < getExpandOffset()) {
                consumed[1] = currentTop - getExpandOffset()
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                scrollOutsideView(consumed[1])
                setStateInternal(STATE_EXPANDED)
            } else {
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                scrollOutsideView(dy)
                setStateInternal(STATE_DRAGGING)
            }
        } else if (dy < 0) { // Downward
            if (!target.canScrollVertically(-1) || slideByOutsideView && state == STATE_DRAGGING) {
                if (newTop <= collapsedOffset || hideable) {
                    consumed[1] = dy
                    ViewCompat.offsetTopAndBottom(child, -dy)
                    scrollOutsideView(dy)
                    setStateInternal(STATE_DRAGGING)
                } else {
                    consumed[1] = currentTop - collapsedOffset
                    ViewCompat.offsetTopAndBottom(child, -consumed[1])
                    scrollOutsideView(consumed[1])
                    setStateInternal(STATE_COLLAPSED)
                }
            }
        }

        dispatchOnSlide(child.top)
        lastNestedScrollDy = dy
        nestedScrolled = true
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        type: Int
    ) {
        if (slideByOutsideView) {
            return
        }
        if (child.top == getExpandOffset()) {
            setStateInternal(STATE_EXPANDED)
            return
        }
        if (nestedScrollingChildRef == null || target != nestedScrollingChildRef?.get() || !nestedScrolled) {
            return
        }

        val top: Int
        val targetState: Int
        if (lastNestedScrollDy > 0) {
            top = getExpandOffset()
            targetState = STATE_EXPANDED
        } else if (hideable && shouldHide(child, getYVelocity())) {
            top = parentHeight
            targetState = STATE_HIDDEN
        } else if (lastNestedScrollDy == 0) {
            val currentTop = child.top
            if (fitToContents) {
                if (abs(currentTop - fitToContentsOffset) < abs(currentTop - collapsedOffset)) {
                    top = fitToContentsOffset
                    targetState = STATE_EXPANDED
                } else {
                    top = collapsedOffset
                    targetState = STATE_COLLAPSED
                }
            } else {
                if (currentTop < halfExpandedOffset) {
                    if (currentTop < abs(currentTop - collapsedOffset)) {
                        top = expandedOffset
                        targetState = STATE_EXPANDED
                    } else {
                        top = halfExpandedOffset
                        targetState = STATE_HALF_EXPANDED
                    }
                } else {
                    if (abs(currentTop - halfExpandedOffset) < abs(currentTop - collapsedOffset)) {
                        top = halfExpandedOffset
                        targetState = STATE_HALF_EXPANDED
                    } else {
                        top = collapsedOffset
                        targetState = STATE_COLLAPSED
                    }
                }
            }
        } else {
            if (fitToContents) {
                top = collapsedOffset
                targetState = STATE_COLLAPSED
            } else {
                // Settle to nearest height.
                val currentTop = child.top
                if (abs(currentTop - halfExpandedOffset) < abs(currentTop - collapsedOffset)) {
                    top = halfExpandedOffset
                    targetState = STATE_HALF_EXPANDED
                } else {
                    top = collapsedOffset
                    targetState = STATE_COLLAPSED
                }
            }
        }
        startSettlingAnimation(child, targetState, top, false)
        nestedScrolled = false
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        // Overridden to prevent the default consumption of the entire scroll distance.
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (slideByOutsideView && state != STATE_EXPANDED) {
            startNestedScrollFling(child, velocityY)
            return true
        }
        return if (nestedScrollingChildRef != null) {
            target == nestedScrollingChildRef?.get() && (state != STATE_EXPANDED || super.onNestedPreFling(
                coordinatorLayout,
                child,
                target,
                velocityX,
                velocityY
            ))
        } else {
            false
        }
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels, or [PEEK_HEIGHT_AUTO] to configure the sheet to peek automatically at 16:9 ratio keyline.
     * @attr ref
     * com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    fun setPeekHeight(peekHeight: Int) {
        slideOffset = parentHeight - peekHeight
        setPeekHeight(peekHeight, false)
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed while optionally animating between the
     * old height and the new height.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels, or [PEEK_HEIGHT_AUTO] to configure the sheet to peek automatically at 16:9 ratio keyline.
     * @param animate    Whether to animate between the old height and the new height.
     * @attr ref
     * com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    private fun setPeekHeight(peekHeight: Int, animate: Boolean) {
        var layout = false
        if (peekHeight == PEEK_HEIGHT_AUTO) {
            if (!peekHeightAuto) {
                peekHeightAuto = true
                layout = true
            }
        } else if (peekHeightAuto || this.peekHeight != peekHeight) {
            peekHeightAuto = false
            this.peekHeight = max(0, peekHeight)
            layout = true
        }
        // If sheet is already laid out, recalculate the collapsed offset based on new setting.
        // Otherwise, let onLayoutChild handle this later.
        if (layout && viewRef != null) {
            calculateCollapsedOffset()
            if (state == STATE_COLLAPSED) {
                val view = viewRef?.get()
                if (view != null) {
                    if (animate) {
                        settleToStatePendingLayout(state)
                    } else {
                        view.requestLayout()
                    }
                }
            }
        }
    }

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet in pixels, or [.PEEK_HEIGHT_AUTO] if the
     * sheet is configured to peek automatically at 16:9 ratio keyline
     * @attr ref
     * com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    fun getPeekHeight(): Int {
        return if (peekHeightAuto) PEEK_HEIGHT_AUTO else peekHeight
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of [.STATE_COLLAPSED], [.STATE_EXPANDED], [.STATE_HIDDEN],
     * or [.STATE_HALF_EXPANDED].
     */
    fun updateState(@State state: Int) {
        if (state == this.state) {
            return
        }
        if (viewRef == null) {
            // The view is not laid out yet; modify mState and let onLayoutChild handle it later
            if (state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_HALF_EXPANDED || hideable && state == STATE_HIDDEN) {
                this.state = state
            }
            return
        }
        settleToStatePendingLayout(state)
    }

    private fun settleToStatePendingLayout(@State state: Int) {
        val child = viewRef?.get() ?: return
        // Start the animation; wait until a pending layout if there is one.
        val parent = child.parent
        if (parent != null && parent.isLayoutRequested && ViewCompat.isAttachedToWindow(child)) {
            val finalState = state
            child.post { settleToState(child, finalState) }
        } else {
            settleToState(child, state)
        }
    }

    fun setStateInternal(@State state: Int) {
        if (this.state == state) {
            return
        }
        this.state = state

        val bottomSheet = viewRef?.get() ?: return
        if (state == STATE_HALF_EXPANDED || state == STATE_EXPANDED) {
            updateImportantForAccessibility(true)
        } else if (state == STATE_HIDDEN || state == STATE_COLLAPSED) {
            updateImportantForAccessibility(false)
        }
        // 状态改变回调
        behaviorCallback?.onStateChanged(bottomSheet, state)
        updateAccessibilityActions()
    }

    private fun calculateCollapsedOffset() {
        val peek: Int = if (peekHeightAuto) {
            max(peekHeightMin, parentHeight - parentWidth * 9 / 16)
        } else {
            peekHeight
        }

        collapsedOffset = if (fitToContents) {
            max(parentHeight - peek, fitToContentsOffset)
        } else {
            parentHeight - peek
        }
    }

    private fun calculateHalfExpandedOffset() {
        halfExpandedOffset = (parentHeight * (1 - halfExpandedRatio)).toInt()
    }

    private fun reset() {
        activePointerId = ViewDragHelper.INVALID_POINTER
        if (velocityTracker != null) {
            velocityTracker?.recycle()
            velocityTracker = null
        }
    }

    fun shouldHide(child: View, yvel: Float): Boolean {
        if (skipCollapsed) {
            return true
        }
        if (child.top < collapsedOffset) {
            // It should not hide, but collapse.
            return false
        }
        val newTop = child.top + yvel * HIDE_FRICTION
        return abs(newTop - collapsedOffset) / peekHeight.toFloat() > HIDE_THRESHOLD
    }

    @VisibleForTesting
    fun findScrollingChild(view: View): View? {
        if (ViewCompat.isNestedScrollingEnabled(view)) {
            return view
        }

        if (view is ViewGroup) {
            view.forEach {
                val scrollingChild = findScrollingChild(it)
                if (scrollingChild != null) {
                    return scrollingChild
                }
            }
        }
        return null
    }

    private fun getYVelocity(): Float {
        if (velocityTracker == null) {
            return 0f
        }
        velocityTracker?.computeCurrentVelocity(1000, maximumVelocity)
        return velocityTracker?.getYVelocity(activePointerId) ?: 0f
    }

    private fun getExpandOffset(): Int {
        return if (fitToContents) fitToContentsOffset else expandedOffset
    }

    private fun settleToState(child: View, state: Int) {
        var tempState = state
        var top: Int

        if (tempState == STATE_COLLAPSED) {
            top = collapsedOffset
        } else if (tempState == STATE_HALF_EXPANDED) {
            top = halfExpandedOffset
            if (fitToContents && top <= fitToContentsOffset) {
                // Skip to the expanded state if we would scroll past the height of the contents.
                tempState = STATE_EXPANDED
                top = fitToContentsOffset
            }
        } else if (tempState == STATE_EXPANDED) {
            top = getExpandOffset()
        } else if (hideable && tempState == STATE_HIDDEN) {
            top = parentHeight
        } else {
            throw IllegalArgumentException("Illegal state argument: $tempState")
        }
        startSettlingAnimation(child, tempState, top, false)
    }

    private fun startSettlingAnimation(
        child: View,
        state: Int,
        top: Int,
        settleFromViewDragHelper: Boolean
    ) {
        val startedSettling =
            if (settleFromViewDragHelper) viewDragHelper?.settleCapturedViewAt(child.left, top)
                ?: false
            else viewDragHelper?.smoothSlideViewTo(child, child.left, top) ?: false

        if (startedSettling) {
            setStateInternal(STATE_SETTLING)
            // STATE_SETTLING won't animate the material shape, so do that here with the target state.
            if (settleRunnable == null) {
                // If the singleton SettleRunnable instance has not been instantiated, create it.
                settleRunnable = SettleRunnable(child, state)
            }
            // If the SettleRunnable has not been posted, post it with the correct state.
            if (!settleRunnable!!.isPosted) {
                settleRunnable!!.targetState = state
                ViewCompat.postOnAnimation(child, settleRunnable)
                settleRunnable!!.isPosted = true
            } else {
                // Otherwise, if it has been posted, just update the target state.
                settleRunnable!!.targetState = state
            }
        } else {
            setStateInternal(state)
        }
    }

    private val dragCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            // 关联滚动的时候，拖动状态有动画
            if (state == STATE_DRAGGING && !slideByOutsideView) {
                return false
            }
            if (touchingScrollingChild) {
                return false
            }
            if (state == STATE_EXPANDED && activePointerId == pointerId) {
                val scroll = nestedScrollingChildRef?.get()
                if (scroll != null && scroll.canScrollVertically(-1)) {
                    // Let the content scroll up
                    return false
                }
            }
            return viewRef?.get() == child
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            dispatchOnSlide(top)
            if (slideByOutsideView) {
                scrollOutsideView(dy * -1)
            }
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (slideByOutsideView && state != STATE_EXPANDED) {
                handleViewDragHelperFling(releasedChild, yvel)
                return
            }

            val top: Int
            @State val targetState: Int
            if (yvel < 0) { // Moving up
                if (fitToContents) {
                    top = fitToContentsOffset
                    targetState = STATE_EXPANDED
                } else {
                    val currentTop = releasedChild.top
                    if (currentTop > halfExpandedOffset) {
                        top = halfExpandedOffset
                        targetState = STATE_HALF_EXPANDED
                    } else {
                        top = expandedOffset
                        targetState = STATE_EXPANDED
                    }
                }
            } else if (hideable && shouldHide(
                    releasedChild,
                    yvel
                ) && (releasedChild.top > collapsedOffset || abs(xvel) < abs(yvel))
            ) {
                // Hide if we shouldn't collapse and the view was either released low or it was a
                // vertical swipe.
                top = parentHeight
                targetState = STATE_HIDDEN
            } else if (yvel == 0f || abs(xvel) > abs(yvel)) {
                // If the Y velocity is 0 or the swipe was mostly horizontal indicated by the X velocity
                // being greater than the Y velocity, settle to the nearest correct height.
                val currentTop = releasedChild.top
                if (fitToContents) {
                    if (abs(currentTop - fitToContentsOffset) < abs(currentTop - collapsedOffset)) {
                        top = fitToContentsOffset
                        targetState = STATE_EXPANDED
                    } else {
                        top = collapsedOffset
                        targetState = STATE_COLLAPSED
                    }
                } else {
                    if (currentTop < halfExpandedOffset) {
                        if (currentTop < abs(currentTop - collapsedOffset)) {
                            top = expandedOffset
                            targetState = STATE_EXPANDED
                        } else {
                            top = halfExpandedOffset
                            targetState = STATE_HALF_EXPANDED
                        }
                    } else {
                        if (abs(currentTop - halfExpandedOffset) < abs(currentTop - collapsedOffset)) {
                            top = halfExpandedOffset
                            targetState = STATE_HALF_EXPANDED
                        } else {
                            top = collapsedOffset
                            targetState = STATE_COLLAPSED
                        }
                    }
                }
            } else { // Moving Down
                if (fitToContents) {
                    top = collapsedOffset
                    targetState = STATE_COLLAPSED
                } else {
                    // Settle to the nearest correct height.
                    val currentTop = releasedChild.top
                    if (abs(currentTop - halfExpandedOffset) < abs(currentTop - collapsedOffset)) {
                        top = halfExpandedOffset
                        targetState = STATE_HALF_EXPANDED
                    } else {
                        top = collapsedOffset
                        targetState = STATE_COLLAPSED
                    }
                }
            }
            startSettlingAnimation(releasedChild, targetState, top, true)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return MathUtils.clamp(
                top,
                getExpandOffset(),
                if (hideable) parentHeight else collapsedOffset
            )
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return if (hideable) {
                parentHeight
            } else {
                collapsedOffset
            }
        }
    }

    private fun dispatchOnSlide(top: Int) {
        if (slideByOutsideView) {
            slideOffset = top
        }

        val bottomSheet = viewRef?.get() ?: return
        if (behaviorCallback != null) {
            val slideOffset = if (top > collapsedOffset) {
                (collapsedOffset - top).toFloat() / (parentHeight - collapsedOffset)
            } else {
                (collapsedOffset - top).toFloat() / (collapsedOffset - getExpandOffset())
            }
            // 上滑回调
            behaviorCallback?.onSlide(bottomSheet, slideOffset, slideByOutsideView)
        }
    }

    private fun handleViewDragHelperFling(child: View, velocityY: Float) {
        if (viewDragHelper != null) {
            if (velocityY < 0) {
                viewDragHelper?.flingCapturedView(0, 0, 0, collapsedOffset)
            } else {
                viewDragHelper?.flingCapturedView(
                    0,
                    parentHeight - collapsedOffset,
                    0,
                    parentHeight - peekHeight
                )
            }

            if (flingRunnable == null) {
                flingRunnable = FlingRunnable(child)
            }
            if (!flingRunnable!!.isPosted) {
                ViewCompat.postOnAnimation(child, flingRunnable)
                flingRunnable!!.isPosted = true
            }
        }
    }

    private var nestedScrollFling = false
    private fun startNestedScrollFling(child: View, velocityY: Float) {
        var tempVelocityY = velocityY
        tempVelocityY *= -1 // 跟ViewDragHelper里面的velocityY正负值相反
        if (tempVelocityY < 0) {
            overScroller?.fling(
                child.left,
                child.top,
                0,
                tempVelocityY.toInt(),
                0,
                0,
                0,
                collapsedOffset
            )
        } else {
            overScroller?.fling(
                child.left,
                child.top,
                0,
                tempVelocityY.toInt(),
                0,
                0,
                parentHeight - collapsedOffset,
                parentHeight - peekHeight
            )
        }

        nestedScrollFling = true
        if (nestedScrollFlingRunnable == null) {
            nestedScrollFlingRunnable = NestedScrollFlingRunnable(child)
        }
        if (!nestedScrollFlingRunnable!!.isPosted) {
            ViewCompat.postOnAnimation(child, nestedScrollFlingRunnable)
            nestedScrollFlingRunnable!!.isPosted = true
        }
    }

    private inner class NestedScrollFlingRunnable : Runnable {

        private val view: View

        var isPosted = false

        constructor(view: View) {
            this.view = view
        }

        override fun run() {
            if (continueNestedScrollFling(view)) {
                ViewCompat.postOnAnimation(view, this)
            }
            isPosted = false
        }
    }

    private fun continueNestedScrollFling(view: View): Boolean {
        if (nestedScrollFling && overScroller != null) {
            var keepGoing = overScroller!!.computeScrollOffset()
            val x = overScroller!!.currX
            val y = overScroller!!.currY
            val dx = x - view.left
            val dy = y - view.top

            if (dx != 0) {
                ViewCompat.offsetLeftAndRight(view, dx)
            }
            if (dy != 0) {
                ViewCompat.offsetTopAndBottom(view, dy)
            }
            if (dx != 0 || dy != 0) {
                scrollOutsideView(dy * -1)
                dispatchOnSlide(y)
            }

            if (keepGoing && x == overScroller!!.finalX && y == overScroller!!.finalY) {
                // Close enough. The interpolator/scroller might think we're still moving
                // but the user sure doesn't.
                overScroller!!.abortAnimation()
                keepGoing = false
            }

            if (!keepGoing) {
                nestedScrollFling = false
            }
        }
        return nestedScrollFling
    }

    private inner class FlingRunnable : Runnable {

        private val view: View

        var isPosted = false

        constructor(view: View) {
            this.view = view
        }

        override fun run() {
            if (viewDragHelper?.continueSettling(true) == true) {
                ViewCompat.postOnAnimation(view, this)
            }
            isPosted = false
        }
    }

    private inner class SettleRunnable : Runnable {

        private val view: View

        var isPosted = false

        @State
        var targetState = 0

        constructor(view: View, @State targetState: Int) {
            this.view = view
            this.targetState = targetState
        }

        override fun run() {
            if (viewDragHelper?.continueSettling(true) == true) {
                ViewCompat.postOnAnimation(view, this)
            } else {
                setStateInternal(targetState)
            }
            isPosted = false
        }
    }

    private fun updateImportantForAccessibility(expanded: Boolean) {
        val viewParent = viewRef?.get()?.parent as? CoordinatorLayout ?: return

        val childCount: Int = viewParent.childCount
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN && expanded) {
            if (importantForAccessibilityMap == null) {
                importantForAccessibilityMap = HashMap<View, Int>(childCount)
            } else {
                // The important for accessibility values of the child views have been saved already.
                return
            }
        }

        for (i in 0 until childCount) {
            val child: View = viewParent.getChildAt(i)
            if (child == viewRef?.get()) {
                continue
            }

            if (!expanded) {
                if (importantForAccessibilityMap?.containsKey(child) == true) {
                    // Restores the original important for accessibility value of the child view.
                    val mode = importantForAccessibilityMap?.get(child)
                        ?: ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO
                    ViewCompat.setImportantForAccessibility(child, mode)
                }
            } else {
                // Saves the important for accessibility value of the child view.
                if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                    importantForAccessibilityMap?.put(child, child.importantForAccessibility)
                }
                ViewCompat.setImportantForAccessibility(
                    child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                )
            }
        }

        if (!expanded) {
            importantForAccessibilityMap = null
        }
    }

    private fun updateAccessibilityActions() {
        val child = viewRef?.get() ?: return

        ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_COLLAPSE)
        ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_EXPAND)
        ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_DISMISS)

        if (hideable && state != STATE_HIDDEN) {
            addAccessibilityActionForState(
                child,
                AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_DISMISS,
                STATE_HIDDEN
            )
        }

        when (state) {
            STATE_EXPANDED -> {
                val nextState = if (fitToContents) STATE_COLLAPSED else STATE_HALF_EXPANDED
                addAccessibilityActionForState(
                    child,
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE,
                    nextState
                )
            }
            STATE_HALF_EXPANDED -> {
                addAccessibilityActionForState(
                    child,
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE,
                    STATE_COLLAPSED
                )
                addAccessibilityActionForState(
                    child,
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND,
                    STATE_EXPANDED
                )
            }
            STATE_COLLAPSED -> {
                val nextState = if (fitToContents) STATE_EXPANDED else STATE_HALF_EXPANDED
                addAccessibilityActionForState(
                    child,
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND,
                    nextState
                )
            }
        }
    }

    private fun addAccessibilityActionForState(
        child: V,
        action: AccessibilityNodeInfoCompat.AccessibilityActionCompat,
        state: Int
    ) {
        ViewCompat.replaceAccessibilityAction(child, action, null) { _, _ ->
            updateState(state)
            true
        }
    }

}

/**
 * 获取LinkBottomSheetBehavior
 */
fun obtainLinkBehaviorFromView(view: View): LinkBottomSheetBehavior<View> {
    val params = view.layoutParams
    if (params !is CoordinatorLayout.LayoutParams) {
        throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
    }

    val behavior = params.behavior
    if (behavior !is LinkBottomSheetBehavior) {
        throw IllegalArgumentException("The view is not associated with LinkBottomSheetBehavior")
    }
    return behavior
}

