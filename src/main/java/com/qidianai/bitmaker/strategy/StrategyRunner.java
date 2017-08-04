package com.qidianai.bitmaker.strategy;

import com.qidianai.bitmaker.config.StrategyCfg;
import com.qidianai.bitmaker.notification.SMTPNotify;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;


/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.strategy
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/

public final class StrategyRunner implements Runnable{
    static StrategyRunner instance_;

    private boolean running = false;
    private Logger log = LogManager.getLogger(getClass().getName());
    private Thread t;
    private LinkedList<StrategyThread> strategyBook = new LinkedList<>();


    /**
     * singleton. set private prevent instance by new
     */
    private StrategyRunner() { }


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
                Object obj = ctor.newInstance();

                Strategy strategy = (Strategy) obj;
                log.info("Prepare strategy " + k);
                strategy.prepare(v.argv);  // prepare strategy here. so runner can catch exceptions

                StrategyThread strategyThread = new StrategyThread(k, strategy);
                strategyThread.setTimeout(v.timeout);
                strategyThread.setStrategyCfg(v);
                strategyBook.add(strategyThread);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Load Userstrategy " + k + " failed. " + e);
            }

        });

        running = true;
        t = new Thread(this);
        t.setName("StrategyRunner");
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
        log.info("StrategyRunner is running");
        strategyBook.forEach(StrategyThread::start);

        while (running) {
            try {
                PrintWriter statusFile = new PrintWriter("strategy.status", "UTF-8");
                long nowSec = Calendar.getInstance().getTimeInMillis() / 1000;
                statusFile.println(String.format("%d", nowSec));

                ListIterator<StrategyThread> iter = strategyBook.listIterator();
                while (iter.hasNext()) {
                    StrategyThread t = iter.next();
                    if (!t.isAlive()) {
                        statusFile.println(String.format("%s %d Dead", t.getStrategyCfg().strategyName, t.getLastTick()));
                        log.error("strategy thread timeout detected  " + t.getStrategyCfg().strategyName);
                        t.forceStop();
                        iter.remove();

                        StrategyCfg.SingleStrategy v = t.getStrategyCfg();
                        String k = v.strategyName;
                        try {
                            Class<?> clazz = Class.forName("com.qidianai.bitmaker.userstrategy." + v.strategyClass);
                            Constructor<?> ctor = clazz.getConstructor();
                            Object obj = ctor.newInstance();

                            Strategy strategy = (Strategy) obj;
                            log.info("Prepare strategy " + k);
                            strategy.prepare(v.argv);  // prepare strategy here. so runner can catch exceptions

                            StrategyThread strategyThread = new StrategyThread(k, strategy);
                            strategyThread.setTimeout(v.timeout);
                            strategyThread.setStrategyCfg(v);
                            iter.add(strategyThread);

                            strategyThread.start();
                        } catch (Exception e) {
                            log.error("Restart Userstrategy " + k + " failed. " + e.getMessage());
                            SMTPNotify.send("Strategy " + v.strategyName + " Restart error",
                                    "Strategy " + v.strategyName + " restart error");
                            e.printStackTrace();
                        }
                    }

                    statusFile.println(String.format("%s %d Alive", t.getStrategyCfg().strategyName, t.getLastTick()));
                }

                statusFile.close();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Runner.run " + e.getMessage());
                e.printStackTrace();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                log.error("Write status file error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Singleton
     * @return Runner instance
     */
    public static StrategyRunner getSingleton() {
        if (instance_ == null) {
            instance_ = new StrategyRunner();
        }

        return instance_;
    }


    /**
     * Singleton
     * @return Runner instance
     */
    public static StrategyRunner getInstance() {
        return getSingleton();
    }
}
