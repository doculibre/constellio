package com.constellio.app.services.metadata;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.metadata.AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotDeleteSchema;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

public class AppSchemasServices {

	private static final String CANNOT_DELETE_DEFAULT_SCHEMA = "cannotDeleteDefaultSchema";
	private static final String METADATA_REFERENCING_SCHEMA = "metadatasReferencingTheSchema";
	private static final String EXISTING_RECORDS_WITH_SCHEMA = "existingRecordsWithSchema";

	private final SchemasDisplayManager schemasDisplayManager;
	private final AppLayerFactory appLayerFactory;
	private final MetadataSchemasManager schemasManager;
	private final SearchServices searchServices;
	private final RecordServices recordServices;

	public AppSchemasServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

	}

	public ValidationErrors isSchemaDeletable(String collection, String schemaCode) {
		ValidationErrors validationErrors = null;
		List<String> references = getReferencesWithDirectAllowedReference(collection, schemaCode);
		MetadataSchema schema = schemasManager.getSchemaTypes(collection).getSchema(schemaCode);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("schemaTitle", schema.getLabels());

		if (schemaCode.endsWith("default")) {
			validationErrors = newValidationErrors(CANNOT_DELETE_DEFAULT_SCHEMA, parameters);
		} else if (!references.isEmpty()) {
			setParametersForMetadataReferencingSchemaError(parameters, collection, schemaCode);
			validationErrors = newValidationErrors(METADATA_REFERENCING_SCHEMA, parameters);
		} else if (searchServices.hasResults(from(schema).returnAll())) {
			setParametersForExistingRecordsWithSchemaError(parameters, schema);
			validationErrors = newValidationErrors(EXISTING_RECORDS_WITH_SCHEMA, parameters);
		}
		return validationErrors;
	}

	public List<Record> getVisibleRecords(String collection, String schemaCode, User user, int numberOfRecords) {
		MetadataSchema metadataSchema = schemasManager.getSchemaTypes(collection).getSchema(schemaCode);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(metadataSchema).returnAll()).filteredWithUser(user, Role.READ);
		List<Record> records = searchServices.search(logicalSearchQuery);
		if (records.size() > numberOfRecords) {
			return records.subList(0, numberOfRecords);
		} else {
			return records;
		}
	}

	public boolean areAllRecordsVisible(String collection, String schemaCode, User user) {
		MetadataSchema metadataSchema = schemasManager.getSchemaTypes(collection).getSchema(schemaCode);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(metadataSchema).returnAll()).filteredWithUser(user, Role.READ);
		Long numberOfVisibleRecords = searchServices.getResultsCount(logicalSearchQuery);
		Long numberOfRecords = searchServices.getResultsCount(from(metadataSchema).returnAll());
		if (numberOfRecords > numberOfVisibleRecords) {
			return false;
		}
		return true;
	}

	public void deleteSchemaCode(String collection, String schemaCode) throws RecordServicesException {
		ValidationErrors validationErrors = isSchemaDeletable(collection, schemaCode);
		if (validationErrors != null) {
			throw new AppSchemasServicesRuntimeException_CannotDeleteSchema(schemaCode);
		}
		updateRecordsWithLinkedSchemas(collection, schemaCode, null);
		schemasManager.deleteCustomSchemas(asList(schemasManager.getSchemaTypes(collection).getSchema(schemaCode)));
	}

	public boolean modifySchemaCode(String collection, final String fromCode, final String toCode)
			throws RecordServicesException {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		validateModificationAllowed(types, fromCode, toCode);

		final String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(fromCode);

		schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(schemaTypeCode).createCustomSchemaCopying(
						new SchemaUtils().getSchemaLocalCode(toCode), new SchemaUtils().getSchemaLocalCode(fromCode));
				Map<Language, String> labels = types.getSchemaType(schemaTypeCode).getSchema(fromCode).getLabels();

				for (Language language : labels.keySet()) {
					labels.put(language, labels.get(language) + " " + $("AppSchemasServices.toDelete"));
				}

			}
		});

		types = schemasManager.getSchemaTypes(collection);

		modifyReferencesWithDirectTarget(types, fromCode, toCode);
		updateRecordsWithLinkedSchemas(collection, fromCode, toCode);
		boolean async = modifyRecordsAndReturnIfAsyncOperations(collection, types.getSchema(fromCode), types.getSchema(toCode));
		configureNewSchema(collection, fromCode, toCode);

		if (!async) {
			schemasManager.deleteCustomSchemas(asList(types.getSchema(fromCode)));
		}
		return async;
	}

	private void updateRecordsWithLinkedSchemas(String collection, String fromCode, String toCode)
			throws RecordServicesException {
		List<Record> records = searchServices.search(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(collection).where(Schemas.LINKED_SCHEMA).isEqualTo(fromCode)));

		Transaction transaction = new Transaction();

		for (Record record : records) {
			transaction.add(record.set(Schemas.LINKED_SCHEMA, toCode));
		}

		recordServices.execute(transaction);

	}

	private void modifyReferencesWithDirectTarget(MetadataSchemaTypes types, final String fromCode,
												  final String toCode) {
		final List<String> referencesCodeToUpdate = getReferencesWithDirectAllowedReference(types.getCollection(), fromCode);
		if (!referencesCodeToUpdate.isEmpty()) {
			schemasManager.modify(types.getCollection(), new MetadataSchemaTypesAlteration() {

				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					for (String referenceCodeToUpdate : referencesCodeToUpdate) {
						MetadataBuilder metadataBuilder = types.getMetadata(referenceCodeToUpdate);
						Set<String> allowedSchemas = new HashSet<String>(metadataBuilder.defineReferences().getSchemas());
						allowedSchemas.remove(fromCode);
						allowedSchemas.add(toCode);

						metadataBuilder.defineReferences().clearSchemas();
						for (String allowedSchema : allowedSchemas) {
							metadataBuilder.defineReferences().add(types.getSchema(allowedSchema));
						}
					}
				}
			});
		}
	}

	private boolean modifyRecordsAndReturnIfAsyncOperations(String collection, final MetadataSchema originalSchema,
															final MetadataSchema destinationSchema) {

		BatchProcessesManager batchProcessesManager = appLayerFactory.getModelLayerFactory().getBatchProcessesManager();
		LogicalSearchCondition condition = from(originalSchema).returnAll();

		if (searchServices.getResultsCount(condition) >= 1000) {

			Map<String, Object> modifiedMetadatas = new HashMap<>();
			modifiedMetadatas.put(originalSchema.get(Schemas.SCHEMA.getLocalCode()).getCode(), destinationSchema.getCode());

			BatchProcessAction action = new ChangeValueOfMetadataBatchProcessAction(modifiedMetadatas);

			Map<String, Object> params = new HashMap<>();
			params.put("from", originalSchema.getLocalCode());
			params.put("to", destinationSchema.getLocalCode());

			batchProcessesManager.addPendingBatchProcess(
					condition, action, $("AppSchemasServices.changeSchemaCodeTask", params));
			return true;
		} else {

			Transaction transaction = new Transaction();
			transaction.getRecordUpdateOptions().setValidationsEnabled(false);

			List<Record> records = searchServices.search(new LogicalSearchQuery(condition));
			for (Record record : records) {
				record.changeSchema(originalSchema, destinationSchema);
				transaction.add(record);
			}

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

			return false;
		}

	}

	private void configureNewSchema(String collection, String fromCode, String toCode) {
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		transaction.add(schemasDisplayManager.getSchema(collection, fromCode).withCode(toCode));

		List<String> definedMetadataCodes = schemasDisplayManager.getDefinedMetadatasIn(collection);

		for (String definedMetadataCode : definedMetadataCodes) {
			if (definedMetadataCode.startsWith(fromCode)) {
				transaction.add(schemasDisplayManager.getMetadata(collection, definedMetadataCode)
						.withCode(definedMetadataCode.replace(fromCode, toCode)));
			}
		}
		schemasDisplayManager.execute(transaction.build());
	}

	private void validateModificationAllowed(MetadataSchemaTypes types, String fromCode, String toCode) {
		SchemaUtils schemaUtils = new SchemaUtils();
		String fromType = schemaUtils.getSchemaTypeCode(fromCode);
		String toType = schemaUtils.getSchemaTypeCode(toCode);

		if (!fromType.equals(toType)) {
			throw new AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotChangeCodeToOtherSchemaType();
		}

		if (fromCode.endsWith("_default") || toCode.endsWith("_default")) {
			throw new AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault();
		}
	}

	private Metadata getMetadata(String collection, String schemaCode) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(schemaCode);
		return schemasManager.getSchemaTypes(collection).getAllMetadatas().onlyReferencesToType(schemaType).get(0);
	}

	private ValidationErrors newValidationErrors(String code, Map<String, Object> parameters) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.add(AppSchemasServices.class, code, parameters);
		return validationErrors;
	}

	private List<String> getReferencesWithDirectAllowedReference(String collection, String schemaCode) {
		List<String> references = new ArrayList<>();

		String schemaType = new SchemaUtils().getSchemaTypeCode(schemaCode);

		for (Metadata metadata : schemasManager.getSchemaTypes(collection).getAllMetadatas().onlyReferencesToType(schemaType)) {
			Set<String> allowedSchemas = metadata.getAllowedReferences().getAllowedSchemas();
			if (allowedSchemas != null && !allowedSchemas.isEmpty()) {
				for (String allowedSchema : allowedSchemas) {
					if (allowedSchema.equals(schemaCode)) {
						references.add(metadata.getCode());
						continue;
					}
				}
			}
		}

		return references;
	}

	private void setParametersForMetadataReferencingSchemaError(Map<String, Object> parameters, String collection,
																String schemaCode) {
		Metadata referenceMetadata = getMetadata(collection, schemaCode);
		MetadataSchemaType metadataSchemaType = schemasManager.getSchemaTypes(collection).getSchemaType(referenceMetadata.getSchemaTypeCode());
		MetadataSchema metadataSchema = schemasManager.getSchemaTypes(collection).getSchema(referenceMetadata.getSchemaCode());
		parameters.put("metadataTitle", referenceMetadata.getLabels());
		parameters.put("metadataSchemaTypeTitle", metadataSchemaType.getLabels());
		parameters.put("metadataSchemaTitle", metadataSchema.getLabels());
	}

	private void setParametersForExistingRecordsWithSchemaError(Map<String, Object> parameters, MetadataSchema schema) {
		Long numberOfRecords = searchServices.getResultsCount(from(schema).returnAll());
		parameters.put("recordsCount", numberOfRecords);
	}

	public void disableSchema(String collection, String schemaCode) {
		changeSchemaState(collection, schemaCode, false);
	}

	public void enableSchema(String collection, String schemaCode) {
		changeSchemaState(collection, schemaCode, true);
	}

	public void changeSchemaState(String collection, final String schemaCode, final boolean active) {
		schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder builder = types.getSchema(schemaCode);
				builder.setActive(active);
			}
		});
	}

}
