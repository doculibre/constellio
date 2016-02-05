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
