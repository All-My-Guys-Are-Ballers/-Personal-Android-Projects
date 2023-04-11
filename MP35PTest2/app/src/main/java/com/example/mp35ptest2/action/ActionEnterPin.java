package com.example.mp35ptest2.action;

import android.content.Context;
import android.content.Intent;

import com.example.mp35ptest2.PinpadActivity;
import com.example.mp35ptest2.core.AAction;
import com.example.mp35ptest2.core.ActionResult;

/**
 * Creation date：2021/8/30 on 16:18
 * Describe:
 * Author:wangweicheng
 */
public class ActionEnterPin extends AAction {
    /**
     * 子类构造方法必须调用super设置ActionStartListener
     * The subclass constructor must call super to set ActionStartListener
     * @param listener {@link ActionStartListener}
     */
    public ActionEnterPin(ActionStartListener listener) {
        super(listener);
    }
    private Context context;
    private String title;
    private String pan;
    private String amount;
    private String cashAmount;
    private int enterPinType;

    public final static int ONLINE_PIN = 0x00;//, // 联机pin
    public final static int OFFLINE_PLAIN_PIN= 0x01;//, // 脱机明文pin
    public final static int OFFLINE_CIPHER_PIN= 0x02;//, // 脱机密文pin

    /**
     *
     * @param context
     * @param title
     * @param pan
     * @param amount
     * @param cashAmount
     * @param enterPinType
     */
    public void setParam(Context context,String title, String pan,String amount,String cashAmount,int enterPinType) {
        this.context = context;
        this.title = title;
        this.pan = pan;
        this.amount = amount;
        this.cashAmount = cashAmount;
        this.enterPinType = enterPinType;
    }

    @Override
    protected void process() {

        Intent intent = new Intent(context, PinpadActivity.class);
        intent.putExtra("NAV_TITLE", title);

        intent.putExtra("TRANS_AMOUNT", amount);
        intent.putExtra("TRANS_AMOUNT_CASH", cashAmount);
        intent.putExtra("PANBLOCK", pan);
        intent.putExtra("ENTERPINTYPE", enterPinType);

        context.startActivity(intent);
    }

    @Override
    public void setResult(ActionResult result) {
        // TODO Auto-generated method stub
        super.setResult(result);
        context = null;
    }
}
