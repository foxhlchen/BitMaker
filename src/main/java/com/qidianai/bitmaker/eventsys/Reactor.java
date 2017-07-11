package com.qidianai.bitmaker.eventsys;

/**
 * Created by fox on 2017/7/8.
 */


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


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


    private ConcurrentHashMap<Class, ConcurrentHashMap<Long, Handler>> regbook = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Event<?>> evQ = new ConcurrentLinkedQueue<>();


    /**
     * set reactor event handler
     * @param rev
     */
    public void setRev(ReactorEvent rev) {
        this.rev = rev;
    }


    /**
     * To subscribe event
     *
     * @param evtype
     * @param hdr
     */
    public void register(Class evtype, Handler hdr) {
        log.info(String.format("Subscribe %s by %s uid %d", evtype.getName(), hdr.getClass().getName(), hdr.getUid()));

        if (! regbook.containsKey(evtype)) {
            regbook.put(evtype, new ConcurrentHashMap<>());
        }

        ConcurrentHashMap<Long, Handler> handlers = regbook.get(evtype);
        handlers.put(hdr.getUid(), hdr);
    }


    /**
     * To unsubscribe event
     * @param evtype
     * @param hdr
     */
    public void unregister(Class evtype, Handler hdr) {
        log.info(String.format("Unsubscribe %s by %s uid %d", evtype.getName(), hdr.getClass().getName(), hdr.getUid()));

        if (! regbook.containsKey(evtype)) {
            return;
        }

        ConcurrentHashMap<Long, Handler> handlers = regbook.get(evtype);
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
     * @param ev
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

        ConcurrentHashMap<Long, Handler> handlers = regbook.get(evtype);
        handlers.forEach((k, v) -> {
            v.handle(ev);
        });
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
    public void start() {
        log.info("Start Reactor");

        running = true;
        t = new Thread(this);
        t.start();
    }

    /**
     * Stop running reactor thread
     */
    public void stop() {
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
    public void join() {
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
        log.info("Run Reactor");

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
                log.error("Reactor.run encounter problem " + e.getMessage());
            }
        }
    }

}

