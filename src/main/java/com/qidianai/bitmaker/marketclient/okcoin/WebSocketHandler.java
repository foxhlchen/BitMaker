package com.qidianai.bitmaker.marketclient.okcoin;

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
        System.out.println(msg);
    }
}
