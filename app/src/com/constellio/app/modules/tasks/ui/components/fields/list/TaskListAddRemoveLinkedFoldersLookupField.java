package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.rm.ui.components.converters.FolderIdToContextCaptionConverter;
import com.constellio.app.modules.tasks.ui.components.fields.TaskLinkedFoldersFieldImpl;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.SelectionChangeListener;

import java.util.List;

/**
 * Created by Constellio on 2017-03-29.
 */
public class TaskListAddRemoveLinkedFoldersLookupField extends ListAddRemoveField<String, TaskLinkedFoldersFieldImpl> {
	private FolderIdToContextCaptionConverter converter = new FolderIdToContextCaptionConverter();

	@Override
	protected TaskLinkedFoldersFieldImpl newAddEditField() {
		TaskLinkedFoldersFieldImpl field = new TaskLinkedFoldersFieldImpl();
		field.setMultiValue(true);
		field.addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(List<Object> newSelection) {
				if (newSelection != null) {
					tryAdd();
				}
			}
		});
		return field;
	}

	@Override
	protected String getItemCaption(Object itemId) {
		return converter.convertToPresentation((String) itemId, String.class, getLocale());
	}

	@Override
	protected boolean isEditPossible() {
		return false;
	}
}
