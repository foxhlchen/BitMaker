package com.qidianai.bitmaker.marketclient.okcoin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.okcoin.websocket.WebSocketService;
import com.qidianai.bitmaker.event.*;
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

        //System.out.println(msg);

        Type headerType = new TypeToken<JsonMsg[]>() {
        }.getType();
        Gson gson = new Gson();
        JsonMsg[] headerPack = gson.fromJson(msg, headerType);

        int idx = 0;
        for (JsonMsg header : headerPack) {
            switch (header.channel) {

                // market ticker quote push
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

                // market trades push
                case "ok_sub_spotcny_eth_trades": {
                    Type tradesType = new TypeToken<JsonMsg<String[][]>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<String[][]>[] trades = gson.fromJson(msg, tradesType);
                    String[][] data = trades[idx].data;
                    System.out.println(data[0][0] + " " + data[0][1]);

                    break;
                }

                // addChannel return
                case "addChannel": {
                    Type type = new TypeToken<JsonMsg<JsonResult>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonResult>[] pack = gson.fromJson(msg, type);
                    JsonResult result = pack[idx].data;

                    result.channel = header.channel;
                    if (result.result) {
                        log.info(result.channel + " is successfully subscribed.");
                    } else {
                        log.error(result.channel + " fail to subscribe. error_code:" + result.error_code);
                    }

                    EvResult evt = new EvResult();
                    evt.setData(result);
                    Reactor.getSingleton().publish(evt);

                    break;
                }

                // login return
                case "login": {
                    Type type = new TypeToken<JsonMsg<JsonResult>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonResult>[] pack = gson.fromJson(msg, type);
                    JsonResult result = pack[idx].data;

                    result.channel = header.channel;
                    if (result.result) {
                        log.info("Successfully login in okcoin.");
                    } else {
                        log.error("Fail to login okcoin. error_code: " + result.error_code);
                    }

                    EvResult evt = new EvResult();
                    evt.setData(result);
                    Reactor.getSingleton().publish(evt);

                    break;
                }

                // OHLC/Kline chart information
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

                // user account info
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

                    break;
                }

                // order result
                case "ok_spotcny_trade": {
                    Type type = new TypeToken<JsonMsg<JsonResult>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonResult>[] pack = gson.fromJson(msg, type);
                    JsonResult result = pack[idx].data;
                    result.channel = header.channel;

                    if (result.result) {
                        log.info(result.channel + " " + result.order_id + " order succeeded.");
                    } else {
                        log.error(result.channel + " " + result.order_id + " order failed. error_code: " + result.error_code);
                    }

                    EvResult evt = new EvResult();
                    evt.setData(result);
                    Reactor.getSingleton().publish(evt);

                    break;
                }

                // cancel order result
                case "ok_spotcny_cancel_order": {
                    Type type = new TypeToken<JsonMsg<JsonResult>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonResult>[] pack = gson.fromJson(msg, type);
                    JsonResult result = pack[idx].data;
                    result.channel = header.channel;

                    if (result.result) {
                        log.info(result.channel + " " + result.order_id + " cancel order succeeded.");
                    } else {
                        log.error(result.channel + " " + result.order_id + " cancel order failed. error_code: " + result.error_code);
                    }

                    EvResult evt = new EvResult();
                    evt.setData(result);
                    Reactor.getSingleton().publish(evt);

                    break;
                }

                case "ok_sub_spotcny_trades": {
                    Type type = new TypeToken<JsonMsg<JsonOrder>[]>() {
                    }.getType();
                    gson = new Gson();
                    JsonMsg<JsonOrder>[] pack = gson.fromJson(msg, type);
                    JsonOrder data = pack[idx].data;

                    EvOrder evt = new EvOrder();
                    evt.setData(data);
                    Reactor.getSingleton().publish(evt);

                    break;
                }
            }


            idx++;
        }
    }
}
