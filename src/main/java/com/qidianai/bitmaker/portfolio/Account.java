package com.qidianai.bitmaker.portfolio;

import com.qidianai.bitmaker.eventsys.HandlerBase;

/**********************************************************
 * BitMaker
 *
 * File: com.qidianai.bitmaker.portfolio
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public abstract class Account extends HandlerBase{
    /**
     * Initialize account
     */
    public abstract void prepare();

    /**
     * update logic
     */
    public abstract void update();

    /**
     * stop logic
     */
    public abstract void exit();
}
