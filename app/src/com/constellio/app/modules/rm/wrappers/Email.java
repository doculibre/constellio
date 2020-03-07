package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

import java.util.List;

public class Email extends Document {

	public static final String SCHEMA = SCHEMA_TYPE + "_email";

	public static final String EMAIL_TO = "emailTo";
	public static final String EMAIL_FROM = "emailFrom";
	public static final String EMAIL_IN_NAME_OF = "emailInNameOf";
	public static final String EMAIL_CC_TO = "emailCCTo";
	public static final String EMAIL_BCC_TO = "emailBCCTo";
	public static final String EMAIL_ATTACHMENTS_LIST = "emailAttachmentsList";
	public static final String EMAIL_OBJECT = "emailObject";
	//	public static final String EMAIL_AUTHOR = "emailAuthor";
	public static final String EMAIL_COMPANY = "emailCompany";
	public static final String EMAIL_SENT_ON = "emailSentOn";
	public static final String EMAIL_RECEIVED_ON = "emailReceivedOn";
	public static final String SUBJECT_TO_BROADCAST_RULE = "subjectToBroadcastRule";
	@Deprecated
	public static final String EMAIL_CONTENT = "emailContent";

	public Email(Record record,
				 MetadataSchemaTypes types) {
		super(record, types);
	}

	public Email setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public Email setFolder(Folder folder) {
		super.setFolder(folder);
		return this;
	}

	public Email setFolder(String folder) {
		super.setFolder(folder);
		return this;
	}

	public List<String> getEmailTo() {
		return get(EMAIL_TO);
	}

	public Email setEmailTo(List<String> emailTo) {
		set(EMAIL_TO, emailTo);
		return this;
	}

	public String getEmailFrom() {
		return get(EMAIL_FROM);
	}

	public Email setEmailFrom(String emailFrom) {
		set(EMAIL_FROM, emailFrom);
		return this;
	}

	//	public List<String> getEmailAuthor() {
	//		return get(EMAIL_AUTHOR);
	//	}
	//
	//	public Document setEmailAuthor(List<String> emailAuthor) {
	//		set(EMAIL_AUTHOR, emailAuthor);
	//		return this;
	//	}

	public List<String> getEmailCompany() {
		return get(EMAIL_COMPANY);
	}

	public Email setEmailCompany(List<String> emailCompany) {
		set(EMAIL_COMPANY, emailCompany);
		return this;
	}

	public String getEmailInNameOf() {
		return get(EMAIL_IN_NAME_OF);
	}

	public Email setEmailInNameOf(String emailInNameOf) {
		set(EMAIL_IN_NAME_OF, emailInNameOf);
		return this;
	}

	public List<String> getEmailCCTo() {
		return get(EMAIL_CC_TO);
	}

	public Email setEmailCCTo(List<String> emailCCTo) {
		set(EMAIL_CC_TO, emailCCTo);
		return this;
	}

	public List<String> getEmailBCCTo() {
		return get(EMAIL_BCC_TO);
	}

	public Email setEmailBCCTo(List<String> emailBCCTo) {
		set(EMAIL_BCC_TO, emailBCCTo);
		return this;
	}

	public List<String> getEmailAttachmentsList() {
		return get(EMAIL_ATTACHMENTS_LIST);
	}

	public Email setEmailAttachmentsList(List<String> emailAttachmentsList) {
		set(EMAIL_ATTACHMENTS_LIST, emailAttachmentsList);
		return this;
	}

	public String getEmailObject() {
		return get(EMAIL_OBJECT);
	}

	public Email setEmailObject(String emailObject) {
		set(EMAIL_OBJECT, emailObject);
		return this;
	}

	public String getEmailContent() {
		return get(EMAIL_CONTENT);
	}

	public Email setEmailContent(String emailContent) {
		set(EMAIL_CONTENT, emailContent);
		return this;
	}

	public LocalDateTime getEmailSentOn() {
		return get(EMAIL_SENT_ON);
	}

	public Email setEmailSentOn(LocalDateTime emailSentOn) {
		set(EMAIL_SENT_ON, emailSentOn);
		return this;
	}

	public LocalDateTime getEmailReceivedOn() {
		return get(EMAIL_RECEIVED_ON);
	}

	public Email setEmailReceivedOn(LocalDateTime emailReceivedOn) {
		set(EMAIL_RECEIVED_ON, emailReceivedOn);
		return this;
	}

	public boolean getSubjectToBroadcastRule() {
		return get(SUBJECT_TO_BROADCAST_RULE);
	}

	public Email setSubjectToBroadcastRule(boolean subjectToBroadcastRule) {
		set(SUBJECT_TO_BROADCAST_RULE, subjectToBroadcastRule);
		return this;
	}

}
