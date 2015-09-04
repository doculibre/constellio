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

import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class TaskStatus extends ValueListItem {
	public static final String SCHEMA_TYPE = "ddvTaskStatus";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String STATUS_TYPE = "statusType";
	public static final String CLOSED_CODE = "X";
	public static final String STANDBY_CODE = "O";

	public TaskStatus(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public TaskStatus setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public TaskStatus setCode(String code) {
		super.setCode(code);
		return this;
	}

	public boolean isFinished() {
		return getStatusType().isFinished();
	}

	public boolean isStarted() {
		return getStatusType().isAfterStandby();
	}

	public boolean isAfterFinished() {
		return getStatusType().isAfterFinished();
	}

	public boolean isBeforeFinished() {
		return getStatusType().isBeforeFinished();
	}

	public TaskStatusType getStatusType() {
		return get(STATUS_TYPE);
	}

	public TaskStatus setStatusType(TaskStatusType statusType) {
		set(STATUS_TYPE, statusType);
		return this;
	}

}
