package com.reactlibrary;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPUtil {
    private static final String TAG = "HTTPUtil";
    static final String ENDPOINT = "https://covid-no-more-be.herokuapp.com/ping";
    private final OkHttpClient client = new OkHttpClient();
    private String android_id = null;

    public HTTPUtil(Context context) {
        try {
            this.android_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {

        }

    }

    public void ping(String deviceType, String data) {
        HttpUrl.Builder URL = HttpUrl.parse(ENDPOINT).newBuilder();
        String url = URL
                .addQueryParameter("dt", deviceType)
                .addQueryParameter("data", data)
                .addQueryParameter("aid", android_id)
                .build().toString();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(httpCallback);
    }

    private Callback httpCallback = new Callback() {
        @Override public void onFailure(Call call, IOException e) {
            Log.e(TAG, "Error " + e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.d(TAG, "Success " + response);
            response.body().close();
        }
    };
}
