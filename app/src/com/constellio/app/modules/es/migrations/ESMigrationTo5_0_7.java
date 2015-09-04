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

import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.ON_DEMANDS;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.SEEDS;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;

import org.joda.time.LocalDate;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.es.connectors.http.ConnectorHttp;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.constants.ESTaxonomies;
import com.constellio.app.modules.es.migrations.EnterpriseSearchMigrationHelper.ESSchemaTypesMigrationHelper;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo5_0_7 extends MigrationHelper implements MigrationScript {

	EnterpriseSearchMigrationHelper migration;

	@Override
	public String getVersion() {
		return "5.0.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_0_7(collection, migrationResourcesProvider, appLayerFactory).migrate();

		updateFormAndDisplayAndTableConfigs(collection, appLayerFactory);
		migration = new EnterpriseSearchMigrationHelper(appLayerFactory, collection,
				migrationResourcesProvider);

		Transaction transaction = new Transaction();
		transaction.add(migration
				.newConnectorType(migration.es.connectorInstance_http.schema(), ConnectorHttp.class, ConnectorType.CODE_HTTP))
				.setDefaultAvailable(new MapStringStringStructure()
						.with("mimetype", String.class.getName())
						.with("charset", String.class.getName())
						.with("language", String.class.getName())
						.with("lastModification", LocalDate.class.getName()));

		migration.modelLayerFactory.newRecordServices().execute(transaction);

		transaction = new Transaction();
		transaction.add(migration
				.newConnectorType(migration.es.connectorInstance_smb.schema(), ConnectorSmb.class, ConnectorType.CODE_SMB));
		migration.modelLayerFactory.newRecordServices().execute(transaction);

		addHttpDocumentToSearch(collection, appLayerFactory);
		addSmbDocumentToSearch(collection, appLayerFactory);
		setupDisplayConfig(collection, appLayerFactory);

		createSmbFoldersTaxonomy(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);

		createFacets(collection, appLayerFactory, migrationResourcesProvider);
	}

	private void createFacets(String collection, AppLayerFactory appLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider)
			throws RecordServicesException {

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = es.getModelLayerFactory().newRecordServices();

		recordServices.add(es.newFacetField()
				.setFieldDataStoreCode(es.connectorSmbDocument.language().getDataStoreCode())
				.setTitle(migrationResourcesProvider.get("init.facet.language"))
				.withLabel("fr", migrationResourcesProvider.get("init.facet.language.fr"))
				.withLabel("en", migrationResourcesProvider.get("init.facet.language.en"))
				.withLabel("es", migrationResourcesProvider.get("init.facet.language.es")));

		recordServices.add(es.newFacetField()
				.setFieldDataStoreCode(es.connectorSmbDocument.extension().getDataStoreCode())
				.setTitle(migrationResourcesProvider.get("init.facet.extension")));

		recordServices.add(es.newFacetField()
				.setFieldDataStoreCode(es.connectorSmbDocument.parent().getDataStoreCode())
				.setTitle(migrationResourcesProvider.get("init.facet.smbFolder")));

	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		// Connector SMB Config/Instance
		SchemaDisplayConfig schemaFormFolderTypeConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, ConnectorSmbInstance.SCHEMA_CODE),
				ConnectorInstance.CONNECTOR_TYPE,
				ConnectorInstance.CODE,
				ConnectorInstance.TITLE,
				ConnectorSmbInstance.DOMAIN,
				ConnectorSmbInstance.USERNAME,
				ConnectorSmbInstance.PASSWORD,
				ConnectorSmbInstance.SEEDS,
				ConnectorSmbInstance.EXCLUSIONS,
				ConnectorSmbInstance.INCLUSIONS,
				ConnectorInstance.ENABLED
		);

		SchemaDisplayConfig schemaDisplayFolderTypeConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, ConnectorSmbInstance.SCHEMA_CODE),
				ConnectorInstance.CONNECTOR_TYPE,
				ConnectorInstance.CODE,
				ConnectorInstance.TITLE,
				ConnectorSmbInstance.DOMAIN,
				ConnectorSmbInstance.USERNAME,
				ConnectorSmbInstance.SEEDS,
				ConnectorSmbInstance.EXCLUSIONS,
				ConnectorSmbInstance.INCLUSIONS,
				ConnectorInstance.ENABLED
		);

		transaction.add(
				schemaDisplayFolderTypeConfig.withFormMetadataCodes(schemaFormFolderTypeConfig.getFormMetadataCodes()));

		manager.execute(transaction);

		manager.saveMetadata(manager.getMetadata(collection,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.PASSWORD).withInputType(
				MetadataInputType.PASSWORD));
	}

	private void createSmbFoldersTaxonomy(String collection, ModelLayerFactory modelLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {
		String title = migrationResourcesProvider.getDefaultLanguageString("init.taxoSmbFolders");
		Taxonomy taxonomy = Taxonomy.createPublic(ESTaxonomies.SMB_FOLDERS, title, collection, ConnectorSmbFolder.SCHEMA_TYPE);
		modelLayerFactory.getTaxonomiesManager().addTaxonomy(taxonomy, modelLayerFactory.getMetadataSchemasManager());
	}

	private void addHttpDocumentToSearch(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaDisplayManagerTransaction transaction = manager.newTransactionBuilderFor(collection).build();

		transaction.add(manager.getType(collection, ConnectorHttpDocument.SCHEMA_TYPE).withSimpleSearchStatus(true)
				.withAdvancedSearchStatus(true));

		manager.execute(transaction);
	}

	private void addSmbDocumentToSearch(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaDisplayManagerTransaction transaction = manager.newTransactionBuilderFor(collection).build();

		transaction.add(manager.getType(collection, ConnectorSmbDocument.SCHEMA_TYPE).withSimpleSearchStatus(true)
				.withAdvancedSearchStatus(true));

		manager.execute(transaction);
	}

	static class SchemaAlterationFor5_0_7 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_0_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.0.7";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder types) {

			ESSchemaTypesMigrationHelper migration = new ESSchemaTypesMigrationHelper(types);

			createConnectorCommonchemaTypes(types, migration);
			createConnectorHTTPSchemas(types, migration);
			createConnectorSMBSchemas(types, migration);
		}

		private void createConnectorCommonchemaTypes(MetadataSchemaTypesBuilder types, ESSchemaTypesMigrationHelper migration) {

			MetadataSchemaBuilder connectorSchema, connectorTypeSchema;

			//-
			//Create ConnectorType schema type
			MetadataSchemaTypeBuilder connectorTypeSchemaType = types.createNewSchemaType(ConnectorType.SCHEMA_TYPE);
			connectorTypeSchema = connectorTypeSchemaType.getDefaultSchema();
			connectorTypeSchema.createUniqueCodeMetadata();
			connectorTypeSchema.createUndeletable(ConnectorType.LINKED_SCHEMA).setType(STRING)
					.setDefaultRequirement(true);
			connectorTypeSchema.createUndeletable(ConnectorType.CONNECTOR_CLASS_NAME).setType(STRING)
					.setDefaultRequirement(true);
			connectorTypeSchema.createUndeletable(ConnectorType.DEFAULT_AVAILABLE_PROPERTIES).setType(STRUCTURE)
					.defineStructureFactory(MapStringStringStructureFactory.class);

			//-
			//Create Connector schema type
			connectorSchema = types.createNewSchemaType(ConnectorInstance.SCHEMA_TYPE).getDefaultSchema();
			connectorSchema.createUniqueCodeMetadata();
			connectorSchema.createUndeletable(ConnectorInstance.CONNECTOR_TYPE)
					.setType(MetadataValueType.REFERENCE).setDefaultRequirement(true).defineReferencesTo(connectorTypeSchemaType);
			connectorSchema.createUndeletable(ConnectorInstance.TRAVERSAL_CODE).setType(MetadataValueType.STRING);
			connectorSchema.createUndeletable(ConnectorInstance.ENABLED).setType(MetadataValueType.BOOLEAN);
			connectorSchema.createUndeletable(ConnectorInstance.PROPERTIES_MAPPING).setType(STRUCTURE).defineStructureFactory(
					MapStringListStringStructureFactory.class);
		}

		private void createConnectorHTTPSchemas(MetadataSchemaTypesBuilder types, ESSchemaTypesMigrationHelper migration) {
			MetadataSchemaBuilder instanceSchema, documentSchema;

			//-
			//Create Connector HTTP instance schema
			instanceSchema = migration.newConnectorInstanceSchema(ConnectorHttpInstance.SCHEMA_LOCAL_CODE);
			instanceSchema.createUndeletable(SEEDS).setType(STRING).setMultivalue(true).setDefaultRequirement(true);
			instanceSchema.createUndeletable(ON_DEMANDS).setType(STRING).setMultivalue(true);

			//-
			//Create Connector HTTP document schema type
			documentSchema = migration.newConnectorDocumentSchemaType(ConnectorHttpDocument.SCHEMA_TYPE,
					ConnectorHttpInstance.SCHEMA_CODE).getDefaultSchema();
			documentSchema.createUndeletable(ConnectorHttpDocument.URL).setType(STRING).setDefaultRequirement(true);
			documentSchema.createUndeletable(ConnectorHttpDocument.BASE_URI).setType(STRING);
			documentSchema.createUndeletable(ConnectorHttpDocument.PARSED_CONTENT).setType(TEXT);
		}

		private void createConnectorSMBSchemas(MetadataSchemaTypesBuilder types2, ESSchemaTypesMigrationHelper migration) {
			MetadataSchemaBuilder instanceSchema, documentSchema, folderSchema;
			MetadataSchemaTypeBuilder folderSchemaType;

			//-
			//Create Connector SMB instance schema
			instanceSchema = migration.newConnectorInstanceSchema(ConnectorSmbInstance.SCHEMA_LOCAL_CODE);
			instanceSchema.createUndeletable(ConnectorSmbInstance.SEEDS).setType(STRING).setMultivalue(true)
					.setDefaultRequirement(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.USERNAME).setType(STRING).setDefaultRequirement(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.PASSWORD).setType(STRING).setDefaultRequirement(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.DOMAIN).setType(STRING).setDefaultRequirement(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.INCLUSIONS).setType(STRING).setMultivalue(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.EXCLUSIONS).setType(STRING).setMultivalue(true);

			//-
			//Create Connector SMB folder schema type
			folderSchemaType = migration.newConnectorDocumentSchemaType(ConnectorSmbFolder.SCHEMA_TYPE,
					ConnectorSmbInstance.SCHEMA_CODE);
			folderSchema = folderSchemaType.getDefaultSchema();
			folderSchema.createUndeletable(ConnectorSmbFolder.URL).setType(STRING).setDefaultRequirement(true);
			folderSchema.createUndeletable(ConnectorSmbFolder.PARENT).defineReferencesTo(folderSchemaType)
					.setChildOfRelationship(true);
			folderSchema.createUndeletable(ConnectorSmbFolder.LAST_FETCH_ATTEMPT).setType(DATE_TIME).setSearchable(true);
			folderSchema.createUndeletable(ConnectorSmbFolder.LAST_FETCHED_STATUS).defineAsEnum(LastFetchedStatus.class)
					.setSearchable(true);

			//-
			//Create Connector SMB document schema type
			documentSchema = migration.newConnectorDocumentSchemaType(ConnectorSmbDocument.SCHEMA_TYPE,
					ConnectorSmbInstance.SCHEMA_CODE).getDefaultSchema();
			documentSchema.createUndeletable(ConnectorSmbDocument.URL).setType(STRING).setDefaultRequirement(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.PARSED_CONTENT).setType(TEXT).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.SIZE).setType(NUMBER).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.PERMISSIONS_HASH).setType(STRING);
			documentSchema.createUndeletable(ConnectorSmbDocument.LAST_MODIFIED).setType(DATE_TIME).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.LAST_FETCH_ATTEMPT).setType(DATE_TIME).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.PARENT).defineReferencesTo(folderSchemaType)
					.setTaxonomyRelationship(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_STATUS).defineAsEnum(LastFetchedStatus.class).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_DETAILS).setType(STRING).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.LANGUAGE).setType(STRING).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.EXTENSION).setType(STRING).setSearchable(true);
		}
	}

	private void updateFormAndDisplayAndTableConfigs(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		//Table
		transactionBuilder.in(ConnectorInstance.SCHEMA_TYPE)
				.addToSearchResult(ConnectorInstance.CONNECTOR_TYPE)
				.afterMetadata(Schemas.TITLE_CODE);
		transactionBuilder.in(ConnectorInstance.SCHEMA_TYPE)
				.addToSearchResult(ConnectorInstance.TRAVERSAL_CODE)
				.afterMetadata(ConnectorInstance.CONNECTOR_TYPE);
		transactionBuilder.in(ConnectorInstance.SCHEMA_TYPE)
				.removeFromSearchResult(Schemas.MODIFIED_ON.getLocalCode());

		//Display
		transactionBuilder.in(ConnectorInstance.SCHEMA_TYPE)
				.addToDisplay(ConnectorInstance.CONNECTOR_TYPE)
				.afterMetadata(Schemas.TITLE_CODE);
		transactionBuilder.in(ConnectorInstance.SCHEMA_TYPE)
				.addToDisplay(ConnectorInstance.TRAVERSAL_CODE)
				.afterMetadata(ConnectorInstance.CONNECTOR_TYPE);
		transactionBuilder.in(ConnectorInstance.SCHEMA_TYPE)
				.addToDisplay(Schemas.MODIFIED_ON.getLocalCode())
				.afterMetadata(ConnectorInstance.SCHEMA_TYPE);

		manager.execute(transactionBuilder.build());
	}
}
