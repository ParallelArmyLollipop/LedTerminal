package com.eric.terminal.led;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.eric.terminal.led.Bean.MediaBean;
import com.eric.terminal.led.Bean.SystemBean;
import com.eric.terminal.led.Bean.TaskBean;
import com.eric.terminal.led.Manager.ApiManager;
import com.eric.terminal.led.Manager.Constants;
import com.eric.terminal.led.Manager.XmlPullManager;
import com.orhanobut.logger.Logger;
import com.shiki.okttp.OkHttpUtils;
import com.shiki.utils.ApkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {
    String mEquipId = "2016060001";
    String mMediaFileName = "media.xml";
    String mSystemFileName = "system.xml";
    String mMediaFileNameTemp = "media-temp.xml";
    String mSystemFileNameTemp = "system-temp.xml";
    String mBaseFileDir;
    SystemBean mSystemBean;
    MediaBean mMediaBean;
    int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Logger.init();

        //首先先读取默认配置文件
        mBaseFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + this.getPackageName();
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


        heart2();

    }

    private void heart1(){
        final Long[] times = new Long[1];
        Observable.interval(0, 20, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long execNum) {
                        Logger.d("运行次数：" + execNum);
                        times[0] = execNum;
                        return getContection();
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s!=null&&s.length()==3;
                    }
                })
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return String.valueOf(s.charAt(1));
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
                        return getPlayList();
                    }
                })
                .flatMap(new Func1<MediaBean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(MediaBean mediaBean) {
                        final boolean[] isDone = {true};
                        Observable.from(mediaBean.getTaskList())
                                .flatMap(new Func1<TaskBean, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(TaskBean taskBean) {
                                        if(downLoadMedia(taskBean)==0){
                                            return Observable.error(new Exception());
                                        }
                                        return Observable.just(taskBean.getUuid());
                                        //return Observable.error(new Exception());
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
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<String>() {
                                    @Override
                                    public void onCompleted() {
                                        Logger.d("onCompleted");
                                        isDone[0] = true;
                                        Observable.just("DownLoad Completed!");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Logger.d("onError：" + e.getMessage());
                                        isDone[0] = false;
                                        Observable.error(e);
                                    }

                                    @Override
                                    public void onNext(String s) {
                                        Logger.d("onNext:" + s);
                                    }
                                });
                        return Observable.just(isDone[0]);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("interval onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d("interval onError：" + e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean b) {
                        Logger.d(times[0]+"----interval onNext:" + b);
                    }
                })
        ;
    }

    private void heart2(){
        final Long[] times = new Long[1];
        Observable.interval(0, 20, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long execNum) {
                        Logger.d("运行次数：" + execNum);
                        times[0] = execNum;
                        return getContection();
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s!=null&&s.length()==3;
                    }
                })
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return String.valueOf(s.charAt(1));
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
                        return getPlayList();
                    }
                })
                .flatMap(new Func1<MediaBean, Observable<TaskBean>>() {
                    @Override
                    public Observable<TaskBean> call(MediaBean mediaBean) {
                        return  Observable.from(mediaBean.getTaskList());
                    }
                })
                .flatMap(new Func1<TaskBean, Observable<?>>() {
                    @Override
                    public Observable<?> call(TaskBean taskBean) {
                        /*if(downLoadMedia(taskBean)==0){
                            return Observable.error(new Exception());
                        }
                        return Observable.just(taskBean.getUuid());*/
                        return Observable.error(new Exception());
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
                })
                .reduce(new Func2<Object, Object, Object>() {
                    @Override
                    public Object call(Object o, Object o2) {
                        return o.toString()+File.separator+o2.toString();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("interval onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d("interval onError：" + e.getMessage());
                    }

                    @Override
                    public void onNext(Object o) {
                        Logger.d(times[0]+"----interval onNext:" + o.toString());
                    }
                })
                /*.flatMap(new Func1<MediaBean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(MediaBean mediaBean) {
                        final boolean[] isDone = {true};
                        Observable.from(mediaBean.getTaskList())
                                .flatMap(new Func1<TaskBean, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(TaskBean taskBean) {
                                        if(downLoadMedia(taskBean)==0){
                                            return Observable.error(new Exception());
                                        }
                                        return Observable.just(taskBean.getUuid());
                                        //return Observable.error(new Exception());
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
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<String>() {
                                    @Override
                                    public void onCompleted() {
                                        Logger.d("onCompleted");
                                        isDone[0] = true;
                                        Observable.just("DownLoad Completed!");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Logger.d("onError：" + e.getMessage());
                                        isDone[0] = false;
                                        Observable.error(e);
                                    }

                                    @Override
                                    public void onNext(String s) {
                                        Logger.d("onNext:" + s);
                                    }
                                });
                        return Observable.just(isDone[0]);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("interval onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d("interval onError：" + e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean b) {
                        Logger.d(times[0]+"----interval onNext:" + b);
                    }
                })*/
        ;
    }

    private String getContection() {
        String msg = "";
        try {
            Response response;
            response = OkHttpUtils.post().addParams("id", mEquipId)
                    .addParams("cp", ApkUtils.getVersionName(TestActivity.this))
                    .addParams("cuuid", mMediaBean.getTaskList().get(mPosition).getUuid())
                    //.addParams("datetime", String.valueOf(mMediaStartTime))
                    .url(ApiManager.GET_CONNECTION).build().execute();
            if (response.isSuccessful()) {
                msg = response.body().string();
                response.body().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    private MediaBean getPlayList() {
        MediaBean mb = null;
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
                mb = XmlPullManager.pullXmlParseMedia(mediaFileTemp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }
            return mb;
        }
    }

    private Integer downLoadMedia(TaskBean tb) {
        Integer result = 1;
        String fileName = tb.getUuid() + ".avi";
        if (tb.getType().equalsIgnoreCase(Constants.TASK_TYPE.JPEG)) {
            fileName = tb.getUuid() + ".jpg";
        }
        final String finalFileName = fileName;
        final Integer[] isSuccess = {0};
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
                    while ((len = is.read(buf)) != -1) {
                        sum += len;
                        raf.write(buf, 0, len);
                    }
                    //删除tmp
                    mediaFileTemp.renameTo(file);
                    response.body().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = 0;
            }
        }
        return result;
    }
}
