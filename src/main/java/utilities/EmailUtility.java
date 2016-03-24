package utilities;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by Davis Treybig
 */

//@Configuration
//@PropertySource("classpath:config.properties")
public class EmailUtility{
	JavaMailSenderImpl sender = new JavaMailSenderImpl();
	Properties javaMailProperties = new Properties();

    //@Value("${email.username}")
    private String username ="ResourceManagerAlerts@gmail.com";

    //@Value("${email.password}")
    private String password = "resourcemanager";

    //@Value("${email.host}")
    private String host = "smtp.gmail.com";

    //@Value("${email.port}")
    private int port = 587;

	public EmailUtility(){
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setHost(host);
        sender.setPort(port);

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