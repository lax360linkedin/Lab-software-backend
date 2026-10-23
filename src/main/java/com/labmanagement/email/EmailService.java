
package com.labmanagement.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${app.reset-password-url}")
	private String resetPasswordUrl;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendPasswordResetEmail(String toEmail, String token) {

		String resetLink = resetPasswordUrl + "?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(toEmail);
		message.setSubject("MLMS - Password Reset");
		message.setText("Hello,\n\n" + "Click this link to reset your MLMS password:\n" + resetLink + "\n\n"
				+ "This link expires in 15 minutes.\n" + "If you did not request this, ignore this email.");

		mailSender.send(message);
	}
}
