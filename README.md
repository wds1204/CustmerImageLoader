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
            return bitmap;        }
        try {
    //第二步：磁盘缓存中获取（在磁盘缓存的查找和添加比较复杂）
         bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
         Log.d(TAG, "loadBitmapFromDiskCache ,bitmap:" + bitmap);
         if (bitmap != null) {
 
                return bitmap;            }
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
     / **
 DisLruche的缓存添加操作是通过Editor完成的，Editor表示一个缓存对象的编辑对象，用图片缓存作为例子，用获取图片的url所对应的key,然后根据key就可以通过editor()来获取Editor对象，如果这个缓存对象正在编辑，那么edit（）会返回null，及这个DiskLruche不允许同时编辑一个缓存对象。
   通过key,获得了Editor对象，如果当前不存在其他Editor对象，那么editor（）就会返回一个新的Editor对象。
    
  
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
    
### 异步加载： 

      采用线程池和Handler来提供ImageLoaderd的
    
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
