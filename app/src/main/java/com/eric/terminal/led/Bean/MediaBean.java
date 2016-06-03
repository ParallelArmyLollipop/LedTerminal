package com.eric.terminal.led.Bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Eric on 2016/6/3.
 */
public class MediaBean implements Parcelable {

    private int style;
    private List<TaskBean> taskList;

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public List<TaskBean> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TaskBean> taskList) {
        this.taskList = taskList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.style);
        dest.writeTypedList(this.taskList);
    }

    public MediaBean() {
    }

    protected MediaBean(Parcel in) {
        this.style = in.readInt();
        this.taskList = in.createTypedArrayList(TaskBean.CREATOR);
    }

    public static final Creator<MediaBean> CREATOR = new Creator<MediaBean>() {
        @Override
        public MediaBean createFromParcel(Parcel source) {
            return new MediaBean(source);
        }

        @Override
        public MediaBean[] newArray(int size) {
            return new MediaBean[size];
        }
    };
}
