package com.yanxuwen.mydrawer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.FloatRange;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.Arrays;

public abstract class BaseDragLayout extends ViewGroup {
    /**
     * 是否可以滑动
     */
    private boolean isSlideable = true;
    /**
     * 边缘滑动
     */
    private boolean isEdgeSlide = true;
    /**
     * 点击外部取消
     */
    private boolean isOutside = true;
    //	/**显示出来的最高比例*/
    private float maxShowScale = 1F;
    private RecyclerView mRecyclerView;

    private boolean isopen;
    public int mode = MODE_NULL;
    private int drag_left;
    private int drag_right;
    private int drag_top;
    private int drag_bottom;

    //边缘大小
    private int mEdgeSize = 30;
    //设置事件滑动，最小值，当小于这个值，不触发当前滑动
    private int mMoveEventSize = 30;
    public final static int MODE_NULL = 0;
    public final static int MODE_ALPHA = 100;
    public final static int MODE_DRAG_LEFT = 1;
    public final static int MODE_DRAG_RIGHT = 2;
    public final static int MODE_DRAG_BOTTOM = 3;
    public final static int MODE_DRAG_TOP = 4;

    private static final int MIN_DRAWER_MARGIN = 80; // dp
    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    /**
     * drawer离父容器右边的最小外边距
     */
    private int mMinDrawerMargin;

    private View mDescView;

    private ViewDragHelper mDragHelper;
    /**
     * drawer显示出来的占自身的百分比
     */
    private float mLeftMenuOnScreen;

    private int pointerId;


    /**
     * 该拖动试图的开关状态
     */
    public interface OnDragViewStatusListener {
        /**
         * 该拖动试图的开关状态
         */
        public void onDragViewStatus(boolean isOpen);
    }

    ;

    /**
     * 该拖动试图的偏移量监听
     */
    public interface OnDragViewOffsetListener {
        /**
         * 该拖动试图的偏移量监听 0~maxShowScale
         */
        public void onDragViewOffset(float Offset);
    }

    ;

    public OnDragViewStatusListener mOnDragViewStatusListener;
    public OnDragViewOffsetListener mOnDragViewOffsetListener;

    public void setOnDragViewStatusListener(OnDragViewStatusListener l) {
        mOnDragViewStatusListener = l;
    }

    ;

    public void setOnDragViewOffsetListener(OnDragViewOffsetListener l) {
        mOnDragViewOffsetListener = l;
    }

    ;

    public void removeOnDragViewStatusListener(OnDragViewStatusListener l) {
        mOnDragViewStatusListener = null;
    }

    ;

    public void removeOnDragViewOffsetListener(OnDragViewOffsetListener l) {
        mOnDragViewOffsetListener = null;
    }

    ;

    /**
     * 状态监听，开关状态
     */
    public abstract void onViewStatus(boolean isOpen);

    /**
     * 偏移量监听
     */
    public abstract void onViewOffset(float mOffset);

    public abstract void initView();

    private void setContentView(View mDescView) {
        mDescView.setClickable(true);
        this.mDescView = mDescView;
    }

    public View getContentView() {
        switch (getMode()) {
            case MODE_DRAG_LEFT:
                return findViewById(drag_left);
            case MODE_DRAG_RIGHT:
                return findViewById(drag_right);
            case MODE_DRAG_TOP:
                return findViewById(drag_top);
            case MODE_DRAG_BOTTOM:
                return findViewById(drag_bottom);

        }
        return this;
    }

    public BaseDragLayout(Context context) {
        this(context, null);
    }

