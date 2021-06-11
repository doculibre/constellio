package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Message extends RecordWrapper {
	public static final String SCHEMA_TYPE = "message";
	public static final String MESSAGE_AUTHOR = "messageAuthor";
	public static final String CONVERSATION = "conversation";
	public static final String MESSAGE_BODY = "messageBody";
	public static final String MESSAGE_BODY_TYPE = "messageBodyType";
	public static final String MESSAGE_PARENT = "messageParent";
	public static final String HAS_URL_IN_MESSAGE = "hasUrlInMessage";
	public static final String MESSAGE_REPLY_COUNT = "messageReplyCount";

	public Message(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getMessageAuthor() {
		return get(MESSAGE_AUTHOR);
	}

	public String getConversation() {
		return get(CONVERSATION);
	}

	public String getMessageBody() {
		return get(MESSAGE_BODY);
	}

	public MessageBodyType getMessageBodyType() {
		return get(MESSAGE_BODY_TYPE);
	}

	public Boolean hasUrlInMessage() {
		return getBooleanWithDefaultValue(HAS_URL_IN_MESSAGE, false);
	}

	public String getMessageParent() {
		return get(MESSAGE_PARENT);
	}

	public int getMessageReplyCount() {
		return ((Double) get(MESSAGE_REPLY_COUNT)).intValue();
	}

	public Message setMessageAuthor(String messageAuthor) {
		set(MESSAGE_AUTHOR, messageAuthor);
		return this;
	}

	public Message setConversation(String conversation) {
		set(CONVERSATION, conversation);
		return this;
	}

	public Message setMessageBody(String messageBody) {
		set(MESSAGE_BODY, messageBody);
		return this;
	}

	public Message setMessageBodyType(MessageBodyType messageBodyType) {
		set(MESSAGE_BODY_TYPE, messageBodyType);
		return this;
	}

	public Message setMessageParent(String messageParent) {
		set(MESSAGE_PARENT, messageParent);
		return this;
	}

}
