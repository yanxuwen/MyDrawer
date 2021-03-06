
# 前言 该抽屉实现了可以定义4边的抽屉，大小随意控制。可实现（仿今日头条的评论抽屉功能）
#### 博客地址：https://www.jianshu.com/p/2abb8c20817f

## 先看下效果图	
![GIF.gif](http://upload-images.jianshu.io/upload_images/6835615-d55e8a1d4b96d8e2.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 依赖：
   compile 'com.yanxuwen.mydrawer:mydrawer:1.2.2’
# 实现：
#### 1.首先自定义类。TextDragLayout 
~~~
public class TextDragLayout extends BaseDragLayout {
    ImageView iv_cover;
    public TextDragLayout(Context context) {
        super(context);
    }

    public TextDragLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TextDragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void onViewStatus(boolean isOpen) {
    }

    @Override
    public void onViewOffset(float mOffset) {
        if(iv_cover!=null){
            iv_cover.setAlpha((float) (mOffset*0.8));
        }
    }

    @Override
    public void initView() {
        setContentView(findViewById(R.id.layout_drag));
        iv_cover= (ImageView) findViewById(R.id.iv_cover);
    }
}
~~~
其实就是继承BaseDragLayout，onViewStatus开关回调，onViewOffset是偏移量回调，initView不想说了。其实没什么东西，这里可以实现你自己的逻辑。

#### 2.xml设置(在你需要的界面上加上该布局)
~~~
<?xml version="1.0" encoding="utf-8"?>
<com.yanxuwen.drawer.TextDragLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/includ_text_drag"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:drag_right= "@+id/layout_drag">
 
    <ImageView
        android:id="@+id/iv_cover"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#000000"
        android:alpha="0"
        />
    <include layout="@layout/test_drag" />
</com.yanxuwen.drawer.TextDragLayout>

~~~
代码iv_cover其实就是阴影部分，由于我们不提供任何阴影的控制，因为不同的需求，阴影不同，所以我们设置阴影很简单，就是在布局在加个子类，透明度为0然后在TextDragLayout 的onViewOffset偏移量回调控制阴影透明度变化，这样就会实现阴影效果。

#3.MainActivity
 ~~~
打开我们只需要mTextDragLayout.open();
关闭只需要 mTextDragLayout.close();
//提供是否可以根据手势滑动，默认true,  false为关闭
 mTextDragLayout.setSlideable(true);
//提供是否可以滑动屏幕边缘来展开抽屉，默认true,  false为关闭功能
 mTextDragLayout.isEdgeSlide(true);
记得在控制back键，返回的时候我们要关闭掉
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTextDragLayout != null && mTextDragLayout.isOpen()) {
                mTextDragLayout.close();
                return true;
            } else{
                finish();
            }

        }
        return false;
    }
~~~
# 4.基本流程就是这样，很简单，但是如果你要实现类似今日头条那样，有个列表，由于都是上下滑动，所以会导致列表不会滑动，之后滑动抽屉，我们只要简单的加上下面这句话即可
~~~
mTextDragLayout.setRecyclerView(mRecyclerView);
加了上面那句话你就会发现，会先滑动列表，列表滑动到顶才会滑动抽屉，是不是跟今日头条一样，支持4个方向的冲突，
~~~
