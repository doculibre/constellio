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
