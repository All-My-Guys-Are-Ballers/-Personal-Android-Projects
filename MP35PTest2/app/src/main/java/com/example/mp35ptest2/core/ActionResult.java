package com.example.mp35ptest2.core;

/**
 * Creation date：2021/6/23 on 15:12
 * Describe:
 * Author:wangweicheng
 */
public class ActionResult {
    /**
     * returns the result
     */
    int ret;
    /**
     * returns the data
     */
    Object data;

    public ActionResult(int ret, Object data) {
        this.ret = ret;
        this.data = data;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
