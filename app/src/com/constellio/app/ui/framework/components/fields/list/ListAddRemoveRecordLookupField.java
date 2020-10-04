package com.constellio.app.ui.framework.components.fields.list;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.SelectionChangeListener;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.vaadin.data.util.ItemSorter;

public class ListAddRemoveRecordLookupField extends ListAddRemoveField<String, LookupRecordField> {

	private String schemaTypeCode;
	private String schemaCode;
	protected boolean ignoreLinkability;
	private boolean sortByCaption;
	private boolean itemInformation;
	protected List<ValueChangeListener> lookupFieldListenerList;
	protected RecordTextInputDataProvider recordTextInputDataProvider;
	protected List<String> idsToIgnore;

	public ListAddRemoveRecordLookupField(String schemaTypeCode) {
		this(schemaTypeCode, null, null, false);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, boolean sortByCaption) {
		this(schemaTypeCode, null, null, sortByCaption);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode) {
		this(schemaTypeCode, schemaCode, null);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode,
										  RecordTextInputDataProvider recordTextInputDataProvider) {
		this(schemaTypeCode, schemaCode, recordTextInputDataProvider, false);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode,
										  RecordTextInputDataProvider recordTextInputDataProvider,
										  boolean sortByCaption) {
		super();
		this.idsToIgnore = new ArrayList<>();
		this.schemaTypeCode = schemaTypeCode;
		this.schemaCode = schemaCode;
		this.recordTextInputDataProvider = recordTextInputDataProvider;
		setItemConverter(new RecordIdToCaptionConverter());
		ignoreLinkability = false;
		this.sortByCaption = sortByCaption;
		lookupFieldListenerList = new ArrayList<>();
	}

	public List<String> getIdsToIgnore() {
		return idsToIgnore;
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
		final LookupRecordField field = buildBaseLookupField();

		for (ValueChangeListener listener : lookupFieldListenerList) {
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

	protected LookupRecordField buildBaseLookupField() {
		return recordTextInputDataProvider == null ?
			   new LookupRecordField(schemaTypeCode, schemaCode, idsToIgnore)
												   : new LookupRecordField(schemaTypeCode, schemaCode, false, recordTextInputDataProvider) {
			@Override
			protected String getReadOnlyMessage() {
				String readOnlyMessage = ListAddRemoveRecordLookupField.this.getReadOnlyMessage();
				if (!StringUtils.isBlank(readOnlyMessage)) {
					return readOnlyMessage;
				} else {
					return super.getReadOnlyMessage();
				}
			}
		};
	}

	protected String getReadOnlyMessage() {
		return null;
	}

	public void addLookupValueChangeListener(ValueChangeListener listener) {
		lookupFieldListenerList.add(listener);
	}

	public Object getLookupFieldValue() {
		if (addEditField != null) {
			return ((LookupRecordField) addEditField).getValue();
		}
		return null;
	}

	@Override
	protected ItemSorter getItemSorter() {
		if (sortByCaption) {
			return buildDefaultItemSorter();
		}
		return super.getItemSorter();
	}

}
