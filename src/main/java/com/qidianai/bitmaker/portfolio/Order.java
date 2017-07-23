package com.qidianai.bitmaker.portfolio;

import java.util.Calendar;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.portfolio
 * Author: fox  
 * Date: 2017/7/23
 *
 **********************************************************/
public class Order {
    public enum OrderStatus {
        OrderCancelled,
        OrderPending,
        OrderPartiallyFilled,
        OrderDone,
        OrderCanceling,
    }

    public class DateTime {
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

        public long toInteger() {
            return Long.parseLong(toString());
        }

        public void load(Calendar car) {
            year = car.get(Calendar.YEAR);
            month = car.get(Calendar.MONTH) + 1;
            day = car.get(Calendar.DAY_OF_MONTH);
            hour = car.get(Calendar.HOUR_OF_DAY);
            min = car.get(Calendar.MINUTE);
            sec = car.get(Calendar.SECOND);
        }
    }

    public String orderId;
    public OrderStatus status;

    public double quantity;
    public double price;
    public double tradedQty;
    public double tradedPrice;

    /**
     * tradedAmt = tradedQty * tradedPrice;
     */
    public double tradedAmt;

    public String symbol;
    public String directType;

    public Calendar createDate;
    public DateTime easyDate;

    @Override
    public String toString() {
        return String.format("orderId: %s, createDate: %s, direction: %s, status: %s, quantity: %f, price: %f, tradedQty: %f, tradedPrice: %f, tradedAmt: %f, symbol: %s",
                orderId, easyDate.toString(), directType, status.name(), quantity, price, tradedQty, tradedPrice, tradedAmt, symbol);
    }
}
