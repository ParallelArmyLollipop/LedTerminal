package com.eric.terminal.led;

import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;

import com.eric.terminal.led.Bean.MediaBean;
import com.eric.terminal.led.Bean.SystemBean;
import com.eric.terminal.led.Bean.TaskBean;
import com.shiki.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path1 = Environment.getExternalStorageDirectory().getPath()+"/Download/system.xml";
        File file1 = new File(path1);

        SystemBean systemBean = pullXmlParseSystem(file1);

        SystemBean systemBean1 = pullXmlParseSystem1(file1);

        String path2 = Environment.getExternalStorageDirectory().getPath()+"/Download/media.xml";
        File file2 = new File(path2);
        MediaBean mediaBean = pullXmlParseMedia(file2);
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
