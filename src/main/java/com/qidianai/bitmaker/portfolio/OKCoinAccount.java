package com.qidianai.bitmaker.portfolio;

import com.qidianai.bitmaker.config.OKCoinCfg;
import com.qidianai.bitmaker.event.EvOrder;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.event.EvUserInfo;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonOrder;
import com.qidianai.bitmaker.marketclient.okcoin.JsonUserInfo;
import com.qidianai.bitmaker.marketclient.okcoin.OKCoinClient;
import com.qidianai.bitmaker.notification.SMTPNotify;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.portfolio
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/

public class OKCoinAccount extends Account {
    private Logger log = LogManager.getLogger(getClass().getName());
    private OKCoinClient okCoinClient;
    private String apiKey = OKCoinCfg.apiKey;
    private String secretKey = OKCoinCfg.secretKey;
    private String url = OKCoinCfg.url;

    private JsonUserInfo lastUserInfo;
    private ConcurrentHashMap<String, Order> orderMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Order> activeOrderMap = new ConcurrentHashMap<>();
    private double initialCny = -1;
    /**
     * Available Chinese Yuan
     */
    private double availableCny;
    /**
     * Available ether
     */
    private double availableEth;

    public double getInitialCny() {
        return initialCny;
    }

    public ConcurrentHashMap<String, Order> getOrderMap() {
        return orderMap;
    }

    public ConcurrentHashMap<String, Order> getActiveOrderMap() {
        return activeOrderMap;
    }

    public double getAvailableCny() {
        return availableCny;
    }

    public double getAvailableEth() {
        return availableEth;
    }


    public void setMarketAccount(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;

    }

    public void setMarketInfo(String apiKey, String secretKey, String url) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.url = url;
    }

    public void buyEth(double price, double amount) {
        log.warn("Buy eth " + price + " " + amount);

        String priceStr = String.format("%.3f", price);
        String amountStr = String.format("%.3f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "buy");
    }

    public void sellEth(double price, double amount) {
        log.warn("Sell eth " + price + " " + amount);

        String priceStr = String.format("%.3f", price);
        String amountStr = String.format("%.3f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "sell");
    }

    public void buyMarketEth(double price) {
        log.warn("Buy eth (market) " + price);

        String priceStr = String.format("%.3f", price);
        String amountStr = null;

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "buy_market");
    }

    public void sellMarketEth(double amount) {
        log.warn("Sell eth (market) "  + amount);

        String priceStr = null;
        String amountStr = String.format("%.3f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "sell_market");
    }

    public void cancelEth(String orderId) {
        okCoinClient.cancelOrder("eth_cny", Long.parseLong(orderId));
    }

    public void queryUserInfo() {
        okCoinClient.getUserInfo();
    }

    public double getTotalAssetValueCny(double lastEthPrice) {
        return availableCny + lastEthPrice * availableEth;
    }

    public void connectMarket() {
        if (apiKey == null || secretKey == null) {
            log.error("connectMarket Failed. apiKey or secretKey is not set.");
            return;
        }
        if (okCoinClient != null) {
            // stop client
        }
        okCoinClient = new OKCoinClient(apiKey, secretKey, url);
        okCoinClient.connect();
        okCoinClient.login();
    }

    public void subscribeMarketQuotation() {
        okCoinClient.subTickerEth();
        okCoinClient.subKlineEth("1min");
        okCoinClient.subKlineEth("5min");
        okCoinClient.subKlineEth("15min");
        okCoinClient.subKlineEth("30min");

    }

    public void reSubscribeMarketQuotation() {
        okCoinClient.reconnect();
    }

    @Override
    public void prepare() {
        Reactor.getInstance().register(EvUserInfo.class, this);
        Reactor.getInstance().register(EvOrder.class, this);

        connectMarket();
        queryUserInfo();
    }

    @Override
    public void update() {
        if (lastUserInfo != null) {
            availableCny = lastUserInfo.info.free.cny;
            availableEth = lastUserInfo.info.free.eth;

            // update initial money CNY
            if (initialCny == -1) {
                initialCny = availableCny;
            }

            if (availableEth < 0.001) {
                initialCny = availableCny;
            }
        }
    }

    @Override
    public void exit() {
        Reactor.getInstance().unregister(EvUserInfo.class, this);
        Reactor.getInstance().unregister(EvOrder.class, this);
    }

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvUserInfo.class) {
            EvUserInfo evt = (EvUserInfo) ev;
            JsonUserInfo data = evt.getData();

            lastUserInfo = data;

        } else if (ev.getType() == EvOrder.class) {
            EvOrder evt = (EvOrder) ev;
            JsonOrder data = evt.getData();

            OkCoinOrder order = new OkCoinOrder();
            order.load(data);

            log.info(order);


            // update orderinfo
            orderMap.put(order.orderId, order);
            activeOrderMap.put(order.orderId, order);


            // Order Finished
            if (order.status == Order.OrderStatus.OrderDone || order.status == Order.OrderStatus.OrderCancelled) {
                SMTPNotify.send("Order Information " + order.orderId, order.toString());
                activeOrderMap.remove(order.orderId);
            }
        }
    }
}
