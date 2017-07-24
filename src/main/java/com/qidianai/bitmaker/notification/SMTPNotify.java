package com.qidianai.bitmaker.notification;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import java.util.Properties;

/**********************************************************
 * BitMaker
 *
 * File: com.qidianai.bitmaker.notification
 * Author: Fox  
 * Date: 7/24/2017
 *
 **********************************************************/

public class SMTPNotify implements Notification {
    static final String fromEmail = "tradewarn@54fox.com"; //requires valid gmail id
    static final String password = "BitMaker666"; // correct password for gmail id
    static final String toEmail = "foxhlchen@qq.vip.com"; // can be any email id


    public static void send(String subject, String body) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.54fox.com"); //SMTP Host
        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", "465"); //SMTP Port

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        Session session = Session.getDefaultInstance(props, auth);
        EmailUtil.sendEmail(session, toEmail,subject, body);

        //EmailUtil.sendAttachmentEmail(session, toEmail,"SSLEmail Testing Subject with Attachment", "SSLEmail Testing Body with Attachment");

        //EmailUtil.sendImageEmail(session, toEmail,"SSLEmail Testing Subject with Image", "SSLEmail Testing Body with Image");
    }
}
