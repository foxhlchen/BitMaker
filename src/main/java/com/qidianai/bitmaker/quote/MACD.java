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
 * Date: 2017/8/1
 *
 **********************************************************/

class HistQueue extends ArrayDeque<JsonKline> {
    /**
     * short period alpha =2/(N+1) N=12
     */
    static final double alpha_short = 2.0 / 13;

    /**
     * long period alpha =2/(N+1) N=20
     */
    static final double alpha_long = 2.0 / 27;

    /**
     * DEA alpha =2/(N+1) N=9
     */
    static final double alpha_dea = 2.0 / 10;

    /**
     * max Queue size
     */
    static final int QueueSize = 200;

    final Object dataLock = new Object();
    //ArrayList<Double> diffArray = new ArrayList<>(N + 5);
    /**
     * macd
     */
    double macd = Double.MIN_NORMAL;

    HistQueue() {
        super(QueueSize + 5);
    }

    public double getMacd() {
        return macd;
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

    double calcMACD(double alphaS, double alphaL, double alphaDea) {
        double emaS = 0;
        double emaL = 0;
        double diff = 0;
        double dea = 0;
        double macd = 0;

        synchronized (dataLock) {
            Iterator<JsonKline> it = this.descendingIterator();
            while (it.hasNext()) {
                JsonKline kline = it.next();
                double price = kline.closePrice;

                //first element
                if (emaS == 0) {
                    emaS = price;
                    emaL = price;

                    continue;
                }

                emaS += (price - emaS) * alpha_short;
                emaL += (price - emaL) * alpha_long;

                diff = emaS - emaL;
                dea = dea + alphaDea * (diff - dea);
                macd = 2 * (diff - dea);
            }
        }

//        System.out.println("===============");
//        System.out.println(this.getFirst().easyDate);
//        System.out.println(this.size());
//        System.out.println(emaS);
//        System.out.println(emaL);
//        System.out.println(diff);
//        System.out.println(dea);
//        System.out.println(macd);

        return macd;
    }

    void updateMACD() {
        if (size() < QueueSize) {
            return;
        }
        macd = calcMACD(alpha_short, alpha_long, alpha_dea);
    }

}


public class MACD extends Quotation {
    protected Logger log = LogManager.getLogger(getClass().getName());
    HistQueue histKline15m = new HistQueue();
    HistQueue histKline30m = new HistQueue();
    HistQueue histKline1m = new HistQueue();
    HistQueue histKline5m = new HistQueue();
    private String tag;
    private String namespace;

    public double getMACD(String period) {
        double macd = 0;

        switch (period) {
            case "1min":
                macd = histKline1m.getMacd();

                break;
            case "15min":
                macd = histKline15m.getMacd();

                break;
            case "30min":
                macd = histKline30m.getMacd();

                break;
            case "5min":
                macd = histKline5m.getMacd();

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
        histKline1m.updateMACD();
        histKline5m.updateMACD();
        histKline15m.updateMACD();
        histKline30m.updateMACD();
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
}
