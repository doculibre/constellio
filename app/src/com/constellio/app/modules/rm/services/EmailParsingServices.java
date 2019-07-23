package com.constellio.app.modules.rm.services;

import com.auxilii.msgparser.Message;
import com.auxilii.msgparser.MsgParser;
import com.auxilii.msgparser.attachment.Attachment;
import com.auxilii.msgparser.attachment.FileAttachment;
import com.constellio.app.modules.rm.wrappers.Email;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.parsers.POIFSChunkParser;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Deprecated
public class EmailParsingServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailParsingServices.class);

	public static final String EMAIL_MIME_TYPES = "mimeTypes";
	public static final String EMAIL_ATTACHMENTS = "attachments";

	private RMSchemasRecordsServices rm;

	public EmailParsingServices(RMSchemasRecordsServices rm) {
		this.rm = rm;
	}

	//KEEP
	@SuppressWarnings("unchecked")
	public Email newEmail(String fileName, InputStream messageInputStream) {
		Map<String, Object> parsedEmail = parseEmail(fileName, messageInputStream);

		Email email = rm.newEmail();

		String subject = (String) parsedEmail.get(Email.SUBJECT);
		String object = (String) parsedEmail.get(Email.EMAIL_OBJECT);
		Date sentOn = (Date) parsedEmail.get(Email.EMAIL_SENT_ON);
		Date receivedOn = (Date) parsedEmail.get(Email.EMAIL_RECEIVED_ON);
		String from = (String) parsedEmail.get(Email.EMAIL_FROM);
		List<String> to = (List<String>) parsedEmail.get(Email.EMAIL_TO);
		List<String> ccTo = (List<String>) parsedEmail.get(Email.EMAIL_CC_TO);
		List<String> bccTo = (List<String>) parsedEmail.get(Email.EMAIL_BCC_TO);
		List<String> attachmentFileNames = (List<String>) parsedEmail.get(Email.EMAIL_ATTACHMENTS_LIST);

		LocalDateTime sentOnDateTime = sentOn != null ? new LocalDateTime(sentOn.getTime()) : null;
		LocalDateTime receivedOnDateTime = receivedOn != null ? new LocalDateTime(receivedOn.getTime()) : null;

		email.setSubject(subject);
		email.setEmailObject(object);
		email.setEmailSentOn(sentOnDateTime);
		email.setEmailReceivedOn(receivedOnDateTime);
		email.setEmailFrom(from);
		email.setEmailTo(to);
		email.setEmailCCTo(ccTo);
		email.setEmailBCCTo(bccTo);
		email.setEmailAttachmentsList(attachmentFileNames);

		return email;
	}

	//KEEP
	private Map<String, Object> parseEml(InputStream messageInputStream) {
		Map<String, Object> parsed = new HashMap<String, Object>();

		Properties props = System.getProperties();
		props.put("mail.host", "smtp.dummydomain.com");
		props.put("mail.transport.protocol", "smtp");

		Session mailSession = Session.getDefaultInstance(props, null);
		try {
			MimeMessage message = new MimeMessage(mailSession, messageInputStream);
			messageInputStream.close();
			String subject = message.getSubject();
			String object = subject;
			Date sentDate = message.getSentDate();
			Date receivedDate = message.getReceivedDate();

			Address from = message.getFrom()[0];
			Address[] to = message.getRecipients(RecipientType.TO);
			Address[] cc = message.getRecipients(RecipientType.CC);
			Address[] bcc = message.getRecipients(RecipientType.BCC);

			ByteArrayOutputStream contentOs = new ByteArrayOutputStream();
			message.writeTo(contentOs);
			contentOs.close();
			String content = contentOs.toString("UTF-8");

			parsed.put(Email.SUBJECT, subject);
			parsed.put(Email.EMAIL_OBJECT, object);
			parsed.put(Email.EMAIL_SENT_ON, sentDate);
			parsed.put(Email.EMAIL_RECEIVED_ON, receivedDate);
			parsed.put(Email.EMAIL_FROM, "" + from);
			parsed.put(Email.EMAIL_TO, addressesAsStringList(to));
			parsed.put(Email.EMAIL_CC_TO, addressesAsStringList(cc));
			parsed.put(Email.EMAIL_BCC_TO, addressesAsStringList(bcc));

			Map<String, InputStream> attachments = new HashMap<String, InputStream>();
			parsed.put(EMAIL_ATTACHMENTS, attachments);

			Map<String, String> mimeTypes = new HashMap<String, String>();
			parsed.put(EMAIL_MIME_TYPES, mimeTypes);

			List<String> attachmentFileNames = new ArrayList<>();
			parsed.put(Email.EMAIL_ATTACHMENTS_LIST, attachmentFileNames);

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
							attachments.put(partFileName, inputAttachment);
							mimeTypes.put(partFileName, bodyPart.getContentType());
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
		return parsed;
	}

	//KEEP
	private static List<String> addressesAsStringList(Address[] addresses) {
		List<String> addressesStr = new ArrayList<>();
		if (addresses != null) {
			for (Address address : addresses) {
				addressesStr.add(address.toString());
			}
		}
		return addressesStr;
	}

	//KEEP
	public Map<String, Object> parseMsg(InputStream messageInputStream) {
		Map<String, Object> parsed = new HashMap<String, Object>();
		try {
			byte[] messageBytes = IOUtils.toByteArray(messageInputStream);
			messageInputStream.close();

			Chunks CHUNKS = new Chunks();
			POIFSFileSystem filesystem = new POIFSFileSystem(new ByteArrayInputStream(messageBytes));
			ChunkGroup[] chunkGroups = POIFSChunkParser.parse(filesystem);
			for (ChunkGroup chunkGroup : chunkGroups) {
				for (Chunk chunk : chunkGroup.getChunks()) {
					Chunk recordChunk;
					int chunkId = chunk.getChunkId();
					if (chunkId == MAPIProperty.BODY.id) {
						if (chunk instanceof ByteChunk) {
							final ByteChunk byteChunk = (ByteChunk) chunk;
							recordChunk = new StringChunk(byteChunk.getChunkId(), byteChunk.getType()) {
								@Override
								public String get7BitEncoding() {
									return byteChunk.getAs7bitString();
								}

								@Override
								public void set7BitEncoding(String encoding) {
									super.set7BitEncoding(encoding);
								}

								@Override
								public void readValue(InputStream value)
										throws IOException {
									byteChunk.readValue(value);
								}

								@Override
								public void writeValue(OutputStream out)
										throws IOException {
									byteChunk.writeValue(out);
								}

								@Override
								public String getValue() {
									return new String(byteChunk.getValue());
								}

								@Override
								public byte[] getRawValue() {
									return byteChunk.getValue();
								}

								@Override
								public void setValue(String str) {
									byteChunk.setValue(str.getBytes());
								}

								@Override
								public String toString() {
									return byteChunk.toString();
								}

								@Override
								public String getEntryName() {
									return byteChunk.getEntryName();
								}
							};
						} else {
							recordChunk = chunk;
						}
					} else {
						recordChunk = chunk;
					}
					CHUNKS.record(recordChunk);
				}
			}

			String from = getValue(CHUNKS.getDisplayFromChunk());
			String subject = getValue(CHUNKS.getSubjectChunk());
			String to = getValue(CHUNKS.getDisplayToChunk());
			String cc = getValue(CHUNKS.getDisplayCCChunk());
			String bcc = getValue(CHUNKS.getDisplayBCCChunk());
			String content = getValue(CHUNKS.getTextBodyChunk());

			MsgParser msgp = new MsgParser();
			Message msg = msgp.parseMsg(new ByteArrayInputStream(messageBytes));
			Date sentDate = msg.getDate();
			Date receivedDate = msg.getDate();

			parsed.put(Email.SUBJECT, subject);
			parsed.put(Email.EMAIL_OBJECT, subject);
			parsed.put(Email.EMAIL_SENT_ON, sentDate);
			parsed.put(Email.EMAIL_RECEIVED_ON, receivedDate);
			parsed.put(Email.EMAIL_FROM, from);
			parsed.put(Email.EMAIL_CC_TO, splitAddresses(to));
			parsed.put(Email.EMAIL_CC_TO, splitAddresses(cc));
			parsed.put(Email.EMAIL_BCC_TO, splitAddresses(bcc));
			insertMsgAttachments(parsed, msg);

		} catch (UnsupportedOperationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return parsed;
	}

	//KEEP
	private String getValue(StringChunk chunk) {
		return chunk == null ? null : chunk.getValue();
	}

	//KEEP
	private static List<String> splitAddresses(String addresses) {
		return Arrays.asList(StringUtils.split(addresses, ";"));
	}

	//KEEP
	private static void insertMsgAttachments(Map<String, Object> parsed, Message msg) {
		Map<String, InputStream> attachments = new HashMap<String, InputStream>();
		parsed.put(EMAIL_ATTACHMENTS, attachments);

		List<String> attachmentFileNames = new ArrayList<>();
		parsed.put(Email.EMAIL_ATTACHMENTS_LIST, attachmentFileNames);

		Map<String, String> mimeTypes = new HashMap<String, String>();
		parsed.put(EMAIL_MIME_TYPES, mimeTypes);

		List<Attachment> atts = msg.getAttachments();
		for (Attachment att : atts) {
			if (att instanceof FileAttachment) {
				FileAttachment file = (FileAttachment) att;
				String fileName = file.getFilename();
				attachments.put(file.getLongFilename(), new ByteArrayInputStream(file.getData()));
				mimeTypes.put(fileName, file.getMimeTag());
				attachmentFileNames.add(fileName);
			}
		}
	}

	//KEEP
	public Map<String, Object> parseEmail(String fileName, InputStream messageInputStream) {
		Map<String, Object> parsedMessage;
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		if ("eml".equals(extension)) {
			parsedMessage = parseEml(messageInputStream);
		} else if ("msg".equals(extension)) {
			parsedMessage = parseMsg(messageInputStream);
		} else {
			throw new IllegalArgumentException("Invalid file name : " + fileName);
		}
		return parsedMessage;
	}
}
