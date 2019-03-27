package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.vaadin.data.util.ItemSorter;

@SuppressWarnings("unchecked")
public class ListAddRemoveRecordLookupField extends ListAddRemoveField<String, LookupRecordField> {

	private String schemaTypeCode;
	private String schemaCode;
	private boolean ignoreLinkability;
	private boolean sortByCaption;

	public ListAddRemoveRecordLookupField(String schemaTypeCode) {
		this(schemaTypeCode, null, false);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, boolean sortByCaption) {
		this(schemaTypeCode, null, sortByCaption);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode) {
		this(schemaTypeCode, schemaCode, false);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode, boolean sortByCaption) {
		super();
		this.schemaTypeCode = schemaTypeCode;
		this.schemaCode = schemaCode;
		this.sortByCaption = sortByCaption;
		setItemConverter(new RecordIdToCaptionConverter());
		ignoreLinkability = false;
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
		if (addEditField != null) {
			addEditField.setIgnoreLinkability(ignoreLinkability);
		}
	}

	@Override
	protected LookupRecordField newAddEditField() {
		LookupRecordField field = new LookupRecordField(schemaTypeCode, schemaCode);
		field.setIgnoreLinkability(ignoreLinkability);
		return field;
	}

	@Override
	protected ItemSorter getItemSorter() {
		if(sortByCaption) {
			return buildDefaultItemSorter();
		}
		return super.getItemSorter();
	}
}
