package com.yanxuwen.drawer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.yanxuwen.mydrawer.DrawerLayout;

public class DrawerActivity extends Activity implements DrawerLayout.OnDrawerStatusListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawer);
		final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.dragLayout);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mDrawerLayout.open();
			}
		}, 5000);
		mDrawerLayout.setOnDrawerStatusListener(this);
	}

	@Override
	public void onStatus(boolean isOpen) {
		Toast.makeText(this, isOpen ? "打开" : "关闭", Toast.LENGTH_LONG).show();
	}
}
