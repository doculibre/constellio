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
package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class FolderType extends ValueListItem implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "ddvFolderType";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String LINKED_SCHEMA = "linkedSchema";

	public FolderType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public FolderType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public FolderType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public FolderType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public FolderType setLinkedSchema(String folderSchema) {
		set(LINKED_SCHEMA, folderSchema);
		return this;
	}

}
