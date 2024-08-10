package com.krypto.Mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Properties;

public class mailSend {

    public HashMap mailsend(HashMap map) {
        HashMap rtnmap = new HashMap();
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            String username = "kumarannathan871999@gmail.com";
            String password = "xwxlywcstlppuebx";

            Session session = Session.getDefaultInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = prepareMailMessage(session, username, map);
            Transport.send(message);
            rtnmap.put("authcode", map.get("authcode").toString());
            rtnmap.put("userid", map.get("userid").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnmap;
    }

    public Message prepareMailMessage(Session session, String fromid, HashMap map) {
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(fromid));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(map.get("emailid").toString()));
            message.setSubject("Authenticator Auth Code");
            String body = "Please find the authenticator code <br/> " + map.get("authcode").toString();

            message.setContent("This is authCode " + body, "text/html");

            message.setContent("This is authCode " + body, "text/html");

            message.setContent("This is authCode " + body, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }


}
