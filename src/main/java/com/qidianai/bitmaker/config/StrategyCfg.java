package com.qidianai.bitmaker.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.config
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class StrategyCfg {

    private static ConcurrentHashMap<String, SingleStrategy> strategyMap = new ConcurrentHashMap<String, SingleStrategy>() {};

    public static class SingleStrategy {
        public String strategyName;
        public String strategyClass;
        public boolean enable;
        public int timeout;

        public String args;

        public HashMap<String, String> argv;

        public void load(Properties prop) {
            String prefix = "strategy." + strategyName + ".args.";

            String argsListString = args;
            if (args == null) {
                return;
            }
            argv = new HashMap<>();
            String[] args = argsListString.split(",");
            for (String arg : args) {
                String argName = arg.trim();
                if (argName.isEmpty())
                    continue;

                String argValue = prop.getProperty(prefix + argName, null);
                argv.put(argName, argValue);
            }
        }
    }


    private static void loadSingle(Properties prop, String strategyName) {
        String prefix = "strategy." + strategyName + ".";
        SingleStrategy singleStrategy = new SingleStrategy();
        singleStrategy.strategyName = strategyName;
        singleStrategy.strategyClass = prop.getProperty(prefix + "class", null);
        singleStrategy.enable = !prop.getProperty(prefix + "enable", null).equals("0");
        singleStrategy.timeout = Integer.parseInt(prop.getProperty(prefix + "timeout", "30"));

        singleStrategy.args = prop.getProperty(prefix + "args", null);
        singleStrategy.load(prop);

        strategyMap.put(strategyName, singleStrategy);
    }

    public static void load(Properties prop) throws IOException {
        String strategyListString = prop.getProperty("strategy.list", null);
        if (strategyListString == null) {
            throw new IOException("no strategies!");
        }

        String[] strategies = strategyListString.split(",");
        for (String entry : strategies) {
            String strategyName = entry.trim();
            if (strategyName.isEmpty())
                continue;
            loadSingle(prop, strategyName);
        }
    }

    public static ConcurrentHashMap<String, SingleStrategy> getStrategyMap() {
        return strategyMap;
    }
}
