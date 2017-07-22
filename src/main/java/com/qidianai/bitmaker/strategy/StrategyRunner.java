package com.qidianai.bitmaker.strategy;

import com.qidianai.bitmaker.config.StrategyCfg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.qidianai.bitmaker.userstrategy.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.strategy
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/

public class StrategyRunner implements Runnable{
    private boolean running = false;
    private Logger log = LogManager.getLogger(getClass().getName());
    private Thread t;
    private LinkedList<StrategyThread> strategyBook = new LinkedList<StrategyThread>();

    /**
     * Initial and Run runner.
     */
    public void start() {
        log.info("Start StrategyRunner");

        ConcurrentHashMap<String, StrategyCfg.SingleStrategy> strategyMap = StrategyCfg.getStrategyMap();
        strategyMap.forEach((k, v) -> {
            if (!v.enable)
                return;

            try {
                Class<?> clazz = Class.forName("com.qidianai.bitmaker.userstrategy." + v.strategyClass);
                Constructor<?> ctor = clazz.getConstructor();
                Object obj = ctor.newInstance(new Object[]{});

                Strategy strategy = (Strategy) obj;
                strategy.prepare();
                StrategyThread strategyThread = new StrategyThread(k, strategy);
                strategyBook.add(strategyThread);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                    InstantiationException | InvocationTargetException e) {
                log.error("Load Userstrategy " + k + " failed. " + e.getMessage());
            }

        });

        running = true;
        t = new Thread(this);
        t.start();
    }

    public void stop() {
        log.info("Stop StrategyRunner");
        strategyBook.forEach(StrategyThread::stop);

        running = false;
        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("StrategyRunner.stop Join Error " + e.getMessage());
            }
        }
    }

    public void join() {
        log.info("Runner Thread Join");

        if (t != null)  {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("RunnerThread.join Join Error " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        log.info("StrategyRunner Running");
        strategyBook.forEach(StrategyThread::start);

        while (running) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.error("Runner.run " + e.getMessage());
            }
        }
    }
}
