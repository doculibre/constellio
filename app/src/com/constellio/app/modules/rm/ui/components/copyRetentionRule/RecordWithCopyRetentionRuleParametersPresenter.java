package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import static java.util.Arrays.asList;

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
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
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
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordWithCopyRetentionRuleParametersPresenter {
	RecordWithCopyRetentionRuleParametersFields fields;

	public RecordWithCopyRetentionRuleParametersPresenter(RecordWithCopyRetentionRuleParametersFields fields) {
		this.fields = fields;
	}

	void rmFieldsCreated(RecordVO recordVO) {
		CopyRetentionRuleDependencyField retentionRuleField = fields.getCopyRetentionRuleDependencyField();
		final BatchProcessRequest request = toRequest(fields.getSelectedRecords(), recordVO);
		if (retentionRuleField != null) {
			retentionRuleField.addValueChangeListener(new CopyRetentionRuleDependencyField.RetentionValueChangeListener() {
				@Override
				public void valueChanged(String newValue) {
					updateFields(newValue, request);
				}
			});
		}
	}

	private static List<String> excludedMetadatas = asList(Schemas.IDENTIFIER.getLocalCode(), Schemas.CREATED_ON.getLocalCode(),
			Schemas.MODIFIED_ON.getLocalCode(), RMObject.FORM_CREATED_ON, RMObject.FORM_MODIFIED_ON);

	private BatchProcessRequest toRequest(List<String> selectedRecords, RecordVO recordVO) {
		ConstellioFactories constellioFactories = fields.getConstellioFactories();
		SessionContext sessionContext = fields.getSessionContext();
		User user = presenterService(constellioFactories.getModelLayerFactory()).getCurrentUser(sessionContext);
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(recordVO.getSchema().getCode());
		MetadataSchemaType schemaType = coreSchemas(sessionContext.getCurrentCollection(),
				constellioFactories.getModelLayerFactory()).getTypes().getSchemaType(schemaTypeCode);
		MetadataSchema schema = coreSchemas(sessionContext.getCurrentCollection(),
				constellioFactories.getModelLayerFactory()).getTypes().getSchema(recordVO.getSchema().getCode());
		Map<String, Object> fieldsModifications = new HashMap<>();
		for (MetadataVO metadataVO : recordVO.getMetadatas()) {
			Metadata metadata = schema.get(metadataVO.getLocalCode());
			Object value = recordVO.get(metadataVO);
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL
					&& value != null
					&& !metadata.isSystemReserved()
					&& !excludedMetadatas.contains(metadata.getLocalCode())) {
				fieldsModifications.put(metadataVO.getCode(), value);
			}
		}

		return new BatchProcessRequest(selectedRecords, user, schemaType, fieldsModifications);
	}

	private SchemasRecordsServices coreSchemas(String collection, ModelLayerFactory modelLayerFactory) {
		return new SchemasRecordsServices(collection, modelLayerFactory);
	}

	private PresenterService presenterService(ModelLayerFactory model) {
		return new PresenterService(model);
	}

	private void updateFields(String dependencyRecordId, BatchProcessRequest request) {
		CopyRetentionRuleField copyRetentionRuleField = fields.getCopyRetentionRuleField();
		if (StringUtils.isNotBlank(dependencyRecordId)) {
			List<CopyRetentionRule> copyRetentionRules = getOptions(dependencyRecordId, request);
			filterByType(copyRetentionRules);
			copyRetentionRuleField.setOptions(copyRetentionRules);
			if (copyRetentionRules.size() == 1) {
				copyRetentionRuleField.setFieldValue(copyRetentionRules.get(0).getId());
			}
			if (copyRetentionRules.size() == 0) {
				copyRetentionRuleField.setVisible(false);
			} else {
				copyRetentionRuleField.setVisible(true);
			}
		} else {
			copyRetentionRuleField.setOptions(new ArrayList<CopyRetentionRule>());
			copyRetentionRuleField.setVisible(false);
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

		for (Record record : transaction.getModifiedRecords()) {

			List<CopyRetentionRule> choicesForRecord = new ArrayList<>();

			if (request.getSchemaType().getCode().equals(Folder.SCHEMA_TYPE)) {

				choicesForRecord = rm.wrapFolder(record).getApplicableCopyRules();

			} else if (request.getSchemaType().getCode().equals(Folder.SCHEMA_TYPE)) {

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
