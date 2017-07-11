package com.qidianai.bitmaker.eventsys;

/**
 * Created by fox on 2017/7/8.
 */
public interface ReactorEvent {
    void onIdle(Reactor r);
    void onPublish(Reactor r, Event ev);
}
