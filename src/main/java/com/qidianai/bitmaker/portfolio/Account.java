package com.qidianai.bitmaker.portfolio;

/**********************************************************
 * BitMaker
 *
 * File: com.qidianai.bitmaker.portfolio
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public abstract class Account {
    public abstract void prepare();
    public abstract void connectMarket();
    public abstract void update();
}
