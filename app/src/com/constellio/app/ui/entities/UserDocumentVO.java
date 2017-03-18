package com.constellio.app.ui.entities;

import static com.constellio.model.entities.records.wrappers.UserDocument.CONTENT;
import static com.constellio.model.entities.records.wrappers.UserDocument.FOLDER;
import static com.constellio.model.entities.records.wrappers.UserDocument.FORM_CREATED_ON;
import static com.constellio.model.entities.records.wrappers.UserDocument.FORM_MODIFIED_ON;
import static com.constellio.model.entities.records.wrappers.UserDocument.USER_FOLDER;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;

public class UserDocumentVO extends RecordVO {
	
	public UserDocumentVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
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
	
	public ContentVersionVO getContent() {
		return get(CONTENT);
	}
	
	public void setContent(ContentVersionVO contentVersionVO) {
		set(CONTENT, contentVersionVO);
	}
	
	public String getFolder() {
		return super.get(FOLDER);
	}
	
	public void setFolder(String folder) {
		super.set(FOLDER, folder);
	}
	
	public String getUserFolder() {
		return super.get(USER_FOLDER);
	}
	
	public void setUserFolder(String folder) {
		super.set(USER_FOLDER, folder);
	}
	
	public String getFileName() {
		ContentVersionVO contentVersionVO = getContent();
		return contentVersionVO != null ? contentVersionVO.getFileName() : null;
	}
	
	public String getMimeType() {
		ContentVersionVO contentVersionVO = getContent();
		return contentVersionVO != null ? contentVersionVO.getMimeType() : null;
	}
	
	public long getLength() {
		ContentVersionVO contentVersionVO = getContent();
		return contentVersionVO != null ? contentVersionVO.getLength() : 0;
	}

	@Override
	public String toString() {
		String fileName = getFileName();
		long length = getLength();
		return fileName + " (" + FileUtils.byteCountToDisplaySize(length) + ")";
	}

}
