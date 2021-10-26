package com.viact.viact_android.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

public class PinPoint implements Parcelable {
    public int id = -1;
    public String p_id = "";
    public float x = 0.0f;
    public float y = 0.0f;
    public String name = "";
    public String note = "";
    public String create_time = "";
    public String update_time = "";

    public ImageView iv_mark = null;

    public PinPoint(){
        id = -1;
        p_id = "";
        x = 0.0f;
        y = 0.0f;
        name = "";
        note = "";
        create_time = "";
        update_time = "";
    }

    protected PinPoint(Parcel in) {
        id = in.readInt();
        p_id = in.readString();
        x = in.readFloat();
        y = in.readFloat();
        name = in.readString();
        note = in.readString();
        create_time = in.readString();
        update_time = in.readString();
    }

    public static final Creator<PinPoint> CREATOR = new Creator<PinPoint>() {
        @Override
        public PinPoint createFromParcel(Parcel in) {
            return new PinPoint(in);
        }

        @Override
        public PinPoint[] newArray(int size) {
            return new PinPoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(p_id);
        parcel.writeFloat(x);
        parcel.writeFloat(y);
        parcel.writeString(name);
        parcel.writeString(note);
        parcel.writeString(create_time);
        parcel.writeString(update_time);
    }
}
