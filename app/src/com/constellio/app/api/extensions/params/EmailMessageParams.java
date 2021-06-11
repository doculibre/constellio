package com.constellio.app.api.extensions.params;

import com.constellio.model.services.emails.EmailServices.MessageAttachment;

import java.util.List;

public class EmailMessageParams {

	private final String filenamePrefix;

	private final String signature;

	private final String subject;

	private final String from;

	private final String userFullName;

	private final List<MessageAttachment> attachments;

	public EmailMessageParams(String filenamePrefix, String signature, String subject, String from,
							  List<MessageAttachment> attachments) {
		this.filenamePrefix = filenamePrefix;
		this.signature = signature;
		this.subject = subject;
		this.from = from;
		this.attachments = attachments;
		this.userFullName = "";
	}

	public EmailMessageParams(String filenamePrefix, String signature, String subject, String from,
							  List<MessageAttachment> attachments,String userFullName){
		this.filenamePrefix = filenamePrefix;
		this.signature = signature;
		this.subject = subject;
		this.from = from;
		this.attachments = attachments;
		this.userFullName = userFullName;
	}

	public String getFilenamePrefix() {
		return filenamePrefix;
	}

	public String getSignature() {
		return signature;
	}

	public String getSubject() {
		return subject;
	}

	public String getFrom() {
		return from;
	}

	public String getUserFullName() { return userFullName;}

	public List<MessageAttachment> getAttachments() {
		return attachments;
	}

}
