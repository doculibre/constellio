package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRuleFactory;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
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
import com.constellio.model.utils.EnumWithSmallCodeUtils;

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
		String typeId = fields.getType();

		List<CopyRetentionRule> options;
		if (typeId != null) {
			options = new ArrayList<>();
			if (Folder.SCHEMA_TYPE.equals(request.getSchemaType().getCode())) {
				RetentionRule uniformRule = getUniformFolderRetentionRuleOrNull(request);
				CopyType uniformCopyType = getUniformCopyType(request);

				if (uniformRule != null) {

					Set<String> ids = new HashSet<>();
					for (CopyRetentionRule copyRetentionRule : uniformRule.getCopyRetentionRules()) {
						if (typeId.equals(copyRetentionRule.getTypeId())
								&& copyRetentionRule.getCopyType() == uniformCopyType
								&& !ids.contains(copyRetentionRule.getId())) {
							ids.add(copyRetentionRule.getId());
							options.add(copyRetentionRule);
						}
					}

					if (options.isEmpty()) {
						for (CopyRetentionRule copyRetentionRule : uniformRule.getCopyRetentionRules()) {
							if (copyRetentionRule.getTypeId() == null
									&& copyRetentionRule.getCopyType() == uniformCopyType
									&& !ids.contains(copyRetentionRule.getId())) {
								ids.add(copyRetentionRule.getId());
								options.add(copyRetentionRule);
							}
						}
					}

				}

			}

		} else {

			if (request.getQuery() != null) {
				options = getOptionsWithQuery(request);
			} else {
				options = getOptionsWithIds(request);
			}
		}

		return options;
	}

	private CopyType getUniformCopyType(BatchProcessRequest request) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(request.getSchemaType().getCollection(), appLayerFactory);

		if (request.getQuery() != null) {
			SearchServices searchServices = fields.getConstellioFactories().getModelLayerFactory().newSearchServices();

			SPEQueryResponse response = searchServices
					.query(request.getQuery().setNumberOfRows(0).addFieldFacet(rm.folder.copyStatus().getDataStoreCode()));
			List<FacetValue> values = response.getFieldFacetValues().get(rm.folder.copyStatus().getDataStoreCode());

			if (values.size() == 1) {
				return (CopyType) EnumWithSmallCodeUtils.toEnumWithSmallCode(CopyType.class, values.get(0).getValue());
			} else {
				return null;
			}

		} else {

			CopyType copyType = null;

			for (String aFolderId : request.getIds()) {
				Folder folder = rm.getFolder(aFolderId);
				if (copyType == null) {
					copyType = folder.getCopyStatus();
				} else if (!copyType.equals(folder.getCopyStatus())) {
					return null;
				}
			}

			return copyType;

		}

	}

	private RetentionRule getUniformFolderRetentionRuleOrNull(BatchProcessRequest request) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(request.getSchemaType().getCollection(), appLayerFactory);

		if (request.getQuery() != null) {
			SearchServices searchServices = fields.getConstellioFactories().getModelLayerFactory().newSearchServices();

			SPEQueryResponse response = searchServices
					.query(request.getQuery().setNumberOfRows(0).addFieldFacet(rm.folder.retentionRule().getDataStoreCode()));
			List<FacetValue> values = response.getFieldFacetValues().get(rm.folder.retentionRule().getDataStoreCode());

			if (values.size() == 1) {
				return rm.getRetentionRule(values.get(0).getValue());
			} else {
				return null;
			}

		} else {

			String retentionRuleId = null;

			for (String aFolderId : request.getIds()) {
				Folder folder = rm.getFolder(aFolderId);
				if (retentionRuleId == null) {
					retentionRuleId = folder.getRetentionRule();
				} else if (!retentionRuleId.equals(folder.getRetentionRule())) {
					return null;
				}
			}

			return retentionRuleId == null ? null : rm.getRetentionRule(retentionRuleId);

		}

	}

	private List<CopyRetentionRule> getOptionsWithQuery(BatchProcessRequest request) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(request.getSchemaType().getCollection(), appLayerFactory);
		List<CopyRetentionRule> copyRetentionRules = new ArrayList<>();
		CopyRetentionRuleFactory copyRetentionRuleFactory = new CopyRetentionRuleFactory();
		CopyRetentionRuleInRuleFactory copyRetentionRuleInRuleFactory = new CopyRetentionRuleInRuleFactory();
		SearchServices searchServices = fields.getConstellioFactories().getModelLayerFactory().newSearchServices();

		SPEQueryResponse response = searchServices
				.query(request.getQuery().setNumberOfRows(0).addFieldFacet(rm.folder.applicableCopyRule().getDataStoreCode()));
		Map<String, List<FacetValue>> applicableCopyRuleList = response.getFieldFacetValues();
		for (FacetValue facetValue : applicableCopyRuleList.get(rm.folder.applicableCopyRule().getDataStoreCode())) {
			if (facetValue.getQuantity() == response.getNumFound()) {
				if (request.getSchemaType().getCode().equals(Document.SCHEMA_TYPE)) {
					copyRetentionRules.add(((CopyRetentionRuleInRule) copyRetentionRuleInRuleFactory.build(facetValue.getValue()))
							.getCopyRetentionRule());
				} else {
					copyRetentionRules.add((CopyRetentionRule) copyRetentionRuleFactory.build(facetValue.getValue()));
				}
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
