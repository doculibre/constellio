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
package com.constellio.model.entities.records.wrappers;

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
		return get(PARAMETERS);
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
