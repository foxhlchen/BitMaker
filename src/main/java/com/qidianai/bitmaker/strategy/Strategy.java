package com.qidianai.bitmaker.strategy;

import com.qidianai.bitmaker.eventsys.HandlerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.strategy
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public abstract class Strategy extends HandlerBase {
    String strategyName;
    protected Logger log = LogManager.getLogger(getClass().getName());
    protected String className = getClass().getName();
    /**
     * Initialize Strategy
     */
    public abstract void prepare();

    /**
     * Strategy Logic
     */
    public abstract void run();

    /**
     * Stop Strategy
     */
    public abstract void stop();
}
