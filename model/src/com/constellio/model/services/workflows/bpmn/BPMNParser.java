package com.constellio.model.services.workflows.bpmn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.constellio.model.entities.workflows.definitions.AllUsersSelector;
import com.constellio.model.entities.workflows.definitions.BPMNProperty;
import com.constellio.model.entities.workflows.definitions.GroupSelector;
import com.constellio.model.entities.workflows.definitions.RoleSelector;
import com.constellio.model.entities.workflows.definitions.UserSelector;
import com.constellio.model.entities.workflows.definitions.WorkflowAction;
import com.constellio.model.entities.workflows.definitions.WorkflowCondition;
import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;
import com.constellio.model.entities.workflows.definitions.WorkflowRouting;
import com.constellio.model.entities.workflows.definitions.WorkflowRoutingDestination;
import com.constellio.model.entities.workflows.definitions.WorkflowServiceTask;
import com.constellio.model.entities.workflows.definitions.WorkflowTask;
import com.constellio.model.entities.workflows.definitions.WorkflowUserTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.workflows.bpmn.BPMNParserRuntimeException.BPMNParserRuntimeException_InvalidCondition;
import com.constellio.model.services.workflows.general.WorkflowConditions;
import com.constellio.model.utils.InstanciationUtils;

public class BPMNParser {

	public static final String SEQUENCE_FLOW = "sequenceFlow";
	public static final String ID = "id";
	public static final String USER_PREFIX = "user:";
	public static final String GROUP_PREFIX = "group:";
	public static final String ROLE_PREFIX = "role:";
	private static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
	Namespace activitiNamespace;
	Document document;
	Namespace namespace;

	Map<String, String> mapping;
	WorkflowConfiguration workflowConfiguration;

	public BPMNParser(Document document, Map<String, String> mapping, WorkflowConfiguration workflowConfiguration) {
		this.document = document;
		this.mapping = mapping;
		namespace = document.getRootElement().getNamespace();
		activitiNamespace = document.getRootElement().getNamespace("activiti");
		this.workflowConfiguration = workflowConfiguration;
	}

	public WorkflowDefinition build() {
		Map<String, WorkflowTask> tasks = new HashMap<>();

		Element rootElement = document.getRootElement().getChild("process", namespace);

		Map<String, WorkflowRouting> routings = new HashMap<>();
		for (Element gateway : rootElement.getChildren(EXCLUSIVE_GATEWAY, namespace)) {
			routings.put(gateway.getAttributeValue(ID), null);
		}
		routings.put(WorkflowRoutingDestination.DESTINATION_START, startRouting());

		Map<String, WorkflowRoutingDestination> routingDestinations = new HashMap<>();
		for (Element sequenceFlow : rootElement.getChildren(SEQUENCE_FLOW, namespace)) {
			String id = sequenceFlow.getAttributeValue(ID);
			String sourceRef = sequenceFlow.getAttributeValue("sourceRef");
			String targetRef = sequenceFlow.getAttributeValue("targetRef");
			Element conditionElement = sequenceFlow.getChild("conditionExpression", namespace);
			WorkflowRoutingDestination routingDestination;
			if (conditionElement != null) {
				routingDestination = createRoutingDestinationWithCondition(targetRef, sourceRef, conditionElement);
			} else {
				routingDestination = new WorkflowRoutingDestination(WorkflowConditions.directCondition(), targetRef, sourceRef);
			}
			if (routings.containsKey(sourceRef)) {
				addDestinationToSourceRouting(routings, sourceRef, routingDestination);
			}
			routingDestinations.put(id, routingDestination);
		}

		parseServiceTasks(tasks, rootElement, routingDestinations);

		parseUserTasks(tasks, rootElement, routingDestinations);

		String collection = workflowConfiguration.getCollection();
		return new WorkflowDefinition(workflowConfiguration.getId(), tasks, true, routings, collection);
	}

	private WorkflowRouting startRouting() {
		return new WorkflowRouting(WorkflowRoutingDestination.DESTINATION_START);
	}

	private void parseUserTasks(Map<String, WorkflowTask> tasks, Element rootElement,
			Map<String, WorkflowRoutingDestination> routingDestinations) {
		for (Element userTaskElement : rootElement.getChildren("userTask", namespace)) {
			String id = userTaskElement.getAttributeValue(ID);
			String usersString = userTaskElement.getAttributeValue("candidateUsers", activitiNamespace);
			String groupsString = userTaskElement.getAttributeValue("candidateGroups", activitiNamespace);
			AllUsersSelector userSelector = parseUserSelector(usersString, groupsString);
			List<BPMNProperty> fields = parseFields(userTaskElement);
			String taskSchema = userTaskElement.getAttributeValue("formKey", activitiNamespace);
			int dueDate = Integer.parseInt(userTaskElement.getAttributeValue("dueDate", activitiNamespace));
			tasks.put(id,
					new WorkflowUserTask(id, taskSchema, getRoutingsForTaskId(routingDestinations, id), userSelector, dueDate,
							fields));
		}
	}

