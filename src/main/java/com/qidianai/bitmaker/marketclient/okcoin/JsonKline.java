package com.qidianai.bitmaker.marketclient.okcoin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.marketclient.okcoin
 * Author: fox  
 * Date: 2017/7/23
 *
 **********************************************************/
public class JsonKline {
    public enum KlinePeriod {
        kLineNone,
        kLine1Min,
        kLine15Min,
        kLine30Min,
    }

    public class KlineDateTime {
        int year;
        int month;
        int day;
        int hour;
        int min;
        int sec;

        @Override
        public String toString() {
            return String.format("%d%02d%02d%02d%02d%02d", year, month, day, hour, min, sec);
        }
    }

    public KlinePeriod klinePeriod;
    public Calendar klineDate;
    public String timeStamp_ms;
    public double openPrice;
    public double highPrice;
    public double lowPrice;
    public double closePrice;
    public double volumn;
    public KlineDateTime easyDate;

    public void load(KlinePeriod period, ArrayList<String> kline) throws ParseException {
        klinePeriod = period;
        timeStamp_ms = kline.get(0);
        openPrice = Double.parseDouble(kline.get(1));
        highPrice = Double.parseDouble(kline.get(2));
        lowPrice = Double.parseDouble(kline.get(3));
        closePrice = Double.parseDouble(kline.get(4));
        volumn = Double.parseDouble(kline.get(5));


        klineDate = new GregorianCalendar();
        klineDate.setTimeInMillis(Long.parseLong(timeStamp_ms));

        easyDate.year = klineDate.get(Calendar.YEAR);
        easyDate.month = klineDate.get(Calendar.MONTH);
        easyDate.day = klineDate.get(Calendar.DAY_OF_MONTH);
        easyDate.hour = klineDate.get(Calendar.HOUR_OF_DAY);
        easyDate.min = klineDate.get(Calendar.MINUTE);
        easyDate.sec = klineDate.get(Calendar.SECOND);
    }
}
