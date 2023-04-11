package com.example.mp35ptest.utlis;

/**
 * Creation date：2021/8/30 on 16:27
 * Describe:
 * Author:wangweicheng
 */
public class PanUtlis {
//    private static String getPinblock(String pin,String pan){
//
//    }



    /**
     *
     * @param track
     * @return
     */
    public static String getPan(String track) {
        if (track == null)
            return null;

        int len = track.indexOf('=');
        if (len < 0) {
            len = track.indexOf('D');
            if (len < 0)
                return null;
        }

        if ((len < 10) || (len > 19))
            return null;
        return track.substring(0, len);
    }
    /**
     * 获取有效期
     *
     * @param track
     * @return
     */
    public static String getExpDate(String track) {
        if (track == null)
            return null;

        int index = track.indexOf('=');
        if (index < 0) {
            index = track.indexOf('D');
            if (index < 0)
                return null;
        }

        if (index + 5 > track.length())
            return null;
        return track.substring(index + 1, index + 5);
    }
}
