package com.example.webmuasam.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class EmailService {
    private final MailSender mailSender;
    private final JavaMailSender javaMailSender;
    public EmailService(MailSender mailSender, JavaMailSender javaMailSender) {
        this.mailSender = mailSender;
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleEmail(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hoalv1237890@gmail.com");
        message.setSubject("Testting from spring boot");
        message.setText("Hello World");
        this.mailSender.send(message);
    }
    @Async
    public void sendEmailSync(String to,String subject,String content,boolean isMultipart,boolean isHtml){
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try{
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage,isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content,isHtml);
            this.javaMailSender.send(mimeMessage);
        }catch (MailException | MessagingException e){
            System.out.println("ERROR SEND EMAIL: "+e);
        }

    }
}
