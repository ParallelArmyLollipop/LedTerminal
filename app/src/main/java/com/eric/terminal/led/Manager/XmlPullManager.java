package com.eric.terminal.led.Manager;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Xml;

import com.eric.terminal.led.Bean.MediaBean;
import com.eric.terminal.led.Bean.SystemBean;
import com.eric.terminal.led.Bean.TaskBean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Eric on 2016/6/8.
 */
public class XmlPullManager {

    public static SystemBean pullXmlParseSystem(Context context, int resource){
        XmlResourceParser parser = context.getResources().getXml(resource);
        return pullSystem(parser);
    }

    public static SystemBean pullXmlParseSystem(File file){
        try {
            FileInputStream fis = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();//由android.util.xml创建一个XmlPullParser实例
            parser.setInput(fis,"UTF-8");//设置输入流并指明编码方式
            return pullSystem(parser);
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    private static SystemBean pullSystem(XmlPullParser parser){
        SystemBean systemBean = new SystemBean();
        try {
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

    public static MediaBean pullXmlParseMedia(Context context, int resource){
        XmlResourceParser parser = context.getResources().getXml(resource);
        return pullMedia(parser);
    }


    public static MediaBean pullXmlParseMedia(File file){
        try {
            FileInputStream fis = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();//由android.util.xml创建一个XmlPullParser实例
            parser.setInput(fis,"UTF-8");//设置输入流并指明编码方式
            return pullMedia(parser);
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    private static MediaBean pullMedia(XmlPullParser parser){
        MediaBean mediaBean = new MediaBean();
        mediaBean.setTaskList(new ArrayList<TaskBean>());
        try {
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


    /*public SystemBean pullXmlParseSystem(File file){
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
    }*/
}
