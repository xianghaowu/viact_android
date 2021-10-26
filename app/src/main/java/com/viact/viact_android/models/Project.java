package com.viact.viact_android.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Project implements Parcelable {
    public int id = 0;
    public String name = "";
    public String address = "";
    public String note = "";
    public String site_map = "";
    public String sync = "false";
    public String create_time = "";
    public String update_time = "";

    public Project(){
        id = 0;
        name = "";
        address = "";
        note = "";
        site_map = "";
        sync = "false";
        create_time = "";
        update_time = "";
    }

    protected Project(Parcel in) {
        id = in.readInt();
        name = in.readString();
        address = in.readString();
        note = in.readString();
        site_map = in.readString();
        sync = in.readString();
        create_time = in.readString();
        update_time = in.readString();
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeString(note);
        parcel.writeString(site_map);
        parcel.writeString(sync);
        parcel.writeString(create_time);
        parcel.writeString(update_time);
    }
}
