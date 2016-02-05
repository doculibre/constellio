package com.constellio.app.modules.tasks.model.validators;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class TaskValidator implements RecordValidator {
	public static final String ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL = "assignationDateAndAssignedOnAndAssignerShouldBeAllNullOrAllNotNull";
	public static final String DUE_DATE_MUST_BE_LESSER_OR_EQUAL_THAN_PARENT_DUE_DATE = "dueDateMustBeLesserOrEqualThanParentDueDate";

	@Override
	public void validate(Record record, MetadataSchemaTypes types, MetadataSchema schema, ConfigProvider configProvider,
			ValidationErrors validationErrors) {
		Task task = new Task(record, types);
		validate(task, schema, configProvider, validationErrors);
	}

	public void validate(Task task, MetadataSchema schema, ConfigProvider configProvider, ValidationErrors validationErrors) {
		boolean taskAssignationIsNull = taskAssignationIsNull(task);
		if (task.getAssigner() == null || task.getAssignedOn() == null || taskAssignationIsNull) {
			if (!(task.getAssigner() == null && task.getAssignedOn() == null && task.getAssignee() == null)) {
				validationErrors.add(getClass(), ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL);
			}
		}

		if (task.getDueDate() != null && task.getParentTaskDueDate() != null
				&& task.getDueDate().isAfter(task.getParentTaskDueDate())) {
			validationErrors.add(getClass(), DUE_DATE_MUST_BE_LESSER_OR_EQUAL_THAN_PARENT_DUE_DATE);
		}
	}

	private boolean taskAssignationIsNull(Task task) {
		return task.getAssignee() == null && (task.getAssigneeGroupsCandidates() == null || task.getAssigneeGroupsCandidates()
				.isEmpty())
				&& (task.getAssigneeUsersCandidates() == null || task.getAssigneeUsersCandidates().isEmpty());
	}
}
