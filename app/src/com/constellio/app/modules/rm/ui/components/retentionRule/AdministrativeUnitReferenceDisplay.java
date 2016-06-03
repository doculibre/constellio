package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

public class AdministrativeUnitReferenceDisplay extends ReferenceDisplay {

	private static final String SEPARATOR = " > ";

	public AdministrativeUnitReferenceDisplay(RecordVO recordVO) {
		super(recordVO);
	}

	public AdministrativeUnitReferenceDisplay(String recordId) {
		super(recordId);
	}

	@Override
	public void setCaption(String caption) {

		RecordIdToCaptionConverter recordIdToCaptionConverter = new RecordIdToCaptionConverter();
		ConstellioUI ui = ConstellioUI.getCurrent();
		String collection = ui.getSessionContext().getCurrentCollection();
		ModelLayerFactory modelLayerFactory = ui.getConstellioFactories().getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, ui);

		Record record = recordServices.getDocumentById(getRecordId());
		String id = record.get(rmSchemasRecordsServices.administrativeUnit.parent());
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(caption);
		while (id != null) {
			record = recordServices.getDocumentById(id);
			String value = recordIdToCaptionConverter.convertToPresentation(id, String.class, getLocale()) + SEPARATOR;
			stringBuilder.insert(0, value);
			id = record.get(rmSchemasRecordsServices.administrativeUnit.parent());
		}
		super.setCaption(stringBuilder.toString());
	}
}
