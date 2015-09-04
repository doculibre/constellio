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
