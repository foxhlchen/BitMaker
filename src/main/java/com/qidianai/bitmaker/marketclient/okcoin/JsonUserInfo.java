package com.qidianai.bitmaker.marketclient.okcoin;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.marketclient.okcoin
 * Author: fox  
 * Date: 2017/7/23
 *
 **********************************************************/


public class JsonUserInfo {
    public class Detail {
        public double btc;
        public double usd;
        public double cny;
        public double eth;
        public double ltc;
    }

    public class Asset {
        public double total;
        public double net;
    }


    public class Info {
        public Info funds;

        Asset asset;
        public Detail free;
        public Detail freezed;
    }


    double borrowFreeze;
    public Info info;


    public void rearrange() {
        if (info.funds != null) {
            info = info.funds;
        }
    }
}
