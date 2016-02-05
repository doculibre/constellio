package com.constellio.app.modules.tasks.model.validators;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

public class TaskStatusValidatorTest extends ConstellioTest {
	TaskStatusValidator validator = new TaskStatusValidator();
	@Mock
	TaskStatus taskStatus;
	ValidationErrors errors = new ValidationErrors();

	@Before
	public void setUp()
			throws Throwable {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void givenTaskStatusWithCode1AndCloseTypeWhenValidateThenError()
			throws Exception {
		when(taskStatus.getCode()).thenReturn("code1");
		when(taskStatus.getStatusType()).thenReturn(CLOSED);
		validator.validate(taskStatus, errors);
		assertThat(errors.getValidationErrors()).isNotEmpty();
	}

	@Test
	public void givenTaskStatusWithCodeXAndCloseTypeWhenValidateThenOk()
			throws Exception {
		when(taskStatus.getCode()).thenReturn(CLOSED_CODE);
		when(taskStatus.getStatusType()).thenReturn(CLOSED);
		validator.validate(taskStatus, errors);
		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenTaskStatusWithCode1AndNullTypeWhenValidateThenOk()
			throws Exception {
		when(taskStatus.getCode()).thenReturn("code1");
		when(taskStatus.getStatusType()).thenReturn(null);
		validator.validate(taskStatus, errors);
		assertThat(errors.getValidationErrors()).isEmpty();
	}

	//Pas besoin car status code X exist donc on ne pourra l ajouter @Test
	public void givenTaskStatusWithCodeXAndNullTypeWhenValidateThenError()
			throws Exception {
		when(taskStatus.getCode()).thenReturn(CLOSED_CODE);
		when(taskStatus.getStatusType()).thenReturn(null);
		validator.validate(taskStatus, errors);
		assertThat(errors.getValidationErrors()).isNotEmpty();
	}

}
