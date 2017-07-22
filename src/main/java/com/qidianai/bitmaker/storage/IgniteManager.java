package com.qidianai.bitmaker.storage;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.storage
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class IgniteManager {
    private static IgniteManager instance_;
    private static Ignite ignite;


    public static Ignite startIgnite(String cfgpath) {
        ignite = Ignition.start(cfgpath);
        return ignite;
    }

    public static Ignite getIgnite() {
        return ignite;
    }
}
