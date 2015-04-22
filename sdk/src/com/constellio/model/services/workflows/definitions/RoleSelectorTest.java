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
