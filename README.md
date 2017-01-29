# CustmerImageLoader
ImageLoader的实现;Bitmap的高效加载方式、LruCache以及DiskLruCache
##这个ImageLoader具备如下功能：
### 图片的同步加载;
### 图片的异步加载；
### 图片的压缩；
### 内存缓存；
### 磁盘缓存；
### 网络拉取： 
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
        //第一步：内存缓存获取
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmapFromMemCache ,url:" + url);
            return bitmap;
        }

        try {
            //第二步：磁盘缓存中获取（在磁盘缓存的查找和添加比较复杂）
            bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmapFromDiskCache ,bitmap:" + bitmap);
            if (bitmap != null) {
                Log.d(TAG, "loadBitmapFromDiskCache ,url:" + url);
                return bitmap;
            }
            Log.d(TAG, "loadBitmapFromHttp111111");
            //第三步：网络拉取（当从网络下载图片是，通过一个文件输出流写入在系统文件）
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
    
    DisLruche的缓存添加操作是通过Editor完成的，Editor表示一个缓存对象的编辑对象，用图片缓存作为例子，用获取图片的url所对应的key,然后根据key就可以通过editor()来获取Editor对象，如果这个缓存对象正在编辑，那么edit（）会返回null，及这个DiskLruche不允许同时编辑一个缓存对象。
   通过key,获得了Editor对象，如果当前不存在其他Editor对象，那么editor（）就会返回一个新的Editor对象。
