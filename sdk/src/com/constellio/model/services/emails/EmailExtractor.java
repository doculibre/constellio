package com.constellio.model.services.emails;

import com.constellio.sdk.SDKPasswords;
import com.sun.mail.imap.IMAPFolder;
import org.apache.commons.lang3.StringUtils;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailExtractor {
	public static void main(String[] args) throws MessagingException, IOException {
		IMAPFolder folder = null;
		Store store = null;
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");

			Session session = Session.getDefaultInstance(props, null);

			store = session.getStore("imaps");
			store.connect("imap.googlemail.com", SDKPasswords.testIMAPExtractionUsername(), SDKPasswords.testIMAPExtractionPassword());

			folder = (IMAPFolder) store.getFolder("inbox");


			if (!folder.isOpen()) {
				folder.open(Folder.READ_WRITE);
			}
			Message[] messages = folder.getMessages();

		} finally {
			if (folder != null && folder.isOpen()) {
				folder.close(true);
			}
			if (store != null) {
				store.close();
			}
		}
	}

	private static List<File> getAttachments(Message[] messages) throws IOException, MessagingException {
		List<File> attachments = new ArrayList<>();
		for (Message message : messages) {
			Multipart multipart = (Multipart) message.getContent();

			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
					StringUtils.isBlank(bodyPart.getFileName())) {
					continue;
				}
				InputStream inputStream = bodyPart.getInputStream();
				File file = new File("C:\\Workspace\\courriel" + bodyPart.getFileName());
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);
				}
				fileOutputStream.close();
				attachments.add(file);
			}
		}
		return attachments;
	}

}