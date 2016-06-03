package com.eric.terminal.led.Bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eric on 2016/6/3.
 */
public class TaskBean implements Parcelable {
    private String type;
    private String pwd;
    private String uuid;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.pwd);
        dest.writeString(this.uuid);
    }

    public TaskBean() {
    }

    protected TaskBean(Parcel in) {
        this.type = in.readString();
        this.pwd = in.readString();
        this.uuid = in.readString();
    }

    public static final Parcelable.Creator<TaskBean> CREATOR = new Parcelable.Creator<TaskBean>() {
        @Override
        public TaskBean createFromParcel(Parcel source) {
            return new TaskBean(source);
        }

        @Override
        public TaskBean[] newArray(int size) {
            return new TaskBean[size];
        }
    };
}
