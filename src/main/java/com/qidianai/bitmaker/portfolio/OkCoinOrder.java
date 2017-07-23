package com.qidianai.bitmaker.portfolio;

import com.qidianai.bitmaker.marketclient.okcoin.JsonOrder;

import java.util.GregorianCalendar;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.portfolio
 * Author: fox  
 * Date: 2017/7/23
 *
 **********************************************************/
public class OkCoinOrder extends Order {

    /**
     * load order info from broker format
     * @param jsonOrder broker order info
     */
    public void load(JsonOrder jsonOrder) {
        switch (jsonOrder.status) {
            case -1:
                status = OrderStatus.OrderCancelled;
                break;
            case 0:
                status = OrderStatus.OrderPending;
                break;
            case 1:
                status = OrderStatus.OrderPartiallyFilled;
                break;
            case 2:
                status = OrderStatus.OrderDone;
                break;
            case 4:
                status = OrderStatus.OrderCanceling;
                break;
        }

        orderId = String.format("%d", jsonOrder.orderId);
        price = Double.parseDouble(jsonOrder.tradeUnitPrice);
        quantity = Double.parseDouble(jsonOrder.tradeAmount);
        tradedPrice = Double.parseDouble(jsonOrder.averagePrice);
        tradedAmt = Double.parseDouble(jsonOrder.tradePrice);
        tradedQty = Double.parseDouble(jsonOrder.completedTradeAmount);
        symbol = jsonOrder.symbol;
        directType = jsonOrder.tradeType;


        createDate = new GregorianCalendar();
        createDate.setTimeInMillis(jsonOrder.createdDate);

        easyDate = new DateTime();
        easyDate.load(createDate);
    }
}
