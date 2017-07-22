package com.qidianai.bitmaker.config;

import java.util.Properties;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.config
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class OKCoinCfg {
    public static String apiKey;
    public static String secretKey;
    public static String url = "wss://real.okcoin.cn:10440/websocket/okcoinapi";

    public static void load(Properties prop) {
        apiKey = prop.getProperty("okcoin.apiKey", apiKey);
        secretKey = prop.getProperty("okcoin.secretKey", secretKey);
        url = prop.getProperty("okcoin.websocket.url", url);
    }
}
