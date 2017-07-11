package com.qidianai.bitmaker.eventsys;

/**
 * Created by fox on 2017/7/8.
 */
public abstract class Handler {
    private static long UNIQUE_ID = 0;
    private long uid = UNIQUE_ID++;

    public abstract void handle(Event ev);

    public long getUid() {
        return uid;
    }

}
