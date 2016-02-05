package com.constellio.model.entities.records.wrappers;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;

public class EmailToSend extends RecordWrapper {

	public static final String SCHEMA_TYPE = "emailToSend";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String FROM = "from";

	public static final String TO = "to";

	public static final String BCC = "BCC";

	public static final String CC = "CC";

	public static final String SUBJECT = "subject";

	public static final String TEMPLATE = "template";

	public static final String PARAMETERS = "parameters";

	public static final String SEND_ON = "sendOn";

	public static final String TRYING_COUNT = "tryingCount";

	public static final String ERROR = "error";
	public static final String PARAMETER_SEPARATOR = ":";

	public EmailToSend(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	@Override
	public RecordWrapper setTitle(String title) {
		throw new UnsupportedOperationException("Title cannot be set on a user, this metadata is calculated.");
	}

	public static EmailToSend wrapNullable(Record record, MetadataSchemaTypes types) {
		return record == null ? null : new EmailToSend(record, types);
	}

	public EmailAddress getFrom() {
		return get(FROM);
	}

	public EmailToSend setFrom(EmailAddress from) {
		set(FROM, from);
		return this;
	}

	public List<EmailAddress> getTo() {
		return getList(TO);
	}

	public EmailToSend setTo(EmailAddress to) {
		set(TO, asList(to));
		return this;
	}

	public EmailToSend setTo(List<EmailAddress> to) {
		set(TO, to);
		return this;
	}

	public List<EmailAddress> getBCC() {
		return getList(BCC);
	}

	public EmailToSend setBCC(List<EmailAddress> bcc) {
		set(BCC, bcc);
		return this;
	}

	public List<EmailAddress> getCC() {
		return getList(CC);
	}

	public EmailToSend setCC(List<EmailAddress> cc) {
		set(CC, cc);
		return this;
	}

	public String getSubject() {
		return get(SUBJECT);
	}

	public EmailToSend setSubject(String subject) {
		set(SUBJECT, subject);
		return this;
	}

	public String getTemplate() {
		return get(TEMPLATE);
	}

	public EmailToSend setTemplate(String template) {
		set(TEMPLATE, template);
		return this;
	}

	public List<String> getParameters() {
		return getList(PARAMETERS);
	}

	public EmailToSend setParameters(List<String> parameters) {
		set(PARAMETERS, parameters);
		return this;
	}

	public LocalDateTime getSendOn() {
		return get(SEND_ON);
	}

	public EmailToSend setSendOn(LocalDateTime sendOn) {
		set(SEND_ON, sendOn);
		return this;
	}

	public Double getTryingCount() {
		return get(TRYING_COUNT);
	}

	public EmailToSend setTryingCount(Double tryingCount) {
		set(TRYING_COUNT, tryingCount);
		return this;
	}

	public String getError() {
		return get(ERROR);
	}

	public EmailToSend setError(String error) {
		set(ERROR, error);
		return this;
	}
}
