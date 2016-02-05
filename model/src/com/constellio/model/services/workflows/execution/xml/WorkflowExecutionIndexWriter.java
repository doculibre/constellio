package com.constellio.model.services.workflows.execution.xml;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public class WorkflowExecutionIndexWriter {

	private static final String ID = "id";
	private static final String MARKED_AS_WAITING_FOR_SYSTEM = "markedAsWaitingForSystem";
	private static final String WORFLOWS_EXECUTIONS = "worflowsExecution";
	private static final String WORFLOW_EXECUTION = "worflowExecution";
	Document document;

	public WorkflowExecutionIndexWriter(Document document) {
		this.document = document;
	}

	public void add(WorkflowExecution workflowExecution) {

		Element idElement = new Element(ID);
		idElement.setText(workflowExecution.getId());
		Element markedAsWaitingForSystemElement = new Element(MARKED_AS_WAITING_FOR_SYSTEM);
		markedAsWaitingForSystemElement.setText(String.valueOf(workflowExecution.isMarkAsWaitingForSystem()));
		Element workflowExecutionElement = new Element(WORFLOW_EXECUTION);
		workflowExecutionElement.addContent(idElement);
		workflowExecutionElement.addContent(markedAsWaitingForSystemElement);

		Element workflowsElement = document.getRootElement();
		workflowsElement.addContent(workflowExecutionElement);
	}

	public void createEmptyWorkflowsExecutionIndex() {
		Element workflowsElement = new Element(WORFLOWS_EXECUTIONS);
		document.setRootElement(workflowsElement);
	}

	public void remove(WorkflowExecution workflowExecution) {
		Element elementToRemove = null;
		for (Element element : document.getRootElement().getChildren()) {
			if (element.getChildText(ID).equals(workflowExecution.getId())) {
				elementToRemove = element;
				break;
			}
		}
		if (elementToRemove != null) {
			elementToRemove.detach();
		}
	}

	public void update(WorkflowExecution workflowExecution) {
		remove(workflowExecution);
		add(workflowExecution);
	}
}
