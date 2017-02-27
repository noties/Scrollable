package ru.noties.scrollable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import ru.noties.debug.AndroidLogDebugOutput;
import ru.noties.debug.Debug;

/**
 * <p>
 * This is the main {@link android.view.ViewGroup} for implementing Scrollable.
 * It has the same as {@link android.widget.FrameLayout#onMeasure(int, int)} measure logic,
 * but has it's own {@link #onLayout(boolean, int, int, int, int)} logic.
 * </p>
 * <p>
 * Note, that this ViewGroup will layout it's children as if it were an ordinary {@link android.widget.LinearLayout}
 * with orientation set to {@link android.widget.LinearLayout#VERTICAL}.
 * No paddings or margins will affect the layout position of children,

 * although margins will certainly affect measurements.
 * </p>
 * <p>
 * The best usage would be to include two {@link android.view.View} views.
 * The first one would represent the <code>header</code> logic and the second would be scrollable container.
 * Note, that we should make use of {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}
 * as the height attribute for scrollable container. Because it directly affects the scrollable behavior.
 * If you wish to create a <code>sticky</code> effect for one of the views in <code>header</code> ViewGroup,
 * you could specify <code>layout_marginTop</code> attribute for your scrollable layout,
 * which represents the height of your sticky element.
 * </p>
 * <p>
 * The logic behind scenes is simple. We should be able to encapsulate scrollable logic in a way (in a <code>tabs</code> case),
 * that saves us from adding header placeholders and footers (so that scrollY does
 * not change when different tab is selected) to every {@link android.widget.ScrollView}, {@link android.widget.AbsListView} etc.
 * So, instead of modifying scrolling behavior of each scrollable View, we are creating our own
 * ViewGroup that handles it for us.
 * </p>
 * <p>
 * Follow these steps to create your own Scrollable layout:
 * </p>
 *
 * <b>Simple case</b>
 * <pre>
 * {@code
 *     <ru.noties.scrollable.ScrollableLayout
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          app:scrollable_maxScroll="@dimen/header_height"> <!-- (!) -->
 *
 *          <View
 *              android:layout_width="match_parent"
 *              android:layout_height="@dimen/header_height" /> <!-- (!) -- >
 *
 *          <ListView
 *              android:layout_width="match_parent"
 *              android:layout_height="match_parent" />
 *
 *     </ru.noties.scrollable.ScrollableLayout>
 * }
 * </pre>
 *
 * <b>Sticky case</b>
 * (of cause it's just an xml step, you also should implement translation logic in OnScrollChangeListener
 * {@link #setOnScrollChangedListener(OnScrollChangedListener)})
 * <pre>
 *     {@code
 *     <ru.noties.scrollable.ScrollableLayout
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          app:scrollable_maxScroll="@dimen/header_height">
 *
 *          <LinearLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content">
 *
 *              <View
 *                  android:layout_width="match_parent"
 *                  android:layout_height="@dimen/header_height" />
 *
 *              <View
 *                  android:layout_width="match_parent"
 *                  android:layout_height="@dimen/sticky_height" /> <!-- (!) -->
 *
 *          </LinearLayout>
 *
 *          <ListView
 *              android:layout_width="match_parent"
 *              android:layout_height="match_parent"
 *              android:layout_marginTop="@dimen/sticky_height" /> <!-- (!) -->
 *     }
 * </pre>
 *
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 28.03.2015.
 */
public class ScrollableLayout extends FrameLayout {

    // todo
    static {
        Debug.init(new AndroidLogDebugOutput(true));
    }

    // todo, track created ValueAnimators and unsubscribe onDetach

    private static final long DEFAULT_IDLE_CLOSE_UP_ANIMATION = 200L;
    private static final int DEFAULT_CONSIDER_IDLE_MILLIS = 100;
    private static final float DEFAULT_FRICTION = .0565F;

    private final Rect mDraggableRect = new Rect();

    private final List<OnScrollChangedListener> mOnScrollChangedListeners = new ArrayList<>(3);

    private ScrollableScroller mScroller;
    private GestureDetector mScrollDetector;
    private GestureDetector mFlingDetector;

    private CanScrollVerticallyDelegate mCanScrollVerticallyDelegate;

    private int mMaxScrollY;

