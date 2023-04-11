package com.example.mp35ptest.core;

/**
 * Creation date：2021/6/23 on 15:11
 * Describe:
 * Author:wangweicheng
 */
public abstract class AAction {
    /**
     * action 执行开始回调
     * perform the start callback
     */
    public interface ActionStartListener {
        public void onStart(AAction action);
    }

    /**
     * ACTION结束监听器
     * end listener
     */
    public interface ActionEndListener {
        public void onEnd(AAction action, ActionResult result);
    }

    private ActionStartListener startListener;
    private ActionEndListener endListener;

    /**
     * 子类构造方法必须调用super设置ActionStartListener
     * The subclass constructor must call super to set ActionStartListener
     * @param listener
     *            {@link ActionStartListener}
     */
    public AAction(ActionStartListener listener) {
        this.startListener = listener;
    }

    /**
     * 执行ACTION之前需要先设置{@link ActionEndListener}, 此接口内部先调用 {@link ActionStartListener#onStart(AAction)} , 再调用
     * {@link AAction#process}
     */
    public void execute() {
        TransContext.getInstance().setCurrentAction(this);
        if (startListener != null) {
            startListener.onStart(this);
        }
        process();
    }

    /**
     *
     * @param listener
     */
    public void setEndListener(ActionEndListener listener) {
        this.endListener = listener;
    }

    /**
     * action的具体处理方法
     * Action specifies the action method
     */
    protected abstract void process();


    public void setResult(ActionResult result) {
        if (endListener != null) {
            endListener.onEnd(this, result);
        }
    }
}
