package com.constellio.app.modules.rm.extensions;

import static com.constellio.app.modules.rm.model.CopyRetentionRuleFactory.variablePeriodCode;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RMSchemasLogicalDeleteExtension extends RecordExtension {

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	SearchServices searchServices;

	RecordServices recordServices;

	public RMSchemasLogicalDeleteExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		Record deletedRule = event.getRecord();
		if (event.isSchemaType(RetentionRule.SCHEMA_TYPE)) {
			Transaction transaction = new Transaction();

			List<Category> categories = rm.wrapCategories(searchServices.search(new LogicalSearchQuery()
					.setCondition(from(rm.categorySchemaType()).where(rm.categoryRetentionRules()).isEqualTo(deletedRule))));
			for (Category category : categories) {
				List<String> rules = new ArrayList<>(category.getRententionRules());
				rules.remove(deletedRule.getId());
				category.setRetentionRules(rules);
				transaction.add(category);
			}

			List<UniformSubdivision> uniformSubdivisions = rm.wrapUniformSubdivisions(searchServices.search(
					new LogicalSearchQuery().setCondition(from(rm.uniformSubdivision.schemaType())
							.where(rm.uniformSubdivision.retentionRule()).isEqualTo(deletedRule))));
			for (UniformSubdivision uniformSubdivision : uniformSubdivisions) {
				List<String> rules = new ArrayList<>(uniformSubdivision.getRetentionRules());
				rules.remove(deletedRule.getId());
				uniformSubdivision.setRetentionRules(rules);
				transaction.add(uniformSubdivision);
			}

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public ExtensionBooleanResult isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		if (event.isSchemaType(VariableRetentionPeriod.SCHEMA_TYPE)) {
			return isVariableRetentionPeriodLogicallyDeletable(event);

		} else if (event.isSchemaType(RetentionRule.SCHEMA_TYPE)) {
			return isRetentionRuleLogicallyDeletable(event);

		} else if (event.isSchemaType(FilingSpace.SCHEMA_TYPE)) {
			return isFilingSpaceLogicallyDeletable(event);

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}

	}

	private ExtensionBooleanResult isFilingSpaceLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.falseIf(event.isRecordReferenced());
	}

	private ExtensionBooleanResult isRetentionRuleLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		boolean logicallyDeletable = !searchServices.hasResults(from(rm.folderSchemaType())
				.where(rm.folderRetentionRule()).isEqualTo(event.getRecord()));

		return ExtensionBooleanResult.forceTrueIf(logicallyDeletable);
	}

	private ExtensionBooleanResult isVariableRetentionPeriodLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		VariableRetentionPeriod variableRetentionPeriod = rm.wrapVariableRetentionPeriod(event.getRecord());
		String code = variableRetentionPeriod.getCode();
		if (code.equals("888") || code.equals("999")) {
			return ExtensionBooleanResult.FALSE;
		} else {
			long count = searchServices.getResultsCount(from(rm.retentionRule.schemaType())
					.where(rm.retentionRule.copyRetentionRules()).is(variablePeriodCode(code)));
			return ExtensionBooleanResult.trueIf(count == 0);
		}
	}
}
