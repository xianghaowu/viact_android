package com.viact.viact_android.utils;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viact.viact_android.models.UploadedData;
import com.viact.viact_android.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class API {
    private static final String baseUrl                     = "https://api.dev.viact.net/";
    public static final String authUrl                      = baseUrl + "auth/authenticate";
    public static final String getUser                      = baseUrl + "api/authenticated";
    public static final String fileUpload                   = baseUrl +"api/file-upload";
    public static final String sitemapUpload                = baseUrl +"api/device";

    public static void getLoginUser(String token, final APICallback<User> callback) {
        AndroidNetworking.get(getUser)
                .addHeaders("Authorization", "Bearer " + token)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.create();
                            User one = gson.fromJson(response.toString(), User.class);
                            callback.onSuccess(one);
                        } catch (Exception e) {
                            callback.onFailure("Json parse error");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        String bodyStr = anError.getErrorBody();
                        try{
                            JSONObject obj = new JSONObject(bodyStr);
                            if (obj.has("message")){
                                String err_msg = obj.getString("message");
                                callback.onFailure("err_msg");
                            } else {
                                callback.onFailure(bodyStr);
                            }
                        } catch (Exception e){
                            callback.onFailure(bodyStr);
                        }

                    }
                });
    }

    public static void uploadSiteMap(String token, String cc_code, String snapshot, float x, float y, final APICallback<String> callback) {
        try{
            JSONObject pos_obj = new JSONObject();
            pos_obj.put("x", String.valueOf(x));
            pos_obj.put("y", String.valueOf(y));
            JSONObject loc_obj = new JSONObject();
            loc_obj.put("position", pos_obj);
            AndroidNetworking.post(sitemapUpload)
                    .addHeaders("Authorization", "Bearer " + token)
                    .addBodyParameter("company_code", cc_code)
                    .addBodyParameter("snapshot", snapshot)
                    .addJSONObjectBody(loc_obj)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                callback.onSuccess("success");
                            } catch (Exception e) {
                                callback.onFailure("Json parse error");
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            String bodyStr = anError.getErrorBody();
                            try{
                                JSONObject obj = new JSONObject(bodyStr);
                                if (obj.has("message")){
                                    String err_msg = obj.getString("message");
                                    callback.onFailure("err_msg");
                                } else {
                                    callback.onFailure(bodyStr);
                                }
                            } catch (Exception e){
                                callback.onFailure(bodyStr);
                            }

                        }
                    });
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public static void authLogin(String username, String password, final APICallback<String> callback) {
        AndroidNetworking.post(authUrl)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("token")) {
                                String token = response.getString("token");
                                callback.onSuccess(token);
                            } else {
                                callback.onFailure(response.toString());
                            }
                        } catch (Exception e) {
                            callback.onFailure("Json parse error");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        String bodyStr = anError.getErrorBody();
                        try{
                            JSONObject obj = new JSONObject(bodyStr);
                            if (obj.has("message")){
                                String err_msg = obj.getString("message");
                                callback.onFailure("err_msg");
                            } else {
                                callback.onFailure(bodyStr);
                            }
                        } catch (Exception e){
                            callback.onFailure(bodyStr);
                        }

                    }
                });
    }

    public static void uploadCaptureFile(String token, File file, final APICallback<UploadedData> callback) {
        AndroidNetworking.upload(fileUpload)
                .addHeaders("Authorization", "Bearer " + token)
                .addMultipartFile("file", file)
                .setPriority(Priority.MEDIUM)
                .build()
                .setUploadProgressListener((bytesUploaded, totalBytes) -> {
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("message") && response.has("data")) {
                                UploadedData u_data = new UploadedData();
                                u_data.message = response.getString("message");
                                u_data.data = response.getString("data");
                                callback.onSuccess(u_data);
                            } else {
                                callback.onFailure(response.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onFailure(e.toString());
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        callback.onFailure(error.toString());
                    }
                });
    }
}
