package com.constellio.model.services.emails;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.sdk.SDKPasswords;

public class OldSmtpServerTestConfig implements EmailServerConfiguration {

	boolean enabled = true;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public OldSmtpServerTestConfig setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<>();
		/*
		mail.smtp.host=smtp.gmail.com
mail.smtp.socketFactory.port=465
mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
mail.smtp.auth=true
mail.smtp.port=465
        */

		properties.put("mail.smtp.host", "smtp.mandrillapp.com");//-relay
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");

		return properties;
	}

	@Override
	public String getUsername() {
		return SDKPasswords.testSMTPOldUsername();
	}

	@Override
	public String getPassword() {
		return SDKPasswords.testSMTPOldPassword();
	}

	@Override
	public String getDefaultSenderEmail() {
		return SDKPasswords.testSMTPOldUsername();
	}

	@Override
	public EmailServerConfiguration whichIsDisabled() {
		enabled = false;
		return this;
	}

    /*public static void main(String[] args) throws EmailServicesException.EmailServerException, MessagingException {
		EmailServerConfiguration serverTestConfig = new SmtpServerTestConfig();

        EmailServices emailServices = new EmailServices();
        Session session = emailServices.openSession(serverTestConfig);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SDKPasswords.testEmailAccount()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(SDKPasswords.testEmailAccount()));
        message.setSubject("subject");
        message.setText("body");
        Transport.send(message);

    }*/
}
