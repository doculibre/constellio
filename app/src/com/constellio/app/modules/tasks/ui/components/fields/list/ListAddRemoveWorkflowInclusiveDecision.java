package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.ui.components.fields.TaskWorkflowInclusiveDecisionField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskWorkflowInclusiveDecisionFieldImpl;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by constellios on 2017-07-19.
 */
public class ListAddRemoveWorkflowInclusiveDecision extends ListAddRemoveField<String, TaskWorkflowInclusiveDecisionFieldImpl> {

    TaskWorkflowInclusiveDecisionFieldImpl taskWorkflowInclusiveDecisionField;
    private List<String> stringList = new ArrayList<>();
    @Override
    protected TaskWorkflowInclusiveDecisionFieldImpl newAddEditField() {
        taskWorkflowInclusiveDecisionField = new TaskWorkflowInclusiveDecisionFieldImpl();
        taskWorkflowInclusiveDecisionField.addItems(stringList);
        taskWorkflowInclusiveDecisionField.setImmediate(true);
        return new TaskWorkflowInclusiveDecisionFieldImpl();
    }

    public void addItem(String item){
        stringList.add(item);
        taskWorkflowInclusiveDecisionField.addItem(item);
        taskWorkflowInclusiveDecisionField.markAsDirty();
    }
}
