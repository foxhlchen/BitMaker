package com.qidianai.bitmaker.config;

import java.util.Properties;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.config
 * Author: fox  
 * Date: 2017/7/28
 *
 **********************************************************/
public class SMTPCfg {
    public static String smtpHost = "smtp.54fox.com";
    public static  String fromEmail = "tradewarn@54fox.com";
    public static  String password = "BitMaker666";
    public static  String toEmail = "foxhlchen@foxmail.com";

    public static void load(Properties prop) {
        smtpHost = prop.getProperty("smtp.host", smtpHost);
        fromEmail = prop.getProperty("smtp.from", fromEmail);
        toEmail = prop.getProperty("smtp.to", toEmail);
        password = prop.getProperty("smtp.password", password);
    }
}
