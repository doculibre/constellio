package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.services.records.RecordServicesException;

import java.io.InputStream;
import java.util.List;

public interface BatchProcessingPresenter {
	String getOriginType();

	RecordVO newRecordVO(String schema, SessionContext sessionContext);

	InputStream simulateButtonClicked(String selectedType, RecordVO viewObject) throws RecordServicesException;

	void processBatchButtonClicked(String selectedType, RecordVO viewObject) throws RecordServicesException;

	BatchProcessingMode getBatchProcessingMode();

	AppLayerCollectionExtensions getBatchProcessingExtension();

	String getSchema(String schemaType, String type);

	String getTypeSchemaType(String schemaType);

	RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType);

	boolean hasWriteAccessOnAllRecords();

	long getNumberOfRecords();

	void allSearchResultsButtonClicked();

	void selectedSearchResultsButtonClicked();

	boolean isSearchResultsSelectionForm();
	
}
