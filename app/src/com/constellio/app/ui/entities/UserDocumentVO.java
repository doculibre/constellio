package com.constellio.app.ui.entities;

import java.util.List;

import org.apache.commons.io.FileUtils;

import com.constellio.model.entities.records.wrappers.UserDocument;

public class UserDocumentVO extends RecordVO {
	
	public UserDocumentVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}
	
	public final ContentVersionVO getContent() {
		return super.get(UserDocument.CONTENT);
	}
	
	public final void setContent(ContentVersionVO contentVersionVO) {
		super.set(UserDocument.CONTENT, contentVersionVO);
	}
	
	public String getFolder() {
		return super.get(UserDocument.FOLDER);
	}
	
	public void setFolder(String folder) {
		super.set(UserDocument.FOLDER, folder);
	}
	
	public final String getFileName() {
		ContentVersionVO contentVersionVO = getContent();
		return contentVersionVO != null ? contentVersionVO.getFileName() : null;
	}
	
	public final String getMimeType() {
		ContentVersionVO contentVersionVO = getContent();
		return contentVersionVO != null ? contentVersionVO.getMimeType() : null;
	}
	
	public final long getLength() {
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
