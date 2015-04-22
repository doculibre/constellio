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
package com.constellio.app.modules.rm.wrappers;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Document extends RecordWrapper {

	public static final String SCHEMA_TYPE = "document";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String FOLDER = "folder";

	public static final String KEYWORDS = "keywords";

	public static final String DESCRIPTION = "description";

	public static final String CONTENT = "content";
	public static final String TYPE = "type";

	public static final String COMMENTS = "comments";

	public static final String FOLDER_CATEGORY = Folder.CATEGORY;
	public static final String FOLDER_ADMINISTRATIVE_UNIT = Folder.ADMINISTRATIVE_UNIT;
	public static final String FOLDER_FILING_SPACE = Folder.FILING_SPACE;
	public static final String FOLDER_RETENTION_RULE = Folder.RETENTION_RULE;
	public static final String FOLDER_ARCHIVISTIC_STATUS = Folder.ARCHIVISTIC_STATUS;
	public static final String FOLDER_ACTUAL_DEPOSIT_DATE = Folder.ACTUAL_DEPOSIT_DATE;
	public static final String FOLDER_ACTUAL_DESTRUCTION_DATE = Folder.ACTUAL_DESTRUCTION_DATE;
	public static final String FOLDER_ACTUAL_TRANSFER_DATE = Folder.ACTUAL_TRANSFER_DATE;
	public static final String FOLDER_EXPECTED_DEPOSIT_DATE = Folder.EXPECTED_DEPOSIT_DATE;
	public static final String FOLDER_EXPECTED_DESTRUCTION_DATE = Folder.EXPECTED_DESTRUCTION_DATE;
	public static final String FOLDER_EXPECTED_TRANSFER_DATE = Folder.EXPECTED_TRANSFER_DATE;
	public static final String FOLDER_OPENING_DATE = Folder.OPENING_DATE;
	public static final String FOLDER_CLOSING_DATE = Folder.CLOSING_DATE;

	public Document(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Document setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getFolder() {
		return get(FOLDER);
	}

	public Document setFolder(String folder) {
		set(FOLDER, folder);
		return this;
	}

	public Document setFolder(Folder folder) {
		set(FOLDER, folder);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public Document setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public String getKeywords() {
		return get(KEYWORDS);
	}

	public Document setKeywords(String keywords) {
		set(KEYWORDS, keywords);
		return this;
	}

	public Content getContent() {
		return get(CONTENT);
	}

	public Document setContent(Content content) {
		set(CONTENT, content);
		return this;
	}

	public String getType() {
		return get(TYPE);
	}

	public Document setType(DocumentType type) {
		set(TYPE, type);
		return this;
	}

	public Document setType(Record type) {
		set(TYPE, type);
		return this;
	}

	public Document setType(String type) {
		set(TYPE, type);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public Document setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}
}
