package com.qidianai.bitmaker.marketclient.okcoin;

import com.okcoin.websocket.WebSocketService;
import com.okcoin.websocket.test.WebSoketClient;

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

    protected WebSoketClient client;

    OKCoinClient() {}

    public void connect() {
        WebSoketClient client = new WebSoketClient(url, new WebSocketService() {
            @Override
            public void onReceive(String msg) {

            }
        });
        client.start();
    }

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
