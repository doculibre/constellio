package com.constellio.app.modules.rm.ui.pages.legalrequirement.component;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.LegalRequirementReference;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTablePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class LegalRequirementReferenceEditableRecordTablePresenter extends EditableRecordsTablePresenter {

	private SearchServices searchServices;
	private RMSchemasRecordsServices rm;
	private SchemaPresenterUtils schemaPresenterUtils;
	private RecordServices recordServices;

	private String legalRequirementId;

	public LegalRequirementReferenceEditableRecordTablePresenter(AppLayerFactory appLayerFactory,
																 SessionContext sessionContext, String requirementId) {
		super(getSchemas(LegalRequirementReference.SCHEMA_TYPE, appLayerFactory, sessionContext), appLayerFactory, sessionContext);
		legalRequirementId = requirementId;

		searchServices = getAppLayerFactory().getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(getSessionContext().getCurrentCollection(), getAppLayerFactory());
		schemaPresenterUtils = new SchemaPresenterUtils(LegalRequirementReference.DEFAULT_SCHEMA,
				ConstellioFactories.getInstance(), getSessionContext());
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		setUseTheseRecordIdsInstead(getRecordIds());
	}

	private List<String> getRecordIds() {
		LogicalSearchCondition condition = from(rm.legalRequirementReference.schemaType())
				.where(rm.legalRequirementReference.ruleRequirement()).isEqualTo(legalRequirementId)
				.andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull();

		return searchServices.searchRecordIds(condition);
	}

	@Override
	protected List<MetadataVO> getMetadatasThatMustBeHiddenByDefault(List<MetadataVO> availableMetadatas) {
		List<MetadataVO> metadataThatMustBeHiddenByDefault = super.getMetadatasThatMustBeHiddenByDefault(availableMetadatas);

		availableMetadatas.stream()
				.filter(availableMetadata ->
						availableMetadata.getLocalCode().equals(LegalRequirementReference.RULE_REQUIREMENT) ||
						availableMetadata.getLocalCode().equals(LegalRequirementReference.TITLE)
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

	public RecordVO newLegalRequirementReferenceRecordVO() {
		LegalRequirementReference requirementReference = rm.newLegalRequirementReference()
				.setRequirement(legalRequirementId);
		return getRecordToVOBuilder().build(requirementReference.getWrappedRecord(), VIEW_MODE.TABLE, getSessionContext());
	}

	public RecordVO updateRecord(RecordVO recordVO) throws RecordServicesException {
		Record record = recordVO.getRecord();
		schemaPresenterUtils.fillRecordUsingRecordVO(record, recordVO, false);
		return recordVO;
	}

	@Override
	protected List<Record> getRecords(List<String> ids) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(getSessionContext().getCurrentCollection(), getAppLayerFactory());

		List<Record> records = new ArrayList<>();
		List<String> addedIds = addedRecords.stream().map(Record::getId).collect(Collectors.toList());
		List<String> updatedIds = updatedRecords.stream().map(Record::getId).collect(Collectors.toList());
		for (String id : ids) {
			if (addedIds.contains(id)) {
				Record addedRecord = addedRecords.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
				records.add(addedRecord);
			} else if (updatedIds.contains(id)) {
				Record updatedRecord = updatedRecords.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
				records.add(updatedRecord);
			} else {
				records.add(rm.get(id));
			}
		}
		return records;
	}

	@Override
	public void setUseTheseRecordIdsInstead(List<String> useTheseIdsInstead) {
		this.useTheseIdsInstead = useTheseIdsInstead;
		loadedRecords = null;
	}
}
