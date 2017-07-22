package com.qidianai.bitmaker.marketclient.okcoin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.okcoin.websocket.WebSocketService;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Reactor;

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

    @Override
    public void onReceive(String msg) {
        if (msg.charAt(0) == '{')
            return;


        Type headerType = new TypeToken<JsonMsg[]>() {}.getType();
        Gson gson = new Gson();
        JsonMsg[] header = gson.fromJson(msg, headerType);

        if (header[0].channel.equals("ok_sub_spotcny_eth_ticker")) {
            Type tickerType = new TypeToken<JsonMsg<JsonTicker>[]>() {}.getType();
            gson = new Gson();
            JsonMsg<JsonTicker>[] ticker = gson.fromJson(msg, tickerType);
            JsonTicker tickerData = ticker[0].data;

            EvTicker evTicker = new EvTicker();
            //evTicker.setName("ok_sub_spotcny_eth_ticker");
            evTicker.setData(tickerData);
            Reactor.getSingleton().publish(evTicker);

        } else if (header[0].channel.equals("ok_sub_spotcny_eth_trades")) {
            Type tradesType = new TypeToken<JsonMsg<String[][]>[]>() {}.getType();
            gson = new Gson();
            JsonMsg<String[][]>[] trades = gson.fromJson(msg, tradesType);
            String[][] data = trades[0].data;
            System.out.println(data[0][0] + " " + data[0][1]);
        }
    }
}
