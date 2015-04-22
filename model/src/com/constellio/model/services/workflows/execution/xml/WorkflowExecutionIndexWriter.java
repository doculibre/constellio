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
