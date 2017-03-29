package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.rm.ui.components.converters.DocumentIdToContextCaptionConverter;
import com.constellio.app.modules.tasks.ui.components.fields.TaskLinkedDocumentsFieldImpl;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

/**
 * Created by Constellio on 2017-03-29.
 */
public class TaskListAddRemoveLinkedDocumentsLookupField extends ListAddRemoveField<String, TaskLinkedDocumentsFieldImpl> {
    private DocumentIdToContextCaptionConverter converter = new DocumentIdToContextCaptionConverter();

    @Override
    protected TaskLinkedDocumentsFieldImpl newAddEditField() {
        return new TaskLinkedDocumentsFieldImpl();
    }

    //FIXME should be always Vo or not
    @Override
    protected String getItemCaption(Object itemId) {
        return converter.convertToPresentation((String) itemId, String.class, getLocale());
    }
}
