package com.constellio.app.modules.tasks.model.validators;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.STANDBY;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordValidatorParams;

public class TaskStatusValidator implements RecordValidator {
	private static final String INVALID_CODE__TYPE = "invalidCodeTypeAssociation";

	@Override
	public void validate(RecordValidatorParams params) {
		TaskStatus taskStatus = new TaskStatus(params.getValidatedRecord(), params.getTypes());
		validate(taskStatus, params.getValidationErrors());
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