	private List<BPMNProperty> parseFields(Element userTaskElement) {
		List<BPMNProperty> fields = new ArrayList<>();
		for (Element propertyElement : userTaskElement.getChild("extensionElements", namespace)
				.getChildren("formProperty", activitiNamespace)) {
			String id = propertyElement.getAttributeValue(ID);
			String variableCode = propertyElement.getAttributeValue("variable");
			String expressionValue = propertyElement.getAttributeValue("expression");
			fields.add(new BPMNProperty(id, expressionValue, variableCode));
		}
		return fields;
	}

	private AllUsersSelector parseUserSelector(String usersString, String groupsString) {
		List<String> parsedUsers = new ArrayList<>();
		List<String> parsedGroups = new ArrayList<>();
		List<String> parsedRoles = new ArrayList<>();

		if (usersString != null) {
			for (String username : usersString.split(",")) {
				if (username.startsWith(USER_PREFIX)) {
					if (StringUtils.substringAfter(username, USER_PREFIX).startsWith("${")) {
						parsedUsers.add(StringUtils.substringAfter(username, USER_PREFIX));
					} else {
						parsedUsers.add(mapping.get(username));
					}
				} else {
					parsedUsers.add(username);
				}
			}
		}

		for (String groupName : groupsString.split(",")) {
			if (groupName.startsWith(GROUP_PREFIX)) {
				parsedGroups.add(StringUtils.substringAfter(groupName, GROUP_PREFIX));
			} else if (groupName.startsWith(ROLE_PREFIX)) {
				if (StringUtils.substringAfter(groupName, ROLE_PREFIX).startsWith("${")) {
					parsedRoles.add(StringUtils.substringAfter(groupName, ROLE_PREFIX));
				} else {
					parsedRoles.add(mapping.get(groupName));
				}
			}
		}

		return new AllUsersSelector(new RoleSelector(parsedRoles), new GroupSelector(parsedGroups),
				new UserSelector(parsedUsers));
	}

	private void parseServiceTasks(Map<String, WorkflowTask> tasks, Element rootElement,
			Map<String, WorkflowRoutingDestination> routingDestinations) {
		for (Element serviceTask : rootElement.getChildren("serviceTask", namespace)) {
			String id = serviceTask.getAttributeValue(ID);
			String className = serviceTask.getAttributeValue("class", activitiNamespace);
			WorkflowAction action = (WorkflowAction) new InstanciationUtils().instanciate(className);
			List<WorkflowRoutingDestination> taskRoutings = getRoutingsForTaskId(routingDestinations, id);
			tasks.put(id, new WorkflowServiceTask(id, action, taskRoutings));
		}
	}

	private List<WorkflowRoutingDestination> getRoutingsForTaskId(Map<String, WorkflowRoutingDestination> routingDestinations,
			String id) {
		List<WorkflowRoutingDestination> taskRoutings = new ArrayList<>();
		for (WorkflowRoutingDestination routingDestination : routingDestinations.values()) {
			if (routingDestination.getSource().equals(id)) {
				taskRoutings.add(routingDestination);
			}
		}
		return taskRoutings;
	}

	private void addDestinationToSourceRouting(Map<String, WorkflowRouting> routings, String sourceRef,
			WorkflowRoutingDestination routingDestination) {
		if (routings.get(sourceRef) != null) {
			routings.get(sourceRef).addDestination(routingDestination);
		} else {
			WorkflowRouting workflowRouting = new WorkflowRouting(sourceRef);
			workflowRouting.addDestination(routingDestination);
			routings.put(sourceRef, workflowRouting);
		}
	}

	private WorkflowRoutingDestination createRoutingDestinationWithCondition(String targetRef, String sourceRef,
			Element conditionElement) {
		WorkflowCondition condition;
		String conditionValue = conditionElement.getText();
		String extractedCondition = StringUtils.substringBetween(conditionValue, "${", "}");
		if (extractedCondition.contains("==")) {
			final String[] parsedCondition = extractedCondition.split("==");
			condition = new WorkflowCondition() {
				@Override
				public boolean isTrue(WorkflowExecution execution) {
					return execution.getVariable(parsedCondition[0].trim()).equals(parsedCondition[1].trim().replace("\"", ""));
				}
			};
		} else if (extractedCondition.contains("!=")) {
			final String[] parsedCondition = extractedCondition.split("!=");
			condition = new WorkflowCondition() {
				@Override
				public boolean isTrue(WorkflowExecution execution) {
					return !execution.getVariable(parsedCondition[0].trim()).equals(parsedCondition[1].trim().replace("\"", ""));
				}
			};
		} else {
			throw new BPMNParserRuntimeException_InvalidCondition(extractedCondition);
		}
		return new WorkflowRoutingDestination(condition, targetRef, sourceRef);
	}
}
