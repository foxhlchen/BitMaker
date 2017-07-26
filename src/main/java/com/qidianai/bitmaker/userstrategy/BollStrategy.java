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

import java.util.Calendar;
import java.util.HashMap;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.userstrategy
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public final class BollStrategy extends Strategy {
    private final double RISK_FACTOR = 0.9;

    private MarketStatus marketStatus = MarketStatus.mkNormal;
    private OKCoinAccount account = new OKCoinAccount();
    private BollingerBand bollband = new BollingerBand();
    private JsonTicker lastTick = new JsonTicker();
    private long lastUpdate = -1;
    private String namespace = className;

    private boolean isReported = false;

    private void buySignal() {
        log.info("Buy signal is triggered.");

        double availableCny = account.getAvailableCny();
        if (availableCny > lastTick.last * 0.01) {  // Minimum trade volume
            account.buyMarketEth(availableCny);
        }
    }

    private void sellSignal() {
        log.info("Sell signal is triggered.");

        double availableEth = account.getAvailableEth();
        if (availableEth >= 0.01) {
            account.sellMarketEth(availableEth);
        }
    }

    private void riskSignal() {
        log.warn("Risk signal is triggered.");

        SMTPNotify.send("Risk signal", "Risk signal has been triggered at price " + lastTick.last);

        //cancel all active orders
        account.getActiveOrderMap().forEach((orderId, order) -> account.cancelEth(orderId));

        double avalableEth = account.getAvailableEth();
        // sell all ether, close all positions
        if (account.getAvailableEth() >= 0.001) {
            account.sellMarketEth(avalableEth);
        }
    }

    public void sendAccountReport() {
        String reportContent = String.format("Account Total Assets: %.3f, Available Cny: %.3f, Available Eth: %.3f, " +
                        "Last Price: %.3f",
                account.getTotalAssetValueCny(lastTick.last), account.getAvailableCny(), account.getAvailableEth(),
                lastTick.last);

        SMTPNotify.send("AccountReport", reportContent);
    }

    private void dailyReport(int reportHour, int reportMin) {
        int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int nowMin = Calendar.getInstance().get(Calendar.MINUTE);

        if (nowHour == reportHour && nowMin == reportMin) {
            if (!isReported)
                sendAccountReport();
            isReported = true;
        } else {
            isReported = false;
        }
    }

    /**
     * check market connection and reconnect when connection disrupted.
     *
     * @return true for reconnecting
     */
    private boolean checkReconnectMarket() {
        long nowMiliSec = Calendar.getInstance().getTimeInMillis();
        if (lastUpdate != -1) {
            long nowSec = nowMiliSec / 1000;
            if (nowSec - lastUpdate > 30) {
                log.info("timeout, resubscribe market quotation");
                account.reSubscribeMarketQuotation();
                lastUpdate = nowSec;

                return true;
            }
        }

        return false;
    }

    @Override
    public void prepare(HashMap<String, String> args) {
        Reactor.getInstance(namespace).register(EvTicker.class, this);

        bollband.setEventDomain(namespace, namespace);
        account.setEventDomain(namespace, namespace);

        bollband.prepare();
        account.prepare();

        account.subscribeMarketQuotation();
    }

    @Override
    public void run() {
        // reconnect
        if (checkReconnectMarket()) {
            return;
        }

        // update market data
        bollband.update();
        account.update();

        // daily report
        dailyReport(16, 30);

        // risk manage
        if (account.getTotalAssetValueCny(lastTick.last) < account.getInitialCny() * RISK_FACTOR) {
            riskSignal();
        }


        // trade signal
        double percentB_1min = bollband.getPercentB(lastTick.last, "1min");
        double percentB_5min = bollband.getPercentB(lastTick.last, "5min");
        double percentB_15min = bollband.getPercentB(lastTick.last, "15min");
        double percentB_30min = bollband.getPercentB(lastTick.last, "30min");

        double sigLongTerm = percentB_30min;
        double sigShortTerm = percentB_15min;

        switch (marketStatus) {
            case mkNormal: {
                if (sigLongTerm < 0 && sigShortTerm < 0) {
                    log.info("price got into low state.");
                    marketStatus = MarketStatus.mkLower;
                }

                if (sigShortTerm > 1 && sigLongTerm > 1) {
                    log.info("price got into high state.");
                    marketStatus = MarketStatus.mkHigher;
                }

                break;
            }
            case mkHigher: {
                if (sigShortTerm < 1) {
                    sellSignal();

                    log.info("price got into normal state.");
                    marketStatus = MarketStatus.mkNormal;
                }

                break;
            }
            case mkLower: {
                if (sigShortTerm > 0) {
                    buySignal();

                    log.info("price got into normal state.");
                    marketStatus = MarketStatus.mkNormal;
                }
            }
        }

        // sleep until next round
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
        Reactor.getInstance(namespace).unregister(EvTicker.class, this);
    }

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvTicker.class) {
            EvTicker evTicker = (EvTicker) ev;
            JsonTicker ticker = evTicker.getData();
            lastTick = ticker;

            lastUpdate = Calendar.getInstance().getTimeInMillis() / 1000;
        }
    }

    enum MarketStatus {
        mkNormal,
        mkLower,
        mkHigher,
    }
}
