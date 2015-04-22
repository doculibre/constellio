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
package com.constellio.app.modules.rm.ui.entities;

import static com.constellio.app.modules.rm.wrappers.Document.CONTENT;
import static com.constellio.app.modules.rm.wrappers.Document.DESCRIPTION;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER;
import static com.constellio.app.modules.rm.wrappers.Document.KEYWORDS;
import static com.constellio.app.modules.rm.wrappers.Document.TYPE;

import java.util.List;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;

public class DocumentVO extends RecordVO {

	public DocumentVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}
	
	public DocumentVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public String getFolder() {
		return get(FOLDER);
	}

	public void setFolder(String folder) {
		set(FOLDER, folder);
	}

	public void setFolder(FolderVO folder) {
		set(FOLDER, folder);
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public void setDescription(String description) {
		set(DESCRIPTION, description);
	}

	public String getKeywords() {
		return get(KEYWORDS);
	}

	public void setKeywords(String keywords) {
		set(KEYWORDS, keywords);
	}

	public ContentVersionVO getContent() {
		return get(CONTENT);
	}

	public void setContent(ContentVersionVO content) {
		set(CONTENT, content);
	}

	public String getType() {
		return get(TYPE);
	}

	public void setType(RecordVO type) {
		set(TYPE, type);
	}

	public void setType(String type) {
		set(TYPE, type);
	}

}
