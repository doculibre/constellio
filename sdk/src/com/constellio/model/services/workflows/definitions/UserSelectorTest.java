package com.constellio.model.services.workflows.definitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.workflows.definitions.UserSelector;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.sdk.tests.ConstellioTest;

public class UserSelectorTest extends ConstellioTest {

	@Mock WorkflowExecution execution;

	@Before
	public void setUp()
			throws Exception {
		doReturn("thirdUser").when(execution).getVariable("user3");
	}

	@Test
	public void givenTwoUsersAndAVariableThenCorrectValuesReturned()
			throws Exception {
		UserSelector selector = new UserSelector(Arrays.asList("firstUser", "secondUser", "${user3}"));

		List<String> results = selector.getUsers(execution);

		assertThat(results).containsAll(Arrays.asList("firstUser", "secondUser", "thirdUser"));
	}
}
