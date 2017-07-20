package com.qidianai.bitmaker.marketclient.okcoin;

import com.okcoin.websocket.WebSocketBase;
import com.okcoin.websocket.WebSocketService;

/**********************************************************
 * BitMaker
 *
 * File: com.qidianai.bitmaker.marketclient.okcoin
 * Author: fox  
 * Date: 2017/7/20
 *
 **********************************************************/

class WebSocketClient extends WebSocketBase {
    WebSocketClient(String url, WebSocketService serivce) {
        super(url, serivce);
    }


}
