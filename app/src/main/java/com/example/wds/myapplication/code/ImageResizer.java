package com.example.wds.myapplication.code;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * Created by wds on 2017/1/26.
 * Captain 加油吧!!!
 * GitHub:https://github.com/wds1204
 * Email:wdsmyhome@hotmail.com
 * 完成图片的压缩功能
 * <p/>
 * 1:将BitmapFactoty.Options的inJustDecodeBounds参数设置为true(只会解析图片的原始图片宽高，并不会真正地加载图片)
 * 2：从将BitmapFactoty.Options中获取图片的原始宽高信息，它们对应于out Width和outHeight
 * 3:根据采用率地规则并结合目标View的所需大小计算出采样率inSampleSize
 * 4:将BitmapFactoty.Options的inJustDecodeBounds参数设为false,然后重新加载图片
 */
public class ImageResizer {

    private static final String TAG = "imageView";

    public ImageResizer() {
    }

    /**
     * 从Resource获取图片进行压缩
     *
     * @param res
     * @param resId
     * @param reqWidth 压缩至的宽度
     * @param reHeight 压缩至的高度
     * @return
     */
    public static Bitmap decodeSampledFromResource(Resources res, int resId, int reqWidth, int reHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateSampleSize(options, reHeight, reqWidth);
        options.inJustDecodeBounds = false;
        Log.d(TAG, "BitmapFactory.decodeResource(res, resId, options)=" + BitmapFactory.decodeResource(res, resId,
                options));


        return BitmapFactory.decodeResource(res, resId, options);
    }

    public Bitmap decodeSampleBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reHeight) {
        BitmapFactory.Options opints = new BitmapFactory.Options();
        opints.inJustDecodeBounds = true;
        opints.inSampleSize = calculateSampleSize(opints, reHeight, reqWidth);
        opints.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, opints);
    }

    /**
     * 根据采用率地规则并结合目标View的所需大小计算出采样率inSampleSize
     *
     * @param options
     * @param reHeight
     * @param reqWidth
     * @return
     */
    private static int calculateSampleSize(BitmapFactory.Options options, int reHeight, int reqWidth) {
        if (reHeight == 0 || reqWidth == 0) {
            return 1;
        }
        final int outWidth = options.outWidth;
        final int outHeight = options.outHeight;
        Log.d(TAG, "outWidth==" + outWidth + ",outHeight==" + outHeight);
        int inSampleSize = 1;
        /*根据压缩成所需要的宽高：通过循环每次对其宽高进行减半*/
        if (outWidth > reqWidth || outHeight > reHeight) {
            int halfHeight = outHeight / 2;
            int halfWidth = outWidth / 2;
            while ((halfHeight / inSampleSize) / inSampleSize >= reHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d(TAG, "inSampleSize==" + inSampleSize);
        return inSampleSize;
    }

}
