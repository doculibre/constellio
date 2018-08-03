package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.modules.es.model.connectors.*;
import com.constellio.app.modules.es.model.connectors.http.enums.FetchFrequency;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.*;

public class EnterpriseSearchMigrationHelper {

	public final AppLayerFactory appLayerFactory;
	public final ModelLayerFactory modelLayerFactory;
	public final DataLayerFactory dataLayerFactory;
	public final ESSchemasRecordsServices es;
	public final MigrationResourcesProvider resourcesProvider;

	public EnterpriseSearchMigrationHelper(AppLayerFactory appLayerFactory, String collection,
										   MigrationResourcesProvider migrationResourcesProvider) {
		this.es = new ESSchemasRecordsServices(collection, appLayerFactory);
		this.resourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
	}

	public ConnectorType newConnectorType(MetadataSchema schema, Class<?> connectorClass, String connectorTypeCode) {

		Map<Language, String> titles = resourcesProvider
				.getLanguagesString("init." + schema.getCode().replace("-", ".").replace("_", "."));
		return es.newConnectorType().setCode(connectorTypeCode).setTitles(titles).setLinkedSchema(schema.getCode())
				.setConnectorClassName(connectorClass.getName());
	}

	public static class ESSchemaTypesMigrationHelper {

		private MetadataSchemaTypesBuilder types;

		public ESSchemaTypesMigrationHelper(MetadataSchemaTypesBuilder types) {
			this.types = types;
		}

		public MetadataSchemaTypeBuilder newConnectorDocumentSchemaType(String schemaType,
																		String connectorInstanceSchemaCode) {

			MetadataSchemaBuilder connectorInstanceSchema = types.getSchema(connectorInstanceSchemaCode);
			MetadataSchemaBuilder connectorTypeSchemaType = types.getDefaultSchema(ConnectorType.SCHEMA_TYPE);

			MetadataBuilder connectorInstanceConnectorType = connectorInstanceSchema
					.getMetadata(ConnectorInstance.CONNECTOR_TYPE).getInheritance();

			MetadataSchemaTypeBuilder typeBuilder = types.createNewSchemaType(schemaType);
			MetadataSchemaBuilder schemaBuilder = typeBuilder.getDefaultSchema();
			typeBuilder.setInTransactionLog(false);
			MetadataBuilder connectorDocConnector = schemaBuilder.createUndeletable(ConnectorDocument.CONNECTOR)
					.setType(REFERENCE).setDefaultRequirement(true).defineReferencesTo(connectorInstanceSchema);

			schemaBuilder.createUndeletable(ConnectorDocument.TRAVERSAL_CODE).setType(STRING).setDefaultRequirement(true);
			schemaBuilder.createUndeletable(ConnectorDocument.CONNECTOR_TYPE).setType(REFERENCE)
					.defineReferencesTo(connectorTypeSchemaType).setDefaultRequirement(true);
			schemaBuilder.createUndeletable(ConnectorDocument.URL).setType(STRING).setDefaultRequirement(true);
			schemaBuilder.createUndeletable(ConnectorDocument.MIMETYPE).setType(STRING);
			schemaBuilder.createUndeletable(ConnectorDocument.FETCHED).setType(BOOLEAN).setDefaultValue(Boolean.TRUE);
			//schemaBuilder.createUndeletable(ConnectorDocument.SEARCHABLE).setType(BOOLEAN).setDefaultValue(Boolean.TRUE);
			schemaBuilder.createUndeletable(ConnectorDocument.FETCHED_DATETIME).setType(DATE_TIME);
			schemaBuilder.createUndeletable(ConnectorDocument.STATUS).defineAsEnum(ConnectorDocumentStatus.class);
			schemaBuilder.createUndeletable(ConnectorDocument.FETCH_FREQUENCY).defineAsEnum(FetchFrequency.class);
			schemaBuilder.createUndeletable(ConnectorDocument.FETCH_DELAY).setType(NUMBER).setDefaultValue(10);
			schemaBuilder.createUndeletable(ConnectorDocument.NEXT_FETCH).setType(DATE_TIME)
					.defineDataEntry().asCalculated(NextFetchCalculator.class);
			schemaBuilder.createUndeletable(ConnectorDocument.NEVER_FETCH).setType(BOOLEAN);
			schemaBuilder.createUndeletable(ConnectorDocument.ERROR_CODE).setType(STRING);
			schemaBuilder.createUndeletable(ConnectorDocument.ERROR_MESSAGE).setType(STRING);
			schemaBuilder.createUndeletable(ConnectorDocument.ERROR_STACK_TRACE).setType(TEXT);
			schemaBuilder.createUndeletable(ConnectorDocument.ERRORS_COUNT).setType(NUMBER).setDefaultValue(0);
			schemaBuilder.createUndeletable(ConnectorDocument.LAST_MODIFIED).setType(DATE_TIME).setSearchable(true);

			return typeBuilder;
		}

		public MetadataSchemaBuilder newConnectorInstanceSchema(String schemaLocalCode) {
			MetadataSchemaTypeBuilder connectorInstanceSchemaType = types.getSchemaType(ConnectorInstance.SCHEMA_TYPE);
			return connectorInstanceSchemaType.createCustomSchema(schemaLocalCode);
		}
	}
}

