package com.constellio.app.extensions.records;

import com.constellio.app.extensions.records.params.NavigationParams;

public interface RecordNavigationExtension {

	//	private final String collection;
	//	protected RecordServices recordServices;
	//
	//	public RecordNavigationExtension(String collection, AppLayerFactory appLayerFactory) {
	//		this.collection = collection;
	//		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	//	}

	//	public boolean isViewForRecordId(String recordId) {
	//		String schemaCode = getSchemaCodeForRecordId(recordId);
	//		return isViewForSchemaCode(schemaCode);
	//	}
	//
	//	private boolean isViewForSchemaCode(String schemaCode) {
	//		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
	//		return isViewForSchemaTypeCode(schemaTypeCode);
	//	}
	//
	//	public boolean isViewForRecordVO(RecordVO recordVO) {
	//		String schemaCode = recordVO.getSchema().getCode();
	//		return isViewForSchemaCode(schemaCode);
	//	}
	//
	//	protected String getSchemaCodeForRecordId(String recordId) {
	//		Record record = recordServices.getDocumentById(recordId);
	//		return record.getSchemaCode();
	//	}
	//
	//	protected String getSchemaTypeCodeForSchemaCode(String schemaCode) {
	//		return new SchemaUtils().getSchemaTypeCode(schemaCode);
	//	}

	//	public void navigateToView(ConstellioNavigator constellioNavigator, String recordId) {
	//		String schemaCode = getSchemaCodeForRecordId(recordId);
	//		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
	//		navigateToView(constellioNavigator, recordId, schemaTypeCode);
	//	}
	//
	//	public void navigateToView(ConstellioNavigator constellioNavigator, RecordVO recordVO) {
	//		String schemaCode = recordVO.getSchema().getCode();
	//		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
	//		navigateToView(constellioNavigator, recordVO.getId(), schemaTypeCode);
	//	}
	//
	//	public void navigateToEdit(ConstellioNavigator constellioNavigator, RecordVO recordVO) {
	//		String schemaCode = recordVO.getSchema().getCode();
	//		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
	//		navigateToEdit(constellioNavigator, recordVO.getId(), schemaTypeCode);
	//	}
	//
	//	public void navigateToEdit(ConstellioNavigator constellioNavigator, String recordId) {
	//		String schemaCode = getSchemaCodeForRecordId(recordId);
	//		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
	//		navigateToEdit(constellioNavigator, recordId, schemaTypeCode);
	//	}

	void navigateToEdit(NavigationParams navigationParams);

	void navigateToView(NavigationParams navigationParams);

	boolean isViewForSchemaTypeCode(String schemaTypeCode);

	void prepareLinkToView(NavigationParams navigationParams);

}
