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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.workflows.definitions.BPMNProperty;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.sdk.tests.ConstellioTest;

public class BPMNPropertyTest extends ConstellioTest {

	@Mock WorkflowExecution execution;

	private String code1 = "first.variable";
	private String code2 = "second.variable";
	private String code3 = "third.variable";
	private String value1 = "firstValue";
	private String value2 = "secondValue";
	private String value3 = "thirdValue";

	@Before
	public void setUp()
			throws Exception {
		doReturn(value1).when(execution).getVariable(code1);
		doReturn(value2).when(execution).getVariable(code2);
		doReturn(value3).when(execution).getVariable(code3);
	}

	@Test
	public void givenExpressionWithThreeVariablesThenAllVariablesReplaced()
			throws Exception {
		BPMNProperty property = new BPMNProperty("anId",
				"The ${first.variable} comes before the ${second.variable}, which comes before the ${third.variable}.", null);

		String parsedExpression = property.getParsedExpression(execution);

		assertThat(parsedExpression).isEqualTo("The firstValue comes before the secondValue, which comes before the thirdValue.");
	}
}
