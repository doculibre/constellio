/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

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

		String title = resourcesProvider.getDefaultLanguageString("init." + schema.getCode().replace("-", ".").replace("_", "."));
		return es.newConnectorType().setCode(connectorTypeCode).setTitle(title).setLinkedSchema(schema.getCode())
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

			MetadataBuilder connectorDocConnector = schemaBuilder.createUndeletable(ConnectorDocument.CONNECTOR)
					.setType(REFERENCE).setDefaultRequirement(true).defineReferencesTo(connectorInstanceSchema);

			schemaBuilder.createUndeletable(ConnectorDocument.TRAVERSAL_CODE).setType(STRING).setDefaultRequirement(true);

			schemaBuilder.createUndeletable(ConnectorDocument.CONNECTOR_TYPE).setType(REFERENCE)
					.defineReferencesTo(connectorTypeSchemaType).setDefaultRequirement(true);

			schemaBuilder.createUndeletable(ConnectorDocument.FETCHED).setType(BOOLEAN).setDefaultValue(Boolean.TRUE);

			return typeBuilder;
		}

		public MetadataSchemaBuilder newConnectorInstanceSchema(String schemaLocalCode) {
			MetadataSchemaTypeBuilder connectorInstanceSchemaType = types.getSchemaType(ConnectorInstance.SCHEMA_TYPE);
			return connectorInstanceSchemaType.createCustomSchema(schemaLocalCode);
		}
	}
}

