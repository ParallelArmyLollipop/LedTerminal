package com.eric.terminal.led;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.eric.terminal.led.Bean.MediaBean;
import com.eric.terminal.led.Bean.SystemBean;
import com.eric.terminal.led.Bean.TaskBean;
import com.eric.terminal.led.Manager.ApiManager;
import com.eric.terminal.led.Manager.Constants;
import com.eric.terminal.led.Manager.XmlPullManager;
import com.orhanobut.logger.Logger;
import com.shiki.okttp.OkHttpUtils;
import com.shiki.utils.ApkUtils;
import com.shiki.utils.DateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,MediaPlayer.OnCompletionListener
        ,MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,MediaPlayer.OnPreparedListener
        ,MediaPlayer.OnSeekCompleteListener,MediaPlayer.OnVideoSizeChangedListener {
    @Bind(R.id.iv_main)
    ImageView mIvMain;
    @Bind(R.id.video_surface)
    SurfaceView mVideoSurface;
    private SurfaceHolder mHolder;

    String mEquipId = "2016060001";
    String mMediaFileName = "media.xml";
    String mSystemFileName = "system.xml";
    String mMediaFileNameTemp = "media-temp.xml";
    String mSystemFileNameTemp = "system-temp.xml";
    String mBaseFileDir;
    SystemBean mSystemBean;
    MediaBean mMediaBean;
    MediaPlayer mPlayer;

    int mJpegShowtime = 15;
    int mPosition;
    long mMediaStartTime;

    OkHttpClient mClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Logger.init();

        //给SurfaceView添加CallBack监听
        mHolder = mVideoSurface.getHolder();
        mHolder.addCallback(this);
        //为了可以播放视频或者使用Camera预览，我们需要指定其Buffer类型
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //下面开始实例化MediaPlayer对象
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnInfoListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnVideoSizeChangedListener(this);

        //首先先读取默认配置文件
        mBaseFileDir = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"Android"+File.separator+"data"+File.separator+this.getPackageName();
        File baseFileDir = new File(mBaseFileDir);
        if (!baseFileDir.exists()) {
            baseFileDir.mkdirs();
        }
        File mediaFile = new File(mBaseFileDir, mMediaFileName);
        File systemFile = new File(mBaseFileDir, mSystemFileName);
        if (mediaFile.exists() && systemFile.exists()) {
            mSystemBean = XmlPullManager.pullXmlParseSystem(systemFile);
            mMediaBean = XmlPullManager.pullXmlParseMedia(mediaFile);
        } else {
            mSystemBean = XmlPullManager.pullXmlParseSystem(this, R.xml.system);
            mMediaBean = XmlPullManager.pullXmlParseMedia(this, R.xml.media);
        }

        if(mSystemBean != null){
            //设置图片播放时长
            mJpegShowtime = mSystemBean.getJpegshowtime();
            //设置媒体音量
            mPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
            mPlayer.setVolume(mSystemBean.getVolume()/10.0f,mSystemBean.getVolume()/10.0f);
        }

        //播放默认配置文件中媒体
        /*if(mMediaBean!=null){
            //设置任务style（暂时全部全屏）
            switch (mMediaBean.getStyle()){
                case Constants.TASK_STYLE.FULL_SCREEN:
                    break;
                default:
                    break;
            }

            for(int i=0;i<mMediaBean.getTaskList().size();i++){
                mPosition = i;
                TaskBean tb = mMediaBean.getTaskList().get(i);
                if(showMedia(tb)){
                    break;
                }
            }
        }*/

        //起心跳获取是否需要更新
        startHeartbeat1();

        /*Observable.interval(0, 60, TimeUnit.SECONDS).map(new Func1<Long, MediaBean>() {
            @Override
            public MediaBean call(Long aLong) {
                MediaBean mb = new MediaBean();
                List<TaskBean> tbList = new ArrayList<>();
                for (int i=0;i<10;i++){
                    TaskBean tb = new TaskBean();
                    tb.setUuid("uuid:"+i);
                    tbList.add(tb);
                }
                mb.setTaskList(tbList);
                return mb;
            }
        }).flatMap(new Func1<MediaBean, Observable<TaskBean>>() {
            @Override
            public Observable<TaskBean> call(MediaBean mediaBean) {
                return Observable.from(mediaBean.getTaskList());
            }
        }).map(new Func1<TaskBean, String>() {
            @Override
            public String call(TaskBean taskBean) {
                return taskBean.getUuid()+"----";
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Logger.d("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Logger.d("onError");
            }

            @Override
            public void onNext(String s) {
                Logger.d("onNext:"+s);
            }
        });*/
    }

    private boolean showMedia(TaskBean tb){
        boolean result = false;
        String fileName = tb.getUuid() + ".avi";
        if(tb.getType().equalsIgnoreCase(Constants.TASK_TYPE.JPEG)){
            fileName = tb.getUuid() + ".jpg";
        }
        File file = new File(mBaseFileDir,fileName);
        if(file.exists()){
            result = true;
            mMediaStartTime = DateUtils.getMillis(new Date());
            if(tb.getType().equalsIgnoreCase(Constants.TASK_TYPE.JPEG)){
                mVideoSurface.setVisibility(View.GONE);
                mIvMain.setVisibility(View.VISIBLE);
                Bitmap bm = BitmapFactory.decodeFile(mBaseFileDir + File.separator + fileName);
                mIvMain.setImageBitmap(bm);
                //设定间隔时间
                Observable.timer(mJpegShowtime,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        Logger.d("showMedia onNext");
                        mPosition = (mPosition + 1) % mMediaBean.getTaskList().size();
                        TaskBean taskBean = mMediaBean.getTaskList().get(mPosition);
                        showMedia(taskBean);
                    }
                });
            }else if(tb.getType().equalsIgnoreCase(Constants.TASK_TYPE.VIDEO)){
                Logger.d("showMedia avi");
                try {
                    mPlayer.setDataSource(mBaseFileDir + File.separator + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(mVideoSurface.getVisibility() == View.GONE){
                    mVideoSurface.setVisibility(View.VISIBLE);
                }else{
                    mPlayer.prepareAsync();
                }
                mIvMain.setVisibility(View.GONE);
            }
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 当SurfaceView中的Surface被创建的时候被调用
        //在这里我们指定MediaPlayer在当前的Surface中进行播放
        mPlayer.setDisplay(holder);
        //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了
        mPlayer.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 当Surface尺寸等参数改变时触发
        Log.v("Surface Change:::", "surfaceChanged called");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("Surface Destory:::", "surfaceDestroyed called");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 当MediaPlayer播放完成后触发
        Log.v("Play Over:::", "onComletion called");
        mPlayer.reset();
        mPosition = (mPosition+1) % mMediaBean.getTaskList().size();
        TaskBean taskBean = mMediaBean.getTaskList().get(mPosition);
        showMedia(taskBean);
    }

    @Override
    public boolean onError(MediaPlayer mp, int whatError, int extra) {
        Log.v("Play Error:::", "onError called");
        switch (whatError) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.v("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.v("Play Error:::", "MEDIA_ERROR_UNKNOWN");
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int whatInfo, int extra) {
        // 当一些特定信息出现或者警告时触发
        switch(whatInfo){
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();//播放视频
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // seek操作完成时触发
        Log.v("Seek Completion", "onSeekComplete called");
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        // 当video大小改变时触发
        //这个方法在设置player的source后至少触发一次
        Log.v("Video Size Change", "onVideoSizeChanged called");
    }


    private void startHeartbeat() {
        Observable.interval(0, 60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long execNum) {
                        Logger.d("运行次数：" + execNum);
                        try {
                            Response response = OkHttpUtils.post().addParams("id", mEquipId)
                                    .addParams("cp", ApkUtils.getVersionName(MainActivity.this))
                                    .addParams("cuuid", mMediaBean.getTaskList().get(mPosition).getUuid())
                                    //.addParams("datetime", String.valueOf(mMediaStartTime))
                                    .url(ApiManager.GET_CONNECTION).build().execute();
                            if (response.isSuccessful()) {
                                String msg = response.body().string();
                                response.body().close();
                                return msg;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String msg) {
                        return msg != null && msg.length() == 3;
                    }
                })
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String msg) {
                        return String.valueOf(msg.charAt(1));
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.equalsIgnoreCase("1");
                    }
                })
                .map(new Func1<String, MediaBean>() {

                    @Override
                    public MediaBean call(String s) {
                        //请求播放列表
                        InputStream is = null;
                        FileOutputStream fos = null;
                        try {
                            Response response = OkHttpUtils.get().addParams("id",mEquipId)
                                    .url(ApiManager.GET_PLAY_LIST_3).build().execute();
                            if (response.isSuccessful()) {
                                byte[] buf = new byte[2048];
                                int len = 0;
                                is = response.body().byteStream();
                                final long total = response.body().contentLength();
                                long sum = 0;
                                File dir = new File(mBaseFileDir);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                File mediaFileTemp = new File(dir, mMediaFileNameTemp);
                                fos = new FileOutputStream(mediaFileTemp);
                                while ((len = is.read(buf)) != -1) {
                                    sum += len;
                                    fos.write(buf, 0, len);
                                    /*final long finalSum = sum;
                                    Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);*/
                                }
                                fos.flush();
                                response.body().close();
                                return XmlPullManager.pullXmlParseMedia(mediaFileTemp);
                            }else{
                                Logger.d("OkHttpUtils execute " + response.isSuccessful());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        } finally {
                            try {
                                if (is != null) is.close();
                            } catch (IOException e) {
                            }
                            try {
                                if (fos != null) fos.close();
                            } catch (IOException e) {
                            }
                        }
                        return null;
                    }
                })
                .flatMap(new Func1<MediaBean, Observable<TaskBean>>() {
                    @Override
                    public Observable<TaskBean> call(MediaBean mediaBean) {
                        if(mediaBean!=null){
                            return Observable.from(mediaBean.getTaskList());
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .map(new Func1<TaskBean, Integer>() {
                    @Override
                    public Integer call(TaskBean tb) {
                        final Integer[] isSuccess = {0};
                        if (tb != null) {
                            String fileName = tb.getUuid() + ".avi";
                            if (tb.getType().equalsIgnoreCase(Constants.TASK_TYPE.JPEG)) {
                                fileName = tb.getUuid() + ".jpg";
                            }
                            final String finalFileName = fileName;
                            Observable.just(tb)
                                    .flatMap(new Func1<TaskBean, Observable<String>>() {
                                        @Override
                                        public Observable<String> call(TaskBean taskBean) {
                                            File file = new File(mBaseFileDir, finalFileName);
                                            if (!file.exists()) {
                                                //判断是否需要断点续传
                                                File fileTemp = new File(mBaseFileDir, finalFileName + ".tmp");
                                                String url = "http://61.129.70.157:8089/Media/upload/" + finalFileName;
                                                Request request = null;
                                                InputStream is = null;
                                                RandomAccessFile raf;
                                                long fileTempLength = 0;
                                                Response response;
                                                try {
                                                    if (fileTemp.exists()) {
                                                        fileTempLength = fileTemp.length();
                                                        response = OkHttpUtils.get().addHeader("range", "bytes=" + fileTempLength + "-")
                                                                .url(url).build().execute();
                                                    } else {
                                                        response = OkHttpUtils.get()
                                                                .url(url).build().execute();
                                                    }
                                                    if (response.isSuccessful()) {
                                                        byte[] buf = new byte[2048];
                                                        int len;
                                                        is = response.body().byteStream();
                                                        final long total = response.body().contentLength();
                                                        long sum = 0;
                                                        File dir = new File(mBaseFileDir);
                                                        if (!dir.exists()) {
                                                            dir.mkdirs();
                                                        }
                                                        File mediaFileTemp = new File(dir, finalFileName + ".tmp");
                                                        raf = new RandomAccessFile(mediaFileTemp, "rw");
                                                        raf.seek(fileTempLength);

                                                        //fos = new FileOutputStream(mediaFile);

                                                        while ((len = is.read(buf)) != -1) {
                                                            sum += len;
                                                            //fos.write(buf, 0, len);

                                                            raf.write(buf, 0, len);

                                    /*final long finalSum = sum;
                                    Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);*/
                                                        }

                                                        //fos.flush();
                                                        //删除tmp
                                                        mediaFileTemp.renameTo(file);
                                                        response.body().close();
                                                    }
                                                } catch (IOException e) {
                                                    //e.printStackTrace();
                                                    return Observable.error(e);
                                                }
                                            }
                                            return Observable.just("");
                                        }
                                    })
                                    .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                                @Override
                                public Observable<?> call(Observable<? extends Throwable> errors) {
                                    return errors.zipWith(Observable.range(1, 10), new Func2<Throwable, Integer, Integer>() {
                                        @Override
                                        public Integer call(Throwable throwable, Integer integer) {
                                            Logger.d("重试:" + integer);
                                            return integer;
                                        }
                                    });
                                }
                            }).subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {
                                    Logger.d("Download onCompleted:"+finalFileName);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.d("Download onError:"+finalFileName);
                                }

                                @Override
                                public void onNext(String s) {
                                    isSuccess[0] = 1;
                                    Logger.d("Download onNext:"+finalFileName);
                                }
                            });
                        }
                        return isSuccess[0];
                    }
                })
                .reduce(new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer, Integer integer2) {
                        Logger.d("reduce:"+integer+"*"+integer2);
                        return integer*integer2;
                    }
                })
                //总共重试10次，重试间隔500毫秒
                //.retryWhen(new RetryWithDelay(10,500))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d("onError:"+e.getMessage());
                    }

                    @Override
                    public void onNext(Integer i) {
                        Logger.d("onNext:"+i);
                    }
                });
    }

    private void startHeartbeat1() {
        Observable.interval(0, 60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long execNum) {
                        Logger.d("运行次数：" + execNum);
                        try {
                            Response response = OkHttpUtils.post().addParams("id", mEquipId)
                                    .addParams("cp", ApkUtils.getVersionName(MainActivity.this))
                                    .addParams("cuuid", mMediaBean.getTaskList().get(mPosition).getUuid())
                                    //.addParams("datetime", String.valueOf(mMediaStartTime))
                                    .url(ApiManager.GET_CONNECTION).build().execute();
                            if (response.isSuccessful()) {
                                String msg = response.body().string();
                                response.body().close();
                                return msg;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String msg) {
                        return msg != null && msg.length() == 3;
                    }
                })
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String msg) {
                        return String.valueOf(msg.charAt(1));
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.equalsIgnoreCase("1");
                    }
                })
                .map(new Func1<String, MediaBean>() {

                    @Override
                    public MediaBean call(String s) {
                        //请求播放列表
                        InputStream is = null;
                        FileOutputStream fos = null;
                        try {
                            Response response = OkHttpUtils.get().addParams("id",mEquipId)
                                    .url(ApiManager.GET_PLAY_LIST_3).build().execute();
                            if (response.isSuccessful()) {
                                byte[] buf = new byte[2048];
                                int len = 0;
                                is = response.body().byteStream();
                                final long total = response.body().contentLength();
                                long sum = 0;
                                File dir = new File(mBaseFileDir);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                File mediaFileTemp = new File(dir, mMediaFileNameTemp);
                                fos = new FileOutputStream(mediaFileTemp);
                                while ((len = is.read(buf)) != -1) {
                                    sum += len;
                                    fos.write(buf, 0, len);
                                    /*final long finalSum = sum;
                                    Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);*/
                                }
                                fos.flush();
                                response.body().close();
                                return XmlPullManager.pullXmlParseMedia(mediaFileTemp);
                            }else{
                                Logger.d("OkHttpUtils execute " + response.isSuccessful());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        } finally {
                            try {
                                if (is != null) is.close();
                            } catch (IOException e) {
                            }
                            try {
                                if (fos != null) fos.close();
                            } catch (IOException e) {
                            }
                        }
                        return null;
                    }
                })
                .map(new Func1<MediaBean, Boolean>() {
                    @Override
                    public Boolean call(MediaBean mediaBean) {
                        final boolean[] isDone = {false};
                        Observable.from(mediaBean.getTaskList()).flatMap(new Func1<TaskBean, Observable<Integer>>() {
                            @Override
                            public Observable<Integer> call(TaskBean tb) {
                                String fileName = tb.getUuid() + ".avi";
                                if (tb.getType().equalsIgnoreCase(Constants.TASK_TYPE.JPEG)) {
                                    fileName = tb.getUuid() + ".jpg";
                                }
                                final String finalFileName = fileName;
                                final Integer[] isSuccess = {0};
                                Observable.just(tb)
                                        .flatMap(new Func1<TaskBean, Observable<String>>() {
                                            @Override
                                            public Observable<String> call(TaskBean taskBean) {
                                                File file = new File(mBaseFileDir, finalFileName);
                                                if (!file.exists()) {
                                                    //判断是否需要断点续传
                                                    File fileTemp = new File(mBaseFileDir, finalFileName + ".tmp");
                                                    String url = "http://61.129.70.157:8089/Media/upload/" + finalFileName;
                                                    Request request = null;
                                                    InputStream is = null;
                                                    RandomAccessFile raf;
                                                    long fileTempLength = 0;
                                                    Response response;
                                                    try {
                                                        if (fileTemp.exists()) {
                                                            fileTempLength = fileTemp.length();
                                                            response = OkHttpUtils.get().addHeader("range", "bytes=" + fileTempLength + "-")
                                                                    .url(url).build().execute();
                                                        } else {
                                                            response = OkHttpUtils.get()
                                                                    .url(url).build().execute();
                                                        }
                                                        if (response.isSuccessful()) {
                                                            byte[] buf = new byte[2048];
                                                            int len;
                                                            is = response.body().byteStream();
                                                            final long total = response.body().contentLength();
                                                            long sum = 0;
                                                            File dir = new File(mBaseFileDir);
                                                            if (!dir.exists()) {
                                                                dir.mkdirs();
                                                            }
                                                            File mediaFileTemp = new File(dir, finalFileName + ".tmp");
                                                            raf = new RandomAccessFile(mediaFileTemp, "rw");
                                                            raf.seek(fileTempLength);

                                                            //fos = new FileOutputStream(mediaFile);

                                                            while ((len = is.read(buf)) != -1) {
                                                                sum += len;
                                                                //fos.write(buf, 0, len);

                                                                raf.write(buf, 0, len);

                                    /*final long finalSum = sum;
                                    Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);*/
                                                            }

                                                            //fos.flush();
                                                            //删除tmp
                                                            mediaFileTemp.renameTo(file);
                                                            response.body().close();
                                                        }
                                                    } catch (IOException e) {
                                                        //e.printStackTrace();
                                                        return Observable.error(e);
                                                    }
                                                }
                                                return Observable.just("");
                                            }
                                        })
                                        .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                                            @Override
                                            public Observable<?> call(Observable<? extends Throwable> errors) {
                                                return errors.zipWith(Observable.range(1, 10), new Func2<Throwable, Integer, Integer>() {
                                                    @Override
                                                    public Integer call(Throwable throwable, Integer integer) {
                                                        Logger.d("重试:" + integer);
                                                        return integer;
                                                    }
                                                });
                                            }
                                        }).subscribe(new Subscriber<String>() {
                                    @Override
                                    public void onCompleted() {
                                        Logger.d("Download onCompleted:" + finalFileName);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        isSuccess[0] = 0;
                                        Logger.d("Download onError:" + finalFileName);
                                    }

                                    @Override
                                    public void onNext(String s) {
                                        isSuccess[0] = 1;
                                        Logger.d("Download onNext:" + finalFileName);
                                    }
                                });
                                return Observable.just(isSuccess[0]);
                            }
                        }).scan(new Func2<Integer, Integer, Integer>() {
                            @Override
                            public Integer call(Integer integer, Integer integer2) {
                                Logger.d("scan:"+integer * integer2);
                                return integer * integer2;
                            }
                        }).flatMap(new Func1<Integer, Observable<String>>() {
                            @Override
                            public Observable<String> call(Integer integer) {
                                Logger.d("flatMap:"+integer);
                                if(integer == 0){
                                    return Observable.error(new Exception());
                                }
                                return Observable.just("OK");
                            }
                        }).subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {
                                Logger.d("from onCompleted");
                                isDone[0] = true;
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.d("from onError");
                                isDone[0] = false;
                            }

                            @Override
                            public void onNext(String s) {

                            }
                        });
                        return isDone[0];
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d("onError:"+e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean b) {
                        Logger.d("onNext:"+b);
                    }
                });
    }

    public class RetryWithDelay implements
            Func1<Observable<? extends Throwable>, Observable<?>> {

        private final int maxRetries;
        private final int retryDelayMillis;
        private int retryCount;

        public RetryWithDelay(int maxRetries, int retryDelayMillis) {
            this.maxRetries = maxRetries;
            this.retryDelayMillis = retryDelayMillis;
        }

        @Override
        public Observable<?> call(Observable<? extends Throwable> attempts) {
            return attempts
                    .flatMap(new Func1<Throwable, Observable<?>>() {
                        @Override
                        public Observable<?> call(Throwable throwable) {
                            if (++retryCount <= maxRetries) {
                                // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                                return Observable.timer(retryDelayMillis,
                                        TimeUnit.MILLISECONDS);
                            }
                            // Max retries hit. Just pass the error along.
                            return Observable.error(throwable);
                        }
                    });
        }
    }


    private void update(){
        File systemFileTemp = null;
        File mediaFileTemp = null;
        try {
            //请求终端配置信息
            Request request = new Request.Builder()
                    .url(ApiManager.GET_CONFIG + "?id="+mEquipId)
                    .build();
            Response response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Logger.d("OkHttpUtils execute " + response.code());
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    final long total = response.body().contentLength();
                    long sum = 0;
                    File dir = new File(mBaseFileDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    systemFileTemp = new File(dir, mSystemFileNameTemp);
                    fos = new FileOutputStream(systemFileTemp);
                    while ((len = is.read(buf)) != -1) {
                        sum += len;
                        fos.write(buf, 0, len);
                        /*final long finalSum = sum;
                        Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);*/
                    }
                    fos.flush();
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                    }
                }
            } else {
                Logger.d("OkHttpUtils execute " + response.isSuccessful());
            }

            //请求播放列表
            request = new Request.Builder()
                    .url(ApiManager.GET_PLAY_LIST_3 + "?id="+mEquipId)
                    .build();
            response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Logger.d("OkHttpUtils execute " + response.code());
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    final long total = response.body().contentLength();
                    long sum = 0;
                    File dir = new File(mBaseFileDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    mediaFileTemp = new File(dir, mMediaFileNameTemp);
                    fos = new FileOutputStream(mediaFileTemp);
                    while ((len = is.read(buf)) != -1) {
                        sum += len;
                        fos.write(buf, 0, len);
                        /*final long finalSum = sum;
                        Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);*/
                    }
                    fos.flush();
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                    }
                }
            } else {
                Logger.d("OkHttpUtils execute " + response.isSuccessful());
            }

            //下载具体媒体（上报下载进度）
            SystemBean sb = XmlPullManager.pullXmlParseSystem(systemFileTemp);
            MediaBean mb = XmlPullManager.pullXmlParseMedia(mediaFileTemp);
            String mediaUrl = "http://"+sb.getMedia()+"/";
            //http://61.129.70.157:8089/Media/upload/a58ec83b-8818-4a18-b6a5-8bc9453e5beb.avi
            for (TaskBean tb : mb.getTaskList()) {
                String fileName = tb.getUuid() + ".avi";
                if(tb.getType().equalsIgnoreCase(Constants.TASK_TYPE.JPEG)){
                    fileName = tb.getUuid() + "." + tb.getType();
                }
                File file = new File(mBaseFileDir,fileName);
                if(!file.exists()){
                    //下载
                    request = new Request.Builder()
                            .url(mediaUrl + "upload/"+fileName)
                            .build();
                    response = mClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Logger.d("OkHttpUtils execute " + response.code());
                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len = 0;
                        FileOutputStream fos = null;
                        try {
                            is = response.body().byteStream();
                            final long total = response.body().contentLength();
                            long sum = 0;
                            File dir = new File(mBaseFileDir);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            mediaFileTemp = new File(dir, mMediaFileNameTemp);
                            fos = new FileOutputStream(mediaFileTemp);
                            while ((len = is.read(buf)) != -1) {
                                sum += len;
                                fos.write(buf, 0, len);
                                final long finalSum = sum;
                                Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);
                            }
                            fos.flush();
                        } finally {
                            try {
                                if (is != null) is.close();
                            } catch (IOException e) {
                            }
                            try {
                                if (fos != null) fos.close();
                            } catch (IOException e) {
                            }
                        }
                    } else {
                        Logger.d("OkHttpUtils execute " + response.isSuccessful());
                    }
                }
            }

            //上报播放任务更新完成
            FormBody body = new FormBody.Builder().add("id", mEquipId)
                    .add("success", "1").build();
            request = new Request.Builder()
                    .url(ApiManager.SUBMIT)
                    .post(body)
                    .build();
            response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Observable.interval(0, 60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long number) {
                        Logger.d("hello world…." + number);

                        FormBody body = new FormBody.Builder().add("id", mEquipId)
                                .add("cp", ApkUtils.getVersionName(MainActivity.this))
                                .add("cuuid", mMediaBean.getTaskList().get(mPosition).getUuid())
                                .add("datetime", String.valueOf(mMediaStartTime)).build();
                        Request request = new Request.Builder()
                                .url(ApiManager.GET_CONNECTION)
                                .post(body)
                                .build();
                        Response response = null;
                        try {
                            response = mClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                Logger.d("GET_CONNECTION " + response.body().string());
                                this.onCompleted();
                                //解析心跳结果
                                String result = response.body().string();
                                if(result.length() == 3){
                                    if(result.substring(2).equalsIgnoreCase("1")){
                                        //走更新流程
                                        update();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        body = new FormBody.Builder().add("id", "2016060001").build();
                        request = new Request.Builder()
                                .url(ApiManager.GET_CONFIG + "?id=2016060001")
                                .build();
                        response = null;
                        try {
                            response = mClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                Logger.d("OkHttpUtils execute " + response.code());
                                //Logger.d("OkHttpUtils execute "+response.body().contentLength());
                                InputStream is = null;
                                byte[] buf = new byte[2048];
                                int len = 0;
                                FileOutputStream fos = null;
                                try {
                                    is = response.body().byteStream();
                                    final long total = response.body().contentLength();
                                    long sum = 0;
                                    File dir = new File(fileDir);
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    File file = new File(dir, systemFileName);
                                    fos = new FileOutputStream(file);
                                    while ((len = is.read(buf)) != -1) {
                                        sum += len;
                                        fos.write(buf, 0, len);
                                        final long finalSum = sum;
                                        Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);

                                    }
                                    fos.flush();
                                } finally {
                                    try {
                                        if (is != null) is.close();
                                    } catch (IOException e) {
                                    }
                                    try {
                                        if (fos != null) fos.close();
                                    } catch (IOException e) {
                                    }
                                }
                            } else {
                                Logger.d("OkHttpUtils execute " + response.isSuccessful());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*//*


                        *//*OkHttpUtils.post()
                                .addParams("id", "2016060001")
                                .addParams("cp", "1.0.0")
                                .addParams("cuuid", "1347b284-668f-4853-b9f8-ee6e0108b4b3")
                                .addParams("datetime", String.valueOf(DateUtils.getMillis(new Date())))
                                .url(ApiManager.GET_CONNECTION)
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                        Logger.d("GET_CONNECTION " + e.getMessage());
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        Logger.d("GET_CONNECTION " + response);
                                    }
                                });*//*
                    }
                });*/


        /*final String fileDir = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        String mediaFileName = "media-new.xml";
        final String systemFileName = "system-new.xml";*/


        /*OkHttpUtils.get().addParams("id","2016060001").url(ApiManager.GET_CONFIG).build().execute(new FileCallback(fileDir,systemFileName) {
            @Override
            public void inProgress(float progress) {
                Logger.d("OkHttpUtils "+progress);
            }

            @Override
            public void onError(Call call, Exception e) {
                Logger.d("OkHttpUtils "+e.getMessage());
            }

            @Override
            public void onResponse(File response) {
                Logger.d("OkHttpUtils "+response.getName()+"----"+response.getPath());
            }
        });

        OkHttpUtils.get().addParams("id","2016060001").url(ApiManager.GET_PLAY_LIST_3).build().execute(new FileCallback(fileDir,mediaFileName) {
            @Override
            public void inProgress(float progress) {
                Logger.d("OkHttpUtils "+progress);
            }

            @Override
            public void onError(Call call, Exception e) {
                Logger.d("OkHttpUtils "+e.getMessage());
            }

            @Override
            public void onResponse(File response) {
                Logger.d("OkHttpUtils "+response.getName()+"----"+response.getPath());
            }
        });*/

        /*Observable.interval(10, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Log.d("MainActivity", "completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("MainActivity", "error");
                    }

                    @Override
                    public void onNext(Long number) {
                        //Log.d("MainActivity","hello world…."+number);
                        Logger.d("hello world…." + number);

                        FormBody body = new FormBody.Builder().add("id", "2016060001")
                                .add("cp", "1.0.0")
                                .add("cuuid", "1347b284-668f-4853-b9f8-ee6e0108b4b3")
                                .add("datetime", String.valueOf(DateUtils.getMillis(new Date()))).build();
                        Request request = new Request.Builder()
                                .url(ApiManager.GET_CONNECTION)
                                .post(body)
                                .build();
                        *//*client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Logger.d("GET_CONNECTION " + e.getMessage());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Logger.d("GET_CONNECTION " + response.body().toString());
                            }
                        });*//*
                        Response response = null;
                        try {
                            response = mClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                Logger.d("GET_CONNECTION " + response.body().string());
                            } else {
                                throw new IOException("Unexpected code " + response);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        //body = new FormBody.Builder().add("id", "2016060001").build();
                        request = new Request.Builder()
                                .url(ApiManager.GET_CONFIG + "?id=2016060001")
                                .build();
                        response = null;
                        try {
                            response = mClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                Logger.d("OkHttpUtils execute " + response.code());
                                //Logger.d("OkHttpUtils execute "+response.body().contentLength());
                                InputStream is = null;
                                byte[] buf = new byte[2048];
                                int len = 0;
                                FileOutputStream fos = null;
                                try {
                                    is = response.body().byteStream();
                                    final long total = response.body().contentLength();
                                    long sum = 0;
                                    File dir = new File(fileDir);
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    File file = new File(dir, systemFileName);
                                    fos = new FileOutputStream(file);
                                    while ((len = is.read(buf)) != -1) {
                                        sum += len;
                                        fos.write(buf, 0, len);
                                        final long finalSum = sum;
                                        Logger.d("OkHttpUtils execute " + finalSum * 1.0f / total);

                                    }
                                    fos.flush();
                                } finally {
                                    try {
                                        if (is != null) is.close();
                                    } catch (IOException e) {
                                    }
                                    try {
                                        if (fos != null) fos.close();
                                    } catch (IOException e) {
                                    }
                                }
                            } else {
                                Logger.d("OkHttpUtils execute " + response.isSuccessful());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        *//*OkHttpUtils.post()
                                .addParams("id", "2016060001")
                                .addParams("cp", "1.0.0")
                                .addParams("cuuid", "1347b284-668f-4853-b9f8-ee6e0108b4b3")
                                .addParams("datetime", String.valueOf(DateUtils.getMillis(new Date())))
                                .url(ApiManager.GET_CONNECTION)
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                        Logger.d("GET_CONNECTION " + e.getMessage());
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        Logger.d("GET_CONNECTION " + response);
                                    }
                                });*//*
                    }
                });*/


        /*Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> observer) {

                Schedulers.newThread().createWorker()
                        .schedulePeriodically(new Action0() {
                            @Override
                            public void call() {
                                observer.onNext("1");
                            }
                        }, 0, 5, TimeUnit.SECONDS);
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.d("MainActivity","polling…."+s);
            }
        }) ;*/

        /*Observable.timer(2, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {

            }
        });*/

        /*String path1 = Environment.getExternalStorageDirectory().getPath()+"/Download/system.xml";
        File file1 = new File(path1);

        SystemBean systemBean = pullXmlParseSystem(file1);

        SystemBean systemBean1 = pullXmlParseSystem1(file1);

        String path2 = Environment.getExternalStorageDirectory().getPath()+"/Download/media.xml";
        File file2 = new File(path2);
        MediaBean mediaBean = pullXmlParseMedia(file2);*/
}
