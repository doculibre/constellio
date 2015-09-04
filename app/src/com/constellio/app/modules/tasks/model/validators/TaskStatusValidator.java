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
package com.constellio.app.modules.tasks.model.validators;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.STANDBY;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class TaskStatusValidator implements RecordValidator {
	private static final String INVALID_CODE__TYPE = "invalidCodeTypeAssociation";

	@Override
	public void validate(Record record, MetadataSchemaTypes types, MetadataSchema schema, ConfigProvider configProvider,
			ValidationErrors validationErrors) {
		TaskStatus taskStatus = new TaskStatus(record, types);
		validate(taskStatus, validationErrors);
	}

	void validate(TaskStatus taskStatus, ValidationErrors validationErrors) {
		TaskStatusType statusType = taskStatus.getStatusType();
		if (statusType != null) {
			if (statusType == CLOSED) {
				String statusCode = taskStatus.getCode();
				if ((statusCode == null) || !statusCode.equals(CLOSED_CODE)) {
					validationErrors.add(getClass(), $(INVALID_CODE__TYPE) + " (" + CLOSED_CODE + ", " + statusCode + ")");
				}
			} else if (statusType == STANDBY) {
				String statusCode = taskStatus.getCode();
				if ((statusCode == null) || !statusCode.equals(STANDBY_CODE)) {
					validationErrors.add(getClass(), $(INVALID_CODE__TYPE) + " (" + STANDBY_CODE + ", " + statusCode + ")");
				}
			}
		}
	}
}
