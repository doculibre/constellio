package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.rm.ui.components.converters.FolderIdToContextCaptionConverter;
import com.constellio.app.modules.tasks.ui.components.fields.TaskLinkedFoldersFieldImpl;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

/**
 * Created by Constellio on 2017-03-29.
 */
public class TaskListAddRemoveLinkedFoldersLookupField extends ListAddRemoveField<String, TaskLinkedFoldersFieldImpl> {
    private FolderIdToContextCaptionConverter converter = new FolderIdToContextCaptionConverter();

    @Override
    protected TaskLinkedFoldersFieldImpl newAddEditField() {
        return new TaskLinkedFoldersFieldImpl();
    }

    //FIXME should be always Vo or not
    @Override
    protected String getItemCaption(Object itemId) {
        return converter.convertToPresentation((String) itemId, String.class, getLocale());
    }
}
