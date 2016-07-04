package com.constellio.app.services.metadata;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

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
		return false;
	}

	public void deleteSchemaCode(String collection, String schemaCode) {

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
		configureNewSchema(collection, fromCode, toCode);

		schemasManager.deleteCustomSchemas(asList(types.getSchema(fromCode)));
	}

	private void modifyRecords(String collection, final MetadataSchema originalSchema, final MetadataSchema destinationSchema) {
		try {
			new ActionExecutorInBatch(searchServices, "Modify schema of records", 1000) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					System.out.println(records);
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
