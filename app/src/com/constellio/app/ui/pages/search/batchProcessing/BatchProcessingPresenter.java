package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServicesException;

import java.io.InputStream;
import java.util.List;

public interface BatchProcessingPresenter {
	String getOriginType(String schemaType);

	RecordVO newRecordVO(String schema, String schemaType, SessionContext sessionContext);

	InputStream simulateButtonClicked(String selectedType, String schemaType, RecordVO viewObject)
			throws RecordServicesException;

	boolean processBatchButtonClicked(String selectedType, String schemaType, RecordVO viewObject)
			throws RecordServicesException;

	AppLayerCollectionExtensions getBatchProcessingExtension();

	String getSchema(String schemaType, String type);

	String getTypeSchemaType(String schemaType);

	RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType);

	boolean hasWriteAccessOnAllRecords(String schemaType);

	long getNumberOfRecords(String schemaType);

	void allSearchResultsButtonClicked();

	void selectedSearchResultsButtonClicked();

	boolean isSearchResultsSelectionForm();

	boolean batchEditRequested(String code, Object convertedValue, String schemaType);

	List<MetadataVO> getMetadataAllowedInBatchEdit(String schemaType);

	ValidationErrors validateBatchProcessing();

	boolean validateUserHaveBatchProcessPermissionOnAllRecords(String schemaType);
}
