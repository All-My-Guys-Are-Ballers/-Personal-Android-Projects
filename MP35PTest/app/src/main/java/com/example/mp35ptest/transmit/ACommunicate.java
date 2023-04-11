package com.example.mp35ptest.transmit;

import okhttp3.MediaType;

/**
 * Creation date：2021/9/1 on 15:05
 * Describe:
 * Author:wangweicheng
 */
public abstract class ACommunicate {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    /**
     * init
     *
     * @return
     */
    public abstract int onInitPath();

    /**
     * send and recv host
     *
     * @param data
     * @return
     */
    public abstract Response onSendAndRecv(String data);
}
