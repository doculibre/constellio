package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.SelectionChangeListener;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ListAddRemoveRecordLookupField extends ListAddRemoveField<String, LookupRecordField> {

	private String schemaTypeCode;
	private String schemaCode;
	protected boolean ignoreLinkability;
	private boolean itemInformation;
	protected List<ValueChangeListener> lookupFieldListenerList;

	public ListAddRemoveRecordLookupField(String schemaTypeCode) {
		this(schemaTypeCode, null);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode) {
		super();
		this.schemaTypeCode = schemaTypeCode;
		this.schemaCode = schemaCode;
		setItemConverter(new RecordIdToCaptionConverter());
		ignoreLinkability = false;
		lookupFieldListenerList = new ArrayList<>();
	}

	@Override
	protected boolean isEditPossible() {
		return false;
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
		final LookupRecordField field = new LookupRecordField(schemaTypeCode, schemaCode) {
			@Override
			protected String getReadOnlyMessage() {
				String readOnlyMessage = ListAddRemoveRecordLookupField.this.getReadOnlyMessage();
				if(!StringUtils.isBlank(readOnlyMessage)) {
					return readOnlyMessage;
				} else {
					return super.getReadOnlyMessage();
				}
			}
		};

		for(ValueChangeListener listener: lookupFieldListenerList) {
			field.addValueChangeListener(listener);
		}
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

	protected String getReadOnlyMessage() {
		return null;
	}

	public void addLookupValueChangeListener(ValueChangeListener listener) {
		lookupFieldListenerList.add(listener);
	}

	public Object getLookupFieldValue() {
		if(addEditField != null) {
			return ((LookupRecordField) addEditField).getValue();
		}
		return null;
	}
}
