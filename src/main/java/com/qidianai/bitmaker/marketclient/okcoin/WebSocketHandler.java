package com.qidianai.bitmaker.marketclient.okcoin;

import com.google.gson.Gson;
import com.okcoin.websocket.WebSocketService;

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

        System.out.println(msg);

        Gson gson = new Gson();
        JsonTicker[] ticker = gson.fromJson(msg, JsonTicker[].class);
        System.out.println(ticker[0].channel);
        System.out.println(ticker[0].data.buy);
    }
}
