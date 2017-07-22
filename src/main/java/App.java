import com.qidianai.bitmaker.config.IgniteCfg;
import com.qidianai.bitmaker.config.OKCoinCfg;
import com.qidianai.bitmaker.config.StrategyCfg;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.OKCoinClient;
import com.qidianai.bitmaker.storage.IgniteManager;
import com.qidianai.bitmaker.strategy.StrategyRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class App {
    private Logger log = LogManager.getLogger(getClass().getName());


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("need argument <config file>");
            System.exit(-1);
        }


        try {
            Properties prop = new Properties();
            InputStream configFile = new FileInputStream(args[0]);
            prop.load(configFile);

            OKCoinCfg.load(prop);
            IgniteCfg.load(prop);
            StrategyCfg.load(prop);

            //IgniteManager.startIgnite(IgniteCfg.cfgpath);
            Reactor reactor = new Reactor();
            reactor.start();

            StrategyRunner strategyRunner = new StrategyRunner();
            strategyRunner.start();

            strategyRunner.join();
            reactor.join();

        } catch (IOException e) {
            System.out.println("Error while loading configuration file: " + e.getMessage());
        }

    }
}
