package com.arashivision.sdk.demo.util;

public interface APICallback<T> {
    void onSuccess(T response);
    void onFailure(String error);
}
