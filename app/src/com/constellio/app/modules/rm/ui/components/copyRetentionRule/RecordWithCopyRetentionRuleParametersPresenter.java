package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
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
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;

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
					updateFields(newValue);
				}
			});
		}
	}

	private BatchProcessRequest toRequest() {
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
		String dependencyValue = fields.getCopyRetentionRuleDependencyField().getFieldValue();
		if (StringUtils.isNotBlank(dependencyValue)) {
			Metadata copyRetentionRuleDependencyMetadata = getCopyRetentionRuleDependencyMetadata(defaultSchema);
			fieldsModifications.put(copyRetentionRuleDependencyMetadata.getCode(), dependencyValue);
		}
		fieldsModifications.put(Folder.COPY_STATUS_ENTERED, CopyType.PRINCIPAL);
		return new BatchProcessRequest(fields.getSelectedRecords(), user, schemaType, fieldsModifications);
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

	public void updateFields(String dependencyRecordId) {
		CopyRetentionRuleField copyRetentionRuleField = fields.getCopyRetentionRuleField();
		BatchProcessRequest request = toRequest();
		List<CopyRetentionRule> copyRetentionRules = getOptions(dependencyRecordId, request);
		copyRetentionRuleField.setOptions(copyRetentionRules);
		if (copyRetentionRules.size() == 1) {
			copyRetentionRuleField.setFieldValue(copyRetentionRules.get(0).getId());
		}
		if (copyRetentionRules.size() == 0) {
			copyRetentionRuleField.setVisible(false);
		} else {
			copyRetentionRuleField.setVisible(true);
		}
	}

	private void filterByType(final List<CopyRetentionRule> copyRetentionRules) {
		if (StringUtils.isBlank(fields.getType())) {
			return;
		}
		CollectionUtils.filter(copyRetentionRules, new Predicate() {
			@Override
			public boolean evaluate(Object arg) {
				if (arg instanceof CopyRetentionRule) {
					if (((CopyRetentionRule) arg).getTypeId().equals(fields.getType())) {
						return true;
					}
				}
				return false;
			}
		});
	}

	private List<CopyRetentionRule> getOptions(String dependencyRecordId, BatchProcessRequest request) {
		String typeId = fields.getType();
		AppLayerFactory appLayerFactory = fields.getConstellioFactories().getAppLayerFactory();
		String collection = fields.getSessionContext().getCurrentCollection();
		Locale locale = fields.getSessionContext().getCurrentLocale();
		BatchProcessingPresenterService presenterService = new BatchProcessingPresenterService(collection, appLayerFactory,
				locale);

		Transaction transaction = presenterService.prepareTransaction(request);

		Set<String> sharedChoicesIds = null;
		List<CopyRetentionRule> sharedChoices = null;

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());

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
