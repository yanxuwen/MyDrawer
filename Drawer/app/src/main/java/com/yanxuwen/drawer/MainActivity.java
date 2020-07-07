package com.yanxuwen.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.yanxuwen.mydrawer.BaseDragLayout;

public class MainActivity extends AppCompatActivity {
    TextDragLayout mTextDragLayout;
    TextDragLayout mTextDragLayout2;
    TextDragLayout mTextDragLayout3;
    TextDragLayout mTextDragLayout4;
    TextDragLayout mTextDragLayout5;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextDragLayout = (TextDragLayout)findViewById(R.id.includ_text_drag);
//        mTextDragLayout2 = (TextDragLayout)findViewById(R.id.includ_text_drag2);
//        mTextDragLayout3 = (TextDragLayout)findViewById(R.id.includ_text_drag3);
//        mTextDragLayout4 = (TextDragLayout)findViewById(R.id.includ_text_drag4);
//        mTextDragLayout5= (TextDragLayout)findViewById(R.id.includ_text_drag5);
        mTextDragLayout.setEdgeSize(20000);
        mTextDragLayout.setMoveEventSize(200);
        mTextDragLayout.setOnDragViewStatusListener(new BaseDragLayout.OnDragViewStatusListener() {
            @Override
            public void onDragViewStatus(boolean isOpen) {
                Log.e("xxxx","右边抽屉是否打开"+isOpen);
            }
        });
        mTextDragLayout.setOnDragViewOffsetListener(new BaseDragLayout.OnDragViewOffsetListener() {
            @Override
            public void onDragViewOffset(float Offset) {
                Log.e("xxxx","右边抽屉偏移量"+Offset);
            }
        });

    }
    public void onFullRight(View v){
        mTextDragLayout.open();
    }

    public void onFullLeft(View v){
        Toast.makeText(this,"测试",Toast.LENGTH_SHORT).show();
    }

    public void onFullBottom(View v){
        Toast.makeText(this,"测试",Toast.LENGTH_SHORT).show();
    }

    public void onFullTop(View v){
        Toast.makeText(this,"测试",Toast.LENGTH_SHORT).show();
    }
//    public void onFullLeft(View v){
//        mTextDragLayout2.open();
//    }
//    public void onFullBottom(View v){
//        mTextDragLayout3.open();
//    }
//    public void onFullTop(View v){
//        mTextDragLayout4.open();
//    }
//    public void onFullAlpha(View v){
//        mTextDragLayout5.open();
//    }

    public void onControlDrawer(View v){
        Intent intent = new Intent(MainActivity.this, DrawerActivity.class);
        startActivity(intent);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTextDragLayout != null && mTextDragLayout.isOpen()) {
                mTextDragLayout.close();
                return true;
            }
            if (mTextDragLayout2 != null && mTextDragLayout2.isOpen()) {
                mTextDragLayout2.close();
                return true;
            }
            if (mTextDragLayout3 != null && mTextDragLayout3.isOpen()) {
                mTextDragLayout3.close();
                return true;
            }
            if (mTextDragLayout4 != null && mTextDragLayout4.isOpen()) {
                mTextDragLayout4.close();
                return true;
            }else{
                finish();
            }

        }
        return false;
    }
}
