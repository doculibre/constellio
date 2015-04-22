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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.workflows.definitions.WorkflowCondition;
import com.constellio.model.entities.workflows.definitions.WorkflowRouting;
import com.constellio.model.entities.workflows.definitions.WorkflowRoutingDestination;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.sdk.tests.ConstellioTest;

public class WorkflowRoutingTest extends ConstellioTest {

	@Mock WorkflowExecution execution;

	WorkflowRoutingDestination destination1, destination2, destination3;
	List<WorkflowRoutingDestination> destinations;

	@Before
	public void setUp()
			throws Exception {
		destination1 = new WorkflowRoutingDestination(falseCondition(), "destination1", "start");
		destination2 = new WorkflowRoutingDestination(trueCondition(), "destination2", "destination1");
		destination3 = new WorkflowRoutingDestination(falseCondition(), "destination3", "destination2");

		destinations = Arrays.asList(destination1, destination2, destination3);
	}

	@Test
	public void whenGettingDestinationThenRightDestinationReturned()
			throws Exception {
		WorkflowRouting routing = new WorkflowRouting("zeRouting", destinations);

		String returnedDestination = routing.getDestination(execution);
		assertThat(returnedDestination).isEqualTo("destination2");
	}

	@Test
	public void givenNoTrueConditionwhenGettingDestinationThenRightDestinationReturned()
			throws Exception {
		WorkflowRouting routing = new WorkflowRouting("zeRouting", destinations);

		String returnedDestination = routing.getDestination(execution);
		assertThat(returnedDestination).isEqualTo("destination2");
	}

	private WorkflowCondition falseCondition() {
		return new WorkflowCondition() {
			@Override
			public boolean isTrue(WorkflowExecution execution) {
				return false;
			}
		};
	}

	private WorkflowCondition trueCondition() {
		return new WorkflowCondition() {
			@Override
			public boolean isTrue(WorkflowExecution execution) {
				return true;
			}
		};
	}
}
