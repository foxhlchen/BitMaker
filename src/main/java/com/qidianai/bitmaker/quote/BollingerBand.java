package com.qidianai.bitmaker.quote;

import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKline;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKlineBatch;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.quote
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
class BandQueue extends ArrayDeque<JsonKline> {
    /**
     * band period
     */
    static final int N = 20;

    /**
     * band magnification, K times std over average
     */
    static final int K = 2;
    final Object dataLock = new Object();
    double upperBand = Double.MAX_VALUE;
    double lowerBand = Double.MIN_NORMAL;
    ArrayList<Double> diffArray = new ArrayList<>(N + 5);

    BandQueue() {
        super(N + 5);
    }

    public double getUpperBand() {
        return upperBand;
    }

    public void setUpperBand(double upperBand) {
        this.upperBand = upperBand;
    }

    public double getLowerBand() {
        return lowerBand;
    }

    public void setLowerBand(double lowerBand) {
        this.lowerBand = lowerBand;
    }

    /**
     * Add kline data to history queue
     */
    void pushKline(JsonKline jsonKline) {
        synchronized (dataLock) {
            JsonKline first = peekFirst();

            if (first != null) {
                // abandon old kline
                if (jsonKline.getDateInt() < first.getDateInt()) {
                    return;
                }

                // update latest kline
                if (jsonKline.getDateInt() == first.getDateInt()) {
                    first.update(jsonKline);
                    return;
                }
            }

            push(jsonKline);

            while (size() > N) {
                removeLast();
            }
        }
    }

    /**
     * Update bollinger band
     */
    void updateBand() {
        if (size() != N) {
            return; //data incomplete
        }

        double average = getAverage();
        double std = calculateStd();

        upperBand = BollingerBand.round(average + K * std);
        lowerBand = BollingerBand.round(average - K * std);
    }

    /**
     * get bollinger band average
     *
     * @return average price
     */
    public double getAverage() {
        // simple moving average here
        // use last N period price mean
        return calculateMean();
    }

    /**
     * calculate mean average
     *
     * @return mean average
     */
    private double calculateMean() {
        double sum = 0;
        synchronized (dataLock) {
            for (JsonKline kline : this) {
                double val = kline.closePrice; //(kline.highPrice - kline.lowPrice) / 2 + kline.lowPrice;

                sum += val;
            }
        }

        return sum / size();
    }


    /**
     * calculate variance
     *
     * @return variance
     */
    private double calculateVariance() {
        diffArray.clear();

        double mean = calculateMean();
        synchronized (dataLock) {
            for (JsonKline kline : this) {
                double val = kline.closePrice; //(kline.highPrice - kline.lowPrice) / 2 + kline.lowPrice;

                diffArray.add(Math.pow(val - mean, 2));
            }
        }

        double variance = 0;
        for (double v : diffArray) {
            variance += v;
        }

        variance /= size();

        return variance;
    }

    /**
     * calculate standard deviation
     *
     * @return standard deviation
     */
    private double calculateStd() {
        double variance = calculateVariance();
        return Math.sqrt(variance);
    }
}

public class BollingerBand extends Quotation {
    /**
     * default round decimal places
     */
    public static final int RoundPlaces = 3;
    protected Logger log = LogManager.getLogger(getClass().getName());
    protected BandQueue histKline15m = new BandQueue();
    protected BandQueue histKline30m = new BandQueue();
    protected BandQueue histKline1m = new BandQueue();
    protected BandQueue histKline5m = new BandQueue();
    String tag;
    String namespace;

    public static double round(double num) {
        return round(num, RoundPlaces);
    }

    public static double round(double num, int places) {
        double k = Math.pow(10, places);
        num *= k;
        num = Math.round(num);
        num /= k;

        return num;
    }

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvTicker.class) {
            EvTicker evTicker = (EvTicker) ev;
            JsonTicker ticker = evTicker.getData();
            //log.info("lastPrice: " + ticker.last);
        } else if (ev.getType() == EvKline.class) {
            EvKline evKline = (EvKline) ev;
            JsonKlineBatch batch = evKline.getData();
            batch.getKlinelist().forEach(jsonKline -> {
                switch (jsonKline.klinePeriod) {
                    case kLine1Min:
                        histKline1m.pushKline(jsonKline);

                        break;

                    case kLine15Min:
                        histKline15m.pushKline(jsonKline);

                        break;

                    case kLine30Min:
                        histKline30m.pushKline(jsonKline);

                        break;
                    case kLine5Min:
                        histKline5m.pushKline(jsonKline);

                        break;
                }

            });
        }
    }

    public double getUpperBand(String period) {
        double price = Double.MAX_VALUE;

        switch (period) {
            case "1min":
                price = histKline1m.getUpperBand();

                break;
            case "15min":
                price = histKline15m.getUpperBand();

                break;
            case "30min":
                price = histKline30m.getUpperBand();

                break;
            case "5min":
                price = histKline5m.getUpperBand();

                break;
        }

        return price;
    }

    public double getLowerBand(String period) {
        double price = Double.MIN_NORMAL;

        switch (period) {
            case "1min":
                price = histKline1m.getLowerBand();

                break;
            case "15min":
                price = histKline15m.getLowerBand();

                break;
            case "30min":
                price = histKline30m.getLowerBand();

                break;
            case "5min":
                price = histKline5m.getLowerBand();

                break;
        }

        return price;
    }

    public double getMiddleBand(String period) {
        double price = Double.MIN_NORMAL;

        switch (period) {
            case "1min":
                price = histKline1m.getAverage();

                break;
            case "15min":
                price = histKline15m.getAverage();

                break;
            case "30min":
                price = histKline30m.getAverage();

                break;

            case "5min":
                price = histKline5m.getAverage();

                break;
        }

        return price;
    }

    public double getPercentB(double lastPrice, String period) {
        double upperBand = getUpperBand(period);
        double lowerBand = getLowerBand(period);

        return round((lastPrice - lowerBand) / (upperBand - lowerBand));
    }

    public double getBandWidth(String period) {
        double upperBand = getUpperBand(period);
        double lowerBand = getLowerBand(period);
        double middleBand = getMiddleBand(period);

        return round((upperBand - lowerBand) / middleBand);
    }

    @Override
    public void prepare() {
        //Reactor.getInstance(namespace).register(EvTicker.class, this);
        Reactor.getInstance(namespace).register(EvKline.class, this);
    }

    @Override
    public void update() {
        histKline1m.updateBand();
        histKline15m.updateBand();
        histKline30m.updateBand();
        histKline5m.updateBand();
    }

    @Override
    public void stop() {
        Reactor.getInstance(namespace).unregister(EvKline.class, this);
    }

    @Override
    public void setEventDomain(String tag, String namespace) {
        this.tag = tag;
        this.namespace = namespace;
    }
}
