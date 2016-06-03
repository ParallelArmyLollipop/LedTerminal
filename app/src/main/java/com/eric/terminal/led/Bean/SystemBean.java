package com.eric.terminal.led.Bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eric on 2016/6/3.
 */
public class SystemBean implements Parcelable {
    private String opentime;
    private String closetime;
    private String sendtime;
    private int sendtype;
    private String mobilephone;
    private String autoupdate;
    private String systime;
    private int volume;
    private int bright;
    private String weather;
    private String romid;
    private String server;
    private String media;
    private int jpegshowtime;

    public String getOpentime() {
        return opentime;
    }

    public void setOpentime(String opentime) {
        this.opentime = opentime;
    }

    public String getClosetime() {
        return closetime;
    }

    public void setClosetime(String closetime) {
        this.closetime = closetime;
    }

    public String getSendtime() {
        return sendtime;
    }

    public void setSendtime(String sendtime) {
        this.sendtime = sendtime;
    }

    public int getSendtype() {
        return sendtype;
    }

    public void setSendtype(int sendtype) {
        this.sendtype = sendtype;
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone;
    }

    public String getAutoupdate() {
        return autoupdate;
    }

    public void setAutoupdate(String autoupdate) {
        this.autoupdate = autoupdate;
    }

    public String getSystime() {
        return systime;
    }

    public void setSystime(String systime) {
        this.systime = systime;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getBright() {
        return bright;
    }

    public void setBright(int bright) {
        this.bright = bright;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getRomid() {
        return romid;
    }

    public void setRomid(String romid) {
        this.romid = romid;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public int getJpegshowtime() {
        return jpegshowtime;
    }

    public void setJpegshowtime(int jpegshowtime) {
        this.jpegshowtime = jpegshowtime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.opentime);
        dest.writeString(this.closetime);
        dest.writeString(this.sendtime);
        dest.writeInt(this.sendtype);
        dest.writeString(this.mobilephone);
        dest.writeString(this.autoupdate);
        dest.writeString(this.systime);
        dest.writeInt(this.volume);
        dest.writeInt(this.bright);
        dest.writeString(this.weather);
        dest.writeString(this.romid);
        dest.writeString(this.server);
        dest.writeString(this.media);
        dest.writeInt(this.jpegshowtime);
    }

    public SystemBean() {
    }

    protected SystemBean(Parcel in) {
        this.opentime = in.readString();
        this.closetime = in.readString();
        this.sendtime = in.readString();
        this.sendtype = in.readInt();
        this.mobilephone = in.readString();
        this.autoupdate = in.readString();
        this.systime = in.readString();
        this.volume = in.readInt();
        this.bright = in.readInt();
        this.weather = in.readString();
        this.romid = in.readString();
        this.server = in.readString();
        this.media = in.readString();
        this.jpegshowtime = in.readInt();
    }

    public static final Creator<SystemBean> CREATOR = new Creator<SystemBean>() {
        @Override
        public SystemBean createFromParcel(Parcel source) {
            return new SystemBean(source);
        }

        @Override
        public SystemBean[] newArray(int size) {
            return new SystemBean[size];
        }
    };
}
