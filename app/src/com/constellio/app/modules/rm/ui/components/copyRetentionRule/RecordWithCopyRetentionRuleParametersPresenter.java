package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class RecordWithCopyRetentionRuleParametersPresenter {
	RecordWithCopyRetentionRuleParametersFields fields;

	public RecordWithCopyRetentionRuleParametersPresenter(RecordWithCopyRetentionRuleParametersFields fields) {
		this.fields = fields;
	}

	void rmFieldsCreated() {
		CopyRetentionRuleDependencyField retentionRuleField = fields.getCopyRetentionRuleDependencyField();
		if (retentionRuleField != null) {
			retentionRuleField.addValueChangeListener(new CopyRetentionRuleDependencyField.RetentionValueChangeListener() {
				@Override
				public void valueChanged(String newValue) {
					updateFields();
				}
			});
		}
	}

	BatchProcessRequest toRequest() {
		ConstellioFactories constellioFactories = fields.getConstellioFactories();
		SessionContext sessionContext = fields.getSessionContext();
		User user = presenterService(constellioFactories.getModelLayerFactory()).getCurrentUser(sessionContext);
		String schemaTypeCode = fields.getSchemaType();
		SchemasRecordsServices schemas = coreSchemas(sessionContext.getCurrentCollection(),
				constellioFactories.getModelLayerFactory());
		MetadataSchemaTypes types = schemas.getTypes();
		MetadataSchemaType schemaType = types.getSchemaType(schemaTypeCode);
		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		Map<String, Object> fieldsModifications = new HashMap<>();
		if (StringUtils.isNotBlank(fields.getType())) {
			Metadata typeMetadata = defaultSchema.getMetadata("type");
			fieldsModifications.put(typeMetadata.getCode(), fields.getType());
		}
		String dependencyValue = getDependencyValue();
		if (StringUtils.isNotBlank(dependencyValue)) {
			Metadata copyRetentionRuleDependencyMetadata = getCopyRetentionRuleDependencyMetadata(defaultSchema);
			fieldsModifications.put(copyRetentionRuleDependencyMetadata.getCode(), dependencyValue);
		}
		//fieldsModifications.put(Folder.COPY_STATUS_ENTERED, CopyType.PRINCIPAL);
		return new BatchProcessRequest(fields.getSelectedRecords(), fields.getQuery(), user, schemaType, fieldsModifications);
	}

	String getDependencyValue() {
		return fields.getCopyRetentionRuleDependencyField().getFieldValue();
	}

	private Metadata getCopyRetentionRuleDependencyMetadata(MetadataSchema defaultSchema) {
		String schemaTypeCode = fields.getSchemaType();
		if (schemaTypeCode.equals(Folder.SCHEMA_TYPE)) {
			return defaultSchema.getMetadata(Folder.RETENTION_RULE_ENTERED);
		} else if (schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
			return defaultSchema.getMetadata(Document.FOLDER);
		} else {
			throw new RuntimeException("Unsupported schema type " + schemaTypeCode);
		}
	}

	private SchemasRecordsServices coreSchemas(String collection, ModelLayerFactory modelLayerFactory) {
		return new SchemasRecordsServices(collection, modelLayerFactory);
	}

	private PresenterService presenterService(ModelLayerFactory model) {
		return new PresenterService(model);
	}

	public void updateFields() {
		CopyRetentionRuleField copyRetentionRuleField = fields.getCopyRetentionRuleField();
		BatchProcessRequest request = toRequest();
		List<CopyRetentionRule> copyRetentionRules = getOptions(request);
		copyRetentionRuleField.setOptions(copyRetentionRules);
		if (copyRetentionRules.size() == 1) {
			copyRetentionRuleField.setFieldValue(copyRetentionRules.get(0).getId());
		}
		if (copyRetentionRules.size() <= 1) {
			copyRetentionRuleField.setVisible(false);
		} else {
			copyRetentionRuleField.setVisible(true);
		}
	}

	List<CopyRetentionRule> getOptions(BatchProcessRequest request) {

		if(request.getQuery() != null) {
			return getOptionsWithQuery(request);
		}
		else {
			return getOptionsWithIds(request);
		}
	}

	private List<CopyRetentionRule> getOptionsWithQuery(BatchProcessRequest request) {
		List<CopyRetentionRule> copyRetentionRules = new ArrayList<>();
		CopyRetentionRuleFactory copyRetentionRuleFactory = new CopyRetentionRuleFactory();
		SearchServices searchServices = fields.getConstellioFactories().getModelLayerFactory().newSearchServices();

		SPEQueryResponse response = searchServices.query(request.getQuery().setNumberOfRows(0).addFieldFacet("applicableCopyRule_ss"));
		Map<String, List<FacetValue>> applicableCopyRuleList = response.getFieldFacetValues();
		for(FacetValue facetValue: applicableCopyRuleList.get("applicableCopyRule_ss")) {
			if(facetValue.getQuantity() == response.getNumFound()) {
				copyRetentionRules.add((CopyRetentionRule) copyRetentionRuleFactory.build(facetValue.getValue()));
			}
		}


		return copyRetentionRules;
	}

	private List<CopyRetentionRule> getOptionsWithIds(BatchProcessRequest request) {
		ConstellioFactories constellioFactories = fields.getConstellioFactories();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		RecordServices recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(request.getSchemaType().getCollection(), appLayerFactory);

		String typeId = fields.getType();
		String collection = fields.getSessionContext().getCurrentCollection();
		Locale locale = fields.getSessionContext().getCurrentLocale();
		BatchProcessingPresenterService presenterService = new BatchProcessingPresenterService(collection, appLayerFactory,
				locale);

		SchemaUtils schemaUtils = new SchemaUtils();
		Transaction transaction = presenterService.prepareTransaction(request, false);
		for (Record record : transaction.getRecords()) {
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = schemaUtils.getSchemaTypeCode(schemaCode);
			if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				rm.wrapFolder(record).setMediumTypes(new ArrayList<Object>());
			}
		}
		for (Record record : transaction.getRecords()) {
			recordServices.recalculate(record);
		}
		Set<String> sharedChoicesIds = null;
		List<CopyRetentionRule> sharedChoices = null;

		for (Record record : transaction.getRecords()) {

			List<CopyRetentionRule> choicesForRecord = new ArrayList<>();

			if (request.getSchemaType().getCode().equals(Folder.SCHEMA_TYPE)) {

				choicesForRecord = rm.wrapFolder(record).getApplicableCopyRules();

			} else if (request.getSchemaType().getCode().equals(Document.SCHEMA_TYPE)) {

				choicesForRecord = new ArrayList<>();
				for (CopyRetentionRuleInRule inRule : rm.wrapDocument(record).getApplicableCopyRules()) {
					choicesForRecord.add(inRule.getCopyRetentionRule());
				}

			} else {
				throw new ImpossibleRuntimeException("Unsupported type : " + request.getSchemaType().getCode());
			}

			if (sharedChoicesIds == null) {
				sharedChoices = choicesForRecord;
				sharedChoicesIds = toIds(sharedChoices);
			} else {
				if (!sharedChoicesIds.equals(toIds(choicesForRecord))) {
					return new ArrayList<>();
				}
			}
		}

		return sharedChoices;
	}

	private Set<String> toIds(List<CopyRetentionRule> copyRetentionRules) {

		Set<String> ids = new HashSet<>();

		for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
			ids.add(copyRetentionRule.getId());
		}

		return ids;
	}
}
