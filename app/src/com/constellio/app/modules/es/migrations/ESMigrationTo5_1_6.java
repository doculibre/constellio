package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.connectors.http.ConnectorHttp;
import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.constants.ESTaxonomies;
import com.constellio.app.modules.es.migrations.EnterpriseSearchMigrationHelper.ESSchemaTypesMigrationHelper;
import com.constellio.app.modules.es.model.connectors.AuthenticationScheme;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.model.connectors.structures.TraversalScheduleFactory;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.ConnectorFieldFactory;
import com.constellio.app.modules.es.services.mapping.ConnectorFieldValidator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.TaxonomyAlreadyExists;

import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.es.model.connectors.ConnectorInstance.ENABLED;
import static com.constellio.app.modules.es.model.connectors.ConnectorInstance.TRAVERSAL_SCHEDULE;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.AUTHENTICATION_SCHEME;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.DAYS_BEFORE_REFETCHING;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.DOMAIN;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.EXCLUDE_PATTERNS;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.INCLUDE_PATTERNS;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.MAX_LEVEL;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.NUMBER_OF_DOCUMENTS_PER_JOBS;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.NUMBER_OF_JOBS_IN_PARALLEL;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.ON_DEMANDS;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.PASSWORD;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.SEEDS;
import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.USERNAME;
import static com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType.ACTIVE_DIRECTORY;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static java.util.Arrays.asList;

public class ESMigrationTo5_1_6 extends MigrationHelper implements MigrationScript {

	EnterpriseSearchMigrationHelper migration;
	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public String getVersion() {
		return "5.1.6";
	}

	String configurationTab;
	String executionTab;
	String credentialsTab;
	String ldapUserTab;
	Map<String, Map<Language, String>> groups;

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.migrationResourcesProvider = migrationResourcesProvider;

		configurationTab = "default:connectors.configurationTab";

		executionTab = "connectors.executionTab";

		credentialsTab = "connectors.credentialsTab";

		ldapUserTab = "connectors.ldapUserTab";
		groups = migrationResourcesProvider
				.getLanguageMap(asList(configurationTab, executionTab, credentialsTab, ldapUserTab));

		clearExistingRecordsAndSchemas(collection, appLayerFactory);
		deleteESFacets(collection, appLayerFactory);

		new SchemaAlterationFor5_1_6(collection, migrationResourcesProvider, appLayerFactory).migrate();

		configureConnectorsInstancesAndDocumentsDisplay(collection, appLayerFactory);

		createConnectorTypes(collection, migrationResourcesProvider, appLayerFactory);

		createSmbFoldersTaxonomy(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);

		createFacets(collection, appLayerFactory, migrationResourcesProvider);

