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
        if (!MyUtils.isWifi(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("初次使用会从网络下载大概5MB的图片，确认要下载吗？");
            builder.setTitle("注意");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCanGetBitmapFromNetWork = true;
                    mImageAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("否", null);
            builder.show();
        } else {
            mCanGetBitmapFromNetWork = true;
        }

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
