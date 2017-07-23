package com.qidianai.bitmaker.quote;

import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.event.EvTest;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKline;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKlineBatch;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.quote
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
class KlineQueue extends ArrayDeque<JsonKline> {
    KlineQueue(int numElements) {
        super(numElements);
    }

    public void pushKline(JsonKline jsonKline) {
        JsonKline first = super.getFirst();
        if (jsonKline.getDateInt() < first.getDateInt()) {
            return;
        }

        if (jsonKline.getDateInt() == first.getDateInt()) {
            first.update(jsonKline);
            return;
        }



        super.push(jsonKline);
    }
}

public class BollingerBand extends Quotation {
    protected Logger log = LogManager.getLogger(getClass().getName());
    protected KlineQueue histKline15m = new KlineQueue(25);
    protected KlineQueue histKline30m = new KlineQueue(25);
    protected KlineQueue histKline1m = new KlineQueue(25);

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
                        log.info(jsonKline.klinePeriod);
                        log.info(jsonKline.timeStamp_ms);
                        log.info(jsonKline.easyDate);
                        break;

                    case kLine15Min:
                        break;

                    case kLine30Min:
                        break;
                }

            });
        }
    }

    @Override
    public void prepare() {
        Reactor.getInstance().register(EvTicker.class, this);
        Reactor.getInstance().register(EvKline.class, this);
    }

    @Override
    public void update() {

    }
}
