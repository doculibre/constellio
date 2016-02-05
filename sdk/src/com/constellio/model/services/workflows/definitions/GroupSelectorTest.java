package com.constellio.model.services.workflows.definitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.workflows.definitions.GroupSelector;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.sdk.tests.ConstellioTest;

public class GroupSelectorTest extends ConstellioTest {

	@Mock WorkflowExecution execution;

	@Before
	public void setUp()
			throws Exception {
		doReturn("thirdGroup").when(execution).getVariable("group3");
	}

	@Test
	public void givenTwoGroupsAndAVariableThenCorrectValuesReturned()
			throws Exception {
		GroupSelector selector = new GroupSelector(Arrays.asList("firstGroup", "secondGroup", "${group3}"));

		List<String> results = selector.getGroups(execution);

		assertThat(results).containsAll(Arrays.asList("firstGroup", "secondGroup", "thirdGroup"));
	}
}
