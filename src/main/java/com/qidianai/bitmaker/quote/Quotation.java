package com.qidianai.bitmaker.quote;

import com.qidianai.bitmaker.eventsys.HandlerBase;

/**
 * Created by fox on 2017/7/8.
 */
public abstract class Quotation extends HandlerBase {
    /**
     * register quotation event
     */
    public abstract void prepare();

    /**
     * update quotation indicators
     */
    public abstract void update();

    /**
     * stop quotation services
     */
    public abstract void stop();

    /**
     * set Event Domain
     */
    public abstract void setEventDomain(String tag, String namespace);

}
