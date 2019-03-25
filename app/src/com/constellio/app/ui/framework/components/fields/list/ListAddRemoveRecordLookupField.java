package com.constellio.app.ui.framework.components.fields.list;

import java.util.List;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.SelectionChangeListener;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class ListAddRemoveRecordLookupField extends ListAddRemoveField<String, LookupRecordField> {

	private String schemaTypeCode;
	private String schemaCode;
	private boolean ignoreLinkability;
	private boolean itemInformation;

	public ListAddRemoveRecordLookupField(String schemaTypeCode) {
		this(schemaTypeCode, null);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode) {
		super();
		this.schemaTypeCode = schemaTypeCode;
		this.schemaCode = schemaCode;
		setItemConverter(new RecordIdToCaptionConverter());
		ignoreLinkability = false;
	}

	public String getSchemaTypeCode() {
		return schemaTypeCode;
	}

	public String getSchemaCode() {
		return schemaCode;
	}

	public boolean isIgnoreLinkability() {
		return ignoreLinkability;
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
		if (addEditField != null) {
			addEditField.setIgnoreLinkability(ignoreLinkability);
		}
	}

	public boolean isItemInformation() {
		return itemInformation;
	}

	public void setItemInformation(boolean itemInformation) {
		this.itemInformation = itemInformation;
	}

	@Override
	protected LookupRecordField newAddEditField() {
		LookupRecordField field = new LookupRecordField(schemaTypeCode, schemaCode);
		field.setIgnoreLinkability(ignoreLinkability);
		field.setMultiValue(true);
		field.setItemInformation(itemInformation);
		field.addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(List<Object> newSelection) {
				if (newSelection != null) {
					tryAdd();
					field.getAutoCompleteField().clear();
				}
			}
		});
		return field;
	}
}
