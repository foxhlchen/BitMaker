package com.qidianai.bitmaker.marketclient.okcoin;

import java.util.LinkedList;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.marketclient.okcoin
 * Author: fox  
 * Date: 2017/7/23
 *
 **********************************************************/
public class JsonKlineBatch {
    LinkedList<JsonKline> klinelist = new LinkedList<>();

    public LinkedList<JsonKline> getKlinelist() {
        return klinelist;
    }

    public void add(JsonKline kline) {
        klinelist.add(kline);
    }
}