    private boolean mIsScrolling;
    private boolean mIsFlinging;

    private MotionEventHook mMotionEventHook;

    private CloseUpAlgorithm mCloseUpAlgorithm;
    private ObjectAnimator mCloseUpAnimator;

    private boolean mSelfUpdateScroll;
    private boolean mSelfUpdateFling;

    private boolean mIsTouchOngoing;

    private CloseUpIdleAnimationTime mCloseUpIdleAnimationTime;
    private CloseUpAnimatorConfigurator mCloseAnimatorConfigurator;

    private View mDraggableView;
    private boolean mIsDraggingDraggable;

    private long mConsiderIdleMillis;

    private boolean mEventRedirected;
    private float mEventRedirectStartedY;

    private float mScaledTouchSlop;

    private OnFlingOverListener mOnFlingOverListener;

    private boolean mAutoMaxScroll;
    private ViewTreeObserver.OnGlobalLayoutListener mAutoMaxScrollYLayoutListener;
    private int mAutoMaxScrollViewId;

    private boolean mOverScrollStarted;
    private OverScrollListener mOverScrollListener;

    private int mScrollingHeaderId;
    private View mScrollingHeader;

    public ScrollableLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ScrollableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScrollableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {

        final TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.ScrollableLayout);
        try {

            final boolean flyWheel = array.getBoolean(R.styleable.ScrollableLayout_scrollable_scrollerFlywheel, false);
            mScroller = initScroller(context, null, flyWheel);

            final float friction = array.getFloat(R.styleable.ScrollableLayout_scrollable_friction, DEFAULT_FRICTION);
            setFriction(friction);

            mMaxScrollY = array.getDimensionPixelSize(R.styleable.ScrollableLayout_scrollable_maxScroll, 0);
            mAutoMaxScroll = array.getBoolean(R.styleable.ScrollableLayout_scrollable_autoMaxScroll, mMaxScrollY == 0);
            mAutoMaxScrollViewId = array.getResourceId(R.styleable.ScrollableLayout_scrollable_autoMaxScrollViewId, 0);

            final long considerIdleMillis = array.getInteger(
                    R.styleable.ScrollableLayout_scrollable_considerIdleMillis,
                    DEFAULT_CONSIDER_IDLE_MILLIS
            );
            setConsiderIdleMillis(considerIdleMillis);

            final boolean useDefaultCloseUp = array.getBoolean(R.styleable.ScrollableLayout_scrollable_defaultCloseUp, false);
            if (useDefaultCloseUp) {
                setCloseUpAlgorithm(new DefaultCloseUpAlgorithm());
            }

            final int closeUpAnimationMillis = array.getInteger(R.styleable.ScrollableLayout_scrollable_closeUpAnimationMillis, -1);
            if (closeUpAnimationMillis != -1) {
                setCloseUpIdleAnimationTime(new SimpleCloseUpIdleAnimationTime(closeUpAnimationMillis));
            }

            final int interpolatorResId = array.getResourceId(R.styleable.ScrollableLayout_scrollable_closeUpAnimatorInterpolator, 0);
            if (interpolatorResId != 0) {
                final Interpolator interpolator = AnimationUtils.loadInterpolator(context, interpolatorResId);
                setCloseAnimatorConfigurator(new InterpolatorCloseUpAnimatorConfigurator(interpolator));
            }

            mScrollingHeaderId = array.getResourceId(R.styleable.ScrollableLayout_scrollable_scrollingHeaderId, 0);

        } finally {
            array.recycle();
        }

        mScrollDetector = new GestureDetector(context, new ScrollGestureListener());
        mFlingDetector  = new GestureDetector(context, new FlingGestureListener(context));

        mMotionEventHook = new MotionEventHook(new MotionEventHookCallback() {
            @Override
            public void apply(MotionEvent event) {
                ScrollableLayout.super.dispatchTouchEvent(event);
            }
        });

        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (mAutoMaxScroll) {
            processAutoMaxScroll(true);
        }

