package com.constellio.app.modules.tasks.model.validators;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
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

	@Mock MetadataSchemaTypes types;

	@Mock Record record;

	ValidationErrors errors = new ValidationErrors();

	@Before
	public void setUp()
			throws Exception {
		when(record.getSchemaCode()).thenReturn("userTask_default");
		when(task.getWrappedRecord()).thenReturn(record);
		when(task.getMetadataSchemaTypes()).thenReturn(types);

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
