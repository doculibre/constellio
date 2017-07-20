package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.ui.components.fields.TaskWorkflowInclusiveDecisionFieldImpl;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

/**
 * Created by constellios on 2017-07-19.
 */
public class ListAddRemoveWorkflowInclusiveDecisionFieldImpl extends ListAddRemoveField<String, TaskWorkflowInclusiveDecisionFieldImpl> implements ListAddRemoveWorkflowInclusiveDecisionField {

    TaskWorkflowInclusiveDecisionFieldImpl taskWorkflowInclusiveDecisionField;
    
    public ListAddRemoveWorkflowInclusiveDecisionFieldImpl() {
        taskWorkflowInclusiveDecisionField = new TaskWorkflowInclusiveDecisionFieldImpl();
    }
    
    @Override
    protected TaskWorkflowInclusiveDecisionFieldImpl newAddEditField() {
        return taskWorkflowInclusiveDecisionField;
    }

    public void addItem(String item){
        taskWorkflowInclusiveDecisionField.addItem(item);
    }
    
}
