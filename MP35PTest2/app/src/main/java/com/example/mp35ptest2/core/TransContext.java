package com.example.mp35ptest2.core;

import android.content.Context;
import android.text.TextUtils;

/**
 * Creation date：2021/6/23 on 15:12
 * Describe:
 * Author:wangweicheng
 */
public class TransContext {
    private static TransContext transContext = null;

    private String operID;

    private Context currentContext;
    private AAction currentAction;

    private TransContext() {

    }
    public static void close(){
        if (transContext != null) {
            transContext = null;
        }
    }
    public static synchronized TransContext getInstance() {
        if (transContext == null) {
            transContext = new TransContext();
        }
        return transContext;
    }

    public String getOperID() {
        if (TextUtils.isEmpty(operID)) operID = "01";
        return operID;
    }

    public void setOperID(String operID) {
        this.operID = operID;
    }

    public Context getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(Context currentContext) {
        this.currentContext = currentContext;
    }

    public AAction getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(AAction currentAction) {
        this.currentAction = currentAction;
    }

}
