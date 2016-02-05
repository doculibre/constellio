package com.constellio.model.services.workflows.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;
import com.constellio.model.entities.workflows.trigger.ActionCompletion;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggerType;
import com.constellio.model.entities.workflows.trigger.TriggeredWorkflowDefinition;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.workflows.bpmn.WorkflowBPMNDefinitionsService;
import com.constellio.model.services.workflows.config.WorkflowsConfigManagerRuntimeException.WorkflowsConfigManagerRuntimeException_InvalidWorkflowConfiguration;
import com.constellio.model.services.workflows.config.WorkflowsConfigManagerRuntimeException.WorkflowsConfigManagerRuntimeException_NoWorkflowConfigurationForThisCollection;
import com.constellio.model.services.workflows.config.WorkflowsConfigManagerRuntimeException.WorkflowsConfigManagerRuntimeException_WorkflowConfigurationAlreadyExists;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class WorkflowsConfigManagerAcceptanceTest extends ConstellioTest {

	WorkflowsConfigManager workflowsConfigManager;
	WorkflowConfiguration workflowConfigurationRecordCreated;
	WorkflowConfiguration workflowConfigurationRecordModified;
	WorkflowConfiguration workflowConfigurationRecordDeleted;
	WorkflowConfiguration workflowConfigurationMetadataModified;
	WorkflowConfiguration anotherWorkflowConfigurationRecordCreated;
	WorkflowConfiguration workflowConfigurationManual;
	WorkflowConfiguration anotherWorkflowConfigurationManual;
	WorkflowConfiguration invalidWorkflowConfigurationMetadataModified;

	WorkflowConfiguration retrievedworkflowConfigurationRecordCreated;
	WorkflowConfiguration retrievedworkflowConfigurationRecordModified;
	WorkflowConfiguration retrievedworkflowConfigurationRecordDeleted;
	WorkflowConfiguration retrievedworkflowConfigurationMetadataModified;

	ConfigManager configManager;
	MetadataSchemasManager schemasManager;
	@Mock WorkflowBPMNDefinitionsService workflowBPMNDefinitionsService;
	@Mock WorkflowDefinition workflowDefinition1;
	@Mock WorkflowDefinition workflowDefinition2;
	@Mock WorkflowDefinition workflowDefinition3;
	@Mock WorkflowDefinition workflowDefinition4;

	String bpmnFileName1;
	String bpmnFileName2;
	String bpmnFileName3;
	String bpmnFileName4;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection());

		Map<String, String> mapping1 = new HashMap<>();
		mapping1.put("key1", "value1");
		Map<String, String> mapping2 = new HashMap<>();
		mapping2.put("key2", "value2");
		Map<String, String> mapping3 = new HashMap<>();
		mapping3.put("key3", "value3");
		Map<String, String> mapping4 = new HashMap<>();
		mapping4.put("key4", "value4");
		bpmnFileName1 = "bpmnFileName1";
		bpmnFileName2 = "bpmnFileName2";
		bpmnFileName3 = "bpmnFileName3";
		bpmnFileName4 = "bpmnFileName4";

		when(workflowBPMNDefinitionsService
				.getWorkflowDefinition(eq(bpmnFileName1), eq(mapping1), any(WorkflowConfiguration.class))).thenReturn(
				workflowDefinition1);
		when(workflowBPMNDefinitionsService.getWorkflowDefinition(eq(bpmnFileName2), eq(mapping2), any(
				WorkflowConfiguration.class))).thenReturn(workflowDefinition2);
		when(workflowBPMNDefinitionsService.getWorkflowDefinition(eq(bpmnFileName3), eq(mapping3), any(
				WorkflowConfiguration.class))).thenReturn(workflowDefinition3);
		when(workflowBPMNDefinitionsService.getWorkflowDefinition(eq(bpmnFileName4), eq(mapping4), any(
				WorkflowConfiguration.class))).thenReturn(workflowDefinition4);

		workflowsConfigManager = getModelLayerFactory().getWorkflowsConfigManager();
		//				new WorkflowsConfigManager(getDataLayerFactory().getConfigManager(),
		//						getModelLayerFactory().getCollectionsListManager(), workflowBPMNDefinitionsService);
		workflowsConfigManager.workflowBPMNDefinitionsService = workflowBPMNDefinitionsService;

		createWorkflows();
		addWorkflows();
	}

	@Test
	public void whenAddWorkflowsThenCanTheysCanBeRetrieved()
			throws Exception {

		retrievedworkflowConfigurationRecordCreated = workflowsConfigManager.getWorkflows(zeCollection).get(0);
		retrievedworkflowConfigurationRecordModified = workflowsConfigManager.getWorkflows(zeCollection).get(1);
		retrievedworkflowConfigurationRecordDeleted = workflowsConfigManager.getWorkflows(zeCollection).get(2);
		retrievedworkflowConfigurationMetadataModified = workflowsConfigManager.getWorkflows(zeCollection).get(3);
		assertThatWorkflowConfigurationsAreEquals(retrievedworkflowConfigurationRecordCreated,
				workflowConfigurationRecordCreated);
		assertThatWorkflowConfigurationsAreEquals(retrievedworkflowConfigurationRecordModified,
				workflowConfigurationRecordModified);
		assertThatWorkflowConfigurationsAreEquals(retrievedworkflowConfigurationRecordDeleted,
				workflowConfigurationRecordDeleted);
		assertThatWorkflowConfigurationsAreEquals(retrievedworkflowConfigurationMetadataModified,
				workflowConfigurationMetadataModified);
	}

	@Test
	public void givenSameWorkflowWhenAddUpdateWorkflowsThenUpdateIt()
			throws Exception {

		workflowConfigurationRecordCreated.setBpmnFilename("updated");
		workflowsConfigManager.addUpdateWorkflow(workflowConfigurationRecordCreated);

		assertThat(workflowsConfigManager.getWorkflows(zeCollection)).hasSize(4);
		retrievedworkflowConfigurationRecordCreated = workflowsConfigManager.getWorkflows(zeCollection).get(3);
		assertThatWorkflowConfigurationsAreEquals(retrievedworkflowConfigurationRecordCreated,
				workflowConfigurationRecordCreated);
		assertThat(retrievedworkflowConfigurationRecordCreated.getBpmnFilename()).isEqualTo("updated");
	}

	@Test(expected = WorkflowsConfigManagerRuntimeException_WorkflowConfigurationAlreadyExists.class)
	public void givenNewWorkflowWithSameConfigurationWithManualWhenAddUpdateWorkflowsThenException()
			throws Exception {

		WorkflowConfiguration workflowConfiguration = createAnotherWorkflowRecordCreated();
		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration);

		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration);
	}

	@Test(expected = WorkflowsConfigManagerRuntimeException_WorkflowConfigurationAlreadyExists.class)
	public void givenNewWorkflowWithSameConfigurationWhenAddUpdateWorkflowsThenException()
			throws Exception {

		WorkflowConfiguration workflowConfiguration = createAnotherWorkflowRecordCreated();
		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration);

		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration);
	}

	@Test
	public void givenNewWorkflowWithSameManualConfigurationWhenAddUpdateWorkflowsThenOk()
			throws Exception {

		givenManualWorflows();

		assertThat(workflowsConfigManager.getWorkflows(zeCollection)).hasSize(7);
	}

	@Test(expected = WorkflowsConfigManagerRuntimeException_InvalidWorkflowConfiguration.class)
	public void whenAddInvalidWorkflowThenException()
			throws Exception {

		workflowsConfigManager.addUpdateWorkflow(invalidWorkflowConfigurationMetadataModified);
	}

	@Test
	public void givenWorflowsWhenGetWorkflowDefinitionForRecordCreatedThenReturnIt()
			throws Exception {

		TriggeredWorkflowDefinition triggeredWorkflowDefinition = workflowsConfigManager.getWorkflowDefinitionForCreating(
				zeCollection, "zeSchemaType_schema1");

		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggerType()).isEqualTo(TriggerType.RECORD_CREATED);
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode()).isEqualTo(
				triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode());
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
		assertThat(triggeredWorkflowDefinition.getWorkflowDefinition()).isEqualTo(workflowDefinition1);
	}

	@Test
	public void givenWorflowsWhenGetWorkflowDefinitionForRecordModifiedThenReturnIt()
			throws Exception {

		TriggeredWorkflowDefinition triggeredWorkflowDefinition = workflowsConfigManager.getWorkflowDefinitionForModifying(
				zeCollection, "zeSchemaType_schema1");

		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggerType()).isEqualTo(TriggerType.RECORD_MODIFIED);
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode()).isEqualTo(
				triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode());
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
		assertThat(triggeredWorkflowDefinition.getWorkflowDefinition()).isEqualTo(workflowDefinition2);
	}

	@Test
	public void givenWorflowsWhenGetWorkflowDefinitionForRecordDeletedThenReturnIt()
			throws Exception {

		TriggeredWorkflowDefinition triggeredWorkflowDefinition = workflowsConfigManager.getWorkflowDefinitionForDeleting(
				zeCollection, "zeSchemaType_schema1");

		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggerType()).isEqualTo(TriggerType.RECORD_DELETED);
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode()).isEqualTo(
				triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode());
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
		assertThat(triggeredWorkflowDefinition.getWorkflowDefinition()).isEqualTo(workflowDefinition3);
	}

	@Test
	public void givenWorflowsWhenGetWorkflowDefinitionForMetadataModifiedThenReturnIt()
			throws Exception {

		TriggeredWorkflowDefinition triggeredWorkflowDefinition = workflowsConfigManager.getWorkflowDefinitionForModifiedMetadata(
				zeCollection, "title4");

		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggerType()).isEqualTo(TriggerType.METADATA_MODIFIED);
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode()).isEqualTo("title4");
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
		assertThat(triggeredWorkflowDefinition.getWorkflowDefinition()).isEqualTo(workflowDefinition4);
	}

	@Test(expected = WorkflowsConfigManagerRuntimeException_NoWorkflowConfigurationForThisCollection.class)
	public void givenWorflowsWhenGetWorkflowDefinitionForMetadataModifiedInAnotherSiteThenDoNotReturnIt()
			throws Exception {

		TriggeredWorkflowDefinition triggeredWorkflowDefinition = workflowsConfigManager.getWorkflowDefinitionForModifiedMetadata(
				"site2", "title4");

		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggerType()).isEqualTo(TriggerType.METADATA_MODIFIED);
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredMetadataCode()).isEqualTo("title4");
		assertThat(triggeredWorkflowDefinition.getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
		assertThat(triggeredWorkflowDefinition.getWorkflowDefinition()).isEqualTo(workflowDefinition4);
	}

	@Test
	public void givenWorflowsWhenGetManualWorkflowDefinitionThenReturnThem()
			throws Exception {

		givenManualWorflows();

		List<TriggeredWorkflowDefinition> triggeredWorkflowDefinitions = workflowsConfigManager
				.getManualWorflowDefinitionsFor(zeCollection, "zeSchemaType_schema1");

		assertThat(triggeredWorkflowDefinitions).hasSize(3);
		assertThat(triggeredWorkflowDefinitions.get(0).getTrigger().getTriggerType()).isEqualTo(TriggerType.MANUAL);
		assertThat(triggeredWorkflowDefinitions.get(0).getTrigger().getTriggeredMetadataCode()).isEqualTo(null);
		assertThat(triggeredWorkflowDefinitions.get(0).getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
		assertThat(triggeredWorkflowDefinitions.get(1).getTrigger().getTriggerType()).isEqualTo(TriggerType.MANUAL);
		assertThat(triggeredWorkflowDefinitions.get(1).getTrigger().getTriggeredMetadataCode()).isEqualTo(null);
		assertThat(triggeredWorkflowDefinitions.get(1).getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
		assertThat(triggeredWorkflowDefinitions.get(2).getTrigger().getTriggerType()).isEqualTo(TriggerType.MANUAL);
		assertThat(triggeredWorkflowDefinitions.get(2).getTrigger().getTriggeredMetadataCode()).isEqualTo(null);
		assertThat(triggeredWorkflowDefinitions.get(2).getTrigger().getTriggeredSchemaCode()).isEqualTo("zeSchemaType_schema1");
	}

	private void givenManualWorflows() {
		WorkflowConfiguration workflowConfiguration6 = createAnotherWorkflowManual(6);
		WorkflowConfiguration workflowConfiguration7 = createAnotherWorkflowManual(7);
		WorkflowConfiguration workflowConfiguration8 = createAnotherWorkflowManual(8);
		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration6);
		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration7);
		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration8);
	}

	//

	void assertThatWorkflowConfigurationsAreEquals(WorkflowConfiguration retrievedWorkflowConfiguration,
			WorkflowConfiguration workflowConfiguration) {
		assertThat(retrievedWorkflowConfiguration.getId()).isEqualTo(workflowConfiguration.getId());
		assertThat(retrievedWorkflowConfiguration.getCollection()).isEqualTo(workflowConfiguration.getCollection());
		assertThat(retrievedWorkflowConfiguration.isEnabled()).isEqualTo(workflowConfiguration.isEnabled());
		assertThat(retrievedWorkflowConfiguration.getMapping()).isEqualTo(workflowConfiguration.getMapping());
		Trigger trigger = workflowConfiguration.getTriggers().get(0);
		List<Trigger> retrievedTriggers = retrievedWorkflowConfiguration.getTriggers();
		for (Trigger retrievedTrigger : retrievedTriggers) {
			assertThat(retrievedTrigger.getTriggeredMetadataCode()).isEqualTo(trigger.getTriggeredMetadataCode());
			assertThat(retrievedTrigger.getTriggeredSchemaCode()).isEqualTo(trigger.getTriggeredSchemaCode());
			assertThat(retrievedTrigger.getTriggerType()).isEqualTo(trigger.getTriggerType());
			assertThat(retrievedTrigger.getActionCompletion()).isEqualTo(trigger.getActionCompletion());
		}
		assertThat(retrievedWorkflowConfiguration.getBpmnFilename()).isEqualTo(workflowConfiguration.getBpmnFilename());
	}

	private void createWorkflows() {
		workflowConfigurationRecordCreated = createWorkflowRecordCreated();
		workflowConfigurationRecordModified = createWorkflowRecordModified();
		workflowConfigurationRecordDeleted = createWorkflowRecordDeleted();
		workflowConfigurationMetadataModified = createWorkflowMetadataModified();
		invalidWorkflowConfigurationMetadataModified = createInvalidWorkflowMetadataModified();
	}

	private void addWorkflows() {
		workflowsConfigManager.addUpdateWorkflow(workflowConfigurationRecordCreated);
		workflowsConfigManager.addUpdateWorkflow(workflowConfigurationRecordModified);
		workflowsConfigManager.addUpdateWorkflow(workflowConfigurationRecordDeleted);
		workflowsConfigManager.addUpdateWorkflow(workflowConfigurationMetadataModified);
	}

	WorkflowConfiguration createWorkflowRecordCreated() {
		String id = "id1";
		Trigger trigger = new Trigger(TriggerType.RECORD_CREATED, "zeSchemaType_schema1", null, ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key1", "value1");
		bpmnFileName1 = "bpmnFileName1";

		workflowConfigurationRecordCreated = new WorkflowConfiguration(id, zeCollection, true, mapping, triggers, bpmnFileName1);
		return workflowConfigurationRecordCreated;
	}

	WorkflowConfiguration createWorkflowRecordModified() {
		String id = "id2";
		Trigger trigger = new Trigger(TriggerType.RECORD_MODIFIED, "zeSchemaType_schema1", null, ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key2", "value2");
		bpmnFileName2 = "bpmnFileName2";

		workflowConfigurationRecordModified = new WorkflowConfiguration(id, zeCollection, true, mapping, triggers, bpmnFileName2);
		return workflowConfigurationRecordModified;
	}

	WorkflowConfiguration createWorkflowRecordDeleted() {
		String id = "id3";
		Trigger trigger = new Trigger(TriggerType.RECORD_DELETED, "zeSchemaType_schema1", null, ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key3", "value3");
		bpmnFileName3 = "bpmnFileName3";

		workflowConfigurationRecordDeleted = new WorkflowConfiguration(id, zeCollection, true, mapping, triggers, bpmnFileName3);
		return workflowConfigurationRecordDeleted;
	}

	WorkflowConfiguration createWorkflowMetadataModified() {
		String id = "id4";
		Trigger trigger = new Trigger(TriggerType.METADATA_MODIFIED, "zeSchemaType_schema1", "title4", ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key4", "value4");
		bpmnFileName4 = "bpmnFileName4";

		workflowConfigurationMetadataModified = new WorkflowConfiguration(id, zeCollection, true, mapping, triggers,
				bpmnFileName4);
		return workflowConfigurationMetadataModified;
	}

	WorkflowConfiguration createAnotherWorkflowRecordCreated() {
		String id = "id5";
		Trigger trigger = new Trigger(TriggerType.RECORD_CREATED, "zeSchemaType_schema1", null, ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key1", "value1");
		bpmnFileName1 = "bpmnFileName1";

		anotherWorkflowConfigurationRecordCreated = new WorkflowConfiguration(id, zeCollection, true, mapping, triggers,
				bpmnFileName1);
		return anotherWorkflowConfigurationRecordCreated;
	}

	WorkflowConfiguration createAnotherWorkflowManual(int id) {
		String idStr = "id" + id;
		Trigger trigger = new Trigger(TriggerType.MANUAL, "zeSchemaType_schema1", null, ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key" + id, "value" + id);
		bpmnFileName1 = "bpmnFileName" + id;

		workflowConfigurationRecordCreated = new WorkflowConfiguration(idStr, zeCollection, true, mapping, triggers,
				bpmnFileName1);
		return workflowConfigurationRecordCreated;
	}

	WorkflowConfiguration createInvalidWorkflowMetadataModified() {
		String id = "id";
		Trigger trigger = new Trigger(TriggerType.METADATA_MODIFIED, "zeSchemaType_schema1", null, ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key", "value");
		String bpmnFileName = "bpmnFileName";

		invalidWorkflowConfigurationMetadataModified = new WorkflowConfiguration(id, zeCollection, true, mapping, triggers,
				bpmnFileName);
		return invalidWorkflowConfigurationMetadataModified;
	}
}
