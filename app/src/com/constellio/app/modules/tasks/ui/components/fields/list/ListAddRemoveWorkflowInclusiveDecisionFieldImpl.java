package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.ui.components.fields.TaskWorkflowInclusiveDecisionFieldImpl;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by constellios on 2017-07-19.
 */
public class ListAddRemoveWorkflowInclusiveDecisionFieldImpl extends ListAddRemoveField<String, TaskWorkflowInclusiveDecisionFieldImpl> implements ListAddRemoveWorkflowInclusiveDecisionField {

	TaskWorkflowInclusiveDecisionFieldImpl taskWorkflowInclusiveDecisionField;

	public ListAddRemoveWorkflowInclusiveDecisionFieldImpl() {
		taskWorkflowInclusiveDecisionField = new TaskWorkflowInclusiveDecisionFieldImpl();
		setCaption($("ListAddRemoveWorkflowInclusiveDecisionFieldImpl.inclusiveDecision"));
	}

	@Override
	protected TaskWorkflowInclusiveDecisionFieldImpl newAddEditField() {
		return taskWorkflowInclusiveDecisionField;
	}

	public void addItem(String item) {
		taskWorkflowInclusiveDecisionField.addItem(item);
	}

}
