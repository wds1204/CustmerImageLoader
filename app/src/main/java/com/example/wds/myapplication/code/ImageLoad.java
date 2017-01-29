package com.example.wds.myapplication.code;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.wds.myapplication.DiskLruCache.DiskLruCache;
import com.example.wds.myapplication.DiskLruCache.IOUtil;
import com.example.wds.myapplication.R;
import com.example.wds.myapplication.util.MyUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wds on 2017/1/26.
 * Captain 加油吧!!!
 * GitHub:https://github.com/wds1204
 * Email:wdsmyhome@hotmail.com
 * 在ImageLoade初始化的时候，创建LruCache和DisLruCache，然后还要提供来完成缓存和获取的功能(内存缓存和磁盘缓存)
 */
public class ImageLoad {
    private static ImageLoad sInstace;

    private static final String TAG = "ImageLoad";
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;//50MB
    private static final int TAG_KEY_URL = R.id.imageloader_url;
    private static final int MESSAGE_POST_RESULT = 1;
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int DISK_CACHE_INDEX = 0;


    private final Context mContext;
    private LruCache<String, Bitmap> mBitmapLruCache;
    private DiskLruCache mDiskLruCache;
    private boolean mIsDiskLruCacheCreated = false;
    private ImageResizer mIageResizer = new ImageResizer();
    //采用主线程的Looper来构造对象，这样使得Imageloader可以再主线程中构造了，
    // 另外为了解决View的复用所导致的列表错位问题，再给Imageview设置图片之前都会检查他的url有没有发生变化，
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            String url = (String) imageView.getTag(TAG_KEY_URL);
            Log.d(TAG, "mMainHandler url：" + url + ",result url:" + result.url);
            if (url.equals(result.url)) {
                imageView.setImageBitmap(result.bitmap);
            } else {
                Log.d(TAG, "set image bitmap,but url has changed, ignored!");
            }
        }
    };

    private static ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override

        public Thread newThread(Runnable r) {
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10l;
    /*核心线程数为CPU+1，最大容量为CPU*2+1,线程闲置超时时长为10秒*/
    public static Executor THREAD_POOL_EXCUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE
            , MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

    private ImageLoad(Context context) {
        mContext = context.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;

        mBitmapLruCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int /*sizeof计算缓存对象的大小，这里的大小的单位需要总容量的单位一致，总容量的是KB所以这里也是KB*/sizeOf(String key, Bitmap bitmap) {
                Log.d(TAG, "bitmap:大小" + bitmap.getRowBytes() /*用于计算位图每一行所占用的内存字节数*/ * bitmap.getHeight() / 1024);
                return bitmap.getRowBytes() /*用于计算位图每一行所占用的内存字节数*/ * bitmap.getHeight() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
        File diskCacheDir = MyUtils.getDiskCacheDir(mContext, "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        if (MyUtils.getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void bindBitmap(String url, ImageView imageView) {
        bindBitmap(url, imageView, 0, 0);
    }

    /**
     * 异步加载接口的设计：尝试从内存缓存中读取图片,如果读取成功就直接返回接口，否则会在线程中去调用loadBitmap方法。
     * 当图片加载成功后再将图片、图片地址以及绑定的imageview封装成一个LoaderResult对象，
     * 然后通过mMainHandler向主线程发送一个消息
     *
     * @param url
     * @param imageView
     * @param reqWidth
     * @param reqHeight
     */

    public void bindBitmap(final String url, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_KEY_URL, url);
//        Bitmap bitmap = loadBitmapFromMemCache(url);
//        if (bitmap != null) {
//            imageView.setImageBitmap(bitmap);
//            return;
//        }
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = loadBitmap(url, reqWidth, reqHeight);

                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, url, bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();

                }

            }
        };
        THREAD_POOL_EXCUTOR.execute(loadBitmapTask);

    }

    /**
     * 内存缓存的添加方法
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (mBitmapLruCache.get(key) == null) {
            mBitmapLruCache.put(key, bitmap);
        }
    }

    /**

    /**
     * 同步加载：先从内存中读取图片(因为是内存读取所以不需要再次保存)，
     * 接着尝试从磁盘缓存中读取，最后才是从网络战拉去图片(从网络中拉去图后在保存到本地)
     *
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap loadBitmap(String url, int reqWidth, int reqHeight) {
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmapFromMemCache ,url:" + url);
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmapFromDiskCache ,bitmap:" + bitmap);
            if (bitmap != null) {
                Log.d(TAG, "loadBitmapFromDiskCache ,url:" + url);
                return bitmap;
            }
            Log.d(TAG, "loadBitmapFromHttp111111");

            bitmap = loadBitmapFromHttp(url, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmapFromDiskCache ,url:" + url);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap != null && !mIsDiskLruCacheCreated) {

            bitmap = downLoadBitmapFromUrl(url);
        }
        return bitmap;
    }

    /**
     * 下载从网络拉取的图片
     *
     * @param urlString
     * @return
     */
    private Bitmap downLoadBitmapFromUrl(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    /**
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        /*DiskLruCache的缓存添加*/
        String key = MyUtils.toMD5(url);
        Log.d(TAG, "loadBitmapFromHttp  key==" + key);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);//Editor：表示一个缓存对象的编辑对象
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);//Editor可以得到一个文件输出流，
            Log.e(TAG, "outputStream得到一个文件输出流" + outputStream);
            if (downLoadUrlToStream(url, outputStream)) {//当从网络上下载图片时候通过这个文件输出流写入到文件系统中
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();

        }
        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }

    private boolean downLoadUrlToStream(String urlString
            , OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);

            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "downLoadBitmap failed." + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            IOUtil.closeQuietly(out);
            IOUtil.closeQuietly(in);

        }

        return false;
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.d(TAG, "load bitmap from Ui Thread ,it is not recommended!)");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        String key = MyUtils.toMD5(url);
        Log.d(TAG, "key==" + key);

        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            //从磁盘中获取压缩后的图片
            bitmap = mIageResizer.decodeSampleBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
            //保存到内存中
            if (bitmap != null) {
                addBitmapToMemoryCache(key, bitmap);
            }
        }


        return bitmap;
    }

    private Bitmap loadBitmapFromMemCache(String uri) {
        final String key = MyUtils.toMD5(uri);
        Bitmap bitmap=mBitmapLruCache.get(key);
        return bitmap;
    }

    /**
     * 单例实例化
     * @param context
     * @return
     */
    public static ImageLoad build(Context context) {
        if (sInstace == null) {
            synchronized (ImageLoad.class) {
                sInstace = new ImageLoad(context);
            }
        }
        return sInstace;
    }

    private class LoaderResult {
        public ImageView imageView;
        public String url;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String url, Bitmap bitmap) {
            this.imageView = imageView;
            this.url = url;
            this.bitmap = bitmap;
        }
    }

}
