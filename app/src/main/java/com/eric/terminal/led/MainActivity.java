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
import com.shiki.utils.ApkUtils;
import com.shiki.utils.DateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
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
        if(mMediaBean!=null){
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
        }

        //起心跳获取是否需要更新
        //startHeartbeat();
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
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long execNum) {
                        Logger.d("运行次数：" + execNum);
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
                                return response.body().string();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String msg) {
                        if (msg != null && msg.length() == 3) {
                            if (msg.charAt(2) == '1') {
                                Logger.d("开始更新...");
                                update();
                            }
                            return String.valueOf(msg.charAt(1));
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                               @Override
                               public void call(String flag) {
                                  /* if (flag != null && flag.equals("0")) {
                                       Logger.d("关闭屏幕...");
                                   }
                                   tvMain.setText(System.currentTimeMillis() + "");*/
                               }
                           }
                );
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
