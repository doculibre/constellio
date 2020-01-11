package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

import java.util.List;

public abstract class RMObject extends RecordWrapper {

	public static final String FORM_CREATED_BY = "formCreatedBy";
	public static final String FORM_CREATED_ON = "formCreatedOn";
	public static final String FORM_MODIFIED_BY = "formModifiedBy";
	public static final String FORM_MODIFIED_ON = "formModifiedOn";

	public RMObject(Record record,
					MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
	}

	public abstract FolderStatus getArchivisticStatus();

	public abstract Boolean getBorrowed();

	public abstract List<String> getAlertUsersWhenAvailable();

	public abstract RMObject setAlertUsersWhenAvailable(List<String> users);

	public String getFormCreatedBy() {
		return get(FORM_CREATED_BY);
	}

	public RMObject setFormCreatedBy(User user) {
		set(FORM_CREATED_BY, user);
		return this;
	}

	public LocalDateTime getFormCreatedOn() {
		return get(FORM_CREATED_ON);
	}

	public String getFormModifiedBy() {
		return get(FORM_MODIFIED_BY);
	}

	public RMObject setFormModifiedBy(User user) {
		set(FORM_MODIFIED_BY, user);
		return this;
	}

	public LocalDateTime getFormModifiedOn() {
		return get(FORM_MODIFIED_ON);
	}

	public RMObject setFormCreatedOn(LocalDateTime dateTime) {
		set(FORM_CREATED_ON, dateTime);
		return this;
	}

	public RMObject setFormModifiedOn(LocalDateTime dateTime) {
		set(FORM_MODIFIED_ON, dateTime);
		return this;
	}
}
