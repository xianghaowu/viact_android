package com.viact.viact_android.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Project implements Parcelable {
    public int id = 0;
    public String name = "";
    public String company = "";
    public String address = "";
    public String desc = "";
    public String site_map = "";
    public String sync = "false";

    public Project(){
        id = 0;
        name = "";
        company = "";
        address = "";
        desc = "";
        site_map = "";
        sync = "false";
    }

    protected Project(Parcel in) {
        id = in.readInt();
        name = in.readString();
        company = in.readString();
        address = in.readString();
        desc = in.readString();
        site_map = in.readString();
        sync = in.readString();
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
        parcel.writeString(company);
        parcel.writeString(address);
        parcel.writeString(desc);
        parcel.writeString(site_map);
        parcel.writeString(sync);
    }
}
