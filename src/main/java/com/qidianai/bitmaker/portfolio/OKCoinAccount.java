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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

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

    public HashMap<String, Order> getOrderMap() {
        return orderMap;
    }

    private HashMap<String, Order> orderMap = new HashMap<>();

    /**
     * Available Chinese Yuan
     */
    private double availableCny;

    /**
     * Available ether
     */
    private double availableEth;


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
        String priceStr = String.format("%.3f", price);
        String amountStr = String.format("%.3f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "buy");
    }

    public void sellEth(double price, double amount) {
        String priceStr = String.format("%.3f", price);
        String amountStr = String.format("%.3f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "sell");
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
        okCoinClient.subKlineEth("15min");
        okCoinClient.subKlineEth("30min");

    }

    @Override
    public void prepare() {
        connectMarket();
        queryUserInfo();

        Reactor.getInstance().register(EvUserInfo.class, this);
        Reactor.getInstance().register(EvOrder.class, this);
    }

    @Override
    public void update() {
        if (lastUserInfo != null) {
            availableCny = lastUserInfo.info.free.cny;
            availableEth = lastUserInfo.info.free.eth;
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
        }
    }
}
