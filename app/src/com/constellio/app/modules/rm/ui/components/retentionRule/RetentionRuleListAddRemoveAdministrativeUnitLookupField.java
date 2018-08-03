package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

@SuppressWarnings("unchecked")
public class RetentionRuleListAddRemoveAdministrativeUnitLookupField extends ListAddRemoveRecordLookupField {

	private static final String SEPARATOR = " > ";

	public RetentionRuleListAddRemoveAdministrativeUnitLookupField() {
		super(AdministrativeUnit.SCHEMA_TYPE);
	}

	@Override
	protected String getItemCaption(Object itemId) {
		return super.getItemCaption(itemId);
	}

	@Override
	protected Component newCaptionComponent(String itemId, String caption) {
		Label component = new Label(caption);
		component.setContentMode(ContentMode.HTML);
		RecordIdToCaptionConverter recordIdToCaptionConverter = new RecordIdToCaptionConverter();
		ConstellioUI ui = ConstellioUI.getCurrent();
		String collection = ui.getSessionContext().getCurrentCollection();
		ModelLayerFactory modelLayerFactory = ui.getConstellioFactories().getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, ui);

		Record record = recordServices.getDocumentById(itemId);
		String id = record.get(rmSchemasRecordsServices.administrativeUnit.parent());
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(caption);
		while (id != null) {
			record = recordServices.getDocumentById(id);
			String value = recordIdToCaptionConverter.convertToPresentation(id, String.class, getLocale()) + SEPARATOR;
			stringBuilder.insert(0, value);
			id = record.get(rmSchemasRecordsServices.administrativeUnit.parent());
		}
		component.setValue(stringBuilder.toString());
		return component;
	}
}