		updateFormAndDisplay(collection, appLayerFactory);
		this.migrationResourcesProvider = null;
	}

	private void deleteESFacets(String collection, AppLayerFactory appLayerFactory) {
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);

		MetadataSchemaType facetSchemaType = es.schemaType(Facet.SCHEMA_TYPE);
		if (facetSchemaType.getDefaultSchema().hasMetadataWithCode(Facet.USED_BY_MODULE)) {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.from(facetSchemaType)
					.where(facetSchemaType.getDefaultSchema().getMetadata(Facet.USED_BY_MODULE)).isEqualTo(ConstellioESModule.ID);
			List<Record> esFacets = searchServices.search(new LogicalSearchQuery(condition));

			for (Record esFacet : esFacets) {
				recordServices.physicallyDelete(esFacet, User.GOD);
			}
		}
	}

	private void clearExistingRecordsAndSchemas(String collection, AppLayerFactory appLayerFactory) {
		MetadataSchemasManager schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collection);

		RecordDao recordDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().newRecordDao();
		RecordDeleteServices recordDeleteServices = new RecordDeleteServices(recordDao,
				appLayerFactory.getModelLayerFactory());

		if (schemaTypes.hasType(ConnectorSmbFolder.SCHEMA_TYPE)) {

			recordDeleteServices.totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(
					schemaTypes.getSchemaType(ConnectorSmbFolder.SCHEMA_TYPE));
			recordDeleteServices.totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(
					schemaTypes.getSchemaType(ConnectorSmbDocument.SCHEMA_TYPE));
			recordDeleteServices.totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(
					schemaTypes.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE));

			schemasManager.deleteSchemaTypes(asList(
					schemaTypes.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE),
					schemaTypes.getSchemaType(ConnectorSmbFolder.SCHEMA_TYPE),
					schemaTypes.getSchemaType(ConnectorSmbDocument.SCHEMA_TYPE)
			));
		}

		if (schemaTypes.hasType(ConnectorInstance.SCHEMA_TYPE)) {
			recordDeleteServices.totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(
					schemaTypes.getSchemaType(ConnectorInstance.SCHEMA_TYPE));
			recordDeleteServices.totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(
					schemaTypes.getSchemaType(ConnectorType.SCHEMA_TYPE));

			schemasManager.deleteSchemaTypes(asList(
					schemaTypes.getSchemaType(ConnectorInstance.SCHEMA_TYPE),
					schemaTypes.getSchemaType(ConnectorType.SCHEMA_TYPE)
			));
		}

		if (schemaTypes.hasType(ConnectorLDAPUserDocument.SCHEMA_TYPE)) {
			recordDeleteServices.totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(
					schemaTypes.getSchemaType(ConnectorLDAPUserDocument.SCHEMA_TYPE));

			schemasManager.deleteSchemaTypes(asList(
					schemaTypes.getSchemaType(ConnectorLDAPUserDocument.SCHEMA_TYPE)
			));
		}

	}

	private ConnectorField field(String schemaType, String code, MetadataValueType type) {
		String label = migrationResourcesProvider.get(code);
		return new ConnectorField(schemaType + ":" + code, label, type);
	}

	private void createConnectorTypes(String collection, MigrationResourcesProvider migrationResourcesProvider,
									  AppLayerFactory appLayerFactory)
			throws RecordServicesException {
		migration = new EnterpriseSearchMigrationHelper(appLayerFactory, collection,
				migrationResourcesProvider);

		Transaction transaction = new Transaction();
		transaction.add(migration
				.newConnectorType(migration.es.connectorInstance_http.schema(), ConnectorHttp.class, ConnectorType.CODE_HTTP))
				.setDefaultAvailableConnectorFields(asList(
						field(ConnectorHttpDocument.SCHEMA_TYPE, "charset", STRING),
						field(ConnectorHttpDocument.SCHEMA_TYPE, "language", STRING),
						field(ConnectorHttpDocument.SCHEMA_TYPE, "lastModification", DATE_TIME)));

		transaction.add(migration
				.newConnectorType(migration.es.connectorInstance_smb.schema(), ConnectorSmb.class, ConnectorType.CODE_SMB));
		transaction.add(migration
				.newConnectorType(migration.es.connectorInstance_ldap.schema(), ConnectorLDAP.class,
						ConnectorType.CODE_LDAP)
				.setDefaultAvailableConnectorFields(asList(
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "userAccountControl", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "sAMAccountType", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "primaryGroupID", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "objectSid", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "objectGUID", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "uSNChanged", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "uSNCreated", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "userPrincipalName", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "primaryGroupID", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "name", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "displayName", MetadataValueType.STRING),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "whenChanged", MetadataValueType.DATE),
						field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "whenCreated", MetadataValueType.DATE)
				)));
		migration.modelLayerFactory.newRecordServices().execute(transaction);
	}

	private void updateFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.in(ConnectorHttpInstance.SCHEMA_TYPE)
				.addToDisplay(ConnectorHttpInstance.INCLUDE_PATTERNS, ConnectorHttpInstance.EXCLUDE_PATTERNS)
				.atTheEnd();

		transactionBuilder.in(ConnectorHttpInstance.SCHEMA_TYPE)
				.addToForm(ConnectorHttpInstance.INCLUDE_PATTERNS, ConnectorHttpInstance.EXCLUDE_PATTERNS)
				.atTheEnd();

		transactionBuilder.in(ConnectorLDAPUserDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorLDAPUserDocument.FIRST_NAME,
						ConnectorLDAPUserDocument.LAST_NAME, ConnectorLDAPUserDocument.TELEPHONE,
						ConnectorLDAPUserDocument.EMAIL, ConnectorLDAPUserDocument.ADDRESS)
				.atFirstPosition();
		transactionBuilder.in(ConnectorLDAPUserDocument.SCHEMA_TYPE)
				.removeFromSearchResult(Schemas.MODIFIED_ON.getLocalCode());

		manager.execute(transactionBuilder.build());
	}

	static class SchemaAlterationFor5_1_6 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_1_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.1.6";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder types) {

			ESSchemaTypesMigrationHelper migration = new ESSchemaTypesMigrationHelper(types);

			createConnectorCommonSchemaTypes(types, migration);
			createConnectorHTTPSchemas(types, migration);
			createConnectorSMBSchemas(types, migration);
			createConnectorLDAPSchemas(types, migration);

			updateConnectorHttpDocumentSchemaType(types);
			updateConnectorHttpInstanceSchemaType(types);

		}

		private void createConnectorLDAPSchemas(MetadataSchemaTypesBuilder types,
												ESSchemaTypesMigrationHelper migration) {
			MetadataSchemaBuilder connectorLDAPInstanceSchema = migration.newConnectorInstanceSchema(
					ConnectorLDAPInstance.SCHEMA_LOCAL_CODE);
			//in the current version only AD is supported
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.DIRECTORY_TYPE).defineAsEnum(DirectoryType.class)
					.setDefaultValue(
							ACTIVE_DIRECTORY).setSystemReserved(true).setDefaultRequirement(true);
			//for now we fetch only users
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.FETCH_GROUPS).setType(BOOLEAN)
					.setSystemReserved(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.FETCH_COMPUTERS).setType(
					BOOLEAN).setSystemReserved(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.FETCH_USERS).setType(BOOLEAN)
					.setSystemReserved(true).setDefaultValue(true);

			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.FOLLOW_REFERENCES).setType(BOOLEAN);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.URLS).setType(STRING).setMultivalue(true)
					.setDefaultRequirement(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.INCLUDE_REGEX).setType(STRING);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.EXCLUDE_REGEX).setType(STRING);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.CONNECTION_USERNAME).setType(STRING)
					.setDefaultRequirement(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.PASSWORD).setType(STRING).setEncrypted(true)
					.setDefaultRequirement(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.USERNAME_ATTRIBUTE_NAME).setType(STRING)
					.setDefaultValue("sAMAccountName");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.FIRST_NAME_ATTRIBUTE_NAME).setType(STRING)
					.setDefaultValue("givenName");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.LAST_NAME_ATTRIBUTE_NAME).setType(
					STRING).setDefaultValue("sn");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.EMAIL_ATTRIBUTE_NAME).setType(
					STRING).setDefaultValue("mail");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.ADDRESS_ATTRIBUTE_NAME).setType(STRING)
					.setDefaultValue(
							asList("physicalDeliveryOfficeName", "streetAddress", "l",
									"postalCode", "st", "co", "c")).setMultivalue(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.DISTINGUISHED_NAME_ATTRIBUTE_NAME)
					.setType(STRING).setDefaultValue("distinguishedName").setDefaultRequirement(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.WORK_TITLE_ATTRIBUTE_NAME)
					.setType(STRING).setDefaultValue("title");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.TELEPHONE_ATTRIBUTE_NAME).setType(STRING)
					.setDefaultValue(
							asList("telephoneNumber", "mobile", "ipPhone")).setMultivalue(true);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.DISPLAY_NAME_ATTRIBUTE_NAME)
					.setType(STRING).setDefaultValue("cn");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.COMPANY_ATTRIBUTE_NAME)
					.setType(STRING).setDefaultValue("company");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.DEPARTMENT_ATTRIBUTE_NAME)
					.setType(STRING).setDefaultValue("department");
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.MANAGER_ATTRIBUTE_NAME)
					.setType(STRING).setDefaultValue("manager");

			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.NUMBER_OF_JOBS_IN_PARALLEL).setType(NUMBER)
					.setDefaultValue(1);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.NUMBER_OF_DOCUMENTS_PER_JOB).setType(
					NUMBER).setDefaultValue(10);
			connectorLDAPInstanceSchema.createUndeletable(ConnectorLDAPInstance.USER_BASE_CONTEXT_LIST).setType(
					STRING).setDefaultRequirement(true).setMultivalue(true);

			MetadataSchemaBuilder connectorLDAPDocumentSchema = migration.newConnectorDocumentSchemaType(
					ConnectorLDAPUserDocument.SCHEMA_TYPE, ConnectorLDAPInstance.SCHEMA_CODE).getDefaultSchema();

			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.DISTINGUISHED_NAME).setType(
					STRING).setSystemReserved(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.USERNAME).setType(STRING).setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.FIRST_NAME).setType(STRING).setSearchable(
					true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.LAST_NAME).setType(STRING)
					.setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.EMAIL).setType(STRING).setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.ADDRESS).setType(TEXT).setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.WORK_TITLE).setType(STRING)
					.setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.TELEPHONE).setType(STRING)
					.setMultivalue(true).setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.DISPLAY_NAME).setType(STRING);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.COMPANY).setType(STRING).setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.DEPARTMENT).setType(STRING)
					.setSearchable(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.ENABLED).setType(BOOLEAN)
					.setDefaultValue(true);
			connectorLDAPDocumentSchema.createUndeletable(ConnectorLDAPUserDocument.MANAGER).setType(STRING);
		}

		private void updateConnectorHttpDocumentSchemaType(MetadataSchemaTypesBuilder types) {
			MetadataSchemaTypeBuilder connectorHttpDocumentSchemaType = types.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE);
			MetadataSchemaBuilder connectorHttpDocumentSchema = typesBuilder.getSchema(ConnectorHttpDocument.DEFAULT_SCHEMA);

			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.LEVEL).setType(NUMBER);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.PRIORITY).setType(NUMBER).setDefaultValue(0.5f);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.ON_DEMAND).setType(BOOLEAN);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.COPY_OF).setType(STRING);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.INLINKS).setType(STRING).setMultivalue(true);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.OUTLINKS).setType(STRING).setMultivalue(true);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.CHARSET).setType(STRING);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.DIGEST).setType(STRING);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.CONTENT_TYPE).setType(STRING);
			connectorHttpDocumentSchema.createUndeletable(ConnectorHttpDocument.DOWNLOAD_TIME).setType(NUMBER);

		}

		private void createConnectorCommonSchemaTypes(MetadataSchemaTypesBuilder types,
													  ESSchemaTypesMigrationHelper migration) {

			MetadataSchemaBuilder connectorSchema, connectorTypeSchema;

			//-
			//Create ConnectorType schema type
			MetadataSchemaTypeBuilder connectorTypeSchemaType = types.createNewSchemaTypeWithSecurity(ConnectorType.SCHEMA_TYPE);
			connectorTypeSchema = connectorTypeSchemaType.getDefaultSchema();
			connectorTypeSchema.createUniqueCodeMetadata();
			connectorTypeSchema.get(Schemas.TITLE_CODE).setMultiLingual(true);
			connectorTypeSchema.createUndeletable(ConnectorType.LINKED_SCHEMA).setType(STRING)
					.setDefaultRequirement(true);
			connectorTypeSchema.createUndeletable(ConnectorType.CONNECTOR_CLASS_NAME).setType(STRING)
					.setDefaultRequirement(true);
			connectorTypeSchema.createUndeletable(ConnectorType.DEFAULT_AVAILABLE_FIELDS).setType(STRUCTURE).setMultivalue(true)
					.defineStructureFactory(ConnectorFieldFactory.class).addValidator(ConnectorFieldValidator.class);

			//-
			//Create Connector schema type
			connectorSchema = types.createNewSchemaTypeWithSecurity(ConnectorInstance.SCHEMA_TYPE).getDefaultSchema();
			connectorSchema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true).setMultiLingual(true);
			connectorSchema.createUniqueCodeMetadata();
			connectorSchema.createUndeletable(ConnectorInstance.CONNECTOR_TYPE)
					.setType(MetadataValueType.REFERENCE).setDefaultRequirement(true).defineReferencesTo(connectorTypeSchemaType);
			connectorSchema.createUndeletable(ConnectorInstance.TRAVERSAL_CODE).setType(STRING);
			connectorSchema.createUndeletable(ConnectorInstance.ENABLED).setType(MetadataValueType.BOOLEAN).setDefaultValue(true);
			connectorSchema.createUndeletable(ConnectorInstance.LAST_TRAVERSAL_ON).setType(MetadataValueType.DATE_TIME);

			connectorSchema.createUndeletable(ConnectorInstance.AVAILABLE_FIELDS).setType(STRUCTURE).setMultivalue(true)
					.defineStructureFactory(ConnectorFieldFactory.class).addValidator(ConnectorFieldValidator.class);

			connectorSchema.createUndeletable(ConnectorInstance.PROPERTIES_MAPPING).setType(STRUCTURE)
					.defineStructureFactory(MapStringListStringStructureFactory.class);

			connectorSchema.createUndeletable(TRAVERSAL_SCHEDULE).setType(STRUCTURE).setMultivalue(true)
					.defineStructureFactory(
							TraversalScheduleFactory.class);
		}

		private void createConnectorHTTPSchemas(MetadataSchemaTypesBuilder types,
												ESSchemaTypesMigrationHelper migration) {
			MetadataSchemaBuilder instanceSchema, documentSchema;

			//-
			//Create Connector HTTP instance schema
			instanceSchema = migration.newConnectorInstanceSchema(ConnectorHttpInstance.SCHEMA_LOCAL_CODE);
			instanceSchema.createUndeletable(SEEDS).setType(TEXT).setDefaultRequirement(true);
			instanceSchema.createUndeletable(ON_DEMANDS).setType(TEXT);
			instanceSchema.createUndeletable(INCLUDE_PATTERNS).setType(TEXT);
			instanceSchema.createUndeletable(EXCLUDE_PATTERNS).setType(TEXT);
			instanceSchema.createUndeletable(NUMBER_OF_DOCUMENTS_PER_JOBS).setType(NUMBER)
					.setDefaultValue(10).setDefaultRequirement(true);
			instanceSchema.createUndeletable(NUMBER_OF_JOBS_IN_PARALLEL).setType(NUMBER)
					.setDefaultValue(1).setDefaultRequirement(true);
			instanceSchema.createUndeletable(DAYS_BEFORE_REFETCHING).setType(NUMBER)
					.setDefaultValue(5).setDefaultRequirement(true);
			instanceSchema.createUndeletable(MAX_LEVEL).setType(NUMBER)
					.setDefaultValue(5).setDefaultRequirement(true);
			instanceSchema.createUndeletable(AUTHENTICATION_SCHEME).defineAsEnum(AuthenticationScheme.class);
			instanceSchema.createUndeletable(USERNAME).setType(STRING);
			//FIMXE hidden
			instanceSchema.createUndeletable(PASSWORD).setType(STRING).setEncrypted(true);
			instanceSchema.createUndeletable(DOMAIN).setType(STRING);

			//-
			//Create Connector HTTP document schema type
			documentSchema = migration.newConnectorDocumentSchemaType(ConnectorHttpDocument.SCHEMA_TYPE,
					ConnectorHttpInstance.SCHEMA_CODE).getDefaultSchema();
			documentSchema.createUndeletable(ConnectorHttpDocument.PARSED_CONTENT).setType(TEXT).setSearchable(true);
		}

		private void createConnectorSMBSchemas(MetadataSchemaTypesBuilder types2,
											   ESSchemaTypesMigrationHelper migration) {
			MetadataSchemaBuilder instanceSchema, documentSchema, folderSchema;
			MetadataSchemaTypeBuilder folderSchemaType;

			//-
			//Create Connector SMB instance schema
			instanceSchema = migration.newConnectorInstanceSchema(ConnectorSmbInstance.SCHEMA_LOCAL_CODE);
			instanceSchema.createUndeletable(ConnectorSmbInstance.SEEDS).setType(STRING).setMultivalue(true)
					.setDefaultRequirement(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.USERNAME).setType(STRING).setDefaultRequirement(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.PASSWORD).setType(STRING).setDefaultRequirement(true)
					.setEncrypted(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.DOMAIN).setType(STRING).setDefaultRequirement(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.INCLUSIONS).setType(STRING).setMultivalue(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.EXCLUSIONS).setType(STRING).setMultivalue(true);
			instanceSchema.createUndeletable(ConnectorSmbInstance.RESUME_URL).setType(STRING);

			//-
			//Create Connector SMB folder schema type
			folderSchemaType = migration.newConnectorDocumentSchemaType(ConnectorSmbFolder.SCHEMA_TYPE,
					ConnectorSmbInstance.SCHEMA_CODE);
			folderSchema = folderSchemaType.getDefaultSchema();
			folderSchema.createUndeletable("parent").defineReferencesTo(folderSchemaType)
					.setChildOfRelationship(true);
			folderSchema.createUndeletable(ConnectorSmbFolder.LAST_FETCH_ATTEMPT).setType(DATE_TIME).setSearchable(true);
			folderSchema.createUndeletable(ConnectorSmbFolder.LAST_FETCHED_STATUS).defineAsEnum(LastFetchedStatus.class)
					.setSearchable(true);

			//-
			//Create Connector SMB document schema type
			documentSchema = migration.newConnectorDocumentSchemaType(ConnectorSmbDocument.SCHEMA_TYPE,
					ConnectorSmbInstance.SCHEMA_CODE).getDefaultSchema();
			documentSchema.createUndeletable(ConnectorSmbDocument.PARSED_CONTENT).setType(TEXT).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.SIZE).setType(NUMBER).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.PERMISSIONS_HASH).setType(STRING);
			documentSchema.createUndeletable(ConnectorSmbDocument.LAST_FETCH_ATTEMPT).setType(DATE_TIME).setSearchable(true);
			documentSchema.createUndeletable("parent").defineReferencesTo(folderSchemaType)
					.setTaxonomyRelationship(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_STATUS).defineAsEnum(LastFetchedStatus.class)
					.setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_DETAILS).setType(STRING)
					.setSearchable(false);
			documentSchema.createUndeletable(ConnectorSmbDocument.LANGUAGE).setType(STRING).setSearchable(true);
			documentSchema.createUndeletable(ConnectorSmbDocument.EXTENSION).setType(STRING).setSearchable(true);
		}

		private void updateConnectorHttpInstanceSchemaType(MetadataSchemaTypesBuilder types) {
			MetadataSchemaTypeBuilder connectorHttpInstanceSchemaType = types.getSchemaType(ConnectorHttpInstance.SCHEMA_TYPE);
			MetadataSchemaBuilder connectorHttpInstanceSchema = connectorHttpInstanceSchemaType
					.getSchema(ConnectorHttpInstance.SCHEMA_LOCAL_CODE);

		}
	}

	private void createFacets(String collection, AppLayerFactory appLayerFactory,
							  MigrationResourcesProvider migrationResourcesProvider)
			throws RecordServicesException {

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = es.getModelLayerFactory().newRecordServices();

		// Facets common to all connectors
		Facet mimetypeFacet = es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorDocument.mimetype().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.mimetype"));
		addAllMimetypeLabels(mimetypeFacet);
		recordServices.add(mimetypeFacet);

		// Facets for SMB connector
		recordServices.add(es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorSmbDocument.language().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.language"))
				.withLabel("fr", migrationResourcesProvider.get("init.facet.language.fr"))
				.withLabel("en", migrationResourcesProvider.get("init.facet.language.en"))
				.withLabel("es", migrationResourcesProvider.get("init.facet.language.es")));

		recordServices.add(es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorSmbDocument.extension().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.extension")));

		//		recordServices.add(es.newFacetField()
		//				.setUsedByModule(ConstellioESModule.ID)
		//				.setFieldDataStoreCode(es.connectorSmbDocument.parent().getDataStoreCode())
		//				.setTitle(migrationResourcesProvider.get("init.facet.smbFolder")));

		recordServices.add(es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorLdapUserDocument.enabled().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.ldapUserEnabled"))
				//FIXME
				.withLabel("_TRUE_", migrationResourcesProvider.get("init.facet.ldapUserEnabled.true"))
				.withLabel("_FALSE_", migrationResourcesProvider.get("init.facet.ldapUserEnabled.false")));

	}

	private void addAllMimetypeLabels(Facet mimetypeFacet) {
		addLabelForMimetype(mimetypeFacet, "application/x-7z-compressed");
		addLabelForMimetype(mimetypeFacet, "application/pdf");
		addLabelForMimetype(mimetypeFacet, "image/bmp");
		addLabelForMimetype(mimetypeFacet, "message/rfc822");
		addLabelForMimetype(mimetypeFacet, "image/gif");
		addLabelForMimetype(mimetypeFacet, "video/h261");
		addLabelForMimetype(mimetypeFacet, "video/h263");
		addLabelForMimetype(mimetypeFacet, "video/h264");
		addLabelForMimetype(mimetypeFacet, "text/html");
		addLabelForMimetype(mimetypeFacet, "image/x-icon");
		addLabelForMimetype(mimetypeFacet, "image/jpeg");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-powerpoint.template.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-word.document.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-word.template.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/x-msdownload");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-excel");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-excel.sheet.binary.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-excel.template.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-excel.sheet.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.slide");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.template");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		addLabelForMimetype(mimetypeFacet, "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-powerpoint");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-powerpoint.presentation.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/vnd.ms-powerpoint.slideshow.macroenabled.12");
		addLabelForMimetype(mimetypeFacet, "application/vnd.visio");
		addLabelForMimetype(mimetypeFacet, "audio/x-ms-wma");
		addLabelForMimetype(mimetypeFacet, "video/x-ms-wmv");
		addLabelForMimetype(mimetypeFacet, "application/msword");
		addLabelForMimetype(mimetypeFacet, "audio/midi");
		addLabelForMimetype(mimetypeFacet, "video/mpeg");
		addLabelForMimetype(mimetypeFacet, "video/mp4");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.calc");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.calc.template");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.draw");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.draw.template");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.impress");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.impress.template");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.math");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.writer");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.writer.global");
		addLabelForMimetype(mimetypeFacet, "application/vnd.sun.xml.writer.template");
		addLabelForMimetype(mimetypeFacet, "image/vnd.adobe.photoshop");
		addLabelForMimetype(mimetypeFacet, "image/png");
		addLabelForMimetype(mimetypeFacet, "application/x-rar-compressed");
		addLabelForMimetype(mimetypeFacet, "application/rtf");
		addLabelForMimetype(mimetypeFacet, "application/rss+xml");
		addLabelForMimetype(mimetypeFacet, "image/tiff");
		addLabelForMimetype(mimetypeFacet, "text/plain");
		addLabelForMimetype(mimetypeFacet, "audio/x-wav");
		addLabelForMimetype(mimetypeFacet, "application/xml");
		addLabelForMimetype(mimetypeFacet, "application/zip");
	}

	private void addLabelForMimetype(Facet facet, String mimetype) {
		facet.withLabel(mimetype, this.migrationResourcesProvider.get("init.facet.mimetype." + mimetype));
	}

	public static void createSmbFoldersTaxonomy(String collection, ModelLayerFactory modelLayerFactory,
												MigrationResourcesProvider migrationResourcesProvider) {

		Map<Language, String> mapLangageTitre = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
				migrationResourcesProvider, "init.taxoSmbFolders");

		Taxonomy taxonomy = Taxonomy
				.createPublic(ESTaxonomies.SMB_FOLDERS, mapLangageTitre, collection, ConnectorSmbFolder.SCHEMA_TYPE);

		try {
			modelLayerFactory.getTaxonomiesManager().addTaxonomy(taxonomy, modelLayerFactory.getMetadataSchemasManager());
		} catch (TaxonomyAlreadyExists e) {

		}
	}

	private void configureConnectorsInstancesAndDocumentsDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		configureConnectorInstanceDisplayAndSearchDisplay(transaction, manager, collection);

		configureHttpConnectorDisplay(transaction, manager, collection);
		configureSmbConnectorDisplay(transaction, manager, collection);
		configureLDAPConnectorDisplay(transaction, manager, collection);
		configureHttpDocumentDisplay(transaction, manager, collection);
		configureSmbDocumentDisplay(transaction, manager, collection);
		configureLDAPDocumentDisplay(transaction, manager, collection);

		manager.execute(transaction.build());
	}

	private void configureLDAPDocumentDisplay(SchemaTypesDisplayTransactionBuilder transaction,
											  SchemasDisplayManager manager,
											  String collection) {
		transaction
				.add(manager.getType(collection, ConnectorLDAPUserDocument.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));

		transaction
				.add(manager.getMetadata(collection, ConnectorLDAPUserDocument.DEFAULT_SCHEMA, ConnectorLDAPUserDocument.USERNAME)
						.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPUserDocument.DEFAULT_SCHEMA, ConnectorLDAPUserDocument.FIRST_NAME)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPUserDocument.DEFAULT_SCHEMA, ConnectorLDAPUserDocument.LAST_NAME)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorLDAPUserDocument.DEFAULT_SCHEMA, ConnectorLDAPUserDocument.EMAIL)
				.withVisibleInAdvancedSearchStatus(true));

		transaction
				.add(manager.getMetadata(collection, ConnectorLDAPUserDocument.DEFAULT_SCHEMA, ConnectorLDAPUserDocument.ADDRESS)
						.withVisibleInAdvancedSearchStatus(true));
		transaction
				.add(manager
						.getMetadata(collection, ConnectorLDAPUserDocument.DEFAULT_SCHEMA, ConnectorLDAPUserDocument.TELEPHONE)
						.withVisibleInAdvancedSearchStatus(true));

	}

	private void configureLDAPConnectorDisplay(SchemaTypesDisplayTransactionBuilder transaction,
											   SchemasDisplayManager manager,
											   String collection) {
		List<String> form = asList(
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.CODE,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.TITLE,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.CONNECTION_USERNAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.PASSWORD,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.URLS,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.USER_BASE_CONTEXT_LIST,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.INCLUDE_REGEX,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.EXCLUDE_REGEX,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.FOLLOW_REFERENCES,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ENABLED,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + TRAVERSAL_SCHEDULE,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.NUMBER_OF_DOCUMENTS_PER_JOB,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.NUMBER_OF_JOBS_IN_PARALLEL,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.DISTINGUISHED_NAME_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.USERNAME_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.FIRST_NAME_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.LAST_NAME_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.EMAIL_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.TELEPHONE_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.ADDRESS_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.WORK_TITLE_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.DISPLAY_NAME_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.COMPANY_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.DEPARTMENT_ATTRIBUTE_NAME,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.MANAGER_ATTRIBUTE_NAME
		);

		List<String> display = asList(
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.CODE,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.TITLE,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.URLS,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.USER_BASE_CONTEXT_LIST,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.INCLUDE_REGEX,
				ConnectorLDAPInstance.SCHEMA_CODE + "_" + ConnectorLDAPInstance.EXCLUDE_REGEX
		);

		transaction.addReplacing(transaction.getModifiedSchema(ConnectorLDAPInstance.SCHEMA_CODE)
				.withFormMetadataCodes(form)
				.withDisplayMetadataCodes(display));

		transaction
				.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.CONNECTION_USERNAME)
						.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.PASSWORD)
				.withMetadataGroup(configurationTab).withInputType(MetadataInputType.PASSWORD));
		transaction.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.DIRECTORY_TYPE)
				.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.URLS)
				.withMetadataGroup(configurationTab));
		transaction
				.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.FOLLOW_REFERENCES)
						.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.FETCH_USERS)
				.withMetadataGroup(configurationTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.USER_BASE_CONTEXT_LIST)
				.withMetadataGroup(configurationTab));

		transaction.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ENABLED)
				.withMetadataGroup(executionTab));
		transaction.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, TRAVERSAL_SCHEDULE)
				.withMetadataGroup(executionTab));
		transaction.add(
				manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE,
						ConnectorLDAPInstance.NUMBER_OF_DOCUMENTS_PER_JOB)
						.withMetadataGroup(executionTab));
		transaction.add(
				manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE,
						ConnectorLDAPInstance.NUMBER_OF_JOBS_IN_PARALLEL)
						.withMetadataGroup(executionTab));

		transaction
				.add(manager.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE,
						ConnectorLDAPInstance.DISTINGUISHED_NAME_ATTRIBUTE_NAME)
						.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.USERNAME_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.FIRST_NAME_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.LAST_NAME_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.EMAIL_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.ADDRESS_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.WORK_TITLE_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.TELEPHONE_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.DISPLAY_NAME_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.COMPANY_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.DEPARTMENT_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));
		transaction.add(manager
				.getMetadata(collection, ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.MANAGER_ATTRIBUTE_NAME)
				.withMetadataGroup(ldapUserTab));

	}

	private void configureSmbConnectorDisplay(SchemaTypesDisplayTransactionBuilder transaction,
											  SchemasDisplayManager manager,
											  String collection) {

		List<String> form = asList(
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.CODE,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.TITLE,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.DOMAIN,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.USERNAME,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.PASSWORD,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.SEEDS,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.EXCLUSIONS,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.INCLUSIONS,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.ENABLED,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.TRAVERSAL_SCHEDULE
				//ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.RESUME_URL
		);

		List<String> display = asList(
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.CODE,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.TITLE,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.DOMAIN,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.USERNAME,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.SEEDS,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.EXCLUSIONS,
				ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.INCLUSIONS
				//ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.RESUME_URL
		);

		transaction.addReplacing(transaction.getModifiedSchema(ConnectorSmbInstance.SCHEMA_CODE)
				.withFormMetadataCodes(form)
				.withDisplayMetadataCodes(display));

		transaction.add(manager.getMetadata(collection, ConnectorSmbInstance.SCHEMA_CODE, ConnectorSmbInstance.PASSWORD)
				.withInputType(MetadataInputType.PASSWORD));

		transaction.add(manager.getMetadata(collection, ConnectorSmbInstance.SCHEMA_CODE, ENABLED)
				.withMetadataGroup(executionTab));
		transaction.add(manager.getMetadata(collection, ConnectorSmbInstance.SCHEMA_CODE, TRAVERSAL_SCHEDULE)
				.withMetadataGroup(executionTab));

	}

	private void configureHttpConnectorDisplay(SchemaTypesDisplayTransactionBuilder transaction,
											   SchemasDisplayManager manager,
											   String collection) {
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, ENABLED)
				.withMetadataGroup(executionTab));
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, NUMBER_OF_DOCUMENTS_PER_JOBS)
				.withMetadataGroup(executionTab));
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, NUMBER_OF_JOBS_IN_PARALLEL)
				.withMetadataGroup(executionTab));
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, DAYS_BEFORE_REFETCHING)
				.withMetadataGroup(executionTab));
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, TRAVERSAL_SCHEDULE)
				.withMetadataGroup(executionTab));

		transaction.add(manager
				.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, ConnectorHttpInstance.AUTHENTICATION_SCHEME)
				.withMetadataGroup(credentialsTab));
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, ConnectorHttpInstance.USERNAME)
				.withMetadataGroup(credentialsTab));
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, ConnectorHttpInstance.PASSWORD)
				.withMetadataGroup(credentialsTab).withInputType(MetadataInputType.PASSWORD));

		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, ConnectorHttpInstance.DOMAIN)
				.withMetadataGroup(credentialsTab));

	}

	private void configureSmbDocumentDisplay(SchemaTypesDisplayTransactionBuilder transaction,
											 SchemasDisplayManager manager,
											 String collection) {
		transaction.add(manager.getType(collection, ConnectorSmbDocument.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));

		transaction.add(manager.getType(collection, ConnectorSmbDocument.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.TITLE)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorSmbDocument.EXTENSION)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorSmbDocument.ERROR_CODE)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorSmbDocument.LANGUAGE)
				.withVisibleInAdvancedSearchStatus(true));

		transaction
				.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorSmbDocument.PARSED_CONTENT)
						.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorSmbDocument.SIZE)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorDocument.FETCHED_DATETIME)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorSmbDocument.URL)
				.withVisibleInAdvancedSearchStatus(true));
	}

	private void configureHttpDocumentDisplay(SchemaTypesDisplayTransactionBuilder transaction,
											  SchemasDisplayManager manager,
											  String collection) {
		transaction.in(ConnectorHttpDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorHttpDocument.URL)
				.atTheEnd();

		transaction.add(manager.getType(collection, ConnectorHttpDocument.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.TITLE)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.CONTENT_TYPE)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.ERROR_CODE)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.LEVEL)
				.withVisibleInAdvancedSearchStatus(true));
		transaction
				.add(manager.getMetadata(collection, ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.PARSED_CONTENT)
						.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorDocument.FETCHED_DATETIME)
				.withVisibleInAdvancedSearchStatus(true));

		transaction.add(manager.getMetadata(collection, ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.URL)
				.withVisibleInAdvancedSearchStatus(true));

	}

	private void configureConnectorInstanceDisplayAndSearchDisplay(SchemaTypesDisplayTransactionBuilder transaction,
																   SchemasDisplayManager manager, String collection) {

		transaction.add(manager.getType(collection, ConnectorInstance.SCHEMA_TYPE)
				.withMetadataGroup(groups));

		transaction.add(manager.getMetadata(collection, ConnectorInstance.DEFAULT_SCHEMA, ConnectorInstance.CONNECTOR_TYPE)
				.withInputType(MetadataInputType.HIDDEN));

		transaction.in(ConnectorInstance.SCHEMA_TYPE)
				.addToSearchResult(ConnectorInstance.CONNECTOR_TYPE)
				.afterMetadata(Schemas.TITLE_CODE);
		transaction.in(ConnectorInstance.SCHEMA_TYPE)
				.addToSearchResult(ConnectorInstance.TRAVERSAL_CODE)
				.afterMetadata(ConnectorInstance.CONNECTOR_TYPE);
		transaction.in(ConnectorInstance.SCHEMA_TYPE)
				.removeFromSearchResult(Schemas.MODIFIED_ON.getLocalCode());

		transaction.in(ConnectorInstance.SCHEMA_TYPE)
				.addToDisplay(ConnectorInstance.CONNECTOR_TYPE)
				.afterMetadata(Schemas.TITLE_CODE);
		transaction.in(ConnectorInstance.SCHEMA_TYPE)
				.addToDisplay(ConnectorInstance.TRAVERSAL_CODE)
				.afterMetadata(ConnectorInstance.CONNECTOR_TYPE);
		transaction.in(ConnectorInstance.SCHEMA_TYPE)
				.addToDisplay(Schemas.MODIFIED_ON.getLocalCode())
				.afterMetadata(ConnectorInstance.SCHEMA_TYPE);
	}
}
