package com.sec.kalman_test;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  {

    private GLSurfaceView mSurfaceView;
    TextView mText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        setContentView(R.layout.glview);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.glview);
        mSurfaceView.setRenderer(new CustomRenderer(this));

        mText = (TextView) findViewById(R.id.something);

        //mSurfaceView = new GLSurfaceView(this);
        //mSurfaceView.setRenderer(new CustomRenderer(this));
        //setContentView(mSurfaceView);

    }


}
