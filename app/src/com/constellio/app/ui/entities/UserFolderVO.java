package com.constellio.app.ui.entities;

import static com.constellio.model.entities.records.wrappers.UserFolder.FORM_CREATED_ON;
import static com.constellio.model.entities.records.wrappers.UserFolder.FORM_MODIFIED_ON;
import static com.constellio.model.entities.records.wrappers.UserFolder.PARENT_USER_FOLDER;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.UserFolder;

public class UserFolderVO extends RecordVO {
	
	public UserFolderVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}
	
	public LocalDateTime getFormCreatedOn() {
		return get(FORM_CREATED_ON);
	}

	public void setFormCreatedOn(LocalDateTime dateTime) {
		set(FORM_CREATED_ON, dateTime);
	}
	
	public LocalDateTime getFormModifiedOn() {
		return get(FORM_MODIFIED_ON);
	}

	public void setFormModifiedOn(LocalDateTime dateTime) {
		set(FORM_MODIFIED_ON, dateTime);
	}

	public String getParent() {
		return get(PARENT_USER_FOLDER);
	}

	public void setParent(String userFolder) {
		set(PARENT_USER_FOLDER, userFolder);
	}

	public void setParent(Record userFolder) {
		set(PARENT_USER_FOLDER, userFolder);
	}

	public void setParent(UserFolder userFolder) {
		set(PARENT_USER_FOLDER, userFolder);
	}

}
