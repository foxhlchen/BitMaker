package com.qidianai.bitmaker.eventsys;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

///
// Created by fox on 2017/7/8.
//


/**
 * Reactor
 *
 * Event Dispatcher
 */
public final class Reactor implements Runnable {
    private Thread t;
    private boolean running = false;
    private Logger log = LogManager.getLogger(getClass().getName());
    private ReactorEvent rev;
    static private Reactor instance_;
    static final Object instanceLock = new Object();
    static HashMap<String, Reactor> reactorMap = new HashMap<>();


    private ConcurrentHashMap<Class, ConcurrentHashMap<Long, HandlerBase>> regbook = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Event<?>> evQ = new ConcurrentLinkedQueue<>();

    /***
     * singleton. set private prevent instance by new
     */
    private Reactor() { }

    /**
     * set reactor event handler
     * @param rev handler
     */
    public void setRev(ReactorEvent rev) {
        this.rev = rev;
    }


    /**
     * To subscribe event
     *
     * @param evtype event type
     * @param hdr handler
     */
    public void register(Class evtype, HandlerBase hdr) {
        log.info(String.format("Subscribe %s by %s uid %d", evtype.getName(), hdr.getClass().getName(), hdr.getUid()));

        if (! regbook.containsKey(evtype)) {
            regbook.put(evtype, new ConcurrentHashMap<>());
        }

        ConcurrentHashMap<Long, HandlerBase> handlers = regbook.get(evtype);
        handlers.put(hdr.getUid(), hdr);
    }


    /**
     * To unsubscribe event
     * @param evtype event type
     * @param hdr handler
     */
    public void unregister(Class evtype, HandlerBase hdr) {
        log.info(String.format("Unsubscribe %s by %s uid %d", evtype.getName(), hdr.getClass().getName(), hdr.getUid()));

        if (! regbook.containsKey(evtype)) {
            return;
        }

        ConcurrentHashMap<Long, HandlerBase> handlers = regbook.get(evtype);
        handlers.remove(hdr.getUid());
    }

    /**
     * Publish Event to all registered handlers immediately in caller's thread.
     *
     * @param ev Event
     */
    public void publishImmediately(Event<?> ev) {
        log.debug("Immediately publish event %s, %s" , ev.getType().getName(), ev.getSender() != null ?
                ev.getSender().getClass().getName() : "");

        _publish(ev);

    }


    /**
     * Store event in EvQ, then publish event from Reactor thread
     * @param ev event
     */
    public void publish(Event<?> ev) {
        evQ.add(ev);
    }


    private void _publish(Event ev) {
        if (rev != null)
            rev.onPublish(this, ev);

        Class evtype = ev.getType();

        if (! regbook.containsKey(evtype))
            return;

        ConcurrentHashMap<Long, HandlerBase> handlers = regbook.get(evtype);
        handlers.forEach((k, v) -> v.handle(ev));
    }


    /**
     * Reset reactor
     */
    public void reset() {
        log.info("reset Reactor");

        running = false;
        regbook.clear();
    }


    /**
     * Create Another thread and Run Reactor.
     */
    private void start() {
        log.info("Start Reactor");

        running = true;
        t = new Thread(this);
        t.start();
    }

    /**
     * Create Another thread and Run Reactor.
     */
    private void start(String namespace) {
        log.info("Start Reactor");

        running = true;
        t = new Thread(this);
        t.setName(namespace);
        t.start();
    }

    /**
     * Stop running reactor thread
     */
    private void stop() {
        log.info("Stop Reactor");

        running = false;
        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("Reactor.stop Join Error " + e.getMessage());
            }
        }
    }


    /**
     * Wait for reactor to die.
     */
    private void join() {
        log.info("Join Reactor Thread");

        if (t != null)  {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("Reactor.join Join Error " + e.getMessage());
            }
        }
    }


    @Override
    public void run() {
        log.info("Reactor is running");

        while (running) {
            try {
                Event ev = evQ.poll();
                if (ev == null) {
                    if (rev != null)
                        rev.onIdle(this);

                    Thread.sleep(10);
                    continue;
                }

                _publish(ev);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Reactor.run encounter problem " + e.getMessage());

            }
        }
    }

    /**
     * Singleton
     * @return Reactor instance
     */
    public static Reactor getSingleton() {
        synchronized (instanceLock) {
            if (instance_ == null) {
                instance_ = new Reactor();
                instance_.start();
            }

            return instance_;
        }
    }


    /**
     * Singleton
     * @return Reactor instance
     */
    public static Reactor getInstance() {
            return getSingleton();
    }

    /**
     * Singleton
     * @return Reactor instance
     */
    public static Reactor getSingleton(String namespace) {
        if (namespace == null)
           return getSingleton();

        synchronized (instanceLock) {
            if (! reactorMap.containsKey(namespace)) {
                Reactor r = new Reactor();
                r.start(namespace);
                reactorMap.put(namespace, r);
            }

            return reactorMap.get(namespace);
        }
    }


    /**
     * Singleton
     * @return Reactor instance
     */
    public static Reactor getInstance(String namespace) {
        return getSingleton(namespace);
    }


    public static void stopReactor() {
        synchronized (instanceLock) {
            instance_.stop();
            instance_ = null;
        }
    }

    public static void stopReactor(String namespace) {
        synchronized (instanceLock) {
            reactorMap.get(namespace).stop();
            reactorMap.remove(namespace);
        }
    }

    public static void stopAllReactor() {
        stopReactor();

        synchronized (instanceLock) {
            reactorMap.forEach((k, v) -> v.stop());

            reactorMap.clear();
        }
    }

    public static void startReactor() {
        getInstance();
    }

    public static void startReactor(String namespace) {
        getInstance(namespace);
    }
}

