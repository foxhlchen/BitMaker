package com.qidianai.bitmaker.notification;

import com.qidianai.bitmaker.config.SMTPCfg;

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

    public static void send(String subject, String body) {

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTPCfg.smtpHost); //SMTP Host
        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", "465"); //SMTP Port

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTPCfg.fromEmail, SMTPCfg.password);
            }
        };

        Session session = Session.getDefaultInstance(props, auth);
        EmailUtil.sendEmail(session, SMTPCfg.fromEmail, SMTPCfg.toEmail,subject, body);

        //EmailUtil.sendAttachmentEmail(session, toEmail,"SSLEmail Testing Subject with Attachment", "SSLEmail Testing Body with Attachment");

        //EmailUtil.sendImageEmail(session, toEmail,"SSLEmail Testing Subject with Image", "SSLEmail Testing Body with Image");
    }
}
