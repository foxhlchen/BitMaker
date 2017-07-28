package com.qidianai.bitmaker.strategy;

import com.qidianai.bitmaker.config.StrategyCfg;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.HandlerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.HashMap;

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
    private int timeout;
    private long lastTick = -1;
    private StrategyCfg.SingleStrategy strategyCfg;

    StrategyThread(String strategyName, Strategy strategy) {
        this.strategyName = strategyName;
        this.strategy = strategy;
    }

    public StrategyCfg.SingleStrategy getStrategyCfg() {
        return strategyCfg;
    }

    public void setStrategyCfg(StrategyCfg.SingleStrategy strategyCfg) {
        this.strategyCfg = strategyCfg;
    }

    public long getLastTick() {
        return lastTick;
    }

    public void setLastTick(long lastTick) {
        this.lastTick = lastTick;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void start() {
        log.info("Start Strategy Thread " + this.strategyName);

        running = true;
        t = new Thread(this);
        t.setName("StrategyThread-" + this.strategyName);
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

    public void forceStop() {
        log.info("Force Stop Strategy Thread" + this.strategyName);

        strategy.stop();
        running = false;

        if (t != null) {
            try {
                //t.interrupt();
                t.stop();
                t.join();
            } catch (InterruptedException e) {
                log.error("Strategy Thread " + this.strategyName + " Stop Join Error " + e.getMessage());
            }
        }
    }

    public boolean isAlive() {
        long nowSec = Calendar.getInstance().getTimeInMillis() / 1000;
        if (lastTick != -1) {
            if (nowSec - lastTick > timeout) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void run() {
        log.info("Strategy " + this.strategyName + " is running");

        while (running) {
            try {
                lastTick = Calendar.getInstance().getTimeInMillis() / 1000;
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
