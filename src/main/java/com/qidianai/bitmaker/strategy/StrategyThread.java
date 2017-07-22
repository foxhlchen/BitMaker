package com.qidianai.bitmaker.strategy;

import com.qidianai.bitmaker.eventsys.Event;
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
public class StrategyThread extends HandlerBase implements Runnable {
    private boolean running = false;
    private Logger log = LogManager.getLogger(getClass().getName());
    private Thread t;
    private String strategyName;
    private Strategy strategy;


    StrategyThread(String strategyName, Strategy strategy) {
        this.strategyName = strategyName;
        this.strategy = strategy;
    }

    public void start() {
        log.info("Start Strategy Thread " + this.strategyName);

        running = true;
        t = new Thread(this);
        t.start();
    }

    public void stop() {
        log.info("Stop Strategy Thread" + this.strategyName);

        strategy.stop();

        running = false;
        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("Strategy Thread " + this.strategyName + " Stop Join Error " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                strategy.run();
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.error("StrategyThread " + strategyName + " failed " + e.getMessage());
            }
        }
    }

    @Override
    public void handle(Event ev) {

    }
}
