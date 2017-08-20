package com.qidianai.bitmaker.userstrategy;

import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKline;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKlineBatch;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import com.qidianai.bitmaker.notification.SMTPNotify;
import com.qidianai.bitmaker.portfolio.OKCoinAccount;
import com.qidianai.bitmaker.quote.BollingerBand;
import com.qidianai.bitmaker.quote.MA;
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
    private final double RISK_FACTOR = 0.96;

    private MarketStatus marketStatus = MarketStatus.mkHigher;
    private OKCoinAccount account = new OKCoinAccount();
    private BollingerBand bollband = new BollingerBand();
    private MACD macd = new MACD();
    private MACD macdFast = new MACD();
    private MA ma = new MA();
    private JsonTicker lastTick = new JsonTicker();
    private JsonKline lastKline1m = new JsonKline();
    private JsonKline lastKline5m = new JsonKline();
    private JsonKline lastKline15m = new JsonKline();
    private JsonKline lastKline30m = new JsonKline();
    private long lastUpdate = -1;
    private String namespace = className;
    private boolean riskProtect = false;
    private double enterPrice = 0;
    private double highest = 0;

    private boolean isReported = false;

    private void buySignal() {
        // cancel all pending orders
        if (account.getActiveOrderMap().size() > 0) {
            account.getActiveOrderMap().forEach((orderId, order) -> account.cancelEth(orderId));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double price = lastTick.sell;
        double availableCny = account.getAvailableCny();


        if (availableCny > price * 0.01) {  // Minimum trade volume
            log.info("Buy signal is triggered.");
            account.buyMarketEth(availableCny);

            //double amount = availableCny / price;
            //amount = Math.floor(amount * 100) / 100;
            //account.buyEth(price, amount);
        }
    }

    private void sellSignal() {
        // cancel all pending orders
        if (account.getActiveOrderMap().size() > 0) {
            account.getActiveOrderMap().forEach((orderId, order) -> account.cancelEth(orderId));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double price = lastTick.last;
        double availableEth = account.getAvailableEth();

        if (availableEth >= 0.01) {
            log.info("Sell signal is triggered.");
            account.sellMarketEth(availableEth);

            //double amount = Math.floor(availableEth * 100) / 100;
            //account.sellEth(price, amount);
        }
    }

    private void riskSignal() {
        log.warn("Risk signal is triggered. price:" + lastTick.last);

        SMTPNotify.send("Risk signal", "Risk signal has been triggered at price " + lastTick.last);

        //cancel all active orders
        account.getActiveOrderMap().forEach((orderId, order) -> account.cancelEth(orderId));

        double avalableEth = account.getAvailableEth();
        log.warn(String.format("Initial assets value %f", account.getInitialCny()));
        log.warn(String.format("now available eth %f, cny %f, total assets value %f", avalableEth,
                account.getAvailableCny(), account.getTotalAssetValueCny(lastTick.last)));
        // sell all ether, close all positions
        if (avalableEth >= 0.01) {
            log.warn(String.format("Risk sell %f", avalableEth));
            account.sellMarketEth(avalableEth);
        }


        riskProtect = true;
    }

    private void setLimitSell(double limitprice) {
        // cancel all pending orders
        if (account.getActiveOrderMap().size() > 0) {
            account.getActiveOrderMap().forEach((orderId, order) -> account.cancelEth(orderId));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double availableEth = account.getAvailableEth();
        if (availableEth >= 0.01) {
            log.info("Set limit sell at price " + limitprice);

            double amount = Math.floor(availableEth * 100) / 100;
            account.sellEth(limitprice, amount);
        }
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

    private void cancelAllOrders() {
        account.getActiveOrderMap().forEach((orderId, order) -> {
            log.info("cancel order " + orderId);
            account.cancelEth(orderId);
        });
    }

    private void doTrade() {
        long nowSec = Calendar.getInstance().getTimeInMillis() / 1000;
        double bband = bollband.getPercentB(lastTick.last, "15min");
        double bbandMiddle = bollband.getMiddleBand("15min");
        double bbandWidth = bollband.getBandWidth("15min");

        double macd = this.macd.getMACD("15min");
        double macdFast = this.macdFast.getMACD("15min");

        double ma5 = this.ma.getMA("15min", 5);
        double ma10 = this.ma.getMA("15min", 10);
        double percentMa = ma5 / ma10;

        boolean bBandSize = bbandWidth > 0.01;
        boolean bBandPosition = lastKline15m.closePrice > bbandMiddle && lastKline15m.openPrice > bbandMiddle;
        boolean bBandPosition2 = (lastKline15m.highPrice - bollband.getLowerBand("15min") ) /
                (bollband.getUpperBand("15min") - bollband.getLowerBand("15min")) > 0.7;
        boolean bMA = percentMa > 1;
        boolean sMA = percentMa < 0.999;

        switch (marketStatus) {
            case mkLower: {
                highest = 0;

                if (bBandSize && (bBandPosition || bBandPosition2) && bMA) {
                    buySignal();

                    marketStatus = MarketStatus.mkHigher;
                    log.info("entering high status");
                    enterPrice = lastTick.last;
                }
                break;
            }
            case mkHigher: {
                double sellFactor = 0.99;

                if (lastKline15m.closePrice > highest) {
                    highest = lastKline15m.closePrice;

                    if (enterPrice >= highest * 0.997) {
                        sellFactor = 0.997;
                    } else if (enterPrice >= highest * 0.993) {
                        sellFactor = 0.993;
                    } else {
                        sellFactor = 0.99;
                    }
                }

                boolean sHigh = lastTick.last <= highest * sellFactor;

                if (sHigh) {
                    sellSignal();
                    highest = 0;
                }

                if (sMA) {
                    sellSignal();
                    highest = 0;
                    marketStatus = MarketStatus.mkLower;
                    log.info("[sMA] entering low status");
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
        Reactor.getInstance(namespace).register(EvKline.class, this);

        bollband.setEventDomain(namespace, namespace);
        macd.setEventDomain(namespace, namespace);
        macdFast.setEventDomain(namespace, namespace);
        ma.setEventDomain(namespace, namespace);
        account.setEventDomain(namespace, namespace);

        macd.setAlpha(12, 26, 9);
        macdFast.setAlpha(6, 9, 3);

        bollband.prepare();
        macd.prepare();
        ma.prepare();
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
        macdFast.update();
        ma.update();

        // change trade day
        dayChange(17, 0);

        // risk manage
        if (account.getAvailableEth() > 0.01 && account.getTotalAssetValueCny(lastTick.last) < account.getInitialCny() * RISK_FACTOR) {
            riskSignal();
        }

        // risk protection, stop trading
        if (riskProtect) {
            return;
        }

        // cancel timeout orders
        cancelAllOrders();

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
        } else if (ev.getType() == EvKline.class) {
            EvKline evKline = (EvKline) ev;
            JsonKlineBatch batch = evKline.getData();
            batch.getKlinelist().forEach(jsonKline -> {
                switch (jsonKline.klinePeriod) {
                    case kLine1Min:
                        lastKline1m = jsonKline;
                        break;

                    case kLine15Min:
                        lastKline15m = jsonKline;
                        break;

                    case kLine30Min:
                        lastKline30m = jsonKline;
                        break;
                    case kLine5Min:
                        lastKline5m = jsonKline;
                        break;
                }

            });
        }
    }

    enum MarketStatus {
        mkNormal,
        mkLower,
        mkHigher,
    }
}
