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
