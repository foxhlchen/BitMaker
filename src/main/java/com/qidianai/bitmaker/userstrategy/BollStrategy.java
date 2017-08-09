package com.qidianai.bitmaker.userstrategy;

import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import com.qidianai.bitmaker.notification.SMTPNotify;
import com.qidianai.bitmaker.portfolio.OKCoinAccount;
import com.qidianai.bitmaker.quote.BollingerBand;
import com.qidianai.bitmaker.quote.MACD;
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
    private final double RISK_FACTOR = 0.99;

    private MarketStatus marketStatus = MarketStatus.mkNormal;
    private OKCoinAccount account = new OKCoinAccount();
    private BollingerBand bollband = new BollingerBand();
    private MACD macd = new MACD();
    private JsonTicker lastTick = new JsonTicker();
    private long lastUpdate = -1;
    private String namespace = className;
    private boolean riskProtect = false;
    private long enterSec = 0;

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

        //prohibit riskProtection
        //riskProtect = true;
    }

    public void sendAccountReport() {
        String reportContent = String.format("Account Total Assets: %.3f, Available Cny: %.3f, Available Eth: %.3f, " +
                        "Last Price: %.3f",
                account.getTotalAssetValueCny(lastTick.last), account.getAvailableCny(), account.getAvailableEth(),
                lastTick.last);

        SMTPNotify.send("AccountReport", reportContent);
    }

    private void dayChange(int occurHour, int occurMin) {
        int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int nowMin = Calendar.getInstance().get(Calendar.MINUTE);

        if (nowHour == occurHour && nowMin == occurMin) {
            ////////change day

            // close risk protection
            riskProtect = false;

            // send account report
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
            if (nowSec - lastUpdate > 180) {
                log.info("timeout, resubscribe market quotation");

                account.connectMarket();
                account.subscribeMarketQuotation();
                account.queryUserInfo();

                lastUpdate = nowSec;

                return true;
            }
        }

        return false;
    }

    private void doTrade() {
        long nowSec = Calendar.getInstance().getTimeInMillis() / 1000;
        long elapsed = nowSec - enterSec;
        double sigShortTerm = bollband.getPercentB(lastTick.last, "1min");
        double sigLongTerm = bollband.getPercentB(lastTick.last, "1min");

        double macd15 = macd.getMACD("1min");

        switch (marketStatus) {
            case mkNormal: {
                if (sigLongTerm < 0 && sigShortTerm < 0) {
                    log.info("price get into low state.");
                    marketStatus = MarketStatus.mkLower;
                    enterSec = nowSec;
                }

                if (sigShortTerm > 0.6 && sigLongTerm > 0.6) {
                    log.info("price get into high state.");
                    marketStatus = MarketStatus.mkHigher;
                    enterSec = nowSec;
                }

                break;
            }
            case mkHigher: {
                if (sigShortTerm > 1) {
                    enterSec = nowSec;
                }

                // sell signal
                if (sigShortTerm < 1.1 && macd15 < -0.7) {
                    sellSignal();

                    log.info("price get into normal state.");
                    marketStatus = MarketStatus.mkNormal;
                    enterSec = nowSec;
                }


                if (sigShortTerm < 0.6 && macd15 < -0.5) {
                    sellSignal();

                    log.info("price get into normal state.");
                    marketStatus = MarketStatus.mkNormal;
                    enterSec = nowSec;
                }
                // dismiss higher state
//                if (elapsed > 5400) {
//                    log.info("higher state dismiss.");
//                    marketStatus = MarketStatus.mkNormal;
//                    enterSec = nowSec;
//                }

                break;
            }
            case mkLower: {
                if (sigShortTerm < 1) {
                    enterSec = nowSec;
                }

                // buy signal
                if (sigShortTerm > -0.1 && sigShortTerm < 0.5 && macd15 > 0.7) {
                    buySignal();

                    log.info("price get into normal state.");
                    marketStatus = MarketStatus.mkNormal;
                    enterSec = nowSec;
                }

                // dismiss lower state
                if (elapsed > 1200 || sigShortTerm > 0.65) {
                    log.info("low state dismiss.");
                    marketStatus = MarketStatus.mkNormal;
                    enterSec = nowSec;
                }


                break;
            }
        }
    }

    @Override
    public void prepare(HashMap<String, String> args) {
        if (args != null && args.containsKey("namespace")) {
            this.namespace = args.get("namespace");
        }

        Reactor.getInstance(namespace).register(EvTicker.class, this);

        bollband.setEventDomain(namespace, namespace);
        macd.setEventDomain(namespace, namespace);
        account.setEventDomain(namespace, namespace);

        macd.setAlpha(6, 10, 3);

        bollband.prepare();
        macd.prepare();
        account.prepare();

        account.subscribeMarketQuotation();
    }

    @Override
    public void run() {
        // sleep until next round
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // reconnect
        if (checkReconnectMarket()) {
            return;
        }

        // update market data
        bollband.update();
        account.update();
        macd.update();

        // change trade day
        dayChange(17, 0);

        // risk manage
        if (account.getTotalAssetValueCny(lastTick.last) < account.getInitialCny() * RISK_FACTOR) {
            riskSignal();
        }

        // risk protection, stop trading
        if (riskProtect) {
            return;
        }

        // trade signal
        doTrade();
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
