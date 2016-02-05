package com.constellio.model.services.workflows.config.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.trigger.ActionCompletion;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggerType;

public class WorkflowsConfigReader {

	private static final String BPMN_FILE_NAME = "bpmnFileName";
	private static final String ACTION_COMPLETION = "actionCompletion";
	private static final String ID = "id";
	private static final String ENABLE = "enable";
	private static final String COLLECTION = "collection";
	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final String MAPPINGS = "mappings";
	private static final String TRIGGER_METADATA_CODE = "triggerMetadataCode";
	private static final String TRIGGER_SCHEMA_CODE = "triggerSchemaCode";
	private static final String TRIGGER_TYPE = "triggerType";
	private static final String TRIGGERS = "triggers";
	Document document;

	public WorkflowsConfigReader(Document document) {
		this.document = document;
	}

	public List<WorkflowConfiguration> getWorkflows() {
		List<WorkflowConfiguration> workflowConfigurations = new ArrayList<WorkflowConfiguration>();
		Element workflowsElement = document.getRootElement();
		for (Element workflowConfigurationElement : workflowsElement.getChildren()) {
			WorkflowConfiguration workflowConfiguration = createWorkflowConfigurationObject(workflowConfigurationElement);
			workflowConfigurations.add(workflowConfiguration);
		}
		return workflowConfigurations;
	}

	private WorkflowConfiguration createWorkflowConfigurationObject(Element workflowConfigurationElement) {

		String id = workflowConfigurationElement.getAttributeValue(ID);
		String collection = workflowConfigurationElement.getChildText(COLLECTION);
		boolean enable = Boolean.valueOf(workflowConfigurationElement.getChildText(ENABLE));
		Map<String, String> mappings = new HashMap<>();
		for (Element mappingElement : workflowConfigurationElement.getChild(MAPPINGS).getChildren()) {
			mappings.put(mappingElement.getChildText(KEY), mappingElement.getChildText(VALUE));
		}
		Trigger trigger;
		List<Trigger> triggers = new ArrayList<>();
		for (Element triggerElement : workflowConfigurationElement.getChild(TRIGGERS).getChildren()) {
			String triggerTypeStr = triggerElement.getChildText(TRIGGER_TYPE);
			TriggerType triggerType = TriggerType.valueOf(triggerTypeStr);
			String triggeredSchemaCode = triggerElement.getChildText(TRIGGER_SCHEMA_CODE);
			String triggeredMetadataCode = triggerElement.getChildText(TRIGGER_METADATA_CODE);
			String actionCompletionStr = triggerElement.getChildText(ACTION_COMPLETION);
			ActionCompletion actionCompletion = ActionCompletion.valueOf(actionCompletionStr);
			if (triggeredMetadataCode.equals("")) {
				triggeredMetadataCode = null;
			}
			trigger = new Trigger(triggerType, triggeredSchemaCode, triggeredMetadataCode, actionCompletion);
			triggers.add(trigger);
		}
		String bpmnFileName = workflowConfigurationElement.getChildText(BPMN_FILE_NAME);
		return new WorkflowConfiguration(id, collection, enable, mappings, triggers, bpmnFileName);
	}
}
