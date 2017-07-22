package com.qidianai.bitmaker.portfolio;

import com.qidianai.bitmaker.config.OKCoinCfg;
import com.qidianai.bitmaker.marketclient.okcoin.OKCoinClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.portfolio
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/

public class OKCoinAccount extends Account {
    private Logger log = LogManager.getLogger(getClass().getName());
    private OKCoinClient okCoinClient;
    private String apiKey = OKCoinCfg.apiKey;
    private String secretKey = OKCoinCfg.secretKey;
    private String url = OKCoinCfg.url;

    public void setMarketAccount(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;

    }

    public void setMarketInfo(String apiKey, String secretKey, String url) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.url = url;
    }

    @Override
    public void connectMarket() {
        if (apiKey == null || secretKey == null) {
            log.error("connectMarket Failed. apiKey or secretKey is not set.");
            return;
        }
        if (okCoinClient != null) {
            // stop client
        }
        okCoinClient = new OKCoinClient(apiKey, secretKey, url);
        okCoinClient.connect();

        okCoinClient.login();
        okCoinClient.subTickerEth();
    }
}
