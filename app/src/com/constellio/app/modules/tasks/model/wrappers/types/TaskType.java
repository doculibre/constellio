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
package com.constellio.app.modules.tasks.model.wrappers.types;

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class TaskType extends ValueListItem implements SchemaLinkingType {
	public static final String SCHEMA_TYPE = "ddvTaskType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String LINKED_SCHEMA = "linkedSchema";

	public TaskType(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public TaskType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public TaskType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public TaskType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	@Override
	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public TaskType setLinkedSchema(String linkedSchema) {
		set(LINKED_SCHEMA, linkedSchema);
		return this;
	}
}
