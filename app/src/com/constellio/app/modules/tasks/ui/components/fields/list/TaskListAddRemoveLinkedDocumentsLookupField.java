package com.constellio.app.modules.tasks.ui.components.fields.list;

import java.util.List;

import com.constellio.app.modules.rm.ui.components.converters.DocumentIdToContextCaptionConverter;
import com.constellio.app.modules.tasks.ui.components.fields.TaskLinkedDocumentsFieldImpl;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.SelectionChangeListener;

/**
 * Created by Constellio on 2017-03-29.
 */
public class TaskListAddRemoveLinkedDocumentsLookupField extends ListAddRemoveField<String, TaskLinkedDocumentsFieldImpl> {
	private DocumentIdToContextCaptionConverter converter = new DocumentIdToContextCaptionConverter();

	@Override
	protected TaskLinkedDocumentsFieldImpl newAddEditField() {
		TaskLinkedDocumentsFieldImpl field = new TaskLinkedDocumentsFieldImpl();
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
}
