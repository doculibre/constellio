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
package com.constellio.model.services.workflows.config.xml;

import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.trigger.Trigger;

public class WorkflowsConfigWriter {

	private static final String BPMN_FILE_NAME = "bpmnFileName";
	private static final String ACTION_COMPLETION = "actionCompletion";
	private static final String ID = "id";
	private static final String ENABLE = "enable";
	private static final String WORKFLOWS = "workflows";
	private static final String WORKFLOW_CONFIGURATION = "workflowConfiguration";
	private static final String COLLECTION = "collection";
	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final String MAPPING = "mapping";
	private static final String MAPPINGS = "mappings";
	private static final String TRIGGER_METADATA_CODE = "triggerMetadataCode";
	private static final String TRIGGER_SCHEMA_CODE = "triggerSchemaCode";
	private static final String TRIGGER_TYPE = "triggerType";
	private static final String TRIGGER = "trigger";
	private static final String TRIGGERS = "triggers";

	Document document;

	public WorkflowsConfigWriter(Document document) {
		this.document = document;
	}

	public void createEmptyWorkflows() {
		Element workflowsElement = new Element(WORKFLOWS);
		document.setRootElement(workflowsElement);
	}

	public void add(WorkflowConfiguration workflowConfiguration) {

		Element collectionElement = new Element(COLLECTION);
		String collection = workflowConfiguration.getCollection();
		collectionElement.setText(collection);

		Element enableElement = new Element(ENABLE);
		boolean enable = workflowConfiguration.isEnabled();
		enableElement.setText(String.valueOf(enable));

		Element triggersElements = new Element(TRIGGERS);
		for (Trigger trigger : workflowConfiguration.getTriggers()) {
			Element triggerTypeElement = createTriggerElement(trigger);
			triggersElements.addContent(triggerTypeElement);
		}

		Element mappingsElements = new Element(MAPPINGS);
		Map<String, String> mappings = workflowConfiguration.getMapping();
		for (Map.Entry<String, String> mapping : mappings.entrySet()) {
			Element mappingElement = createMappingElement(mapping);
			mappingsElements.addContent(mappingElement);
		}

		Element bpmnFileNameElement = new Element(BPMN_FILE_NAME);
		bpmnFileNameElement.setText(workflowConfiguration.getBpmnFilename());

		Element workflowConfigurationElement = new Element(WORKFLOW_CONFIGURATION)
				.setAttribute(ID, workflowConfiguration.getId());
		workflowConfigurationElement.addContent(collectionElement);
		workflowConfigurationElement.addContent(enableElement);
		workflowConfigurationElement.addContent(mappingsElements);
		workflowConfigurationElement.addContent(triggersElements);
		workflowConfigurationElement.addContent(bpmnFileNameElement);
		Element workflowsElement = document.getRootElement();
		workflowsElement.addContent(workflowConfigurationElement);
	}

	public void update(WorkflowConfiguration workflowConfiguration) {
		Element elementToRemove = null;
		for (Element element : document.getRootElement().getChildren()) {
			if (element.getAttributeValue(ID).equals(workflowConfiguration.getId())) {
				elementToRemove = element;
				break;
			}
		}
		if (elementToRemove != null) {
			elementToRemove.detach();
		}
		add(workflowConfiguration);
	}

	private Element createMappingElement(Map.Entry<String, String> mapping) {
		Element keyElement = new Element(KEY);
		keyElement.setText(mapping.getKey());
		Element valueElement = new Element(VALUE);
		valueElement.setText(mapping.getValue());
		Element mappingElement = new Element(MAPPING);
		mappingElement.addContent(keyElement);
		mappingElement.addContent(valueElement);
		return mappingElement;
	}

	private Element createTriggerElement(Trigger trigger) {
		Element triggerTypeElement = new Element(TRIGGER_TYPE);
		triggerTypeElement.setText(trigger.getTriggerType().name());
		Element actionCompletionElement = new Element(ACTION_COMPLETION);
		actionCompletionElement.setText(trigger.getActionCompletion().name());
		Element triggerSchemaCodeElement = new Element(TRIGGER_SCHEMA_CODE);
		triggerSchemaCodeElement.setText(trigger.getTriggeredSchemaCode());
		Element triggerMetadataCodeElement = new Element(TRIGGER_METADATA_CODE);
		triggerMetadataCodeElement.setText(trigger.getTriggeredMetadataCode());
		Element triggerElement = new Element(TRIGGER);
		triggerElement.addContent(triggerTypeElement);
		triggerElement.addContent(triggerSchemaCodeElement);
		triggerElement.addContent(triggerMetadataCodeElement);
		triggerElement.addContent(actionCompletionElement);
		return triggerElement;
	}
}
