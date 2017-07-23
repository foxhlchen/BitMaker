package com.qidianai.bitmaker.userstrategy;

import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.portfolio.Account;
import com.qidianai.bitmaker.portfolio.OKCoinAccount;
import com.qidianai.bitmaker.quote.BollingerBand;
import com.qidianai.bitmaker.strategy.Strategy;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.userstrategy
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class BollStrategy extends Strategy {
    OKCoinAccount account = new OKCoinAccount();
    BollingerBand bollband = new BollingerBand();

    @Override
    public void prepare() {
        bollband.prepare();
        account.connectMarket();
        account.subscribeMarketQuotation();
    }

    @Override
    public void run() {
        System.out.println("upper: " + bollband.getUpperBand("15min"));
        System.out.println("lower: " + bollband.getLowerBand("15min"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void handle(Event ev) {

    }
}
