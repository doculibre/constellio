/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;

public class RetentionRuleDocumentTypeField extends CustomField<RetentionRuleDocumentType> {
	
	private HorizontalLayout layout;
	
	private ComboBox documentTypeField;
	
	private EnumWithSmallCodeComboBox<DisposalType> disposalTypeField;
	
	public RetentionRuleDocumentTypeField() {
		setSizeFull();
		
		layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);
		
		Property<String> documentTypeProperty = new AbstractProperty<String>() {
			@Override
			public String getValue() {
				RetentionRuleDocumentType retentionRuleDocumentType = getRetentionRuleDocumentType();
				return retentionRuleDocumentType != null ? retentionRuleDocumentType.getDocumentTypeId() : null;
			}

			@Override
			public void setValue(String newValue)
					throws com.vaadin.data.Property.ReadOnlyException {
				RetentionRuleDocumentType retentionRuleDocumentType = getRetentionRuleDocumentType();
				if (retentionRuleDocumentType != null) {
					retentionRuleDocumentType.setDocumentTypeId(newValue);
				}
			}

			@Override
			public Class<? extends String> getType() {
				return String.class;
			}
		};
		
		Property<DisposalType> disposalTypeProperty = new AbstractProperty<DisposalType>() {
			@Override
			public DisposalType getValue() {
				RetentionRuleDocumentType retentionRuleDocumentType = getRetentionRuleDocumentType();
				return retentionRuleDocumentType != null ? retentionRuleDocumentType.getDisposalType() : null;
			}

			@Override
			public void setValue(DisposalType newValue)
					throws com.vaadin.data.Property.ReadOnlyException {
				RetentionRuleDocumentType retentionRuleDocumentType = getRetentionRuleDocumentType();
				if (retentionRuleDocumentType != null) {
					retentionRuleDocumentType.setDisposalType(newValue);
				}
			}

			@Override
			public Class<? extends DisposalType> getType() {
				return DisposalType.class;
			}
		};
		
		documentTypeField = new RecordComboBox(DocumentType.DEFAULT_SCHEMA);
		disposalTypeField = new EnumWithSmallCodeComboBox<DisposalType>(DisposalType.class) {
			@Override
			protected boolean isIgnored(String enumCode) {
				return DisposalType.SORT.getCode().equals(enumCode);
			}
		};
		
		documentTypeField.setImmediate(true);
		disposalTypeField.setImmediate(true);
		
		documentTypeField.setPropertyDataSource(documentTypeProperty);
		disposalTypeField.setPropertyDataSource(disposalTypeProperty);
		
		layout.addComponents(documentTypeField, disposalTypeField);
		
		setValue(new RetentionRuleDocumentType());
	}
	
	private RetentionRuleDocumentType getRetentionRuleDocumentType() {
		return getValue();
	}

	@Override
	protected Component initContent() {
		return layout;
	}
	
	public void setDisposalTypeFieldVisible(boolean visible) {
		this.disposalTypeField.setVisible(visible);
	}

	@Override
	public Class<? extends RetentionRuleDocumentType> getType() {
		return RetentionRuleDocumentType.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		if (newDataSource != null) {
			if (newDataSource.getValue() == null) {
				newDataSource.setValue(new RetentionRuleDocumentType());
			}
			super.setPropertyDataSource(newDataSource);
		}
	}

	public void setValue(RetentionRuleDocumentType newValue)
			throws com.vaadin.data.Property.ReadOnlyException {
		if (newValue == null) {
			newValue = new RetentionRuleDocumentType();
		}
		super.setValue(newValue);
		documentTypeField.setValue(newValue.getDocumentTypeId());
		disposalTypeField.setValue(newValue.getDisposalType());
	}

	public void focus() {
		documentTypeField.focus();
	}

	public String getRequiredError() {
		return documentTypeField.getRequiredError();
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		documentTypeField.setRequiredError(requiredMessage);
	}

	@Override
	public String getConversionError() {
		return documentTypeField.getConversionError();
	}

	@Override
	public void setConversionError(String valueConversionError) {
		documentTypeField.setConversionError(valueConversionError);
	}

	@Override
	public ErrorMessage getComponentError() {
		return documentTypeField.getComponentError();
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		documentTypeField.setComponentError(componentError);
	}

	public void commit()
			throws SourceException, InvalidValueException {
		documentTypeField.commit();
		disposalTypeField.commit();
	}

	public void discard()
			throws SourceException {
		documentTypeField.discard();
		disposalTypeField.discard();
	}

	public boolean isValid() {
		// TODO
		return true;
	}

	public void validate()
			throws InvalidValueException {
		// TODO
		super.validate();
	}
	
}