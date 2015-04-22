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
package com.constellio.model.services.workflows.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggerType;
import com.constellio.model.entities.workflows.trigger.TriggeredWorkflowDefinition;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.workflows.bpmn.WorkflowBPMNDefinitionsService;
import com.constellio.model.services.workflows.config.WorkflowsConfigManagerRuntimeException.WorkflowsConfigManagerRuntimeException_InvalidWorkflowConfiguration;
import com.constellio.model.services.workflows.config.WorkflowsConfigManagerRuntimeException.WorkflowsConfigManagerRuntimeException_NoWorkflowConfigurationForThisCollection;
import com.constellio.model.services.workflows.config.WorkflowsConfigManagerRuntimeException.WorkflowsConfigManagerRuntimeException_WorkflowConfigurationAlreadyExists;
import com.constellio.model.services.workflows.config.xml.WorkflowsConfigReader;
import com.constellio.model.services.workflows.config.xml.WorkflowsConfigWriter;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;

public class WorkflowsConfigManager
		implements StatefulService, OneXMLConfigPerCollectionManagerListener<List<WorkflowConfiguration>> {

	private static String WORKFLOWS_CONFIG = "/workflows/config.xml";
	WorkflowBPMNDefinitionsService workflowBPMNDefinitionsService;
	private OneXMLConfigPerCollectionManager<List<WorkflowConfiguration>> oneXMLConfigPerCollectionManager;
	private Map<String, WorkflowDefinition> workflowDefinitions;
	private ConfigManager configManager;
	private CollectionsListManager collectionsListManager;

	public WorkflowsConfigManager(ConfigManager configManager, CollectionsListManager collectionsListManager,
			WorkflowBPMNDefinitionsService workflowBPMNDefinitionsService) {
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
		this.workflowDefinitions = new HashMap<String, WorkflowDefinition>();
		this.workflowBPMNDefinitionsService = workflowBPMNDefinitionsService;

	}

	@Override
	public void initialize() {
		this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				WORKFLOWS_CONFIG, xmlConfigReader(), this);
	}

	public TriggeredWorkflowDefinition getWorkflowDefinitionForCreating(String collection, String schemaCode) {
		return getTriggeredWorflowDefinitionFor(collection, schemaCode, TriggerType.RECORD_CREATED);
	}

	public TriggeredWorkflowDefinition getWorkflowDefinitionForModifying(String collection, String schemaCode) {
		return getTriggeredWorflowDefinitionFor(collection, schemaCode, TriggerType.RECORD_MODIFIED);
	}

	public TriggeredWorkflowDefinition getWorkflowDefinitionForDeleting(String collection, String schemaCode) {
		return getTriggeredWorflowDefinitionFor(collection, schemaCode, TriggerType.RECORD_DELETED);
	}

	public TriggeredWorkflowDefinition getWorkflowDefinitionForModifiedMetadata(String collection, String metadataCode) {
		return getTriggeredWorflowDefinitionFor(collection, metadataCode, TriggerType.METADATA_MODIFIED);
	}

	public List<TriggeredWorkflowDefinition> getManualWorflowDefinitionsFor(String collection, String schemaCode) {
		return getManualTriggeredWorflowDefinitionFor(collection, schemaCode);
	}

	public WorkflowDefinition getWorkflowDefinition(String collection, String id) {
		return workflowDefinitions.get(collection + id);
	}

	public List<WorkflowConfiguration> getWorkflows(String collection) {
		List<WorkflowConfiguration> workflowConfigurations = oneXMLConfigPerCollectionManager.get(collection);
		if (workflowConfigurations != null) {
			return workflowConfigurations;
		} else {
			throw new WorkflowsConfigManagerRuntimeException_NoWorkflowConfigurationForThisCollection(collection);
		}
	}

	public void addUpdateWorkflow(WorkflowConfiguration workflowConfiguration) {
		validateWorkflowConfiguration(workflowConfiguration);
		boolean update = workflowDefinitions.containsKey(workflowConfiguration.getCollection() + workflowConfiguration.getId());
		if (!update) {
			validateThatNotExists(workflowConfiguration);
		}

		String collection = workflowConfiguration.getCollection();
		if (!update) {
			oneXMLConfigPerCollectionManager.updateXML(collection,
					newAddWorkflowConfigurationDocumentAlteration(workflowConfiguration));
		} else {
			oneXMLConfigPerCollectionManager.updateXML(collection,
					newUpdateWorkflowConfigurationDocumentAlteration(workflowConfiguration));
		}
	}

	@Override
	public void onValueModified(String collection, List<WorkflowConfiguration> workflowConfigurations) {
		this.workflowDefinitions.clear();
		for (WorkflowConfiguration workflowConfiguration : workflowConfigurations) {
			WorkflowDefinition workflowDefinition = workflowBPMNDefinitionsService.getWorkflowDefinition(
					workflowConfiguration.getBpmnFilename(), workflowConfiguration.getMapping(), workflowConfiguration);
			workflowDefinitions.put(workflowConfiguration.getCollection() + workflowConfiguration.getId(), workflowDefinition);
		}
	}

	private List<TriggeredWorkflowDefinition> getManualTriggeredWorflowDefinitionFor(String collection, String code) {
		List<TriggeredWorkflowDefinition> triggeredWorkflowDefinitions = new ArrayList<>();
		WorkflowDefinition workflowDefinition = null;
		for (WorkflowConfiguration workflowConfiguration : getWorkflows(collection)) {
			for (Trigger trigger : workflowConfiguration.getTriggers()) {
				String schemaCode = trigger.getTriggeredSchemaCode();
				if (trigger.getTriggerType() == TriggerType.MANUAL && schemaCode.equals(code)) {
					workflowDefinition = workflowDefinitions.get(collection + workflowConfiguration.getId());
					triggeredWorkflowDefinitions.add(new TriggeredWorkflowDefinition(workflowDefinition, trigger));
				}
			}
		}
		return triggeredWorkflowDefinitions;
	}

	private TriggeredWorkflowDefinition getTriggeredWorflowDefinitionFor(String collection, String code,
			TriggerType triggerType) {
		TriggeredWorkflowDefinition triggeredWorkflowDefinition = null;
		for (WorkflowConfiguration workflowConfiguration : getWorkflows(collection)) {
			for (Trigger trigger : workflowConfiguration.getTriggers()) {
				String generiCode = trigger.getTriggeredSchemaCode();
				if (triggerType == TriggerType.METADATA_MODIFIED && trigger.getTriggerType() == TriggerType.METADATA_MODIFIED) {
					generiCode = trigger.getTriggeredMetadataCode();
				}
				if (trigger.getTriggerType() == triggerType && generiCode.equals(code)) {
					WorkflowDefinition workflowDefinition = workflowDefinitions.get(collection + workflowConfiguration.getId());
					triggeredWorkflowDefinition = new TriggeredWorkflowDefinition(workflowDefinition, trigger);
					break;
				}
			}
		}
		return triggeredWorkflowDefinition;
	}

	private void validateWorkflowConfiguration(WorkflowConfiguration workflowConfiguration) {
		for (Trigger trigger : workflowConfiguration.getTriggers()) {
			if (trigger.getTriggerType() == TriggerType.METADATA_MODIFIED && trigger.getTriggeredMetadataCode() == null) {
				throw new WorkflowsConfigManagerRuntimeException_InvalidWorkflowConfiguration();
			}
		}
	}

	private void validateThatNotExists(WorkflowConfiguration workflowConfiguration) {
		for (WorkflowConfiguration retrievedWorkflowConfiguration : getWorkflows(workflowConfiguration.getCollection())) {
			compareWorkflowsConfigurationTriggers(workflowConfiguration, retrievedWorkflowConfiguration);
		}
	}

	private void compareWorkflowsConfigurationTriggers(WorkflowConfiguration workflowConfiguration,
			WorkflowConfiguration retrievedWorkflowConfiguration) {
		for (Trigger retrievedTrigger : retrievedWorkflowConfiguration.getTriggers()) {
			if (retrievedTrigger.getTriggerType() == TriggerType.MANUAL) {
				continue;
			}
			for (Trigger trigger : workflowConfiguration.getTriggers()) {
				if (retrievedTrigger.getTriggerType() == trigger.getTriggerType() && retrievedTrigger.getTriggeredSchemaCode()
						.equals(trigger.getTriggeredSchemaCode())) {
					throw new WorkflowsConfigManagerRuntimeException_WorkflowConfigurationAlreadyExists(
							retrievedTrigger.getTriggeredSchemaCode(), retrievedTrigger.getTriggerType().name());
				}
			}
		}
	}

	private XMLConfigReader<List<WorkflowConfiguration>> xmlConfigReader() {
		return new XMLConfigReader<List<WorkflowConfiguration>>() {
			@Override
			public List<WorkflowConfiguration> read(String collection, Document document) {
				return newWorkflowsConfigManagerReader(document).getWorkflows();
			}

		};
	}

	private WorkflowsConfigReader newWorkflowsConfigManagerReader(Document document) {
		return new WorkflowsConfigReader(document);
	}

	private WorkflowsConfigWriter newWorkflowsConfigManagerWriter(Document document) {
		return new WorkflowsConfigWriter(document);
	}

	DocumentAlteration newAddWorkflowConfigurationDocumentAlteration(final WorkflowConfiguration workflowConfiguration) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newWorkflowsConfigManagerWriter(document).add(workflowConfiguration);
			}
		};
	}

	DocumentAlteration newUpdateWorkflowConfigurationDocumentAlteration(final WorkflowConfiguration workflowConfiguration) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newWorkflowsConfigManagerWriter(document).update(workflowConfiguration);
			}
		};
	}

	public void createCollectionWorkflows(String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				WorkflowsConfigWriter writer = newWorkflowsConfigManagerWriter(document);
				writer.createEmptyWorkflows();
			}
		};
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	@Override
	public void close() {

	}
}
