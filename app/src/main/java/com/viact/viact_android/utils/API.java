package com.viact.viact_android.utils;

import android.graphics.PointF;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viact.viact_android.models.RecVideo;
import com.viact.viact_android.models.UploadedData;
import com.viact.viact_android.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class API {
    private static final String baseUrl                     = "http://13.229.69.223:7000/";//"https://api.dev.viact.net/";
    public static final String authUrl                      = baseUrl + "auth/authenticate";
    private static final String getUser                      = baseUrl + "api/authenticated";
    private static final String fileUpload                   = baseUrl + "api/file-upload";
    private static final String sitemapUpload                = baseUrl + "api/device";

    private static final String uploadSLAMVideo              = baseUrl + "upload-video";


    public static void uploadSLAMVideo(RecVideo recVideo, final APICallback<Pair<String, String>> callback){
        try{
            String[] fp_str = recVideo.first_pos.split(",");
            String[] sp_str = recVideo.second_pos.split(",");
            if (fp_str.length == 2 && sp_str.length == 2){
                JSONObject f_pos = new JSONObject();
                f_pos.put("x", Float.parseFloat(fp_str[0]));
                f_pos.put("y", Float.parseFloat(fp_str[1]));
                JSONObject s_pos = new JSONObject();
                s_pos.put("x", Float.parseFloat(sp_str[0]));
                s_pos.put("y", Float.parseFloat(sp_str[1]));
                File videoFile = new File(recVideo.path);
                AndroidNetworking.upload(uploadSLAMVideo)
//                        .addHeaders("Authorization", "Bearer " + token)
//                        .addHeaders("Content-Type", "application/json")
                        .addMultipartFile("video", videoFile)
                        .addMultipartParameter("first", f_pos.toString())
                        .addMultipartParameter("second", s_pos.toString())
                        .addMultipartParameter("timestamp", recVideo.gap_time)
                        .addMultipartParameter("createtime", recVideo.create_time)
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .setUploadProgressListener((bytesUploaded, totalBytes) -> {
                        })
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    String obj = response.getString("status");
                                    if (obj.equals("success")){
                                        String v_id = response.getString("id");
                                        String url = response.getString("url");
                                        callback.onSuccess(new Pair<>(v_id, url));
                                    } else {
                                        String err = response.getString("error");
                                        callback.onFailure(err);
                                    }
                                } catch (Exception e){
                                    callback.onFailure("JSON Parse error!");
                                }
                            }
                            @Override
                            public void onError(ANError error) {
                                callback.onFailure(error.toString());
                            }
                        });
            } else {
                callback.onFailure("First & Second point error!");
            }

        } catch (JSONException ex){
            ex.printStackTrace();
            callback.onFailure("JSON error!");
        }
    }

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
