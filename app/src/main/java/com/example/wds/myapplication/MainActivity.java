package com.example.wds.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.NumberPicker;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private GridView gridview;
    private ImageAdapter mImageAdapter;
    private boolean mIsGridViewIdle;
    private boolean mCanGetBitmapFromNetWork;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridview = (GridView) findViewById(R.id.gridview);


        mImageAdapter = new ImageAdapter(this,mCanGetBitmapFromNetWork);
        gridview.setAdapter(mImageAdapter);

        gridview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d(TAG, "scrollState:" + scrollState);
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    mIsGridViewIdle = true;

                } else {
                    mIsGridViewIdle = false;
                }
                mImageAdapter.notifyDataSetChanged(mIsGridViewIdle);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });


    }


}
