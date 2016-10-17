package com.constellio.app.services.metadata;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.metadata.AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotDeleteSchema;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class AppSchemasServices {

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

	public boolean isSchemaDeletable(String collection, String schemaCode) {
		if (schemaCode.endsWith("default")) {
			return false;
		} else {
			List<String> references = getReferencesWithDirectAllowedReference(collection, schemaCode);

			MetadataSchema schema = schemasManager.getSchemaTypes(collection).getSchema(schemaCode);
			return references.isEmpty() && !searchServices.hasResults(from(schema).returnAll());
		}
	}

	private List<String> getReferencesWithDirectAllowedReference(String collection, String schemaCode) {
		List<String> references = new ArrayList<>();

		String schemaType = new SchemaUtils().getSchemaTypeCode(schemaCode);

		for (Metadata metadata : schemasManager.getSchemaTypes(collection).getAllMetadatas().onlyReferencesToType(schemaType)) {
			Set<String> allowedSchemas = metadata.getAllowedReferences().getAllowedSchemas();
			if (allowedSchemas != null && !allowedSchemas.isEmpty()) {
				references.add(metadata.getCode());
			}
		}

		return references;
	}

	public void deleteSchemaCode(String collection, String schemaCode) {
		if (!isSchemaDeletable(collection, schemaCode)) {
			throw new AppSchemasServicesRuntimeException_CannotDeleteSchema(schemaCode);
		}
		updateRecordsWithLinkedSchemas(collection, schemaCode, null);
		schemasManager.deleteCustomSchemas(asList(schemasManager.getSchemaTypes(collection).getSchema(schemaCode)));
	}

	public void modifySchemaCode(String collection, final String fromCode, final String toCode) {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		validateModificationAllowed(types, fromCode, toCode);

		final String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(fromCode);

		schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(schemaTypeCode).createCustomSchemaCopying(
						new SchemaUtils().getSchemaLocalCode(toCode), new SchemaUtils().getSchemaLocalCode(fromCode));
			}
		});
		types = schemasManager.getSchemaTypes(collection);

		modifyRecords(collection, types.getSchema(fromCode), types.getSchema(toCode));
		modifyReferencesWithDirectTarget(types, fromCode, toCode);
		updateRecordsWithLinkedSchemas(collection, fromCode, toCode);
		configureNewSchema(collection, fromCode, toCode);

		schemasManager.deleteCustomSchemas(asList(types.getSchema(fromCode)));
	}

	private void updateRecordsWithLinkedSchemas(String collection, String fromCode, String toCode) {
		List<Record> records = searchServices.search(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(collection).where(Schemas.LINKED_SCHEMA).isEqualTo(fromCode)));

		Transaction transaction = new Transaction();

		for (Record record : records) {
			transaction.add(record.set(Schemas.LINKED_SCHEMA, toCode));
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void modifyReferencesWithDirectTarget(MetadataSchemaTypes types, final String fromCode, final String toCode) {
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

	private void modifyRecords(String collection, final MetadataSchema originalSchema, final MetadataSchema destinationSchema) {
		try {
			new ActionExecutorInBatch(searchServices, "Modify schema of records", 1000) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					Transaction transaction = new Transaction();
					transaction.getRecordUpdateOptions().setValidationsEnabled(false);

					for (Record record : records) {
						record.changeSchema(originalSchema, destinationSchema);
						transaction.add(record);
					}

					recordServices.execute(transaction);

				}
			}.execute(from(originalSchema).returnAll());
		} catch (Exception e) {
			throw new RuntimeException(e);
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
}
