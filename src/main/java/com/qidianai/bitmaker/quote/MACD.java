package com.qidianai.bitmaker.quote;

import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;

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
     * short period alpha =2/(N+1)
     */
    static final double alpha_fast = 2.0 / 13;

    /**
     * long period alpha =2/(N+1)
     */
    static final double alpha_slow = 2.0 / 21;

    /**
     * max Q size
     */
    static final int N = 200;

    /**
     * Add kline data to history queue
     *
     */
    void pushKline(JsonKline jsonKline) {
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

    void updateMACD() {

    }

}


public class MACD extends Quotation {
    String tag;
    String namespace;

    protected Logger log = LogManager.getLogger(getClass().getName());

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

    }
}
