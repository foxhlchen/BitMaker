package com.qidianai.bitmaker.quote;

import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKline;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKlineBatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Iterator;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.quote
 * Author: fox  
 * Date: 2017/8/12
 *
 **********************************************************/
class MAQueue extends ArrayDeque<JsonKline> {
    final Object dataLock = new Object();
    /**
     * max Queue size
     */
    static int QueueSize = 200;
    //ArrayList<Double> diffArray = new ArrayList<>(N + 5);


    MAQueue() {
        super(QueueSize + 5);
    }

    public double getMA(int period) {
        return calcMA(period);
    }

    public double getMAVol(int period) {
        return calcMAVol(period);
    }


    /**
     * Add kline data to history queue
     *
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

            while (size() > QueueSize) {
                removeLast();
            }
        }
    }

    double calcMA(int period) {
        double ma = 0;
        int i = 0;

        synchronized (dataLock) {
            for (JsonKline kline : this) {
                if (++i > period)
                    break;

                ma += kline.closePrice;
            }
        }

        ma /= period;

        return ma;
    }

    double calcMAVol(int period) {
        double maVol = 0;
        int i = 0;

        synchronized (dataLock) {
            for (JsonKline kline : this) {
                if (++i > period)
                    break;

                maVol += kline.volumn;
            }
        }

        maVol /= period;

        return maVol;
    }


}


public class MA extends Quotation {
    protected Logger log = LogManager.getLogger(getClass().getName());
    MAQueue maKline1m = new MAQueue();
    MAQueue maKline5m = new MAQueue();
    MAQueue maKline15m = new MAQueue();
    MAQueue maKline30m = new MAQueue();
    MAQueue maKline1day = new MAQueue();

    private String tag;
    private String namespace;

    public double getMA(String period, int MAPeriod) {
        double macd = 0;

        switch (period) {
            case "1min":
                macd = maKline1m.getMA(MAPeriod);

                break;
            case "15min":
                macd = maKline15m.getMA(MAPeriod);

                break;
            case "30min":
                macd = maKline30m.getMA(MAPeriod);

                break;
            case "5min":
                macd = maKline5m.getMA(MAPeriod);

            case "1day":
                macd = maKline1day.getMA(MAPeriod);

                break;
        }

        return macd;
    }

    public double getMAVol(String period, int MAPeriod) {
        double macd = 0;

        switch (period) {
            case "1min":
                macd = maKline1m.getMAVol(MAPeriod);

                break;
            case "15min":
                macd = maKline15m.getMAVol(MAPeriod);

                break;
            case "30min":
                macd = maKline30m.getMAVol(MAPeriod);

                break;
            case "5min":
                macd = maKline5m.getMAVol(MAPeriod);

            case "1day":
                macd = maKline1day.getMAVol(MAPeriod);

                break;
        }

        return macd;
    }

    @Override
    public void prepare() {
        Reactor.getInstance(namespace).register(EvKline.class, this);
    }

    @Override
    public void update() {

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

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvKline.class) {
            EvKline evKline = (EvKline) ev;
            JsonKlineBatch batch = evKline.getData();
            batch.getKlinelist().forEach(jsonKline -> {
                switch (jsonKline.klinePeriod) {
                    case kLine1Min:
                        maKline1m.pushKline(jsonKline);

                        break;

                    case kLine15Min:
                        maKline15m.pushKline(jsonKline);

                        break;

                    case kLine30Min:
                        maKline30m.pushKline(jsonKline);

                        break;
                    case kLine5Min:
                        maKline5m.pushKline(jsonKline);

                        break;

                    case kLine1day:
                        maKline1day.pushKline(jsonKline);
                }

            });
        }
    }
}
