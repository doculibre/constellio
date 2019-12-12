package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.model.CopyRetentionRuleFactory.variablePeriodCode;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasExcept;
import static java.util.Arrays.asList;

public class RMSchemasLogicalDeleteExtension extends RecordExtension {

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	TasksSchemasRecordsServices taskSchemas;

	SearchServices searchServices;

	RecordServices recordServices;
	RecordHierarchyServices recordHierarchyServices;

	public RMSchemasLogicalDeleteExtension(String collection, AppLayerFactory appLayerFactory) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		recordHierarchyServices = new RecordHierarchyServices(modelLayerFactory);
	}

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		Record deletedRule = event.getRecord();
		if (event.isSchemaType(RetentionRule.SCHEMA_TYPE)) {
			Transaction transaction = new Transaction();

			List<Category> categories = rm.wrapCategorys(searchServices.search(new LogicalSearchQuery()
					.setCondition(from(rm.category.schemaType()).where(rm.category.retentionRules()).isEqualTo(deletedRule))));
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
	public ValidationErrors validateLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		if (event.isSchemaType(VariableRetentionPeriod.SCHEMA_TYPE)) {
			return isVariableRetentionPeriodLogicallyDeletable(event);

		} else if (event.isSchemaType(RetentionRule.SCHEMA_TYPE)) {
			return isRetentionRuleLogicallyDeletable(event);

		} else if (event.isSchemaType(FilingSpace.SCHEMA_TYPE)) {
			return isFilingSpaceLogicallyDeletable(event);

		} else if (event.isSchemaType(AdministrativeUnit.SCHEMA_TYPE)) {
			return isAdministrativeUnitLogicallyDeletable(event);

		} else if (event.isSchemaType(Category.SCHEMA_TYPE)) {
			return isCategoryLogicallyDeletable(event);

		} else if (event.isSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			return isContainerLogicallyDeletable(event);

		} else if (event.isSchemaType(Folder.SCHEMA_TYPE)) {
			return isFolderLogicallyDeletable(event);

		} else {
			return new ValidationErrors();
		}

	}

	private ValidationErrors isAdministrativeUnitLogicallyDeletable(
			RecordLogicalDeletionValidationEvent event) {
		ValidationErrors validationErrors = new ValidationErrors();
		LogicalSearchQuery query = filteredQueryForErrorMessages(fromAllSchemasExcept(asList(rm.administrativeUnit.schemaType()))
				.where(Schemas.PATH).isContainingText(event.getRecord().getId()));
		List<Record> recodsInAdministrativeUnit = searchServices.search(query);
		if (event.isRecordReferenced()) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "administrativeUnitIsReferenced");
		}
		if (!recodsInAdministrativeUnit.isEmpty()) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "recordInAdministrativeUnit", toRecordsParameter(recodsInAdministrativeUnit));
		}
		return validationErrors;
	}

	private ValidationErrors isCategoryLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		ValidationErrors validationErrors = new ValidationErrors();
		LogicalSearchQuery logicalSearchQuery = filteredQueryForErrorMessages(fromAllSchemasExcept(asList(rm.category.schemaType()))
				.where(Schemas.PATH).isContainingText(event.getRecord().getId()));
		List<Record> recodsInCategory = searchServices.search(logicalSearchQuery);
		if (event.isRecordReferenced()) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "categoryIsReferenced");
		}
		if (!recodsInCategory.isEmpty()) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "recordInCategory", toRecordsParameter(recodsInCategory));
		}
		return validationErrors;
	}

	private ValidationErrors isFolderLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		//TODO check if user can delete borrowed documents
		ValidationErrors validationErrors = new ValidationErrors();
		long countForBorrowedDocuments = getBorrowedDocumentsInHierarchy(event.getRecord()).size();

		if (countForBorrowedDocuments != 0) {
			User user = event.getUser();
			if (user != null) {
				List<Record> borrowedDocumentsThatUserCanDelete = getBorrowedDocumentsThatUserCanDelete(user, event.getRecord());
				long countForBorrowedDocumentsThatCurrentUserCanDelete = borrowedDocumentsThatUserCanDelete.size();
				if (countForBorrowedDocumentsThatCurrentUserCanDelete != countForBorrowedDocuments) {
					LogicalSearchQuery query = buildBorrowedDocumentsQuery(event).filteredWithUser(user, RMPermissionsTo.DELETE_BORROWED_DOCUMENT);
					List<Record> checkedOutDocument = searchServices.search(query);
					validationErrors.add(RMSchemasLogicalDeleteExtension.class, "folderWithCheckoutDocuments", toRecordsParameter(checkedOutDocument));
				}

			} else {
				List<Record> checkedOutDocument = searchServices.search(buildBorrowedDocumentsQuery(event));
				validationErrors.add(RMSchemasLogicalDeleteExtension.class, "folderWithCheckoutDocuments", toRecordsParameter(checkedOutDocument));
			}
		}

		if (!event.isThenPhysicallyDeleted()) {
			List<Record> tasks = searchServices.search(filteredQueryForErrorMessages(from(rm.userTask.schemaType())
					.where(rm.userTask.linkedFolders()).isEqualTo(event.getRecord().getId())
					.andWhere(taskSchemas.userTask.status()).isNotIn(taskSchemas.getFinishedOrClosedStatuses())
					.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()));
			if (!tasks.isEmpty()) {
				validationErrors.add(RMSchemasLogicalDeleteExtension.class, "folderLinkedToTask", toRecordsParameter(tasks));
			}
		}

		return validationErrors;
	}

	private List<Record> getBorrowedDocumentsInHierarchy(Record record) {
		return recordHierarchyServices.getAllRecordsInHierarchy(record, true).stream()
				.filter(recordHierarchy -> recordHierarchy.isOfSchemaType(Document.SCHEMA_TYPE) &&
										   rm.wrapDocument(recordHierarchy).getContentCheckedOutBy() != null)
				.collect(Collectors.toList());
	}

	private List<Record> getBorrowedDocumentsThatUserCanDelete(User user, Record record) {
		return getBorrowedDocumentsInHierarchy(record).stream()
				.filter(recordHierarchy -> user.has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT).on(recordHierarchy))
				.collect(Collectors.toList());
	}

	private LogicalSearchQuery buildBorrowedDocumentsQuery(RecordLogicalDeletionValidationEvent event) {

		return filteredQueryForErrorMessages(from(rm.document.schemaType())
				.where(rm.document.contentCheckedOutBy()).isNotNull()
				.andWhere(rm.document.folder()).isEqualTo(event.getRecord()));
	}

	private ValidationErrors isContainerLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		ValidationErrors validationErrors = new ValidationErrors();
		List<Record> tasks = searchServices.search(filteredQueryForErrorMessages(from(rm.userTask.schemaType())
						.where(rm.userTask.linkedContainers()).isContaining(asList(event.getRecord().getId()))
						.andWhere(taskSchemas.userTask.status()).isNotIn(taskSchemas.getFinishedOrClosedStatuses())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()
		));
		if (!tasks.isEmpty()) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "containerLinkedToTask", toRecordsParameter(tasks));
		}
		return validationErrors;
	}

	private ValidationErrors isFilingSpaceLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		ValidationErrors validationErrors = new ValidationErrors();
		if (event.isRecordReferenced()) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "referencedRecord");
		}
		return validationErrors;
	}

	private ValidationErrors isRetentionRuleLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		ValidationErrors validationErrors = new ValidationErrors();
		List<Record> folderUsingRetentionRules = searchServices.search(filteredQueryForErrorMessages(from(rm.folder.schemaType())
				.where(rm.folder.retentionRule()).isEqualTo(event.getRecord())));
		if (!folderUsingRetentionRules.isEmpty()) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "retentionRuleUsedByFolder", toRecordsParameter(folderUsingRetentionRules));
		}
		return validationErrors;
	}

	private ValidationErrors isVariableRetentionPeriodLogicallyDeletable(
			RecordLogicalDeletionValidationEvent event) {
		ValidationErrors validationErrors = new ValidationErrors();
		VariableRetentionPeriod variableRetentionPeriod = rm.wrapVariableRetentionPeriod(event.getRecord());
		String code = variableRetentionPeriod.getCode();
		if (code.equals("888") || code.equals("999")) {
			validationErrors.add(RMSchemasLogicalDeleteExtension.class, "cannotDelete888Or999VariableRetentionPeriod");
			return validationErrors;
		} else {
			List<Record> retentionRulesUsingVariablePeriod = searchServices.search(filteredQueryForErrorMessages(from(rm.retentionRule.schemaType())
					.where(rm.retentionRule.copyRetentionRules()).is(variablePeriodCode(code))));
			if (!retentionRulesUsingVariablePeriod.isEmpty()) {
				validationErrors.add(RMSchemasLogicalDeleteExtension.class, "variablePeriodTypeIsUsedInRetentionRule", toRecordsParameter(retentionRulesUsingVariablePeriod));
			}
			return validationErrors;
		}
	}

	private Map<String, Object> toRecordsParameter(List<Record> records) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("records", records);
		return parameters;
	}

	private LogicalSearchQuery filteredQueryForErrorMessages(LogicalSearchCondition logicalSearchCondition) {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		return logicalSearchQuery.setNumberOfRows(10).setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.TITLE, Schemas.LOGICALLY_DELETED_STATUS));
	}
}
