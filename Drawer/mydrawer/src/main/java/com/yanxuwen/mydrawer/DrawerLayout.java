package com.yanxuwen.mydrawer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Flavien Laurent (flavienlaurent.com) on 23/08/13.
 */
public class DrawerLayout extends ViewGroup {
	private  int	mHandleId;
	private  int	mContentId;
	public static final int		ORIENTATION_LEFT		= 0;
	public static final int		ORIENTATION_RIGHT	= 1;
	public static final int		ORIENTATION_TOP	= 2;
	public static final int		ORIENTATION_BOTTOM		= 3;

	public int mdirection =ORIENTATION_BOTTOM;
	private final ViewDragHelper mDragHelper;
	private View mDescView;
	private View content;
	private float mInitialMotionX;
	private float mInitialMotionY;
	/** 可拖动范围（总范围） */
	private int mDragRange;
	private int mTop;
	/** 拖动偏移=拖动的量/可拖动的范围（总范围） 大于0.5f则代表为超过一半 */
	private float mDragOffset;
	/** 判断是否可拖动 */
	private boolean isDrawer = true;

	public interface OnDrawerStatusListener {
		public void onStatus(boolean isOpen);
	}

	OnDrawerStatusListener mOnDrawerStatusListener = null;

	public void setOnDrawerStatusListener(OnDrawerStatusListener l) {
		mOnDrawerStatusListener = l;
	}

	public DrawerLayout(Context context) {
		this(context, null);
	}

