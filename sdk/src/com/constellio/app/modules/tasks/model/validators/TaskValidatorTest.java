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
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TaskValidatorTest extends ConstellioTest {

	TaskValidator validator = new TaskValidator();

	@Mock
	MetadataSchema schema;
	@Mock
	Task task;
	@Mock ConfigProvider configProvider;

	ValidationErrors errors = new ValidationErrors();

	@Before
	public void setUp()
			throws Exception {
		when(task.getAssignee()).thenReturn("zeAssignee");
		when(task.getAssignedOn()).thenReturn(LocalDate.now());
		when(task.getAssigner()).thenReturn("zeAssigner");
	}

	@Test
	public void givenValidWhenValidateThenNoErrors()
			throws Exception {
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenNullAssigneeAndNullAssignedOnAndNullAssignerWhenValidateThenNoErrors()
			throws Exception {
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssignedOn()).thenReturn(null);
		when(task.getAssigner()).thenReturn(null);
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenNotNullAssignerAndOthersNullWhenValidateThenError()
			throws Exception {
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssignedOn()).thenReturn(null);
		when(task.getAssigner()).thenReturn("zAssigner");
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(1);
		assertThat(errors.getValidationErrors().get(0).getCode())
				.contains(TaskValidator.ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL);
	}

	@Test
	public void givenNotNullAssignerAndNotNullAssignedOnAndNullAssigneeButNotNullAssignationUsersCandidatesWhenValidateThenOk()
			throws Exception {
		when(task.getAssignedOn()).thenReturn(LocalDate.now());
		when(task.getAssigner()).thenReturn("zeAssigner");
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssigneeUsersCandidates()).thenReturn(asList("id"));
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(0);
	}

	@Test
	public void givenNotNullAssignationUsersCandidatesAndNullAssignerAndNullAssignedOnAndAssigneeThenOk()
			throws Exception {
		when(task.getAssignedOn()).thenReturn(null);
		when(task.getAssigner()).thenReturn(null);
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssigneeUsersCandidates()).thenReturn(asList("id"));
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(0);
	}

	@Test
	public void givenNotNullAssignerAndNotNullAssignedOnAndNullAssigneeButNotNullAssignationGroupsCandidatesWhenValidateThenOk()
			throws Exception {
		when(task.getAssignedOn()).thenReturn(LocalDate.now());
		when(task.getAssigner()).thenReturn("zeAssigner");
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssigneeGroupsCandidates()).thenReturn(asList("id"));
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(0);
	}

	@Test
	public void givenNotNullAssignationGroupsCandidatesAndNullAssignerAndNullAssignedOnAndAssigneeThenOk()
			throws Exception {
		when(task.getAssignedOn()).thenReturn(null);
		when(task.getAssigner()).thenReturn(null);
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssigneeGroupsCandidates()).thenReturn(asList("id"));
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(0);
	}

	@Test
	public void givenNotNullAssignedOnAndOthersNullWhenValidateThenError()
			throws Exception {
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssignedOn()).thenReturn(LocalDate.now());
		when(task.getAssigner()).thenReturn(null);
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(1);
		assertThat(errors.getValidationErrors().get(0).getCode())
				.contains(TaskValidator.ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL);
	}

	@Test
	public void givenNotNullAssigneeAndOthersNullWhenValidateThenError()
			throws Exception {
		when(task.getAssignee()).thenReturn("zAssignee");
		when(task.getAssignedOn()).thenReturn(null);
		when(task.getAssigner()).thenReturn(null);
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(1);
		assertThat(errors.getValidationErrors().get(0).getCode())
				.contains(TaskValidator.ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL);
	}

	@Test
	public void givenNullAssignerAndNotOthersNotNullValidateThenError()
			throws Exception {
		when(task.getAssignee()).thenReturn("assignee");
		when(task.getAssignedOn()).thenReturn(LocalDate.now());
		when(task.getAssigner()).thenReturn(null);
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(1);
		assertThat(errors.getValidationErrors().get(0).getCode())
				.contains(TaskValidator.ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL);
	}

	@Test
	public void givenNullAssignedOnAndOthersNotNullWhenValidateThenError()
			throws Exception {
		when(task.getAssignee()).thenReturn("assignee");
		when(task.getAssignedOn()).thenReturn(null);
		when(task.getAssigner()).thenReturn("assigner");
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(1);
		assertThat(errors.getValidationErrors().get(0).getCode())
				.contains(TaskValidator.ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL);
	}

	@Test
	public void givenNullAssigneeAndOthersNotNullWhenValidateThenError()
			throws Exception {
		when(task.getAssignee()).thenReturn(null);
		when(task.getAssignedOn()).thenReturn(LocalDate.now());
		when(task.getAssigner()).thenReturn("assigner");
		validator.validate(task, schema, configProvider, errors);
		assertThat(errors.getValidationErrors().size()).isEqualTo(1);
		assertThat(errors.getValidationErrors().get(0).getCode())
				.contains(TaskValidator.ASSIGNATION_DATE_AND_ASSIGNED_ON_ASSIGNER_SHOULD_BE_ALL_NULL_OR_ALL_NOT_NULL);
	}

}
