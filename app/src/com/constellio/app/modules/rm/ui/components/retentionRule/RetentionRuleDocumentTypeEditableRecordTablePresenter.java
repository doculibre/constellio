package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleDocumentTypeToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleDocumentTypeVO;
import com.constellio.app.modules.rm.wrappers.RetentionRuleDocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTablePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;

import java.util.List;

public class RetentionRuleDocumentTypeEditableRecordTablePresenter extends EditableRecordsTablePresenter {


	public RetentionRuleDocumentTypeEditableRecordTablePresenter(
			AppLayerFactory appLayerFactory,
			SessionContext sessionContext) {
		super(getSchemas(RetentionRuleDocumentType.SCHEMA_TYPE, appLayerFactory, sessionContext), appLayerFactory, sessionContext);
	}

	@Override
	public RecordToVOBuilder getRecordToVOBuilder() {
		return getRetentionRuleDocumentTypeToVOBuilder();
	}

	static protected RecordToVOBuilder getRetentionRuleDocumentTypeToVOBuilder() {
		return new RetentionRuleDocumentTypeToVOBuilder();
	}

	@Override
	protected List<MetadataVO> getMetadatasThatMustBeHiddenByDefault(List<MetadataVO> availableMetadatas) {
		List<MetadataVO> metadataThatMustBeHiddenByDefault = super.getMetadatasThatMustBeHiddenByDefault(availableMetadatas);

		availableMetadatas.stream()
				.filter(availableMetadata ->
						availableMetadata.getLocalCode().equals(RetentionRuleDocumentType.RETENTION_RULE) ||
						availableMetadata.getLocalCode().equals(RetentionRuleDocumentType.TITLE)
				)
				.forEach(metadataThatMustBeHiddenByDefault::add);

		return metadataThatMustBeHiddenByDefault;
	}

	static protected MetadataSchemaVO getSchemas(String schemaTypeCode, AppLayerFactory appLayerFactory,
												 SessionContext sessionContext) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		MetadataSchemaToVOBuilder schemaToVOBuilder = new MetadataSchemaToVOBuilder();

		return schemaToVOBuilder.build(rm.schemaType(schemaTypeCode).getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
	}



	public RetentionRuleDocumentTypeVO newRetentionRuleDocumentType(VIEW_MODE viewMode) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(getSessionContext().getCurrentCollection(), getAppLayerFactory());
		MetadataSchemaType schemaType = rm.schemaType(RetentionRuleDocumentType.SCHEMA_TYPE);
		MetadataSchema schema = schemaType.getDefaultSchema();

		RecordServices recordServices = getAppLayerFactory().getModelLayerFactory().newRecordServices();

		Record record = recordServices.newRecordWithSchema(schema);

		return (RetentionRuleDocumentTypeVO) getRetentionRuleDocumentTypeToVOBuilder().build(record, viewMode, getSessionContext());
	}
}