        final View scrollingHeader;
        if (mScrollingHeaderId != 0) {
            scrollingHeader = findViewById(mScrollingHeaderId);
        } else {
            if (getChildCount() > 0) {
                scrollingHeader = getChildAt(0);
            } else {
                scrollingHeader = null;
            }
        }
        mScrollingHeader = scrollingHeader;
    }

    /**
     * Override this method if you wish to create own {@link android.widget.Scroller}
     * @param context {@link android.content.Context}
     * @param interpolator {@link android.view.animation.Interpolator}, the default implementation passes <code>null</code>
     * @param flywheel {@link android.widget.Scroller#Scroller(android.content.Context, android.view.animation.Interpolator, boolean)}
     * @return new instance of {@link android.widget.Scroller} must not bu null
     */
    protected ScrollableScroller initScroller(Context context, Interpolator interpolator, boolean flywheel) {
        return new ScrollableScroller(context, interpolator, flywheel);
    }

    /**
     * Sets friction for current {@link android.widget.Scroller}
     * @see android.widget.Scroller#setFriction(float)
     * @param friction to be applied
     */
    public void setFriction(float friction) {
        mScroller.setFriction(friction);
    }

    /**
     * @see ru.noties.scrollable.CanScrollVerticallyDelegate
     * @param delegate which will be invoked when scroll state of scrollable children is needed
     */
    public void setCanScrollVerticallyDelegate(CanScrollVerticallyDelegate delegate) {
        this.mCanScrollVerticallyDelegate = delegate;
    }

    /**
     * Also can be set via xml attribute <code>scrollable_maxScroll</code>
     * @param maxY the max scroll y available for this View.
     * @see #getMaxScrollY()
     */
    public void setMaxScrollY(int maxY) {
        this.mMaxScrollY = maxY;
    }

    /**
     * @return value which represents the max scroll distance to <code>this</code> View (aka <code>header</code> height)
     * @see #setMaxScrollY(int)
     */
    public int getMaxScrollY() {
        return mMaxScrollY;
    }

    /**
     * Note that this value might be set with xml definition (<pre>{@code app:scrollable_considerIdleMillis="100"}</pre>)
     * @param millis millis after which current scroll
     *               state would be considered idle and thus firing close up logic if set
     * @see #getConsiderIdleMillis()
     * @see #DEFAULT_CONSIDER_IDLE_MILLIS
     */
    public void setConsiderIdleMillis(long millis) {
        mConsiderIdleMillis = millis;
    }

    /**
     * @return current value of millis after which scroll state would be considered idle
     * @see #setConsiderIdleMillis(long)
     */
    public long getConsiderIdleMillis() {
        return mConsiderIdleMillis;
    }

    /**
     * Pass an {@link ru.noties.scrollable.OnScrollChangedListener}
     * if you wish to get notifications when scroll state of <code>this</code> View has changed.
     * It\'s helpful for implementing own logic which depends on scroll state (e.g. parallax, alpha, etc)
     * @param listener to be invoked when {@link #onScrollChanged(int, int, int, int)} has been called.
     *                 Might be <code>null</code> if you don\'t want to receive scroll notifications anymore
     */
    @Deprecated
    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListeners.clear();
        addOnScrollChangedListener(listener);
    }

    public void addOnScrollChangedListener(OnScrollChangedListener listener) {
        if (listener != null) {
            mOnScrollChangedListeners.add(listener);
        }
    }

    public void removeOnScrollChangedListener(OnScrollChangedListener listener) {
        if (listener != null) {
            mOnScrollChangedListeners.remove(listener);
        }
    }

    public void setOnFlingOverListener(OnFlingOverListener onFlingOverListener) {
        this.mOnFlingOverListener = onFlingOverListener;
    }

    /**
     * @see android.view.View#onScrollChanged(int, int, int, int)
     * @see ru.noties.scrollable.OnScrollChangedListener#onScrollChanged(int, int, int)
     * @see CloseUpAlgorithm
     */
    @Override
    public void onScrollChanged(int l, int t, int oldL, int oldT) {

        final boolean changed = t != oldT;

        final int size = changed
                ? mOnScrollChangedListeners.size()
                : 0;

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mOnScrollChangedListeners.get(i).onScrollChanged(t, oldT, mMaxScrollY);
            }
        }

        if (mCloseUpAlgorithm != null) {
            removeCallbacks(mIdleRunnable);
            if (!mSelfUpdateScroll && changed && !mIsTouchOngoing) {
                postDelayed(mIdleRunnable, mConsiderIdleMillis);
            }
        }

        super.onScrollChanged(l, t, oldL, oldT);
    }

    /**
     * Call this method to enable/disable scrolling logic. If called with `value=false`
     * ScrollableLayout won't process any touch events
     * @param value indicating whether or not ScrollableLayout should process touch events
     */
    public void setSelfUpdateScroll(boolean value) {
        mSelfUpdateScroll = value;
    }

    /**
     * @see #setSelfUpdateScroll(boolean)
     * @return current value of `mSelfUpdateScroll`
     */
    public boolean isSelfUpdateScroll() {
        return mSelfUpdateScroll;
    }

    /**
     * Note that {@link DefaultCloseUpAlgorithm} might be set with
     * xml definition (<pre>{@code app:scrollable_defaultCloseUp="true"}</pre>)
     * @param closeUpAlgorithm {@link CloseUpAlgorithm} implementation, might be null
     * @see CloseUpAlgorithm
     * @see DefaultCloseUpAlgorithm
     */
    public void setCloseUpAlgorithm(CloseUpAlgorithm closeUpAlgorithm) {
        this.mCloseUpAlgorithm = closeUpAlgorithm;
    }

    /**
     * Note that {@link SimpleCloseUpIdleAnimationTime} might be set with xml definition
     * (<pre>{@code app:scrollable_closeUpAnimationMillis="200"}</pre>)
     * @param closeUpIdleAnimationTime {@link CloseUpIdleAnimationTime} implementation, might be null
     * @see CloseUpIdleAnimationTime
     * @see SimpleCloseUpIdleAnimationTime
     * @see #DEFAULT_IDLE_CLOSE_UP_ANIMATION
     */
    public void setCloseUpIdleAnimationTime(CloseUpIdleAnimationTime closeUpIdleAnimationTime) {
        this.mCloseUpIdleAnimationTime = closeUpIdleAnimationTime;
    }

    /**
     * @param configurator {@link CloseUpAnimatorConfigurator} implementation
     *                                                        to process current close up
     *                                                        {@link android.animation.ObjectAnimator}, might be null
     * @see CloseUpAnimatorConfigurator
     * @see android.animation.ObjectAnimator
     */
    public void setCloseAnimatorConfigurator(CloseUpAnimatorConfigurator configurator) {
        this.mCloseAnimatorConfigurator = configurator;
    }

    /**
     * Helper method to animate scroll state of ScrollableLayout.
     * Please note, that returned {@link ValueAnimator} is not fully configured -
     * it needs at least `duration` property.
     * Also, there is no checks if the current scrollY is equal to the requested one.
     * @param scrollY the final scroll y to animate to
     * @return {@link ValueAnimator} configured to animate scroll state
     */
    public ValueAnimator animateScroll(final int scrollY) {

        final int startY = getScrollY();
        final int diff = scrollY - startY;

        final ValueAnimator animator = ValueAnimator.ofFloat(.0F, 1.F);
        animator.setEvaluator(new FloatEvaluator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();
                scrollTo(0, (int) (startY + (diff * fraction) + .5F));
            }
        });
        animator.addListener(new SelfUpdateAnimationListener());
        animator.addListener(new CancelAnimatorOnDetachListener(this, animator));
        return animator;
    }

    /**
     * @see View#scrollTo(int, int)
     * @see #setCanScrollVerticallyDelegate(CanScrollVerticallyDelegate)
     * @see #setMaxScrollY(int)
     */
    @Override
    public void scrollTo(int x, int y) {

        final int newY = getNewY(y);

        if (newY < 0) {
            return;
        }

        super.scrollTo(0, newY);
    }

    /**
     * If set to true then ScrollableLayout will listen for global layout change of a view with
     * is passed through xml: scrollable_autoMaxScrollViewId OR first view in layout.
     * With this feature no need to specify `scrollable_maxScrollY` attribute
     * @param autoMaxScroll to listen for child view height and change mMaxScrollY accordingly
     */
    public void setAutoMaxScroll(boolean autoMaxScroll) {
        mAutoMaxScroll = autoMaxScroll;
        processAutoMaxScroll(mAutoMaxScroll);
    }

    /**
     * @see #setAutoMaxScroll(boolean)
     * @return `mAutoMaxScroll` value
     */
    public boolean isAutoMaxScroll() {
        return mAutoMaxScroll;
    }

    public void setOverScrollListener(OverScrollListener listener) {
        mOverScrollListener = listener;
    }

    protected void processAutoMaxScroll(boolean autoMaxScroll) {

        if (getChildCount() == 0) {
            return;
        }

        final View view;
        if (mAutoMaxScrollViewId != 0) {
            view = findViewById(mAutoMaxScrollViewId);
        } else {
            view = getChildAt(0);
        }

        if (view == null) {
            return;
        }

        if (!autoMaxScroll) {
            if (mAutoMaxScrollYLayoutListener != null) {
                ViewUtils.removeGlobalLayoutListener(view, mAutoMaxScrollYLayoutListener);
                mAutoMaxScrollYLayoutListener = null;
            }
        } else {
            // if it's not null, we have already set it
            if (mAutoMaxScrollYLayoutListener == null) {
                mAutoMaxScrollYLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mMaxScrollY = view.getMeasuredHeight();
                    }
                };
                view.getViewTreeObserver().addOnGlobalLayoutListener(mAutoMaxScrollYLayoutListener);
            }
        }
    }

    // we will override this method in order to function with SwipeRefreshLayout (and possible others)
    // also, just in case, we will check if we can scroll to bottom
    @Override
    public boolean canScrollVertically(int direction) {
        return (direction < 0 && getScrollY() > 0)
                || (direction > 0 && mCanScrollVerticallyDelegate.canScrollVertically(direction));
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    protected int getNewY(int y) {

        final int currentY = getScrollY();

        if (currentY == y) {
            return -1;
        }

        final int direction = y - currentY;
        final boolean isScrollingBottomTop = direction < 0;

//        Debug.i(isScrollingBottomTop, direction);

        if (mCanScrollVerticallyDelegate != null) {

            if (isScrollingBottomTop) {

                // if not dragging draggable then return, else do not return
                if (!mIsDraggingDraggable
                        && !mSelfUpdateScroll
                        && mCanScrollVerticallyDelegate.canScrollVertically(direction)) {
                    return -1;
                }
            } else {

                // we are adding support for the scrolling view in the `header` section (first view)
                // we just check if header can scroll in top-bottom direction (but only if we are not dragging draggable view)

//                Debug.i(mIsDraggingDraggable, mSelfUpdateScroll, canHeaderScroll(direction));
                // else check if we are at max scroll
                if ((!mIsDraggingDraggable && !mSelfUpdateScroll && canHeaderScroll(direction))
                        || (currentY == mMaxScrollY && !mCanScrollVerticallyDelegate.canScrollVertically(direction))) {
                    return -1;
                }
            }
        }

        if (y < 0) {
            y = 0;
        } else if (y > mMaxScrollY) {
            y = mMaxScrollY;
        }

        return y;
    }

    /**
     * Sets View which should be included in receiving scroll gestures.
     * Maybe be null
     * @param view you wish to include in scrolling gestures (aka tabs)
     */
    public void setDraggableView(View view) {
        mDraggableView = view;
    }

    @Override
    public boolean dispatchTouchEvent(@SuppressWarnings("NullableProblems") MotionEvent event) {

        if (mSelfUpdateScroll) {
            mIsTouchOngoing = false;
            mIsDraggingDraggable = false;
            mIsScrolling = false;
            mIsFlinging = false;
            mOverScrollStarted = false;
            removeCallbacks(mIdleRunnable);
            removeCallbacks(mScrollRunnable);
            return super.dispatchTouchEvent(event);
        }

        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {

            mIsTouchOngoing = true;
            mScroller.abortAnimation();

            if (mDraggableView != null && mDraggableView.getGlobalVisibleRect(mDraggableRect)) {
                final int x = (int) (event.getRawX() + .5F);
                final int y = (int) (event.getRawY() + .5F);
                mIsDraggingDraggable = mDraggableRect.contains(x, y);
            } else {
                mIsDraggingDraggable = false;
            }
        } else if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL){

            mIsTouchOngoing = false;

            if (mCloseUpAlgorithm != null) {
                removeCallbacks(mIdleRunnable);
                postDelayed(mIdleRunnable, mConsiderIdleMillis);
            }

            // great, now we are able to cancel ghost touch when up event Y == mMaxScrollY
            if (mEventRedirected) {
                if (action == MotionEvent.ACTION_UP) {
                    final float diff = Math.abs(event.getRawY() - mEventRedirectStartedY);
                    if (Float.compare(diff, mScaledTouchSlop) < 0) {
                        event.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }
                mEventRedirected = false;
            }

            cancelOverScroll();
        }

        final boolean isPrevScrolling = mIsScrolling;
        final boolean isPrevFlinging  = mIsFlinging;

        mIsFlinging     = mFlingDetector .onTouchEvent(event);
        mIsScrolling    = mScrollDetector.onTouchEvent(event);

        removeCallbacks(mScrollRunnable);
        post(mScrollRunnable);

        final boolean isIntercepted     = mIsScrolling || mIsFlinging;
        final boolean isPrevIntercepted = isPrevScrolling || isPrevFlinging;

        final boolean shouldRedirectDownTouch = action == MotionEvent.ACTION_MOVE
                && (!isIntercepted && isPrevIntercepted)
                && getScrollY() == mMaxScrollY;

        if (isIntercepted || isPrevIntercepted) {

            mMotionEventHook.hook(event, MotionEvent.ACTION_CANCEL);

            if (!isPrevIntercepted) {
                return true;
            }
        }

        if (shouldRedirectDownTouch) {
            mMotionEventHook.hook(event, MotionEvent.ACTION_DOWN);
            mEventRedirectStartedY = event.getRawY();
            mEventRedirected = true;
        }

        super.dispatchTouchEvent(event);
        return true;
    }

    private void cancelOverScroll() {
        if (mOverScrollListener != null && mOverScrollStarted) {
            mOverScrollListener.onCancelled(this);
        }
        mOverScrollStarted = false;
    }

    private void cancelIdleAnimationIfRunning(boolean removeCallbacks) {

        if (removeCallbacks) {
            removeCallbacks(mIdleRunnable);
        }

        if (mCloseUpAnimator != null && mCloseUpAnimator.isRunning()) {
            mCloseUpAnimator.cancel();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int oldY = getScrollY();
            final int nowY = mScroller.getCurrY();
            scrollTo(0, nowY);
            if (oldY != nowY) {
                onScrollChanged(0, getScrollY(), 0, oldY);
            }
            postInvalidate();
        }
    }

    @Override
    protected int computeVerticalScrollRange() {
        return mMaxScrollY;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();
        if (count > 0) {
            int childTop = 0;
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);
                view.layout(left, childTop, right, childTop + view.getMeasuredHeight());
                childTop += view.getMeasuredHeight();
            }
        }
    }

    private boolean canHeaderScroll(int direction) {
        return mScrollingHeader != null && mScrollingHeader.canScrollVertically(direction);
    }

    private final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {

            final boolean isContinue = mScroller.computeScrollOffset();
            mSelfUpdateFling = isContinue;

            if (isContinue) {

                final int y = mScroller.getCurrY();
                final int nowY = getScrollY();
                final int diff = y - nowY;

                if (diff != 0) {
                    scrollBy(0, diff);
                }

                post(this);
            }
        }
    };

    private final Runnable mIdleRunnable = new Runnable() {
        @Override
        public void run() {

            cancelIdleAnimationIfRunning(false);

            if (mSelfUpdateScroll || mSelfUpdateFling) {
                return;
            }

            final int nowY = getScrollY();

            if (nowY == 0
                    || nowY == mMaxScrollY) {
                return;
            }

            final int endY = mCloseUpAlgorithm.getIdleFinalY(ScrollableLayout.this, nowY, mMaxScrollY);

            if (nowY == endY) {
                return;
            }

            mCloseUpAnimator = ObjectAnimator.ofInt(ScrollableLayout.this, CLOSE_UP_ANIMATION_PROPERTY, nowY, endY);

            final long duration = mCloseUpIdleAnimationTime != null
                    ? mCloseUpIdleAnimationTime.compute(ScrollableLayout.this, nowY, endY, mMaxScrollY)
                    : DEFAULT_IDLE_CLOSE_UP_ANIMATION;

            mCloseUpAnimator.setDuration(duration);
            mCloseUpAnimator.addListener(new SelfUpdateAnimationListener());
            mCloseUpAnimator.addListener(new CancelAnimatorOnDetachListener(ScrollableLayout.this, mCloseUpAnimator));

            if (mCloseAnimatorConfigurator != null) {
                mCloseAnimatorConfigurator.configure(mCloseUpAnimator);
            }

            mCloseUpAnimator.start();
        }
    };

    private class ScrollGestureListener extends GestureListenerAdapter {

        private final int mTouchSlop;
        {
            final ViewConfiguration vc = ViewConfiguration.get(getContext());
            mTouchSlop = vc.getScaledTouchSlop();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            final float absX = Math.abs(distanceX);

            // forbid horizontal scrolling
            if (absX > Math.abs(distanceY)
                    || absX > mTouchSlop) {
                return false;
            }

            // okay, let's break-down the logic for overScroll
            // IF overScrollListener is NULL, just do whatever we did
            // ELSE
            //      we start tracking of overScroll ONLY if we are at scrollY == 0 && direction of scroll is -1 (from top to bottom)
            //      a touch event can `wander` from top to bottom and vice versa
            //          and we still need to apply this:
            //          IF direction == -1 -> call `hasOverScroll`
            //          IF direction == 1 ->

            final int y = getScrollY();
            final int distance = (int) (distanceY + .5F);

            if (mOverScrollListener == null) {
                scrollBy(0, distance);
                return y != getScrollY();
            }

            final int direction = distance < 0 ? -1 : 1;

            if (!mOverScrollStarted) {
                mOverScrollStarted = y == 0 && direction == -1;
            }

            boolean handled = false;

            if (mOverScrollStarted) {
                // here we need to check what direction is this scroll event
                if (direction == 1 && y == 0) {
                    if (mOverScrollListener.hasOverScroll(ScrollableLayout.this, distance)) {
                        mOverScrollListener.onOverScrolled(ScrollableLayout.this, distance);
                        handled = true;
                    } else {
                        mOverScrollListener.clear();
                        mOverScrollStarted = false;
                    }
                } else {
                    mOverScrollListener.onOverScrolled(ScrollableLayout.this, distance);
                }
            }

            if (!handled) {
                scrollBy(0, distance);
                return y != getScrollY();
            } else {
                return true;
            }
        }
    }

    private class FlingGestureListener extends GestureListenerAdapter {

        private static final int MIN_FLING_DISTANCE_DIP = 12;

        private final int mMinFlingDistance;
        private final float mMinVelocity;

        FlingGestureListener(Context context) {
            this.mMinFlingDistance = DipUtils.dipToPx(context, MIN_FLING_DISTANCE_DIP);

            final ViewConfiguration configuration = ViewConfiguration.get(context);
            this.mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (Math.abs(velocityY) < mMinVelocity) {
                return false;
            }

            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                return false;
            }

            // it looks like this is never true
            final int nowY = getScrollY();
            if (nowY < 0 || nowY > mMaxScrollY) {
                return false;
            }

            int velocity = -(int) (velocityY + .5F);

            // if we have fling over listener and we are NOT in collapsed state -> redirect call
            // this will allow to skip unpleasant part with fling over event is dispatched a bit `off`
            // also..
            if (!mIsDraggingDraggable && mOnFlingOverListener != null && mMaxScrollY != getScrollY()) {

                final int maxPossibleFinalY;
                final int duration;

                // we will pass Integer.MAX_VALUE to calculate the maximum possible fling
                mScroller.fling(0, nowY, 0, velocity, 0, 0, 0, Integer.MAX_VALUE);
                maxPossibleFinalY = mScroller.getFinalY();
                duration = mScroller.getSplineFlingDuration(velocityY);
                mOnFlingOverListener.onFlingOver(maxPossibleFinalY - mMaxScrollY, duration);

                mScroller.abortAnimation();
            }

            mScroller.fling(0, nowY, 0, velocity, 0, 0, 0, mMaxScrollY);

            if (mScroller.computeScrollOffset()) {

                final int suggestedY = mScroller.getFinalY();

                if (Math.abs(nowY - suggestedY) < mMinFlingDistance) {
                    mScroller.abortAnimation();
                    return false;
                }

                final int finalY;
                if (suggestedY == nowY || mCloseUpAlgorithm == null) {
                    finalY = suggestedY;
                } else {
                    finalY = mCloseUpAlgorithm.getFlingFinalY(
                            ScrollableLayout.this,
                            suggestedY - nowY < 0,
                            nowY,
                            suggestedY,
                            mMaxScrollY
                    );
                    mScroller.setFinalY(finalY);
                }

                final int newY = getNewY(finalY);

                return !(finalY == nowY || newY < 0);
            }

            return false;
        }
    }

    private static class MotionEventHook {

        final MotionEventHookCallback callback;

        MotionEventHook(MotionEventHookCallback callback) {
            this.callback = callback;
        }

        void hook(MotionEvent event, int action) {
            final int historyAction = event.getAction();
            event.setAction(action);
            callback.apply(event);
            event.setAction(historyAction);
        }
    }

    private interface MotionEventHookCallback {
        void apply(MotionEvent event);
    }

    private static final Property<ScrollableLayout, Integer> CLOSE_UP_ANIMATION_PROPERTY
            = new Property<ScrollableLayout, Integer>(Integer.class, "scrollY") {

        @Override
        public Integer get(ScrollableLayout object) {
            return object.getScrollY();
        }

        @Override
        public void set(final ScrollableLayout layout, final Integer value) {
            layout.setScrollY(value);
        }
    };

    private class SelfUpdateAnimationListener extends AnimatorListenerAdapter {

        private final boolean mInitialValue = mSelfUpdateScroll;

        @Override
        public void onAnimationStart(Animator animation) {
            mSelfUpdateScroll = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mSelfUpdateScroll = mInitialValue;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mSelfUpdateScroll = mInitialValue;
        }

    }

    private static class CancelAnimatorOnDetachListener extends AnimatorListenerAdapter implements OnAttachStateChangeListener {

        private final View mView;
        private final ValueAnimator mAnimator;

        private boolean mSubscribed;

        private CancelAnimatorOnDetachListener(View view, ValueAnimator animator) {
            mView = view;
            mAnimator = animator;
        }

        @Override
        public void onViewAttachedToWindow(View v) {
            // no op
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            if (mAnimator != null
                    && mAnimator.isRunning()) {
                mAnimator.cancel();
            }
            unsubscribe();
        }

        @Override
        public void onAnimationStart(Animator animation) {
            mSubscribed = true;
            mView.addOnAttachStateChangeListener(this);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            unsubscribe();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            unsubscribe();
        }

        private void unsubscribe() {
            if (mSubscribed) {
                mView.removeOnAttachStateChangeListener(this);
                mSubscribed = false;
            }
        }
    }


    @Override
    public Parcelable onSaveInstanceState() {
    	final Parcelable superState = super.onSaveInstanceState();
    	final ScrollableLayoutSavedState savedState = new ScrollableLayoutSavedState(superState);

        savedState.scrollY = getScrollY();

    	return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

    	if (!(state instanceof ScrollableLayoutSavedState)) {
    		super.onRestoreInstanceState(state);
    		return;
    	}

    	final ScrollableLayoutSavedState in = (ScrollableLayoutSavedState) state;
    	super.onRestoreInstanceState(in.getSuperState());

        setScrollY(in.scrollY);
    }

    private static class ScrollableLayoutSavedState extends BaseSavedState {

        int scrollY;

    	ScrollableLayoutSavedState(Parcel source) {
    		super(source);

            scrollY = source.readInt();
    	}

    	ScrollableLayoutSavedState(Parcelable superState) {
    		super(superState);
    	}

    	@Override
    	public void writeToParcel(Parcel out, int flags) {
    		super.writeToParcel(out, flags);

            out.writeInt(scrollY);
    	}

    	public static final Creator<ScrollableLayoutSavedState> CREATOR
    			= new Creator<ScrollableLayoutSavedState>() {

    		@Override
    		public ScrollableLayoutSavedState createFromParcel(Parcel in) {
    			return new ScrollableLayoutSavedState(in);
    		}

    		@Override
    		public ScrollableLayoutSavedState[] newArray(int size) {
    			return new ScrollableLayoutSavedState[size];
    		}
    	};
    }
}
