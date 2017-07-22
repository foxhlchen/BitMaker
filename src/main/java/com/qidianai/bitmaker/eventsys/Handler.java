package com.qidianai.bitmaker.eventsys;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.eventsys
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/

/**
 * Event Handler Interface
 */
public interface Handler {
    void handle(Event ev);
    long getUid();
}