    public BaseDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = getResources().getDisplayMetrics().density;
        float minVel = MIN_FLING_VELOCITY * density;  //1200
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DragLayout);
        drag_left = a.getResourceId(R.styleable.DragLayout_drag_left, 0);
        drag_right = a.getResourceId(R.styleable.DragLayout_drag_right, 0);
        drag_top = a.getResourceId(R.styleable.DragLayout_drag_top, 0);
        drag_bottom = a.getResourceId(R.styleable.DragLayout_drag_bottom, 0);

        a.recycle();
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                //捕获该view
                // return child == mDragView || child == mAutoBackView;
                return child.getId() == drag_left || child.getId() == drag_right || child.getId() == drag_top || child.getId() == drag_bottom;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {

                if (getMode() != MODE_DRAG_LEFT && getMode() != MODE_DRAG_RIGHT && getMode() != MODE_ALPHA)
                    return 0;
                final int leftBound = 0;
                final int rightBound = getWidth() - 0;


                final int newLeft = Math.max(Math.min(left, leftBound), -child.getWidth());

                final int newRight = Math.max(Math.min(left, rightBound), rightBound - child.getWidth());


                return (getMode() == MODE_DRAG_LEFT || getMode() == MODE_ALPHA) ? newLeft : newRight;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {


                if (getMode() != MODE_DRAG_BOTTOM && getMode() != MODE_DRAG_TOP) return 0;
                final int topBound = 0;
                final int bottomBound = getHeight() - 0;

                final int newTop = Math.max(Math.min(top, topBound), -child.getHeight());

                final int newBottom = Math.max(Math.min(top, bottomBound), bottomBound - child.getHeight());


                return (getMode() == MODE_DRAG_TOP) ? newTop : newBottom;
            }

            //手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                int childWidth = releasedChild.getWidth();
                int childHeight = releasedChild.getHeight();

                switch (getMode()) {
                    case MODE_ALPHA:
                    case MODE_DRAG_LEFT:
                        //0~1f,关到开
                        float offset_left = (childWidth + releasedChild.getLeft()) * 1.0f / childWidth;
                        int openLeft = (int) (-(1 - maxShowScale) * childWidth);
                        mDragHelper.settleCapturedViewAt(xvel > 0 || (xvel == 0 && offset_left > 0.5f * maxShowScale) ? openLeft : -childWidth,
                                releasedChild.getTop());
                        invalidate();
                        break;
                    case MODE_DRAG_RIGHT:

                        //0~1，关到开
                        float offset_right = (getWidth() - releasedChild.getLeft()) * 1.0f / childWidth;
                        int openRight = (int) (getWidth() - (maxShowScale) * childWidth);
                        mDragHelper.settleCapturedViewAt(xvel < 0 || (xvel == 0 && offset_right > 0.5f * maxShowScale) ? openRight : getWidth(),
                                releasedChild.getTop());
                        invalidate();
                        break;
                    case MODE_DRAG_TOP:
                        //0~1f,关到开
                        float offset_top = (childHeight + releasedChild.getTop()) * 1.0f / childHeight;
                        int openTop = (int) (-(1 - maxShowScale) * childHeight);
                        mDragHelper.settleCapturedViewAt(releasedChild.getLeft(),
                                yvel > 0 || (yvel == 0 && offset_top > 0.5f * maxShowScale) ? openTop : -childHeight);
                        invalidate();
                        break;
                    case MODE_DRAG_BOTTOM:
                        //0~1，关到开
                        float offset_bottom = (getHeight() - releasedChild.getTop()) * 1.0f / childHeight;
                        int openBottom = (int) (getHeight() - (maxShowScale) * childHeight);
                        mDragHelper.settleCapturedViewAt(releasedChild.getLeft(),

                                yvel < 0 || (yvel == 0 && offset_bottom > 0.5f * maxShowScale) ? openBottom : getHeight());
                        invalidate();
                        break;

                }
            }

            //在边界拖动时回调
            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                BaseDragLayout.this.pointerId = pointerId;
                if (isEdgeSlide()) {
//                    mDragHelper.captureChildView(mDescView, pointerId);
                }
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                int childWidth = changedView.getWidth();
                int childHeight = changedView.getHeight();

                float offset = 0;
                switch (getMode()) {
                    case MODE_DRAG_LEFT:
                        offset = (float) (childWidth + left) / childWidth;
                        break;
                    case MODE_DRAG_RIGHT:
                        offset = (float) (getWidth() - left) / childWidth;

                        break;
                    case MODE_DRAG_TOP:
                        offset = (float) (childHeight + top) / childHeight;

                        break;
                    case MODE_DRAG_BOTTOM:
                        offset = (float) (getHeight() - top) / childHeight;
                        break;
                }
                changedView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
                mLeftMenuOnScreen = offset;
                onViewOffset(offset);
                if (mOnDragViewOffsetListener != null)
                    mOnDragViewOffsetListener.onDragViewOffset(offset);
                if (offset == 0) {
                    setMode(MODE_NULL);
                    isopen = false;
                    onViewStatus(false);
                    if (mOnDragViewStatusListener != null)
                        mOnDragViewStatusListener.onDragViewStatus(false);
                } else if (offset == 1) {
                    isopen = true;
                    onViewStatus(true);
                    if (mOnDragViewStatusListener != null)
                        mOnDragViewStatusListener.onDragViewStatus(true);
                }
                invalidate();
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                if (!getSlideable()) return 0;
                switch (getMode()) {
                    case MODE_DRAG_LEFT:
                    case MODE_DRAG_RIGHT:
                        return getContentView() == child ? child.getWidth() : 0;
                    case MODE_DRAG_TOP:
                    case MODE_DRAG_BOTTOM:
                        return getContentView() == child ? child.getHeight() : 0;
                }
                return getContentView() == child ? child.getWidth() : 0;

            }

        });
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT | ViewDragHelper.EDGE_RIGHT | ViewDragHelper.EDGE_BOTTOM | ViewDragHelper.EDGE_TOP);
        mDragHelper.setMinVelocity(minVel);
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                if (drag_left == childView.getId()
                        || drag_right == childView.getId()
                        || drag_bottom == childView.getId()
                        || drag_top == childView.getId()) {
                    int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
                    int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

                    setMeasuredDimension(
                            resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                            resolveSizeAndState(maxHeight, heightMeasureSpec, 0));

                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

                }
            }
        }
    }

    private boolean initLeft;
    private boolean initRight;
    private boolean initTop;
    private boolean initBottom;
    private boolean isFirstChildContent = true;


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                if (drag_left == childView.getId()
                        || drag_right == childView.getId()
                        || drag_bottom == childView.getId()
                        || drag_top == childView.getId()) {
                    final int menuWidth = childView.getMeasuredWidth();
                    final int menuHeight = childView.getMeasuredHeight();
                    int childLeft;
                    int childTop;
                    if (drag_left == childView.getId() && !initLeft) {
                        initLeft = true;
                        childLeft = -menuWidth + (int) (menuWidth * mLeftMenuOnScreen);
                        childView.layout(childLeft, 0, childLeft + menuWidth,
                                menuHeight);
                    } else if (drag_right == childView.getId() && !initRight) {
                        initRight = true;
                        childLeft = getWidth() - (int) (menuWidth * mLeftMenuOnScreen);
                        childView.layout(childLeft, 0, childLeft + menuWidth,
                                menuHeight);
                    } else if (drag_top == childView.getId() && !initTop) {
                        initTop = true;
                        childTop = -menuHeight + (int) (menuHeight * mLeftMenuOnScreen);
                        childView.layout(0, childTop, menuWidth,
                                childTop + menuHeight);
                    } else if (drag_bottom == childView.getId() && !initBottom) {
                        initBottom = true;
                        childTop = getHeight() - (int) (menuHeight * mLeftMenuOnScreen);
                        childView.layout(0, childTop, menuWidth,
                                childTop + menuHeight);
                    } else {
                        childView.layout(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
                    }
                    childView.setClickable(true);
                } else {
                    if (isFirstChildContent) {
                        isFirstChildContent = false;
                        setContentView(childView);
                        childView.setClickable(true);
                    }
                    childView.layout(l, t, r, b);
                }
            }
        }

    }

    public void close(int mode) {
        mLeftMenuOnScreen = 0.f;
        smoothSlideTo(mLeftMenuOnScreen, mode);
    }

    public void close() {
        mLeftMenuOnScreen = 0.f;
        smoothSlideTo(mLeftMenuOnScreen, getMode());
    }

    public void open(int mode) {
        mLeftMenuOnScreen = maxShowScale;
        smoothSlideTo(mLeftMenuOnScreen, mode);
    }

    /**
     * 如果只有设置一个方向的话，则不用传递类型
     * 如果设置多个方向的话，则默认打开左边
     */
    public void open() {
        if (drag_left != 0) {
            open(MODE_DRAG_LEFT);
        } else if (drag_right != 0) {
            open(MODE_DRAG_RIGHT);
        } else if (drag_top != 0) {
            open(MODE_DRAG_TOP);
        } else if (drag_bottom != 0) {
            open(MODE_DRAG_BOTTOM);
        }
    }

    /**
     * 滑动试图，1f则为滑出屏幕，0f则滑到原始位置，目前为横向滑
     */
    private boolean smoothSlideTo(float slideOffset, int mode) {
        int childWidth = getContentView().getWidth();
        int childHeight = getContentView().getHeight();

        int x = 0;
        int y = 0;
        setMode(mode);
        switch (getMode()) {
            case MODE_DRAG_LEFT:
                x = (int) ((slideOffset * childWidth) - childWidth);
                break;

            case MODE_DRAG_RIGHT:
                x = (int) (getWidth() - slideOffset * childWidth);
                break;
            case MODE_DRAG_TOP:
                y = (int) ((slideOffset * childHeight) - childHeight);
                break;
            case MODE_DRAG_BOTTOM:
                y = (int) (getHeight() - slideOffset * childHeight);
                break;

        }

        switch (getMode()) {
            case MODE_DRAG_RIGHT:
            case MODE_DRAG_LEFT:
                if (mDragHelper.smoothSlideViewTo(getContentView(), x, getContentView().getTop())) {
                    ViewCompat.postInvalidateOnAnimation(this);
                    return true;
                }
                break;
            case MODE_DRAG_BOTTOM:
            case MODE_DRAG_TOP:
                if (mDragHelper.smoothSlideViewTo(getContentView(), getContentView().getLeft(), y)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                    return true;
                }
                break;

        }
        return false;
    }

    float downX = 0;
    float downY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isOutside && !isClickView(getContentView(), ev) && isopen) {
            close();
            return true;
        }
        if (getSlideable() || (!getSlideable() && ev.getAction() == MotionEvent.ACTION_UP)) {
            mDragHelper.shouldInterceptTouchEvent(ev);
        }
        //如果在滑动期间则要拦截子类
        if (mLeftMenuOnScreen > 0 && mLeftMenuOnScreen < 1) {
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = ev.getX();
            downY = ev.getY();
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            //拦截抽屉外的内容
            //如果是其他控件则超过mMoveEventSize，则拦截,拦截
            if (!isopen && getMode() == MODE_NULL) {
                if ((ev.getX() - downX) > mMoveEventSize && drag_left != 0) {
                    setMode(MODE_DRAG_LEFT);
                    mDragHelper.captureChildView(getContentView(), pointerId);
                    return true;
                } else if ((downX - ev.getX()) > mMoveEventSize && drag_right != 0) {
                    setMode(MODE_DRAG_RIGHT);
                    mDragHelper.captureChildView(getContentView(), pointerId);
                    return true;
                } else if ((ev.getY() - downY) > mMoveEventSize && drag_top != 0) {
                    setMode(MODE_DRAG_TOP);
                    mDragHelper.captureChildView(getContentView(), pointerId);
                    return true;
                } else if ((downY - ev.getY()) > mMoveEventSize && drag_bottom != 0) {
                    setMode(MODE_DRAG_BOTTOM);
                    mDragHelper.captureChildView(getContentView(), pointerId);
                    return true;
                }
            } else if (isopen) {
                //拦截列表
                if (mRecyclerView != null && isClickView(mRecyclerView, ev)) {
                    switch (getMode()) {
                        //如果mRecyclerView不等于空，则要多久判断isRecyclerView的滚动情况，后续会加上nestedscrollview
                        case MODE_DRAG_LEFT:
                            if (!mRecyclerView.canScrollHorizontally(5) && (downX - ev.getX()) > 5)
                                return true;
                            break;
                        case MODE_DRAG_RIGHT:
                            if (!mRecyclerView.canScrollHorizontally(-5) && (ev.getX() - downX) > 5)
                                return true;
                            break;
                        case MODE_DRAG_TOP:
                            if (!mRecyclerView.canScrollVertically(-5) && (downY - ev.getY()) > 5)
                                return true;
                            break;
                        case MODE_DRAG_BOTTOM:
                            if (!mRecyclerView.canScrollVertically(5) && (ev.getY() - downY) > 5)
                                return true;
                            break;
                    }
                } else {
                    //拦截抽屉内的内容
                    //如果 打开状态，判断条件要跟上面反方向，则要大于20 则拦截，避免无法抽屉内容，无法点击
                    if (getMode() == MODE_DRAG_LEFT && (downX - ev.getX()) > 20) {
                        return true;
                    } else if (getMode() == MODE_DRAG_RIGHT && (ev.getX() - downX) > 20) {
                        return true;
                    } else if (getMode() == MODE_DRAG_TOP && (downY - ev.getY()) > 20) {
                        return true;
                    } else if (getMode() == MODE_DRAG_BOTTOM && (ev.getY() - downY) > 20) {
                        return true;
                    }
                }
            }

        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        processTouchEvent(event);
        return true;
    }

    private void processTouchEvent(MotionEvent event) {
        if (getSlideable() || (!getSlideable() && event.getAction() == MotionEvent.ACTION_UP)) {
            mDragHelper.processTouchEvent(event);
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    /**
     * 判断是否是打开状态
     */
    public boolean isOpen() {
        return isopen;
    }

    /**
     * 是否可以滑动
     */
    public void setSlideable(boolean isSlideable) {
        this.isSlideable = isSlideable;
    }

    /**
     * 是否可以滑动
     */
    public boolean getSlideable() {
        return isSlideable;
    }

    /**
     * 是否可以边缘滑动
     */
    public boolean isEdgeSlide() {
        return isEdgeSlide;
    }

    /**
     * 设置是否可以边缘滑动
     */
    public void setEdgeSlide(boolean edgeSlide) {
        isEdgeSlide = edgeSlide;
    }

    /**
     * 点击外部取消
     */
    public void setCanceledOnTouchOutside(boolean isOutside) {
        this.isOutside = isOutside;
    }

    /**
     * 点击外部取消
     */
    public boolean isOutside() {
        return isOutside;
    }

    public float getMaxShowScale() {
        return maxShowScale;
    }

    /**
     * 显示出来的最高比例
     */
    private void setMaxShowScale(@FloatRange(from = 0.0, to = 1.0) float maxShowScale) {
        this.maxShowScale = maxShowScale;
    }

    /**
     * 支持RecyclerView的联动，先滑动RecyclerView，然后在滑动该视图
     */
    public void setRecyclerView(final RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }

    /**
     * 设置边缘大小
     */
    public void setEdgeSize(int mEdgeSize) {
        this.mEdgeSize = mEdgeSize;
        Field field = null;
        try {
            field = mDragHelper.getClass().getDeclaredField("mEdgeSize");
            field.setAccessible(true);
            field.set(mDragHelper, mEdgeSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMoveEventSize(int mMoveEventSize) {
        this.mMoveEventSize = mMoveEventSize;
    }


    /**
     * 是否是横向的，如果是则不显示加载样式
     */
    private boolean isRecyclerViewHorizontal() {
        if (mRecyclerView == null) return false;
        RecyclerView.LayoutManager mLayoutManager = mRecyclerView.getLayoutManager();
        if (mLayoutManager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) mLayoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否点击到当前View
     */
    private boolean isClickView(View v, MotionEvent event) {
        if (v != null) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getRawX() > left && event.getRawX() < right
                    && event.getRawY() > top && event.getRawY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
