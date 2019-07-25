package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

public class UserDocument extends RecordWrapper {

	public static final String SCHEMA_TYPE = "userDocument";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String USER = "user";

	public static final String FORM_CREATED_ON = "formCreatedOn";

	public static final String FORM_MODIFIED_ON = "formModifiedOn";

	public static final String CONTENT = "content";

	public static final String CONTENT_SIZE = "contentSize";

	// TODO Move to RM
	public static final String FOLDER = "folder";

	public static final String USER_FOLDER = "userFolder";

	public static final String CONTENT_HASHES = "contentHashes";

	public UserDocument(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getUser() {
		return get(USER);
	}

	public UserDocument setUser(String user) {
		set(USER, user);
		return this;
	}

	public UserDocument setUser(Record user) {
		set(USER, user);
		return this;
	}

	public UserDocument setUser(User user) {
		set(USER, user);
		return this;
	}

	public LocalDateTime getFormCreatedOn() {
		return get(FORM_CREATED_ON);
	}

	public UserDocument setFormCreatedOn(LocalDateTime dateTime) {
		set(FORM_CREATED_ON, dateTime);
		return this;
	}

	public LocalDateTime getFormModifiedOn() {
		return get(FORM_MODIFIED_ON);
	}

	public UserDocument setFormModifiedOn(LocalDateTime dateTime) {
		set(FORM_MODIFIED_ON, dateTime);
		return this;
	}

	public Content getContent() {
		return get(CONTENT);
	}

	public UserDocument setContent(Content content) {
		set(CONTENT, content);
		return this;
	}

	public String getFolder() {
		return get(FOLDER);
	}

	public UserDocument setFolder(String folder) {
		set(FOLDER, folder);
		return this;
	}

	public String getUserFolder() {
		return get(USER_FOLDER);
	}

	public UserDocument setUserFolder(String userFolder) {
		set(USER_FOLDER, userFolder);
		return this;
	}

	public UserDocument setUserFolder(Record userFolder) {
		set(USER_FOLDER, userFolder);
		return this;
	}

	public UserDocument setUserFolder(UserFolder userFolder) {
		set(USER_FOLDER, userFolder);
		return this;
	}

	public Double getContentSize() {
		return get(CONTENT_SIZE);
	}

}
