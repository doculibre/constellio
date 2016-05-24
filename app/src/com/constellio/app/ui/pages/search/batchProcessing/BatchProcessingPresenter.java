package com.constellio.app.ui.pages.search.batchProcessing;

import java.util.List;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.enums.BatchProcessingMode;

public interface BatchProcessingPresenter {
	String getOriginType(List<String> selectedRecordIds);

	RecordVO newRecordVO(List<String> selectedRecordIds, String schema, SessionContext sessionContext);

	void simulateButtonClicked(String selectedType, RecordVO viewObject);

	void processBatchButtonClicked(String selectedType, RecordVO viewObject);

	BatchProcessingMode getBatchProcessingMode();

	AppLayerCollectionExtensions getBatchProcessingExtension();

	String getSchema(String schemaType, String type);

	String getTypeSchemaType(String schemaType);

	RecordFieldFactory newRecordFieldFactory(String selectedType);
}
