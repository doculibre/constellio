package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.vaadin.data.util.ItemSorter;

@SuppressWarnings("unchecked")
public class ListAddRemoveRecordComboBox extends ListAddRemoveField<String, RecordComboBox> {

	private String schemaCode;
	private boolean sortByCaption;

	public ListAddRemoveRecordComboBox(String schemaCode) {
		this(schemaCode, false);
	}

	public ListAddRemoveRecordComboBox(String schemaCode, boolean sortByCaption) {
		super();
		this.schemaCode = schemaCode;
		this.sortByCaption = sortByCaption;
		setItemConverter(new RecordIdToCaptionConverter());
	}

	@Override
	protected RecordComboBox newAddEditField() {
		return new RecordComboBox(schemaCode);
	}

	@Override
	protected ItemSorter getItemSorter() {
		if (sortByCaption) {
			return buildDefaultItemSorter();
		}
		return super.getItemSorter();
	}
}
