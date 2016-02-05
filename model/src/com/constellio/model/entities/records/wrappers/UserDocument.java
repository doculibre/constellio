package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class UserDocument extends RecordWrapper {

	public static final String SCHEMA_TYPE = "userDocument";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String USER = "user";

	public static final String CONTENT = "content";
	
	// TODO Move to RM
	public static final String FOLDER = "folder";

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
	
}
