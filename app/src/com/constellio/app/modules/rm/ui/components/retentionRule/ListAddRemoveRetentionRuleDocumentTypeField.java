package com.constellio.app.modules.rm.ui.components.retentionRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.vaadin.data.Property;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("unchecked")
public class ListAddRemoveRetentionRuleDocumentTypeField extends ListAddRemoveField<RetentionRuleDocumentType, RetentionRuleDocumentTypeField> {
	
	public static final String DISPOSAL_TYPE_PROPERTY = "disposalType";
	
	private RecordIdToCaptionConverter documentTypeConverter = new RecordIdToCaptionConverter();
	
	private Boolean delayedDisposalTypeFieldVisible;
	
	@Override
	protected Component initContent() {
		Component content = super.initContent();
		setWidth("100%");
		
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidthUndefined();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		
		return content;
	}

	@Override
	protected RetentionRuleDocumentTypeField newAddEditField() {
		RetentionRuleDocumentTypeField addEditField = new RetentionRuleDocumentTypeField();
		if (delayedDisposalTypeFieldVisible != null) {
			addEditField.setDisposalTypeFieldVisible(delayedDisposalTypeFieldVisible);
		}
		return addEditField;
	} 

	@Override
	protected void addValue(RetentionRuleDocumentType value) {
		if (value != null && value.getDocumentTypeId() != null) {
			super.addValue(value);
			RetentionRuleDocumentTypeField addEditField = getAddEditField();
			addEditField.setPropertyDataSource(new ObjectProperty<>(new RetentionRuleDocumentType()));
		}
	}

	public final void setDisposalTypeVisible(boolean visible) {
		if (getAddEditField() != null) {
			getAddEditField().setDisposalTypeFieldVisible(visible);
		}
		delayedDisposalTypeFieldVisible = visible;
		
		if (valuesTable != null) {
			List<?> itemIds = new ArrayList<>(valuesTable.getItemIds());
			valuesTable.removeAllItems();
			for (Object itemId : itemIds) {
				RetentionRuleDocumentType retentionRuleDocumentType = (RetentionRuleDocumentType) itemId;
				if (!visible) {
					retentionRuleDocumentType.setDisposalType(null);
				}
				removeValue(retentionRuleDocumentType);
				addValue(retentionRuleDocumentType);
			}
		}
	}

	@Override
	protected Component newCaptionComponent(RetentionRuleDocumentType itemId, String caption) {
		String documentTypeId = itemId.getDocumentTypeId();
		String newCaption = documentTypeConverter.convertToPresentation(documentTypeId, String.class, getLocale());
		return new Label(newCaption);
	}

	@Override
	protected List<?> getExtraColumnPropertyIds() {
		return Arrays.asList(DISPOSAL_TYPE_PROPERTY);
	}

	@Override
	protected Property<?> getExtraColumnProperty(Object itemId, Object propertyId) {
		Property<?> property;
		RetentionRuleDocumentType retentionRuleDocumentType = itemId != null ? getListElementValue(itemId) : null;
		if (DISPOSAL_TYPE_PROPERTY.equals(propertyId)) {
			EnumWithSmallCodeComboBox<DisposalType> disposalTypeField = new EnumWithSmallCodeComboBox<DisposalType>(DisposalType.class) {
				@Override
				protected boolean isIgnored(String enumCode) {
					return DisposalType.SORT.getCode().equals(enumCode);
				}
			};
			disposalTypeField.setPropertyDataSource(new NestedMethodProperty<>(retentionRuleDocumentType, "disposalType"));
			if (delayedDisposalTypeFieldVisible != null) {
				disposalTypeField.setVisible(delayedDisposalTypeFieldVisible);
			}
			property = new ObjectProperty<>(disposalTypeField);
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return property;
	}

	@Override
	protected Class<?> getExtraColumnType(Object propertyId) {
		Class<?> type;
		if (DISPOSAL_TYPE_PROPERTY.equals(propertyId)) {
			type = EnumWithSmallCodeComboBox.class;
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return type;
	}

	@Override
	protected int getExtraColumnWidth(Object propertyId) {
		int width;
		if (DISPOSAL_TYPE_PROPERTY.equals(propertyId)) {
			width = 200;
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return width;
	}

}
