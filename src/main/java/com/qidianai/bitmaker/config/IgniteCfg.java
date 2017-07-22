package com.qidianai.bitmaker.config;

import java.util.Properties;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.config
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class IgniteCfg {
    public static String cfgpath = "./ignite.xml";

    public static void load(Properties prop) {
        cfgpath = prop.getProperty("storage.ignite.cfgpath", cfgpath);
    }
}
