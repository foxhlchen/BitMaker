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
    public abstract void prepare();
    public abstract void connectMarket();
    public abstract void update();
}