	public DrawerLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.DrawerLayout, defStyle, 0 );
		int handleId = a.getResourceId( R.styleable.DrawerLayout_handle, 0 );
		if ( handleId == 0 ) { throw new IllegalArgumentException( "The handle attribute is required and must refer "
				+ "to a valid child." ); }

		int contentId = a.getResourceId( R.styleable.DrawerLayout_content, 0 );
		if ( contentId == 0 ) { throw new IllegalArgumentException( "The content attribute is required and must refer "
				+ "to a valid child." ); }

		if ( handleId == contentId ) { throw new IllegalArgumentException( "The content and handle attributes must refer "
				+ "to different children." ); }
		mHandleId = handleId;
		mContentId = contentId;
		mdirection = a.getInt( R.styleable.DrawerLayout_direction, ORIENTATION_BOTTOM );
		a.recycle();
	}

	@Override
	protected void onFinishInflate() {
		mDescView = findViewById(mHandleId);
		content = findViewById(mContentId);
	}

	/**
	 * 显示最大化，及View滑到原始位置
	 */
	public void close() {
		smoothSlideTo(0f);
	}

	/**
	 * 显示最小化，及View滑出屏幕
	 */
	public void open() {
		smoothSlideTo(1f);
	}

	/**
	 * 滑动试图，1f则为滑出屏幕，0f则滑到原始位置，目前为横向滑
	 */
	boolean smoothSlideTo(float slideOffset) {
		final int topBound = getPaddingTop();
		int y = (int) (topBound + slideOffset * mDragRange);

		if (mDragHelper.smoothSlideViewTo(mDescView, mDescView.getLeft(), y)) {
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}

	private class DragHelperCallback extends ViewDragHelper.Callback {

		/**
		 * 确定当前子view是否可拖动，
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == mDescView;
		}

		/**
		 * 该方法在子view位置发生改变时都会被调用，可以在这个方法中做一些拖动过程中渐变的动画等操作,left距离左边的偏移量
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			switch (mdirection) {
				case ORIENTATION_LEFT:
					break;
				case ORIENTATION_RIGHT:
					break;
				case ORIENTATION_TOP:
				case ORIENTATION_BOTTOM:
					mDragOffset = (float) top / mDragRange;
					mDescView.setPivotY(mDescView.getHeight());
					break;
			}

			// mDescView.setAlpha(1 - mDragOffset);

			invalidate();
		}

		/**
		 * 该方法在手势拖动释放的时候被调用，可以在这里设置子View预期到达的位置，
		 * 如果人为的手势拖动没有到达预期位置，我们可以让子View在人为的拖动结束后，再自动关的滑动到指定位置
		 *
		 * @xvel为X轴的拖动速度正的为向右，0代表滑动停止后一段时间后，释放手指
		 * @yvel为Y轴拖动速度正的为向下
		 */
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			int top = getPaddingTop();
			switch (mdirection) {
				case ORIENTATION_LEFT:
					break;
				case ORIENTATION_RIGHT:
					break;
				case ORIENTATION_TOP:
				case ORIENTATION_BOTTOM:
					// 如果方向向右，或者停止滑动却拖动便宜大于可滑动的一半，则滑动总范围，及整个View滑出屏幕
					// 如果这句话没执行，则代表滑到原始位置。
					if (yvel > 0 || (yvel == 0 && mDragOffset > 0.5f)) {
						top += mDragRange;
					}
					if (top == 0) {
						if (mOnDrawerStatusListener != null)
							mOnDrawerStatusListener.onStatus(false);
					} else {
						if (mOnDrawerStatusListener != null)
							mOnDrawerStatusListener.onStatus(true);
					}
					mDragHelper.settleCapturedViewAt(0, top);
					break;
			}
			invalidate();
		}

		/**
		 * 从字面意思就可以看出这是一个获取边界的方法
		 */
		@Override
		public int getViewVerticalDragRange(View child) {
			return mDragRange;
		}

		/** 设置滑动边界为content的高度 */
		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			final int topBound = getPaddingTop();
			final int bottomBound = content.getHeight();

			final int newTop = Math.min(Math.max(top, topBound), bottomBound);

			return newTop;
		}
		/**
		 * 此方法是用来控制水平方向移动的范围，每次调用
		 * 拿到当前的left值跟DragLayout的根布局的paddingleft值做比较，取两者中大的值
		 * ，这样做是为了防止子view左边滑出根布局的左边界。 拿left和padding
		 * left中的较大值跟可滑动范围的右边界（根布局宽度减去子view的宽度）作比较，取较小值，这样做是为了防止子view右边滑出根布局右边界。
		 */
		// @Override
		// public int clampViewPositionHorizontal(View child, int left, int dx)
		// {
		// final int leftBound = getPaddingLeft();
		// final int rightBound = getWidth() - 0;
		//
		// final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
		//
		// return newLeft;
		// }
	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);
		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			mDragHelper.cancel();
			return false;
		}
		return mDragHelper.shouldInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!isDrawer) {
			return false;
		} else {
			mDragHelper.processTouchEvent(ev);
			return true;
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight =MeasureSpec.getSize(widthMeasureSpec);
		switch (mdirection) {
			case ORIENTATION_LEFT:
				break;
			case ORIENTATION_RIGHT:
				break;
			case ORIENTATION_TOP:
			case ORIENTATION_BOTTOM:
				 maxWidth = MeasureSpec.getSize(widthMeasureSpec);
				 maxHeight = content.getMeasuredHeight()+mDescView.getMeasuredHeight();
				break;
		}
		setMeasuredDimension(
				resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
				resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// 初始化可拖动范围===整个布局的宽度=整个屏幕宽度
		mDragRange = content.getMeasuredHeight();
		switch (mdirection) {
			case ORIENTATION_LEFT:
				break;
			case ORIENTATION_RIGHT:
				break;
			case ORIENTATION_TOP:
				int mTop_content=getHeight()-content.getMeasuredHeight();
				content.layout(0, mTop_content + 0, r, mTop_content + content.getMeasuredHeight());
				int mTop_descView=getHeight()-mDescView.getMeasuredHeight();
				mDescView.layout(0, mTop_descView + 0, r,
						mTop_descView + mDescView.getMeasuredHeight());
				break;
			case ORIENTATION_BOTTOM:
				content.layout(0, mTop + 0, r, mTop + content.getMeasuredHeight());
				mDescView.layout(0, mTop + 0, r,
						mTop + mDescView.getMeasuredHeight());
				break;
		}

	}
}
