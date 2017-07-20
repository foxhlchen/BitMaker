import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.OKCoinClient;
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
        if (args.length != 2) {
            System.out.println("usage: " + args[0] + " <config file>");
            System.exit(-1);
        }


        try {
            Properties prop = new Properties();
            InputStream configfile = new FileInputStream(args[1]);

            prop.load(configfile);

            //okcoin client config
            String apiKey = prop.getProperty("okcoin.apikey");
            String secretKey = prop.getProperty("okcoin.secretKey");
            String okcoin_url = prop.getProperty("okcoin.websocket.url", null);

            OKCoinClient marketClient = new OKCoinClient(apiKey, secretKey);
            if (okcoin_url != null) {
                marketClient.setUrl(okcoin_url);
            }

            marketClient.connect();
            marketClient.subTickerEth();
            marketClient.subTradesEth();


            Reactor reactor = new Reactor();
            reactor.start();
            reactor.join();

        } catch (IOException e) {
            System.out.println("Error while loading configuration file: " + e.getMessage());
        }

    }
}
