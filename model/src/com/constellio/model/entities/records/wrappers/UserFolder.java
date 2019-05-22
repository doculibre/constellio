package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

/**
 * Created by Nicolas D'Amours
 */
public class UserFolder extends RecordWrapper {

	public static final String SCHEMA_TYPE = "userFolder";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String USER = "user";

	public static final String FORM_CREATED_ON = "formCreatedOn";

	public static final String FORM_MODIFIED_ON = "formModifiedOn";

	public static final String PARENT_USER_FOLDER = "parentUserFolder";

	public UserFolder(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getUser() {
		return get(USER);
	}

	public UserFolder setUser(String user) {
		set(USER, user);
		return this;
	}

	public UserFolder setUser(Record user) {
		set(USER, user);
		return this;
	}

	public UserFolder setUser(User user) {
		set(USER, user);
		return this;
	}

	@Override
	public UserFolder setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public LocalDateTime getFormCreatedOn() {
		return get(FORM_CREATED_ON);
	}

	public UserFolder setFormCreatedOn(LocalDateTime dateTime) {
		set(FORM_CREATED_ON, dateTime);
		return this;
	}

	public LocalDateTime getFormModifiedOn() {
		return get(FORM_MODIFIED_ON);
	}

	public UserFolder setFormModifiedOn(LocalDateTime dateTime) {
		set(FORM_MODIFIED_ON, dateTime);
		return this;
	}

	public String getParent() {
		return get(PARENT_USER_FOLDER);
	}

	public UserFolder setParent(String userFolder) {
		set(PARENT_USER_FOLDER, userFolder);
		return this;
	}

	public UserFolder setParent(Record userFolder) {
		set(PARENT_USER_FOLDER, userFolder);
		return this;
	}

	public UserFolder setParent(UserFolder userFolder) {
		set(PARENT_USER_FOLDER, userFolder);
		return this;
	}

}
