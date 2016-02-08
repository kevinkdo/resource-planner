package utilities;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by Davis Treybig
 */

public class EmailUtility{
	JavaMailSenderImpl sender = new JavaMailSenderImpl();
	Properties javaMailProperties = new Properties();


	public EmailUtility(){
        sender.setUsername("ResourceManagerAlerts@gmail.com");
        sender.setPassword("resourcemanager");
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);

        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");

        sender.setJavaMailProperties(javaMailProperties);
	}

	public void sendMessage(String to, String from, String subject, String text){
		SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(text);

        System.out.println("Sending Email to: " + to + ", from email " + from);

        sender.send(message);
	}


}