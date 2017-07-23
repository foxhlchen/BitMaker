package com.qidianai.bitmaker.marketclient.okcoin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.okcoin.websocket.WebSocketService;
import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.event.EvUserInfo;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;

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

        System.out.println(msg);

        Type headerType = new TypeToken<JsonMsg[]>() {
        }.getType();
        Gson gson = new Gson();
        JsonMsg[] headerPack = gson.fromJson(msg, headerType);

        int idx = 0;
        for (JsonMsg header : headerPack) {
            switch (header.channel) {
                case "ok_sub_spotcny_eth_ticker": {
                    Type tickerType = new TypeToken<JsonMsg<JsonTicker>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonTicker>[] ticker = gson.fromJson(msg, tickerType);
                    JsonTicker tickerData = ticker[idx].data;

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
                    String[][] data = trades[idx].data;
                    System.out.println(data[0][0] + " " + data[0][1]);

                    break;
                }

                case "addChannel": {
                    Type type = new TypeToken<JsonMsg<JsonResult>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonResult>[] pack = gson.fromJson(msg, type);
                    JsonResult result = pack[idx].data;
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
                    JsonResult result = pack[idx].data;
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
                    //System.out.println(msg);

                    Type type = new TypeToken<JsonMsg<ArrayList<ArrayList<String>>>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<ArrayList<ArrayList<String>>>[] pack = gson.fromJson(msg, type);
                    ArrayList<ArrayList<String>> data = pack[idx].data;


                    JsonKline.KlinePeriod period = JsonKline.KlinePeriod.kLineNone;
                    switch (header.channel) {
                        case "ok_sub_spotcny_eth_kline_1min":
                            period = JsonKline.KlinePeriod.kLine1Min;

                            break;
                        case "ok_sub_spotcny_eth_kline_15min":
                            period = JsonKline.KlinePeriod.kLine15Min;

                            break;
                        case "ok_sub_spotcny_eth_kline_30min":
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
                    Reactor.getSingleton().publish(evKline);

                    break;
                }

                case "ok_spotcny_userinfo":
                case "ok_sub_spotcny_userinfo": {
                    Type type = new TypeToken<JsonMsg<JsonUserInfo>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonUserInfo>[] pack = gson.fromJson(msg, type);
                    JsonUserInfo data = pack[idx].data;

                    data.rearrange();


                    EvUserInfo evt = new EvUserInfo();
                    evt.setData(data);
                    Reactor.getSingleton().publish(evt);

                    System.out.println(data.info.free.cny);
                    System.out.println(data.info.free.eth);

                    break;
                }
            }


            idx++;
        }
    }
}
