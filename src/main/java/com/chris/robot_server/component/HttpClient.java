package com.chris.robot_server.component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class HttpClient {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();


    public String get(String url) throws IOException {
        Request req = new Request.Builder().url(url).build();
        try (Response res = client.newCall(req).execute()) {
            return res.body().string();
        }
    }
}
