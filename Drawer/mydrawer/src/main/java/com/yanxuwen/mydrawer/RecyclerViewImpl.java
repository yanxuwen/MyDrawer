package com.yanxuwen.mydrawer;

import android.view.View;

/**
 * @author bsnl_yanxuwen
 * @date 2021/3/8 14:59
 * Description :
 */
public interface RecyclerViewImpl {
    boolean canScrollHorizontally(int direction);

    boolean canScrollVertically(int direction);

    View getRecyclerView();

}
