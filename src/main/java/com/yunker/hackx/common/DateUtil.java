package com.yunker.hackx.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by 曹磊(Hackx) on 12/7/2017.
 * Email: caolei@mobike.com
 */
public class DateUtil {

    public static String getCurrentDayStr() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public static String getTomorrowStr() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public static void main(String[] args) {
        System.out.println(getTomorrowStr());
    }
}

