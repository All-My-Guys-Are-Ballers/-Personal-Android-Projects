package com.example.mp35ptest.param;

import android.content.Context;

import com.topwise.cloudpos.aidl.emv.level2.EmvCapk;
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
 * Creation date：2021/4/21 on 9:49
 * Describe:
 * Author:wangweicheng
 */
public class CapkParam extends LoadParam<EmvCapk>{
    protected static final String TAG = LoadParam.class.getSimpleName();

    private static final String CAPKNAME = "capk.xml";
    private static final String CAPKNAME_TEST = "capk_test.xml";





    @Override
    public List<EmvAidParam> saveEmvAidParam() {
        return null;
    }

    @Override
    public List<EmvCapkParam> saveEmvCapkParam() {
        List<EmvCapkParam> capkList= new ArrayList<>();
        for (String capk:list) {
            AppLog.d(TAG,"Capk==000" + capk);
            capkList.add(saveCapk(capk));

        }

        return capkList;
    }

    @Override
    public List<String> init(Context context) {
        long l = System.currentTimeMillis();
        AppLog.e(TAG,"Capk init 0000000000003 " + l);
        try {
            String name = CAPKNAME;
            AppLog.e(TAG,"Capk init 0000000000003 name " + name);
            InputStream open = context.getResources().getAssets().open(name);
            DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuidler = null;
            Document doc = null;
            docBuidler = docFact.newDocumentBuilder();
            doc = docBuidler.parse(open);
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("capk");
            list = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String aidparam = element.getAttribute("capkparam");
                list.add(aidparam);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"CapkParam init err");
            return null;
        }
        AppLog.e(TAG,"Capk init 0000000000004 " + (System.currentTimeMillis() - l));
        AppLog.e(TAG,"CapkParam init ==" +list.toString());
        return list;
    }


}


