package com.qidianai.bitmaker.userstrategy;

import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.portfolio.OKCoinAccount;
import com.qidianai.bitmaker.quote.MACD;
import com.qidianai.bitmaker.strategy.Strategy;

import java.util.HashMap;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.userstrategy
 * Author: fox  
 * Date: 2017/8/1
 *
 **********************************************************/
public class Test extends Strategy {
    private OKCoinAccount account = new OKCoinAccount();
    private String namespace;
    private MACD macd = new MACD();

    @Override
    public void prepare(HashMap<String, String> args) {
        if (args != null && args.containsKey("namespace")) {
            this.namespace = args.get("namespace");
        }

        account.setEventDomain(namespace, namespace);
        macd.setEventDomain(namespace, namespace);

        account.prepare();
        macd.prepare();

        account.subscribeMarketQuotation();
    }

    @Override
    public void run() {
        account.update();
        macd.update();

        //System.out.println(macd.getMACD("15min"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        account.exit();
        macd.stop();
    }

    @Override
    public void handle(Event ev) {

    }
}
