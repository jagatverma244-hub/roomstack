//package com.roomstack.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.Session;
//import jakarta.mail.*;
//import jakarta.mail.internet.*;
//import org.springframework.stereotype.Service;
//
//import java.util.Properties;
//
//@Service
//public class EmailService {
//    public boolean sendemail(String subject,String message,String to)
//    {
//        boolean f = false;
//        String from ="test@domain.com";
//        String host = "localhost";
//        Properties per = System.getProperties();
//        per.put("mail.smtp.host",host);
//        per.put("mail.smtp.port","25");
//        per.put("mail.smtp.auth","false");
//        per.put("mail.smtp.ssl.enable","false");
//        Session session = Session.getInstance(per,null);
//        session.setDebug(true);
//        try {
//            MimeMessage mem = new MimeMessage(session);
//            mem.setFrom(new InternetAddress(from));
//            mem.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
//            mem.setSubject(subject);
//
//            MimeBodyPart htmlPart = new MimeBodyPart();
//            htmlPart.setContent(message,"text/html;charset=utf-8");
//
//            MimeMultipart  multipart = new MimeMultipart();
//            multipart.addBodyPart(htmlPart);
//            mem.setContent(multipart);
//
//            Transport.send(mem);
//            System.out.println("email sent via smtp4dev");
//            f= true;
//
//
//
//        } catch (AddressException e) {
//            throw new RuntimeException(e);
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//        return f;
//    }
//
//}

package com.roomstack.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    public boolean sendemail(String subject, String message, String to) {
        boolean f = false;

        // Replace these with your actual email and app password
        final String from = "jatinteharpuria@gmail.com";
        final String password = "bagzwgouanxfbnfh"; // Gmail App Password

        // SMTP Configuration
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create a Session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        session.setDebug(true); // Optional: shows debug info

        try {
            MimeMessage mem = new MimeMessage(session);
            mem.setFrom(new InternetAddress(from));
            mem.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mem.setSubject(subject);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(message, "text/html;charset=utf-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            mem.setContent(multipart);

            Transport.send(mem);
            System.out.println("✅ Email sent successfully!");
            f = true;

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Email failed to send", e);
        }

        return f;
    }
}

