package com.qidianai.bitmaker.marketclient.okcoin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.okcoin.websocket.WebSocketService;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Reactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

/**********************************************************
 * BitMaker
 *
 * File: com.qidianai.bitmaker.marketclient.okcoin
 * Author: fox  
 * Date: 2017/7/20
 *
 **********************************************************/
public class WebSocketHandler implements WebSocketService {

    private Logger log = LogManager.getLogger(getClass().getName());

    @Override
    public void onReceive(String msg) {
        if (msg.charAt(0) == '{')
            return;

        //System.out.println(msg);

        Type headerType = new TypeToken<JsonMsg[]>() {}.getType();
        Gson gson = new Gson();
        JsonMsg[] headerPack = gson.fromJson(msg, headerType);
        JsonMsg header = headerPack[0];

        switch (header.channel) {
            case "ok_sub_spotcny_eth_ticker": {
                Type tickerType = new TypeToken<JsonMsg<JsonTicker>[]>() {
                }.getType();
                gson = new Gson();
                JsonMsg<JsonTicker>[] ticker = gson.fromJson(msg, tickerType);
                JsonTicker tickerData = ticker[0].data;

                EvTicker evTicker = new EvTicker();
                evTicker.setData(tickerData);
                Reactor.getSingleton().publish(evTicker);

                break;
            }

            case "ok_sub_spotcny_eth_trades": {
                Type tradesType = new TypeToken<JsonMsg<String[][]>[]>() {
                }.getType();
                gson = new Gson();
                JsonMsg<String[][]>[] trades = gson.fromJson(msg, tradesType);
                String[][] data = trades[0].data;
                System.out.println(data[0][0] + " " + data[0][1]);

                break;
            }

            case "addChannel": {
                Type type = new TypeToken<JsonMsg<JsonResult>[]>() {
                }.getType();
                gson = new Gson();
                JsonMsg<JsonResult>[] pack = gson.fromJson(msg, type);
                JsonResult result = pack[0].data;
                if (result.result) {
                    log.info(result.channel + " is successfully subscribed.");
                } else {
                    log.error(result.channel + " fail to subscribe. error_code:" + result.error_code);
                }

                break;
            }

            case "login": {
                Type type = new TypeToken<JsonMsg<JsonResult>[]>() {
                }.getType();
                gson = new Gson();
                JsonMsg<JsonResult>[] pack = gson.fromJson(msg, type);
                JsonResult result = pack[0].data;
                if (result.result) {
                    log.info("Successfully login in okcoin.");
                } else {
                    log.error("Fail to login okcoin. error_code: " + result.error_code);
                }

                break;
            }

            case "ok_sub_spotcny_eth_kline_1min":
            case "ok_sub_spotcny_eth_kline_15min":
            case "ok_sub_spotcny_eth_kline_30min": {
                System.out.println(msg);

                Type type = new TypeToken<JsonMsg<String[][]>[]>() {
                }.getType();
                gson = new Gson();
                JsonMsg<String[][]>[] pack = gson.fromJson(msg, type);
                String[][] data = pack[0].data;

                System.out.println(data.length);

            }

        }
    }
}
