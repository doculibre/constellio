package com.constellio.model.services.workflows.execution.xml;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

public class WorkflowExecutionIndexReader {

	private static final String ID = "id";
	private static final String MARKED_AS_WAITING_FOR_SYSTEM = "markedAsWaitingForSystem";
	Document document;

	public WorkflowExecutionIndexReader(Document document) {
		this.document = document;
	}

	public Map<String, Boolean> getWorkflowsExcecution() {
		Map<String, Boolean> workflowExecutionsIds = new HashMap<String, Boolean>();
		Element workflowsExecutionElement = document.getRootElement();
		for (Element workflowExecutionElement : workflowsExecutionElement.getChildren()) {
			String workflowExecutionId = workflowExecutionElement.getChildText(ID);
			boolean markedAsWaitingForSystem = Boolean
					.valueOf(workflowExecutionElement.getChildText(MARKED_AS_WAITING_FOR_SYSTEM));
			workflowExecutionsIds.put(workflowExecutionId, markedAsWaitingForSystem);
		}
		return workflowExecutionsIds;
	}
}
