package com.example.mp35ptest2.transmit;

import android.util.Log;

import com.topwise.emvtest.entity.TransResult;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Creation date：2021/9/1 on 15:06
 * Describe:
 * Author:wangweicheng
 */
public class OkHttpClient extends ACommunicate{
    private static final String TAG = OkHttpClient.class.getSimpleName();

    protected String path;

    protected int connectTomeOut;
    @Override
    public int onInitPath() {
        //get the hostIp
        path = "https://pn-io-sandbox.azurewebsites.net/io/v1.0/h2hpayments";
        connectTomeOut = 30;
        return 0;
    }

    @Override
    public Response onSendAndRecv(String data) {
        Log.e(TAG,"url ==== " + path +"  "+ connectTomeOut);
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .writeTimeout(connectTomeOut, TimeUnit.SECONDS)
                .readTimeout(connectTomeOut, TimeUnit.SECONDS)
                .connectTimeout(connectTomeOut, TimeUnit.SECONDS).build();


        RequestBody body = RequestBody.create(JSON,data);
        final Request request = new Request.Builder()
                .url(path)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .addHeader("Accept", "application/json")
                .post(body)
                .build();

        okhttp3.Response response = null;
        try {
            response = client.newCall(request).execute();

            Log.d(TAG,"code=== " + response.code());
            if (!response.isSuccessful()){
                return new Response(TransResult.ERR_RECV,null);
            }
            return new Response(TransResult.SUCC, response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"onSendAndRecv=== " + e.getMessage() );
            if (e instanceof SocketTimeoutException) {
                // The server response times out
                Log.d(TAG,"onSendAndRecv=== SocketTimeoutException "  );
                return new Response(TransResult.ERR_RECV, null);


            }else if (e instanceof ConnectException) {
                // The server response times out
                Log.d(TAG,"onSendAndRecv=== ConnectException " );
                return new Response(TransResult.ERR_CONNECT, null);
            }else if (e instanceof UnknownHostException) {
                // Parsing the url error or no Internet
                Log.d(TAG,"onSendAndRecv=== UnknownHostException " );
                return new Response(TransResult.ERR_CONNECT, null);
            }

        }finally {
            if (response != null)
                response.body().close();
        }
        return new Response(TransResult.ERR_RECV, null);
    }
}
