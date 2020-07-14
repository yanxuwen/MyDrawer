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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextDragLayout = (TextDragLayout)findViewById(R.id.includ_text_drag);
        mTextDragLayout.setEdgeSize(20000);
        mTextDragLayout.setMoveEventSize(200);

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

}
