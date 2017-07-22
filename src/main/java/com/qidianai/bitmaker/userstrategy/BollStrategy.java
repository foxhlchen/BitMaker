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
    Account account = new OKCoinAccount();
    BollingerBand boll = new BollingerBand();

    @Override
    public void prepare() {
        account.connectMarket();
        boll.registerEvent();
    }

    @Override
    public void run() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void handle(Event ev) {

    }
}
