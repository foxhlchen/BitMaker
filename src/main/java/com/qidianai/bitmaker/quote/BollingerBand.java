package com.qidianai.bitmaker.quote;

import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.quote
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class BollingerBand extends Quotation {
    protected Logger log = LogManager.getLogger(getClass().getName());

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvTicker.class) {
            EvTicker evTicker = (EvTicker) ev;
            JsonTicker ticker = evTicker.getData();
            log.info("lastPrice: " + ticker.last);
        }
    }

    @Override
    public void prepare() {
        Reactor.getInstance().register(EvTicker.class, this);
    }

    @Override
    public void update() {

    }
}
