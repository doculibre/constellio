package com.constellio.model.services.workflows.definitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.workflows.definitions.RoleSelector;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.sdk.tests.ConstellioTest;

public class RoleSelectorTest extends ConstellioTest {

	@Mock WorkflowExecution execution;

	@Before
	public void setUp()
			throws Exception {
		doReturn("thirdRole").when(execution).getVariable("role3");
	}

	@Test
	public void givenTwoRolesAndAVariableThenCorrectValuesReturned()
			throws Exception {
		RoleSelector selector = new RoleSelector(Arrays.asList("firstRole", "secondRole", "${role3}"));

		List<String> results = selector.getRoles(execution);

		assertThat(results).containsAll(Arrays.asList("firstRole", "secondRole", "thirdRole"));
	}
}
