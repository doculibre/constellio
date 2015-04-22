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

import com.constellio.app.modules.rm.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class StorageSpaceType extends ValueListItem implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "ddvStorageSpaceType";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String LINKED_SCHEMA = "linkedSchema";

	public StorageSpaceType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public StorageSpaceType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public StorageSpaceType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public StorageSpaceType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public StorageSpaceType setLinkedSchema(String folderSchema) {
		set(LINKED_SCHEMA, folderSchema);
		return this;
	}

}
