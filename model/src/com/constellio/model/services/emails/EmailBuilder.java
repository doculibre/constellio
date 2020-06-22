package com.constellio.model.services.emails;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.entities.structures.EmailAddressFactory;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.emails.EmailServices.MessageAttachment;
import com.sun.mail.smtp.SMTPMessage;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.IOUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailBuilder {

	public static final String BACKGROUND_PATH = "back-const-1920x1358_smal.jpg";
	public static final String LOGO_PATH = "logo_eim_203x30.png";
	EmailTemplatesManager emailTemplatesManager;
	private SystemConfigurationsManager systemConfigManager;
	private ContentManager contentManager;

	public EmailBuilder(EmailTemplatesManager emailTemplatesManager, SystemConfigurationsManager systemConfigManager, ContentManager contentManager) {
		this.emailTemplatesManager = emailTemplatesManager;
		this.systemConfigManager = systemConfigManager;
		this.contentManager = contentManager;
	}

	public MimeMessage build(EmailToSend messageToSend, Session session, String defaultFrom)
			throws MessagingException, InvalidBlankEmail {

		EmailAddressFactory factory = new EmailAddressFactory();

		MimeMessage message = new SMTPMessage(session);

		String addressFrom;
		EmailAddress from = messageToSend.getFrom();
		if (from != null) {
			addressFrom = factory.toAddress(from);
		} else {
			if (StringUtils.isBlank(defaultFrom)) {
				throw new InvalidBlankEmail();
			} else {
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
		message.setSentDate(messageToSend.getSendOn().toDate());

		String html = "";
		if(StringUtils.isBlank(messageToSend.getBody())) {
			html = emailTemplatesManager.getCollectionTemplate(messageToSend.getTemplate(), messageToSend.getCollection());

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
		} else {
			html = messageToSend.getBody();
		}

		if (StringUtils.isBlank(messageToSend.getSubject())) {
			Pattern pattern = Pattern.compile("<title>(.*?)</title>");
			Matcher matcher = pattern.matcher(html);
			if (matcher.find() == true) {
				String value = StringEscapeUtils.unescapeHtml(matcher.group(1));
				message.setSubject(value);
			}
		} else {
			message.setSubject(messageToSend.getSubject());
		}

		Multipart multipart = new MimeMultipart();
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(html, "text/html");
		multipart.addBodyPart(htmlPart);

		List<Content> linkedFiles = messageToSend.getLinkedFiles();
		List<MessageAttachment> attachments = new ArrayList<>();

		try {
			for(Content content: linkedFiles) {
				attachments.add(createAttachment(content));
			}
			String charset = "UTF-8";
			for (MessageAttachment messageAttachment : attachments) {
				String filename = messageAttachment.getAttachmentName();
				filename = MimeUtility.encodeText(filename, charset, null);
				MimeBodyPart attachment = new MimeBodyPart();
				DataSource source = new ByteArrayDataSource(messageAttachment.getInputStream(), messageAttachment.getMimeType());
				attachment.setDataHandler(new DataHandler(source));
				attachment.setFileName(filename);
				multipart.addBodyPart(attachment);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			for(MessageAttachment attachment: attachments) {
				IOUtils.closeQuietly(attachment.getInputStream());
			}
		}

		//FIXME see with cis use client logo and other than folders locator?
		FoldersLocator foldersLocator = new FoldersLocator();

		addImagePart(multipart, EmailTemplatesManager.LOGO_ID, new File(foldersLocator.getConstellioThemeImages(), LOGO_PATH));
		//		addImagePart(multipart, EmailTemplatesManager.BACKGROUND_ID,
		//				new File(foldersLocator.getConstellioThemeImages(), BACKGROUND_PATH));

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

	private void addImagePart(Multipart multipart, String imgID, File imgFile)
			throws MessagingException {
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

	MessageAttachment createAttachment(Content content) {

		String hash = content.getCurrentVersion().getHash();
		InputStream inputStream = contentManager.getContentInputStream(hash, content.getCurrentVersion().getFilename());
		String mimeType = content.getCurrentVersion().getMimetype();
		String attachmentName = content.getCurrentVersion().getFilename();
		return new MessageAttachment().setMimeType(mimeType).setAttachmentName(attachmentName).setInputStream(inputStream);
	}
}
