package com.yanxuwen.mydrawer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.FloatRange;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public abstract  class BaseDragLayout extends ViewGroup {
	/**是否可以滑动*/
	private boolean isSlideable=true;
	/**边缘滑动*/
	private boolean isEdgeSlide=true;
	/**点击外部取消*/
	private boolean isOutside=true;
	//	/**显示出来的最高比例*/
	private float maxShowScale=1F;
	private RecyclerView mRecyclerView;
	private boolean isRecyclerViewTop=false;
	private boolean isRecyclerViewBottom=false;
	private boolean isRecyclerViewLeft=false;
	private boolean isRecyclerViewRight=false;

	private boolean isopen;
	public int mode;
	public final static int MODE_ALPHA = 0;
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




	/**
	 * 该拖动试图的开关状态
	 *
	 */
	public interface OnDragViewStatusListener {
		/** 该拖动试图的开关状态 */
		public void onDragViewStatus(boolean isOpen);
	};
	/**
	 * 该拖动试图的偏移量监听
	 *
	 */
	public interface OnDragViewOffsetListener {
		/** 该拖动试图的偏移量监听 0~maxShowScale*/
		public void onDragViewOffset(float Offset);
	};

	public OnDragViewStatusListener mOnDragViewStatusListener;
	public OnDragViewOffsetListener mOnDragViewOffsetListener;

	public void setOnDragViewStatusListener(OnDragViewStatusListener l) {
		mOnDragViewStatusListener = l;
	};
	public void setOnDragViewOffsetListener(OnDragViewOffsetListener l) {
		mOnDragViewOffsetListener = l;
	};
	public void removeOnDragViewStatusListener(OnDragViewStatusListener l) {
		mOnDragViewStatusListener = null;
	};
	public void removeOnDragViewOffsetListener(OnDragViewOffsetListener l) {
		mOnDragViewOffsetListener = null;
	};
	/**
	 * 状态监听，开关状态
	 */
	public abstract void onViewStatus(boolean isOpen);
	/**
	 * 偏移量监听
	 */
	public abstract void onViewOffset(float mOffset);
	public  abstract void initView();
	public void setContentView(View mDescView){
		this.mDescView=mDescView;
	}
	public View getContentView(){
		return mDescView;
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
		int mode = a.getInt(R.styleable.DragLayout_mode, 1);
		a.recycle();
		setMode(mode);
		mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
			@Override
			public boolean tryCaptureView(View child, int pointerId) {
				//捕获该view
				// return child == mDragView || child == mAutoBackView;

				return child == getContentView();
			}

			@Override
			public int clampViewPositionHorizontal(View child, int left, int dx) {

				if(getMode()!=MODE_DRAG_LEFT&&getMode()!=MODE_DRAG_RIGHT&&getMode()!=MODE_ALPHA)return 0;
				final int leftBound = 0;
				final int rightBound = getWidth() - 0;


				final int newLeft = Math.max(Math.min(left, leftBound), -child.getWidth());

				final int newRight = Math.max(Math.min(left, rightBound), rightBound-child.getWidth());



				return (getMode()==MODE_DRAG_LEFT||getMode()==MODE_ALPHA)?newLeft:newRight;
			}

			@Override
			public int clampViewPositionVertical(View child, int top, int dy) {


				if(getMode()!=MODE_DRAG_BOTTOM&&getMode()!=MODE_DRAG_TOP)return 0;
				final int topBound = 0;
				final int bottomBound = getHeight() - 0;

				final int newTop = Math.max(Math.min(top, topBound), -child.getHeight());

				final int newBottom = Math.max(Math.min(top, bottomBound), bottomBound-child.getHeight());



				return (getMode()==MODE_DRAG_TOP)?newTop:newBottom;
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
						int openLeft= (int) (-(1-maxShowScale)*childWidth);
						mDragHelper.settleCapturedViewAt(xvel > 0 || (xvel == 0 && offset_left > 0.5f* maxShowScale )? openLeft : -childWidth,
								releasedChild.getTop());
						invalidate();
						break;
					case MODE_DRAG_RIGHT:

						//0~1，关到开
						float offset_right = (getWidth()-releasedChild.getLeft()) * 1.0f / childWidth;
						int openRight= (int) (getWidth()-(maxShowScale)*childWidth);
						mDragHelper.settleCapturedViewAt(xvel < 0 || (xvel == 0 && offset_right > 0.5f* maxShowScale)?openRight:getWidth() ,
								releasedChild.getTop());
						invalidate();
						break;
					case MODE_DRAG_TOP:
						//0~1f,关到开
						float offset_top = (childHeight + releasedChild.getTop()) * 1.0f / childHeight;
						int openTop= (int) (-(1-maxShowScale)*childHeight);
						mDragHelper.settleCapturedViewAt(releasedChild.getLeft(),
								yvel > 0 || (yvel == 0 && offset_top > 0.5f* maxShowScale)? openTop : -childHeight);
						invalidate();
						break;
					case MODE_DRAG_BOTTOM:
						//0~1，关到开
						float offset_bottom = (getHeight()-releasedChild.getTop()) * 1.0f / childHeight;
						int openBottom= (int) (getHeight()-(maxShowScale)*childHeight);
						mDragHelper.settleCapturedViewAt(releasedChild.getLeft(),

								yvel < 0 || (yvel == 0 && offset_bottom >0.5f* maxShowScale) ? openBottom:getHeight() );
						invalidate();
						break;

				}
			}

			//在边界拖动时回调
			@Override
			public void onEdgeDragStarted(int edgeFlags, int pointerId) {
				if(isEdgeSlide()) {
					mDragHelper.captureChildView(getContentView(), pointerId);
				}
			}

			@Override
			public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
				int childWidth = changedView.getWidth();
				int childHeight = changedView.getHeight();

				float offset = 0;
				switch (getMode()){
					case MODE_DRAG_LEFT:
						offset = (float) (childWidth + left) / childWidth;
						break;
					case MODE_DRAG_RIGHT:
						offset = (float) (getWidth()- left) / childWidth;

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
				if(mOnDragViewOffsetListener!=null)
					mOnDragViewOffsetListener.onDragViewOffset(offset);
				if(offset==0){
					isopen=false;
					onViewStatus(false);
					if (mOnDragViewStatusListener != null)
						mOnDragViewStatusListener.onDragViewStatus(false);
				}else if(offset==1){
					isopen=true;
					onViewStatus(true);
					if (mOnDragViewStatusListener != null)
						mOnDragViewStatusListener.onDragViewStatus(true);
				}
				invalidate();
			}

			@Override
			public int getViewHorizontalDragRange(View child) {
				if(!getSlideable())return 0;
				switch (getMode()){
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
		mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT | ViewDragHelper.EDGE_RIGHT|ViewDragHelper.EDGE_BOTTOM | ViewDragHelper.EDGE_TOP);
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
		if(childCount >0) {
			for (int i = 0; i < childCount; i++) {
				View childView = getChildAt(i);
				if (childView == mDescView) {
					int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
					int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

					setMeasuredDimension(
							resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
							resolveSizeAndState(maxHeight, heightMeasureSpec, 0));

				}else{
					super.onMeasure(widthMeasureSpec, heightMeasureSpec);

				}
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		if(childCount >0){
			for(int i=0;i<childCount;i++){
				View childView = getChildAt(i);
				if(childView == mDescView){
					MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
					final int menuWidth = childView.getMeasuredWidth();
					final int menuHeight = childView.getMeasuredHeight();
					int childLeft;
					int childTop;
					switch (getMode()){
						case MODE_DRAG_LEFT:
							childLeft= -menuWidth + (int) (menuWidth * mLeftMenuOnScreen);
							childView.layout(childLeft, lp.topMargin, childLeft + menuWidth,
									lp.topMargin + menuHeight);
							break;
						case MODE_DRAG_RIGHT:
							childLeft = getWidth()- (int) (menuWidth * mLeftMenuOnScreen);
							childView.layout(childLeft, lp.topMargin, childLeft + menuWidth,
									lp.topMargin + menuHeight);

							break;
						case MODE_DRAG_TOP:
							childTop= -menuHeight + (int) (menuHeight * mLeftMenuOnScreen);
							childView.layout(lp.leftMargin,childTop, lp.leftMargin + menuWidth,
									childTop+ menuHeight);
							break;

						case MODE_DRAG_BOTTOM:
							childTop= getHeight()- (int) (menuHeight * mLeftMenuOnScreen);
							childView.layout(lp.leftMargin,childTop, lp.leftMargin + menuWidth,
									childTop+ menuHeight);
							break;
					}
				}else{
					childView.layout(l,t,r,b);
				}
			}
		}

	}

	public void close() {
		mLeftMenuOnScreen = 0.f;
		smoothSlideTo(mLeftMenuOnScreen);
	}

	public void open() {
		mLeftMenuOnScreen = maxShowScale;
		smoothSlideTo(mLeftMenuOnScreen);
	}
	/**
	 * 滑动试图，1f则为滑出屏幕，0f则滑到原始位置，目前为横向滑
	 */
	private boolean smoothSlideTo(float slideOffset) {
		int childWidth = getContentView().getWidth();
		int childHeight = getContentView().getHeight();

		int x=0;
		int y=0;
		switch (getMode()){
			case MODE_DRAG_LEFT:
				x = (int) ((slideOffset * childWidth)-childWidth);
				break;

			case MODE_DRAG_RIGHT:
				x = (int) (getWidth()-slideOffset * childWidth);
				break;
			case MODE_DRAG_TOP:
				y = (int) ((slideOffset * childHeight)-childHeight);
				break;
			case MODE_DRAG_BOTTOM:
				y = (int) (getHeight()-slideOffset * childHeight);
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
	float downX=0;
	float downY=0;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(getSlideable()||(!getSlideable()&&ev.getAction()==MotionEvent.ACTION_UP)){
			mDragHelper.shouldInterceptTouchEvent(ev);
		}
		//如果在滑动期间则要拦截子类
		if(mLeftMenuOnScreen>0&&mLeftMenuOnScreen<1){
             return true;
		}
		if(ev.getAction()==MotionEvent.ACTION_DOWN){
			downX=ev.getX();
			downY=ev.getY();
		}else if(ev.getAction()==MotionEvent.ACTION_MOVE) {
			if(mRecyclerView!=null){
			switch (getMode()) {
				//如果mRecyclerView不等于空，则要多久判断isRecyclerView的滚动情况，后续会加上nestedscrollview
				case MODE_DRAG_LEFT:
					if ((mRecyclerView!=null?isRecyclerViewRight:false )&& (downX - ev.getX()) > 1) return true;
				case MODE_DRAG_RIGHT:
					if ((mRecyclerView!=null?isRecyclerViewLeft:false )&& (ev.getX() - downX) > 1) return true;
				case MODE_DRAG_TOP:
					if ((mRecyclerView!=null?isRecyclerViewBottom:false )&& (downY - ev.getY()) > 1) return true;
				case MODE_DRAG_BOTTOM:
					if ((mRecyclerView!=null?isRecyclerViewTop:false )&& (ev.getY() - downY) > 1)return true;

			}
			switch (getMode()) {
				case MODE_DRAG_LEFT:
				case MODE_DRAG_RIGHT:
					//如果是竖向的，则x>y则需要拦截子类
					if (!isRecyclerViewHorizontal()) {
						if ((Math.abs(ev.getX() - downX) - Math.abs(ev.getY() - downY)) > 30) {
							return true;
						}
					}
					break;
				case MODE_DRAG_TOP:
				case MODE_DRAG_BOTTOM:
					//如果是横向的，则y>x则需要拦截子类
					if (isRecyclerViewHorizontal()) {
						if ((Math.abs(ev.getY() - downY) - Math.abs(ev.getX() - downX)) > 30) {
							return true;
						}
					}
					break;
			    }
			}else{
				//如果是其他控件则超过20，则拦截
				switch (getMode()) {
					case MODE_DRAG_LEFT:
						if ((downX - ev.getX()) > 20) return true;
					case MODE_DRAG_RIGHT:
						if ((ev.getX() - downX) > 20) return true;
					case MODE_DRAG_TOP:
						if ((downY - ev.getY()) > 20) return true;
					case MODE_DRAG_BOTTOM:
						if ((ev.getY() - downY) > 20)return true;

				}
			}
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(getSlideable()||(!getSlideable()&&event.getAction()==MotionEvent.ACTION_UP)){
			mDragHelper.processTouchEvent(event);
		}
		//如果是第一种模式，则判断是否点击外部，点击外部关闭菜单栏
		//第二种模式，不提给点击外部
		if(isOutside&&isopen&&event.getAction()==MotionEvent.ACTION_UP){
			if(Math.abs(event.getX()-downX)<50&&Math.abs(event.getY()-downY)<50){
				if(!(event.getX()>=getContentView().getX()&&event.getX()<=getContentView().getX()+getContentView().getWidth()&&
						event.getY()>=getContentView().getY()&&event.getY()<=getContentView().getY()+getContentView().getHeight())){
					close();
					return true;
				}
			}

		}else{

		}

		boolean isReturn=false;
		//要根据down来判断，如果触控边缘的话，要激活return true,由于内容控件被BaseDragLayout覆盖住，return true的话则不会执行内容的点击试下，return false则不会滑动该菜单
		//所以理想的方法是，点击边缘跟mLeftMenuOnScreen>0,也就是激活菜单触控，return true，关闭的时候且没有点击边缘的话则return false不处理滑动，处理内容触摸
		switch (getMode()){
			case MODE_DRAG_LEFT:
				isReturn=event.getX()<20;
				break;
			case MODE_DRAG_RIGHT:
				isReturn=getWidth()-event.getX()<20;
				break;
			case MODE_DRAG_TOP:
				isReturn=event.getY()<20;
				break;
			case MODE_DRAG_BOTTOM:
				isReturn=getHeight()-event.getY()<20;
				break;
		}

		return mLeftMenuOnScreen==0?(isReturn):true;
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

	/**判断是否是打开状态*/
	public boolean isOpen(){
		return isopen;
	}
	/**是否可以滑动*/
	public void setSlideable(boolean isSlideable){
		this.isSlideable=isSlideable;
	}
	/**是否可以滑动*/
	public boolean getSlideable(){
		return  isSlideable;
	}
	/**是否可以边缘滑动*/
	public boolean isEdgeSlide() {
		return isEdgeSlide;
	}
	/**设置是否可以边缘滑动*/
	public void setEdgeSlide(boolean edgeSlide) {
		isEdgeSlide = edgeSlide;
	}
	/**点击外部取消*/
	public void setCanceledOnTouchOutside(boolean isOutside){
		this.isOutside=isOutside;
	}
	/**点击外部取消*/
	public boolean isOutside() {
		return isOutside;
	}
	public float getMaxShowScale() {
		return maxShowScale;
	}
	/**显示出来的最高比例*/
	private void setMaxShowScale(@FloatRange(from=0.0, to=1.0)float maxShowScale) {
		this.maxShowScale = maxShowScale;
	}
	/**支持RecyclerView的联动，先滑动RecyclerView，然后在滑动该视图*/
	public void setRecyclerView(final RecyclerView mRecyclerView){
		this.mRecyclerView=mRecyclerView;
		//监听滚动状态
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if(Math.abs(dy)>10){
					isRecyclerViewTop=false;
					isRecyclerViewBottom=false;
				}
				if(Math.abs(dx)>10){
					isRecyclerViewLeft=false;
					isRecyclerViewRight=false;
				}
				if (!(recyclerView.canScrollVertically(-1))&&!isRecyclerViewHorizontal()) {
					//top
					isRecyclerViewTop=true;
				}else if (!(recyclerView.canScrollVertically(1))&&!isRecyclerViewHorizontal()) {
					//bottom
					isRecyclerViewBottom=true;
				}else if (!(recyclerView.canScrollHorizontally(-1))&&isRecyclerViewHorizontal()) {
					//left
					isRecyclerViewLeft=true;
				}else if (!(recyclerView.canScrollHorizontally(1))&&isRecyclerViewHorizontal()) {
					//right
					isRecyclerViewRight=true;
				}

			}
		});
	}
	/**
	 * 是否是横向的，如果是则不显示加载样式
	 */
	private boolean isRecyclerViewHorizontal() {
		if(mRecyclerView==null)return false;
		RecyclerView.LayoutManager mLayoutManager = mRecyclerView.getLayoutManager();
		if (mLayoutManager instanceof LinearLayoutManager) {
			if (((LinearLayoutManager) mLayoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
				return true;
			}
		}
		return false;
	}
}
