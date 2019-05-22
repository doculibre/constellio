package com.constellio.model.services.parser;

import com.auxilii.msgparser.Message;
import com.auxilii.msgparser.MsgParser;
import com.auxilii.msgparser.attachment.Attachment;
import com.auxilii.msgparser.attachment.FileAttachment;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EmailParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailParser.class);

	public Map<String, byte[]> parseEmailAttachements(String fileName, InputStream messageInputStream) {
		Map<String, byte[]> attachements;
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		if ("eml".equals(extension)) {
			attachements = parseEml(messageInputStream);
		} else if ("msg".equals(extension)) {
			attachements = parseMsg(messageInputStream);
		} else {
			throw new IllegalArgumentException("Invalid file name : " + fileName);
		}
		return attachements;
	}


	public Map<String, byte[]> parseEml(InputStream messageInputStream) {

		Properties props = System.getProperties();
		props.put("mail.host", "smtp.dummydomain.com");
		props.put("mail.transport.protocol", "smtp");

		Session mailSession = Session.getDefaultInstance(props, null);
		Map<String, byte[]> attachments = new HashMap<String, byte[]>();
		try {
			MimeMessage message = new MimeMessage(mailSession, messageInputStream);
			messageInputStream.close();

			ByteArrayOutputStream contentOs = new ByteArrayOutputStream();
			message.writeTo(contentOs);
			contentOs.close();


			List<String> attachmentFileNames = new ArrayList<>();
			Object messageContent = message.getContent();
			if (messageContent instanceof MimeMultipart) {
				MimeMultipart mimeMultipart = (MimeMultipart) messageContent;
				int partCount = mimeMultipart.getCount();
				for (int i = 0; i < partCount; i++) {
					try {
						BodyPart bodyPart = mimeMultipart.getBodyPart(i);
						String partFileName = bodyPart.getFileName();
						Object partContent = bodyPart.getContent();
						if (partContent instanceof InputStream) {
							partFileName = MimeUtility.decodeText(partFileName);
							InputStream inputAttachment = (InputStream) partContent;
							attachments.put(partFileName, IOUtils.toByteArray(inputAttachment));
							inputAttachment.close();
							attachmentFileNames.add(partFileName);
						}
					} catch (Throwable t) {
						LOGGER.warn("Error while parsing message content", t);
					}
				}
			}

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return attachments;
	}

	public Map<String, byte[]> parseMsg(InputStream messageInputStream) {
		Map<String, byte[]> attachments = new HashMap<>();
		try {
			byte[] messageBytes = IOUtils.toByteArray(messageInputStream);
			messageInputStream.close();

			MsgParser msgp = new MsgParser();
			Message msg = msgp.parseMsg(new ByteArrayInputStream(messageBytes));

			List<Attachment> atts = msg.getAttachments();
			for (Attachment att : atts) {
				if (att instanceof FileAttachment) {
					FileAttachment file = (FileAttachment) att;
					attachments.put(file.getLongFilename(), file.getData());
				}
			}

		} catch (UnsupportedOperationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return attachments;
	}


}
