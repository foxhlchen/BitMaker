package com.qidianai.bitmaker.userstrategy;

import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import com.qidianai.bitmaker.notification.SMTPNotify;
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
    final double RISK_FACTOR = 0.9;

    MarketStatus marketStatus = MarketStatus.mkNormal;
    OKCoinAccount account = new OKCoinAccount();
    BollingerBand bollband = new BollingerBand();
    JsonTicker lastTick = new JsonTicker();


    public void buySignal() {
        log.info("Buy signal is triggered.");

        double availableCny = account.getAvailableCny();
        if (availableCny > 1) {
            account.buyMarketEth(availableCny);
        }
    }

    public void sellSignal() {
        log.info("Sell signal is triggered.");

        double availableEth = account.getAvailableEth();
        if (availableEth >= 0.001) {
            account.sellMarketEth(availableEth);
        }
    }

    public void riskSignal() {
        log.warn("Risk signal is triggered.");

        SMTPNotify.send("Risk signal", "Risk signal has been triggered at price " + lastTick.last);

        //cancel all active orders
        account.getActiveOrderMap().forEach((orderId, order) -> {
            account.cancelEth(orderId);
        });

        double avalableEth = account.getAvailableEth();
        // sell all ether, close all positions
        if (account.getAvailableEth() >= 0.001) {
            account.sellMarketEth(avalableEth);
        }
    }

    @Override
    public void prepare() {
        Reactor.getInstance().register(EvTicker.class, this);

        bollband.prepare();
        account.prepare();
        account.subscribeMarketQuotation();
    }

    @Override
    public void run() {
        bollband.update();
        account.update();

        // risk manage
        if (account.getTotalAssetValueCny(lastTick.last) < account.getInitialCny() * RISK_FACTOR) {
            riskSignal();
        }

        double percentB_1min = bollband.getPercentB(lastTick.last, "1min");
        double percentB_15min = bollband.getPercentB(lastTick.last, "15min");
        double percentB_30min = bollband.getPercentB(lastTick.last, "30min");

        switch (marketStatus) {
            case mkNormal: {
                if (percentB_15min < 0 && percentB_30min < 0) {
                    log.info("price got into low state.");
                    marketStatus = MarketStatus.mkLower;
                }

                if (percentB_15min > 1 && percentB_30min > 1) {
                    log.info("price got into high state.");
                    marketStatus = MarketStatus.mkHigher;
                }

                break;
            }
            case mkHigher: {
                if (percentB_30min < 1) {
                    sellSignal();

                    log.info("price got into normal state.");
                    marketStatus = MarketStatus.mkNormal;
                }

                break;
            }
            case mkLower: {
                if (percentB_30min > 0) {
                    buySignal();

                    log.info("price got into normal state.");
                    marketStatus = MarketStatus.mkNormal;
                }
            }
        }


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        account.exit();
        bollband.stop();
        Reactor.getInstance().unregister(EvTicker.class, this);
    }

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvTicker.class) {
            EvTicker evTicker = (EvTicker) ev;
            JsonTicker ticker = evTicker.getData();
            lastTick = ticker;
        }
    }

    enum MarketStatus {
        mkNormal,
        mkLower,
        mkHigher,
    }
}
