package com.viact.viact_android.utils;

public interface APICallback<T> {
    void onSuccess(T response);
    void onFailure(String error);
}
