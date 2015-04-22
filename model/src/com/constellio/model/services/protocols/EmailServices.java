/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.protocols;

import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.constellio.model.entities.notifications.Email;
import com.constellio.model.services.notifications.EmailServicesRuntimeException.EmailServicesRuntimeException_CannotSendEmail;
import com.constellio.model.services.notifications.EmailServicesRuntimeException.EmailServicesRuntimeException_UnsupportedEncodingException;
import com.constellio.model.services.notifications.SmtpServerConfig;

public class EmailServices {

	private final SmtpServerConfig smtpServerConfig;

	public EmailServices(SmtpServerConfig smtpServerConfig) {
		this.smtpServerConfig = smtpServerConfig;
	}

	public void sendEmail(Email email) {

		Session session = Session.getInstance(smtpServerConfig.getProperties(), new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(smtpServerConfig.getEmail(), smtpServerConfig.getPassword());
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(smtpServerConfig.getEmail(), smtpServerConfig.getUser()));
			for (Entry<String, String> to : email.getTo().entrySet()) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.getKey(), to.getValue()));
			}
			message.setSubject(email.getSubject());
			message.setContent(email.getContent(), "text/html; charset=utf-8");

			Transport.send(message);
		} catch (MessagingException e) {
			throw new EmailServicesRuntimeException_CannotSendEmail(e);
		} catch (UnsupportedEncodingException e) {
			throw new EmailServicesRuntimeException_UnsupportedEncodingException(e);
		}
	}

	public SmtpServerConfig getSmtpServerConfig() {
		return smtpServerConfig;
	}
}
