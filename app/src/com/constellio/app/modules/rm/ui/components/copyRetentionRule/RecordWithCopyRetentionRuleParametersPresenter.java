package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;

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

	private void updateFields(String dependencyRecordId) {
		CopyRetentionRuleField copyRetentionRuleField = fields.getCopyRetentionRuleField();
		if (StringUtils.isNotBlank(dependencyRecordId)) {
			List<CopyRetentionRule> copyRetentionRules = getOptions(dependencyRecordId);
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

	public List<CopyRetentionRule> getOptions(String dependencyRecordId) {
		ConstellioFactories constellioFactories = fields.getConstellioFactories();
		SessionContext sessionContext = fields.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		if (fields.getSchemaType().equals(Folder.SCHEMA_TYPE)) {
			RetentionRule retentionRule = rm.getRetentionRule(dependencyRecordId);
			List<CopyRetentionRule> copyRetentionRules = new ArrayList<>(retentionRule.getCopyRetentionRules());

			return copyRetentionRules;
		} else {
			Folder parentFolder = rm.getFolder(dependencyRecordId);

			List<CopyRetentionRule> copyRetentionRules = new ArrayList<>();
			copyRetentionRules.addAll(parentFolder.getApplicableCopyRules());

			return copyRetentionRules;
		}
	}

	public List<CopyRetentionRule> getOptions(String typeId, BatchProcessRequest request) {

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
