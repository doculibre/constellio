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
package com.constellio.model.services.emails;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.entities.structures.EmailAddressFactory;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.sun.mail.smtp.SMTPMessage;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EmailBuilder {

	public static final String BACKGROUND_PATH = "VAADIN/themes/constellio/images/back-const-1920x1358_smal.jpg";
	public static final String LOGO_PATH = "VAADIN/themes/constellio/images/logo_eim_203x30.png";
	EmailTemplatesManager emailTemplatesManager;
	private SystemConfigurationsManager systemConfigManager;

	public EmailBuilder(EmailTemplatesManager emailTemplatesManager, SystemConfigurationsManager systemConfigManager) {
		this.emailTemplatesManager = emailTemplatesManager;
		this.systemConfigManager = systemConfigManager;
	}

	public Message build(EmailToSend messageToSend, Session session, String defaultFrom) throws MessagingException, InvalidBlankEmail {

		EmailAddressFactory factory = new EmailAddressFactory();

		Message message = new SMTPMessage(session);

		String addressFrom;
		EmailAddress from = messageToSend.getFrom();
		if(from != null){
			addressFrom = factory.toAddress(from);
		}else{
			if(StringUtils.isBlank(defaultFrom)){
				throw new InvalidBlankEmail();
			}else{
				addressFrom = defaultFrom;
			}
		}
		InternetAddress internetAddressFrom = new InternetAddress(addressFrom, true);
		InternetAddress[] internetAddressesTo = getInternetAddresses(messageToSend, factory, RecipientType.TO);
		InternetAddress[] internetAddressesBcc = getInternetAddresses(messageToSend, factory, RecipientType.BCC);
		InternetAddress[] internetAddressesCc = getInternetAddresses(messageToSend, factory, RecipientType.CC);
		message.setFrom(internetAddressFrom);
		message.setRecipients(RecipientType.TO, internetAddressesTo);
		message.setRecipients(RecipientType.BCC, internetAddressesBcc);
		message.setRecipients(RecipientType.CC, internetAddressesCc);
		message.setSubject(messageToSend.getSubject());
		message.setSentDate(messageToSend.getSendOn().toDate());

		String html = emailTemplatesManager.getCollectionTemplate(messageToSend.getTemplate(), messageToSend.getCollection());
		//Set<String> imgs = new HashSet<>();
		for (String parameter : messageToSend.getParameters()) {
			String key = parameter.substring(0, parameter.indexOf(EmailToSend.PARAMETER_SEPARATOR));
			String value = parameter.substring(parameter.indexOf(EmailToSend.PARAMETER_SEPARATOR) + 1);
			html = html.replaceAll("\\$\\{" + key + "\\}", value);
			/*if (value.contains("'cid:")) {
				String cid = value.substring(value.indexOf("cid:"));
				String path = cid.substring(cid.indexOf("cid:") + 4, cid.indexOf("'"));
				cleanString(path);
				imgs.add(path);
			}*/
		}

		Multipart multipart = new MimeMultipart();
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(html, "text/html");
		multipart.addBodyPart(htmlPart);
		//FIXME see with cis use client logo and other than folders locator?
		FoldersLocator foldersLocator = new FoldersLocator();
		addImagePart(multipart, EmailTemplatesManager.LOGO_ID, new File(foldersLocator.getAppProjectWebContent(), LOGO_PATH));
		addImagePart(multipart, EmailTemplatesManager.BACKGROUND_ID, new File(foldersLocator.getAppProjectWebContent(), BACKGROUND_PATH));

		/*for (String img : imgs) {
			MimeBodyPart imagePart = new MimeBodyPart();
			DataSource fileDataSource = new FileDataSource(img);
			imagePart.setDataHandler(new DataHandler(fileDataSource));
			imagePart.setHeader("Content-ID", "<" + img + ">");
			multipart.addBodyPart(imagePart);
		}*/
		message.setContent(multipart);
		return message;
	}

	private void addImagePart(Multipart multipart, String imgID, File imgFile) throws MessagingException {
		MimeBodyPart imgPart = new MimeBodyPart();
		imgPart.setHeader("Content-ID", "<" + imgID + ">");//"<image>"
		DataSource img = new FileDataSource(imgFile);
		imgPart.setDataHandler(new DataHandler(img));
		multipart.addBodyPart(imgPart);
	}

	private InternetAddress[] getInternetAddresses(EmailToSend messageToSend, EmailAddressFactory factory,
			RecipientType recipientType)
			throws AddressException {
		List<EmailAddress> emailAddresses = new ArrayList<>();
		if (recipientType == RecipientType.TO) {
			emailAddresses = messageToSend.getTo();
		} else if (recipientType == RecipientType.CC) {
			emailAddresses = messageToSend.getCC();
		} else if (recipientType == RecipientType.BCC) {
			emailAddresses = messageToSend.getBCC();
		}
		InternetAddress[] internetAddresses = new InternetAddress[emailAddresses.size()];
		for (int i = 0; i < emailAddresses.size(); i++) {
			String address = factory.toAddress(emailAddresses.get(i));
			internetAddresses[i] = new InternetAddress(address);
		}
		return internetAddresses;
	}

	public static class InvalidBlankEmail extends Exception {
		public InvalidBlankEmail() {
			super("Invalid blank email address");
		}
	}
}
