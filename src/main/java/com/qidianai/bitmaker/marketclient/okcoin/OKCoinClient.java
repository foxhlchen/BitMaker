package com.qidianai.bitmaker.marketclient.okcoin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.okcoin.rest.stock.IStockRestApi;
import com.okcoin.rest.stock.impl.StockRestApi;
import com.okcoin.websocket.WebSocketService;
import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.eventsys.Reactor;
import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;


/**********************************************************
 * BitMaker
 *
 * File: com.qidianai.bitmaker.marketclient.okcoin
 * Author: Fox  
 * Date: 7/11/2017
 *
 **********************************************************/

public class OKCoinClient {
    private String apiKey;
    private String secretKey;
    private String url = "wss://real.okcoin.cn:10440/websocket/okcoinapi";

    private IStockRestApi restApi;
    private WebSocketClient client;
    private Logger log = LogManager.getLogger(getClass().getName());
    private String tag;
    private String namespace;

    public OKCoinClient() {}

    public OKCoinClient(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public OKCoinClient(String apiKey, String secretKey, String url) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.url = url;
    }

    public void setEventDomain(String tag, String namespace) {
        this.tag = tag;
        this.namespace = namespace;
    }

    // ------------ Connect market --------
    public void connect() {
        client = new WebSocketClient(url, new WebSocketHandler(tag, namespace));
        client.setKey(apiKey, secretKey);
        restApi =  new StockRestApi("https://www.okcoin.cn", apiKey, secretKey);

        client.start();
        log.info("OKCoinClient Connected");
    }

    public void reconnect() {
        client.reConnect();
    }

    // ------------ Quotation --------------

    public void getKlineEth(String type, String size, String since) {
        String ret = null;

        try {
            ret = restApi.kLine(type, "eth_cny", size, since);
        } catch (HttpException|IOException e) {
            e.printStackTrace();
        }

        if (ret == null || ret.charAt(0) == '{') {
            log.error("Get Kline " + type + " Failed. " + ret);
            return;
        }


        Type datatype = new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType();
        Gson gson = new Gson();
        ArrayList<ArrayList<String>> data = gson.fromJson(ret, datatype);

        JsonKline.KlinePeriod period = JsonKline.KlinePeriod.kLineNone;
        switch (type) {
            case "1min":
                period = JsonKline.KlinePeriod.kLine1Min;

                break;
            case "5min":
                period = JsonKline.KlinePeriod.kLine5Min;

                break;
            case "15min":
                period = JsonKline.KlinePeriod.kLine15Min;

                break;
            case "30min":
                period = JsonKline.KlinePeriod.kLine30Min;

                break;
        }

        JsonKlineBatch batch = new JsonKlineBatch();
        for (ArrayList<String> kline : data) {
            JsonKline jsonKline = new JsonKline();
            try {
                jsonKline.load(period, kline);
            } catch (ParseException e) {
                log.error("Kline parse timestamp error " + e.getMessage());
            }
            batch.add(jsonKline);
        }

        EvKline evKline = new EvKline();
        evKline.setData(batch);
        evKline.setTag(tag);
        Reactor.getSingleton(namespace).publish(evKline);
    }

    public boolean subTickerEth() {
        if (client == null) {
            log.warn("subTickerEth failed client is disconnected");
            return false;
        }

        log.info("subscribe ticker eth");
        client.addChannel("ok_sub_spotcny_eth_ticker");

        return true;
    }

    public boolean subTickerBtc() {
        if (client == null) {
            log.warn("subTickerBtc failed client is disconnected");
            return false;
        }

        log.info("subscribe ticker btc");
        client.addChannel("ok_sub_spotcny_btc_ticker");

        return true;
    }

    public boolean subTradesEth(String freq) {
        if (client == null) {
            log.warn("subTradesEth failed client is disconnected");
            return false;
        }

        log.info("subscribe trades eth");
        client.addChannel("ok_sub_spotcny_eth_trades");

        return true;
    }

    /**
     * subscribe kline
     * @param freq 1min, 3min, 5min, 15min, 30min, 1hour, 2hour, 4hour, 6hour, 12hour, day, 3day, week
     * @return is succeeded
     */
    public boolean subKlineEth(String freq) {
        if (client == null) {
            log.warn("subTradesEth failed client is disconnected");
            return false;
        }

        log.info("subscribe kline eth " + freq);
        client.addChannel("ok_sub_spotcny_eth_kline_" + freq);

        return true;
    }

    // ------------ trade --------------
    public boolean login() {
        if (client == null) {
            log.warn("login failed client is disconnected");
            return false;
        }

        log.info("login to okcoin");
        client.login();
        return true;

    }

    public boolean getUserInfo() {
        if (client == null) {
            log.warn("getUserInfo failed client is disconnected");
            return false;
        }

        log.info("getUserInfo");
        client.getUserInfo(apiKey, secretKey);
        return true;
    }

    public boolean spotTrade(String symbol, String price, String amt, String type) {
        if (client == null) {
            log.warn("SpotTrade failed client is disconnected");
            return false;
        }

        log.info(String.format("SpotTrade Symbol:%s Price:%s Amount:%s Type:%s", symbol, price, amt, type));
        client.spotTrade(apiKey, secretKey, symbol, price, amt, type);

        return true;
    }

    public boolean cancelOrder(String symbol, Long orderId) {
        if (client == null) {
            log.warn("cancelorder "+ orderId + " failed client is disconnected");
            return false;
        }

        client.cancelOrder(apiKey, secretKey, symbol, orderId);

        return true;
    }

    // ------------ getter setter -----------

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
