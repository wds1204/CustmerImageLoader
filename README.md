# CustmerImageLoader
ImageLoader的实现;Bitmap的高效加载方式、LruCache以及DiskLruCache
##这个ImageLoader具备如下功能：
### 图片的同步加载;
### 图片的异步加载；
### 图片的压缩；
### 内存缓存；
### 磁盘缓存；
### 网络拉取： 
### /**
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
    
