import com.qidianai.bitmaker.config.IgniteCfg;
import com.qidianai.bitmaker.config.OKCoinCfg;
import com.qidianai.bitmaker.config.SMTPCfg;
import com.qidianai.bitmaker.config.StrategyCfg;
import com.qidianai.bitmaker.eventsys.Reactor;

import com.qidianai.bitmaker.notification.SMTPNotify;
import com.qidianai.bitmaker.strategy.StrategyRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;



public class App {
    private Logger log = LogManager.getLogger(getClass().getName());
    private static boolean running = false;
    private static String hostname = "Unknown";

    private static void run() {
        running = true;
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void getHostName() {
        try
        {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        }
        catch (UnknownHostException ex)
        {
            System.out.println("Hostname can not be resolved");
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: Java App <config file>");
            System.out.println("BitMaker v0.07 081601");
            System.exit(-1);
        }

        getHostName();
        System.out.println("Starting BitMaker From " + System.getProperty("user.dir") + " at " + hostname);


        try {
            Properties prop = new Properties();
            InputStream configFile = new FileInputStream(args[0]);
            prop.load(configFile);

            OKCoinCfg.load(prop);
            IgniteCfg.load(prop);
            StrategyCfg.load(prop);
            SMTPCfg.load(prop);

            //IgniteManager.startIgnite(IgniteCfg.cfgpath);
            //Reactor.startReactor();

            StrategyRunner strategyRunner = StrategyRunner.getInstance();
            strategyRunner.start();

            //SMTPNotify.send("BitMaker Start", "From " + System.getProperty("user.dir") + " at " + hostname);
            run();

            Reactor.stopAllReactor();
            strategyRunner.stop();

        } catch (IOException e) {
            System.out.println("Error while loading configuration file: " + e.getMessage());
        }

    }
}
