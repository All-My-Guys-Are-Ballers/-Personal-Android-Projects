package com.example.mp35ptest.param;

import android.content.Context;

import com.topwise.manager.AppLog;
import com.topwise.manager.emv.entity.EmvAidParam;
import com.topwise.manager.emv.entity.EmvCapkParam;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Creation date：2021/4/21 on 10:03
 * Describe:
 * Author:wangweicheng
 */
public class AidParam extends LoadParam<EmvAidParam>{
    protected static final String TAG = LoadParam.class.getSimpleName();
    private static final String AIDNAME = "aid.xml";


    @Override
    public List<EmvAidParam> saveEmvAidParam() {
        if (list == null || list.size() == 0) return null;

        long l = System.currentTimeMillis();
        AppLog.e(TAG,"Aid saveAll 0000000000001 " + l );
        List<EmvAidParam> aidList= new ArrayList<>();
        for (String aid :list) {
            AppLog.d(TAG,"Aid==" + aid);
            aidList.add(saveAid(aid));
        }
        AppLog.e(TAG,"Aid saveAll 0000000000002 " + (System.currentTimeMillis() - l));
        return aidList;
    }

    @Override
    public List<EmvCapkParam> saveEmvCapkParam() {
        return null;
    }

    /**
     * 从xml文件 解析List<String>
     * @param context
     * @return
     */
    @Override
    public List<String> init(Context context) {
        long l = System.currentTimeMillis();
        AppLog.e(TAG,"Aid init 0000000000001 " + l);
        try {
            InputStream open = context.getResources().getAssets().open(AIDNAME);
            DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuidler = null;
            Document doc = null;
            docBuidler = docFact.newDocumentBuilder();
            doc = docBuidler.parse(open);
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("aid");

            list = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String aidparam = element.getAttribute("aidparam");
                list.add(aidparam);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"AidParam init err");
            return null;
        }
        AppLog.e(TAG,"Aid init 0000000000002 " + (System.currentTimeMillis() - l));
        AppLog.d(TAG,"AidParam init ==" +list.toString());

        return list;
    }


}
