package com.alipay.sofa.serverless.arklet.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lunarscave
 */
public class ConvertUtils {

    public static double bytes2Megabyte(Long bytes) {
        return ((double) bytes) / 1024 / 1024;
    }

    public static String endDate2Duration(Date date) {
        long duration = System.currentTimeMillis() - date.getTime();
        return new SimpleDateFormat("HH-mm-ss").format(duration);
    }

    public static double millisecond2Second(Date date) {
        return ((double) System.currentTimeMillis() - date.getTime()) / 1000;
    }

}