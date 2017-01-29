package com.example.wds.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.wds.myapplication.code.ImageLoad;
import com.example.wds.myapplication.util.MyUtils;

/**
 * Created by wds on 2017/1/27.
 * Captain 加油吧!!!
 * GitHub:https://github.com/wds1204
 * Email:wdsmyhome@hotmail.com
 */
public class ImageAdapter extends BaseAdapter {
    private static final String TAG = "ImageAdapter";
    private  boolean mCanGetBitmapFromNetWork=false;
    private Context context;
    private Drawable drawable;

    private ImageLoad mImageLoad;
    private boolean mIsGridViewIdle = true;
    String[] imageUrls = {
            "http://p14.qhimg.com/t019ecd5d7adc1a44c6.jpg",
            "http://pic21.nipic.com/20120520/10129128_222457178140_2.jpg",
            "http://images.huanqiu.com/sarons/2013/07/116d17a4df51bc8d73114145f427a8f8.jpg",
            "http://www.tpqq.com/newpic/20120626/1-120626225101.jpg",
            "http://i1.sinaimg.cn/ent/s/j/2011-08-05/U3987P28T3D3378745F326DT20110805151015.jpg",
            "http://himg2.huanqiu.com/attachment2010/2012/0815/20120815010548174.jpg",
            "http://ugc.qpic.cn/baikepic2/22898/cut-20141209132550-1229160613.jpg/0",
            "http://pic4.nipic.com/20090920/2579038_083829459784_2.jpg",
            "http://photocdn.sohu.com/20100319/Img270951972.jpg",
            "http://photocdn.sohu.com/20100224/Img270408223.jpg",
            "http://blog.gxnews.com.cn/upload/images/2007/8/u10003/20078301392558849.jpg",
            "http://image11.m1905.cn/uploadfile/2012/0511/20120511111911671.jpg",
            "http://himg2.huanqiu.com/attachment2010/2013/1011/20131011061834728.jpg",
            "http://cdn.duitang.com/uploads/item/201205/28/20120528121739_Umany.jpeg",
            "http://www.tpqq.com/newpic/20120626/1-120626225111-50.jpg",
            "http://i9.hexunimg.cn/2014-02-06/161922457.jpg",
            "http://pic4.nipic.com/20090920/2579038_083829376718_2.jpg",
            "http://yule.kantsuu.com/UploadFiles/201101/20110119112715545.jpg",
            "http://www.tpqq.com/newpic/20120626/1-120626225125.jpg",
            "http://a2.att.hudong.com/56/60/16300001203327134076608372476.jpg",
            "http://www.sinaimg.cn/dy/slidenews/4_img/2014_23/704_1329212_736057.jpg",
            "http://www.tpqq.com/newpic/20120626/1-120626225102.jpg",
            "http://pic21.nipic.com/20120520/10129128_225642380142_2.jpg",
            "http://a2.att.hudong.com/27/60/16300001203327134076607120542.jpg",
            "http://photocdn.sohu.com/20100609/Img272666987.jpg",
            "http://img4.duitang.com/uploads/item/201205/28/20120528121855_UwYcy.jpeg",
            "http://a4.att.hudong.com/61/80/01300000930043128162803219827.jpg",
            "http://n.sinaimg.cn/transform/20141203/cesifvx0154395.jpg",
            "http://img6.cache.netease.com/house/2014/4/28/20140428154956c1344.jpg",
            "http://i3.cqnews.net/fashion/attachement/jpg/site82/20120628/bc305bca96f21156590125.jpg",
            "http://i0.qhimg.com/t01884f47db2ce24ab0.jpg",
            "http://photocdn.sohu.com/20071022/Img252783293.jpg",
            "http://a0.att.hudong.com/52/73/01300000432220130009733683588.jpg"
    };


    public ImageAdapter(Context context, boolean mCanGetBitmapFromNetWork) {
        this.context = context;
        mImageLoad = ImageLoad.build(context);
        drawable = context.getResources().getDrawable(R.drawable.image_default);
        boolean wifi = MyUtils.isWifi(context);
        this.mCanGetBitmapFromNetWork = mCanGetBitmapFromNetWork;


    }

    @Override
    public int getCount() {
        return imageUrls.length;
    }

    @Override
    public String getItem(int position) {
        return imageUrls[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.image_list_item, parent, false);
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageView imageView = holder.imageView;

        String tag = (String) imageView.getTag(R.id.imageloader_url);
        String url = getItem(position);
        Log.d(TAG, "tag==" + tag + ",url==" + url);
        if (!url.equals(tag)) {
            imageView.setImageDrawable(drawable);

        }
        if (mIsGridViewIdle&&mCanGetBitmapFromNetWork) {
            //imageView.setTag(url);
            mImageLoad.bindBitmap(url, imageView);
        }

        return convertView;
    }

    public void notifyDataSetChanged(boolean mIsGridViewIdle) {
        this.mIsGridViewIdle = mIsGridViewIdle;
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView imageView;
    }

}
