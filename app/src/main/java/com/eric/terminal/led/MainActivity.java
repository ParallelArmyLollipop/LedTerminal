package com.eric.terminal.led;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;

import com.eric.terminal.led.Manager.ApiManager;
import com.eric.terminal.led.Bean.MediaBean;
import com.eric.terminal.led.Bean.SystemBean;
import com.eric.terminal.led.Bean.TaskBean;
import com.orhanobut.logger.Logger;
import com.shiki.okttp.OkHttpUtils;
import com.shiki.okttp.callback.FileCallback;
import com.shiki.okttp.callback.StringCallback;
import com.shiki.utils.DateUtils;
import com.shiki.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    OkHttpClient mClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.init();
        Logger.d("MainActivity onCreate");

        final String fileDir = Environment.getExternalStorageDirectory().getPath()+"/Download/";
        String mediaFileName = "media-new.xml";
        final String systemFileName = "system-new.xml";


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

        Observable.interval(10, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Log.d("MainActivity","completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("MainActivity","error");
                    }

                    @Override
                    public void onNext(Long number) {
                        //Log.d("MainActivity","hello world…."+number);
                        Logger.d("hello world…."+number);

                        FormBody body = new FormBody.Builder().add("id", "2016060001")
                                .add("cp", "1.0.0")
                                .add("cuuid", "1347b284-668f-4853-b9f8-ee6e0108b4b3")
                                .add("datetime", String.valueOf(DateUtils.getMillis(new Date()))).build();
                        Request request = new Request.Builder()
                                .url(ApiManager.GET_CONNECTION)
                                .post(body)
                                .build();
                        /*client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Logger.d("GET_CONNECTION " + e.getMessage());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Logger.d("GET_CONNECTION " + response.body().toString());
                            }
                        });*/
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
                                .url(ApiManager.GET_CONFIG+"?id=2016060001")
                                .build();
                        response = null;
                        try {
                            response = mClient.newCall(request).execute();
                            if(response.isSuccessful()){
                                Logger.d("OkHttpUtils execute "+response.code());
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
                                        Logger.d("OkHttpUtils execute "+finalSum * 1.0f / total);

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
                            }else{
                                Logger.d("OkHttpUtils execute "+response.isSuccessful());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        /*OkHttpUtils.post()
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
                                });*/
                    }
                });


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

    public SystemBean pullXmlParseSystem(File file){
        SystemBean systemBean = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();//由android.util.xml创建一个XmlPullParser实例
            parser.setInput(fis,"UTF-8");//设置输入流并指明编码方式
            int eventType = parser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        systemBean = new SystemBean();
                        break;
                    case XmlPullParser.START_TAG:
                        if(systemBean!=null){
                            if(parser.getName().equalsIgnoreCase("opentime")){
                                parser.next();
                                systemBean.setOpentime(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("opentime")){
                                parser.next();
                                systemBean.setOpentime(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("closetime")){
                                parser.next();
                                systemBean.setClosetime(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("sendtime")){
                                parser.next();
                                systemBean.setSendtime(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("sendtype")){
                                parser.next();
                                if(StringUtils.isEmpty(parser.getText())){
                                    systemBean.setSendtype(0);
                                }else{
                                    systemBean.setSendtype(Integer.parseInt(parser.getText()));
                                }
                            }else if(parser.getName().equalsIgnoreCase("mobilephone")){
                                parser.next();
                                systemBean.setMobilephone(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("autoupdate")){
                                parser.next();
                                systemBean.setAutoupdate(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("systime")){
                                parser.next();
                                systemBean.setSystime(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("volume")){
                                parser.next();
                                if(StringUtils.isEmpty(parser.getText())){
                                    systemBean.setSendtype(0);
                                }else{
                                    systemBean.setVolume(Integer.parseInt(parser.getText()));
                                }
                            }else if(parser.getName().equalsIgnoreCase("bright")){
                                parser.next();
                                if(StringUtils.isEmpty(parser.getText())){
                                    systemBean.setSendtype(0);
                                }else{
                                    systemBean.setBright(Integer.parseInt(parser.getText()));
                                }
                            }else if(parser.getName().equalsIgnoreCase("weather")){
                                parser.next();
                                systemBean.setWeather(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("romid")){
                                parser.next();
                                systemBean.setRomid(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("server")){
                                parser.next();
                                systemBean.setServer(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("media")){
                                parser.next();
                                systemBean.setMedia(parser.getText());
                            }else if(parser.getName().equalsIgnoreCase("jpegshowtime")){
                                parser.next();
                                if(StringUtils.isEmpty(parser.getText())){
                                    systemBean.setSendtype(0);
                                }else{
                                    systemBean.setJpegshowtime(Integer.parseInt(parser.getText()));
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  systemBean;
    }

    public SystemBean pullXmlParseSystem1(File file){
        SystemBean systemBean = new SystemBean();
        try {
            FileInputStream fis = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();//由android.util.xml创建一个XmlPullParser实例
            parser.setInput(fis,"UTF-8");//设置输入流并指明编码方式
            Field field = null;
            while (parser.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlResourceParser.START_TAG) {
                    if (!parser.getName().equals("root")) {
                        field = systemBean.getClass().getDeclaredField(parser.getName());
                    }
                } else if (parser.getEventType() == XmlPullParser.END_TAG) {
                } else if (parser.getEventType() == XmlPullParser.TEXT) {
                    field.setAccessible(true);
                    if(field.getType().equals(int.class)){
                        field.set(systemBean, Integer.parseInt(parser.getText()));
                    }else{
                        field.set(systemBean, parser.getText());
                    }

                }
                parser.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            systemBean = null;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            systemBean = null;
        } catch (IOException e) {
            e.printStackTrace();
            systemBean = null;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            systemBean = null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            systemBean = null;
        }
        return  systemBean;
    }


    public MediaBean pullXmlParseMedia(File file){
        MediaBean mediaBean = new MediaBean();
        //List<TaskBean> taskBeanList = new ArrayList<TaskBean>();
        mediaBean.setTaskList(new ArrayList<TaskBean>());
        try {
            FileInputStream fis = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();//由android.util.xml创建一个XmlPullParser实例
            parser.setInput(fis,"UTF-8");//设置输入流并指明编码方式
            int eventType = parser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equalsIgnoreCase("task")){
                            mediaBean.setStyle(Integer.parseInt(parser.getAttributeValue(null,"style")));// 通过属性名来获取属性值
                        }else if(parser.getName().equalsIgnoreCase("uuid")){
                            TaskBean taskBean = new TaskBean();
                            taskBean.setType(parser.getAttributeValue(null,"type"));
                            taskBean.setPwd(parser.getAttributeValue(null,"pwd"));
                            parser.next();
                            taskBean.setUuid(parser.getText());
                            mediaBean.getTaskList().add(taskBean);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  mediaBean;
    }



}
