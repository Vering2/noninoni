package noninoni.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncEmailService {
	 @Autowired
	    private JavaMailSender javaMailSender;

	    @Async
	    public void sendAsync(SimpleMailMessage email) {
	        javaMailSender.send(email);
	    }

}
