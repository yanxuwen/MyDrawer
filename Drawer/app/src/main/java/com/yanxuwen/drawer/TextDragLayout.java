package com.yanxuwen.drawer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.yanxuwen.mydrawer.BaseDragLayout;

/**
 * 作者：严旭文 on 2017/2/16 15:37
 * 邮箱：420255048@qq.com
 */
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
        iv_cover= (ImageView) findViewById(R.id.iv_cover);
    }
}
