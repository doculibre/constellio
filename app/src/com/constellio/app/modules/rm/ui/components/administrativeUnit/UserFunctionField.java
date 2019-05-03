package com.constellio.app.modules.rm.ui.components.administrativeUnit;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.StatusFilter.ACTIVES;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.UserFunction;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

public class UserFunctionField extends CustomField<UserFunctionItem> {
	
	private UserFunctionItem delayedValue;
	
	private I18NHorizontalLayout mainLayout;
	
	private LookupField<String> lookupUserField;
	
	private ComboBox functionField;
	
	private RecordIdToCaptionConverter idToCaptionConverter = new RecordIdToCaptionConverter();
	
	@Override
	protected Component initContent() {
		addStyleName("select-function-field");
		
		mainLayout = new I18NHorizontalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);
		
		functionField = new BaseComboBox();
		functionField.setCaption($("UserFunctionField.function"));
		functionField.setTextInputAllowed(false);
		
		List<String> functionIds = getFunctionIds();
		for (String functionId : functionIds) {
			String functionLabel = idToCaptionConverter.convertToPresentation(functionId, String.class, getLocale());
			functionField.addItem(functionId);
			functionField.setItemCaption(functionId, functionLabel);
		}
		
		lookupUserField = new LookupRecordField(User.SCHEMA_TYPE);
		lookupUserField.setCaption($("UserFunctionField.user"));
		
		if (delayedValue != null) {
			functionField.setValue(delayedValue.getFunctionId());
			lookupUserField.setValue(delayedValue.getUserId());
			delayedValue = null;
		}
		
		mainLayout.addComponents(functionField, lookupUserField);
		
		return mainLayout;
	}
	
	private List<String> getFunctionIds() {
		List<String> functionIds = new ArrayList<>();
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();

		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(UserFunction.SCHEMA_TYPE);
		LogicalSearchCondition condition = from(schema).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition).filteredByStatus(ACTIVES).sortAsc(Schemas.TITLE);
		
		Iterator<Record> it = modelLayerFactory.newSearchServices().search(query).iterator();
		while (it.hasNext()) {
			Record userFunctionRecord = it.next();
			functionIds.add(userFunctionRecord.getId());
		}
		return functionIds;
	}

	@Override
	public Object getConvertedValue() {
		Object convertedValue;
		String functionId = (String) functionField.getValue();
		String userId = (String) lookupUserField.getValue();
		if (functionId != null && userId != null) {
			convertedValue = new UserFunctionItem(functionId, userId);
		} else {
			convertedValue = null;
		}
		return convertedValue;
	}

	@Override
	protected void setValue(UserFunctionItem newFieldValue, boolean repaintIsNotNeeded, boolean ignoreReadOnly)
			throws ReadOnlyException, ConversionException, InvalidValueException {
		if (functionField != null && lookupUserField != null) {
			String newFunctionId;
			String newUserId;
			if (newFieldValue != null) {
				newFunctionId = newFieldValue.getFunctionId();
				newUserId = newFieldValue.getUserId();
			} else {
				newFunctionId = null;
				newUserId = null;
			}
			functionField.setValue(newFunctionId);
			lookupUserField.setValue(newUserId);
		} else {
			delayedValue = newFieldValue;
		}
	}

	@Override
	public Class<? extends UserFunctionItem> getType() {
		return UserFunctionItem.class;
	}
	
}
