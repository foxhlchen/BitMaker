package com.qidianai.bitmaker.eventsys;

/**
 * Created by fox on 2017/7/8.
 */
public abstract class HandlerBase implements Handler {
    private static long UNIQUE_ID = 0;
    private long uid = UNIQUE_ID++;

    @Override
    public abstract void handle(Event ev);

    @Override
    public long getUid() {
        return uid;
    }
}
