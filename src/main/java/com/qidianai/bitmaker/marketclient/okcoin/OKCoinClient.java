package com.qidianai.bitmaker.marketclient.okcoin;

import com.okcoin.websocket.WebSocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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

    private WebSocketClient client;
    private Logger log = LogManager.getLogger(getClass().getName());

    public OKCoinClient() {}

    public OKCoinClient(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public void connect() {
        client = new WebSocketClient(url, new WebSocketHandler());
        client.start();
        log.info("OKCoinClient Connected");
    }


    // ------------ Quotation --------------

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

    public boolean subTradesEth() {
        if (client == null) {
            log.warn("subTradesEth failed client is disconnected");
            return false;
        }

        log.info("subscribe trades eth");
        client.addChannel("ok_sub_spotcny_eth_trades");

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
