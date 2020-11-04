package com.magaofei.gateway.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * @author magaofei
 * @date 11/4/20
 */
public class HttpClient {

    static String request() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:8808")
                .build();
        try (Response response = client.newCall(request).execute()){
            return Objects.requireNonNull(response.body()).string();
        }
    }
    public static void main(String[] args) {
        try {
            System.out.println("result = " + request());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
