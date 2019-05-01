package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.connectors.http.ConnectorHttp;
import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

public class ESMigrationCombo implements ComboMigrationScript {
	@Override
	public List<MigrationScript> getVersions() {
		List<MigrationScript> scripts = new ArrayList<>();

		scripts.add(new ESMigrationTo5_1_6());
		scripts.add(new ESMigrationTo6_1());
		scripts.add(new ESMigrationTo6_2());
		scripts.add(new ESMigrationTo6_4());
		scripts.add(new ESMigrationTo6_5_42());
		scripts.add(new ESMigrationTo6_5_58());
		scripts.add(new ESMigrationTo7_1_3());
		scripts.add(new ESMigrationTo7_4_1());
		scripts.add(new ESMigrationTo7_4_2());
		scripts.add(new ESMigrationTo7_4_3());
		scripts.add(new ESMigrationTo7_5());
		scripts.add(new ESMigrationTo7_6_1());
		scripts.add(new ESMigrationTo7_6_1_1());
		scripts.add(new ESMigrationTo7_6_2());
		scripts.add(new ESMigrationTo7_6_3());
		scripts.add(new ESMigrationTo7_6_6());
		scripts.add(new ESMigrationTo7_7());
		scripts.add(new ESMigrationTo7_7_0_42());
		scripts.add(new ESMigrationTo8_0());
		scripts.add(new ESMigrationTo8_0_1());
		scripts.add(new ESMigrationTo8_0_2());

		return scripts;
	}

	@Override
	public String getVersion() {
		return getVersions().get(getVersions().size() - 1).getVersion();
	}

	GeneratedESMigrationCombo generatedComboMigration;

	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.migrationResourcesProvider = migrationResourcesProvider;
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		generatedComboMigration = new GeneratedESMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		generatedComboMigration.applyGeneratedRoles();
		generatedComboMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		applySchemasDisplay2(collection, migrationResourcesProvider, appLayerFactory.getMetadataSchemasDisplayManager());

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		ESMigrationTo5_1_6.createSmbFoldersTaxonomy(collection, modelLayerFactory, migrationResourcesProvider);

		recordServices.execute(createRecordTransaction(collection, migrationResourcesProvider, appLayerFactory, types));
	}

	private void applySchemasDisplay2(String collection, MigrationResourcesProvider migrationResourcesProvider,
									  SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		manager.execute(transaction.build());
	}

	private Transaction createRecordTransaction(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory, MetadataSchemaTypes types) {
		Transaction transaction = new Transaction();

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
		EnterpriseSearchMigrationHelper migration = new EnterpriseSearchMigrationHelper(appLayerFactory, collection,
				migrationResourcesProvider);

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

		// Facets common to all connectors
		Facet mimetypeFacet = es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorDocument.mimetype().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.mimetype"));
		addAllMimetypeLabels(mimetypeFacet);
		transaction.add(mimetypeFacet);

		// Facets for SMB connector
		transaction.add(es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorSmbDocument.language().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.language"))
				.withLabel("fr", migrationResourcesProvider.get("init.facet.language.fr"))
				.withLabel("en", migrationResourcesProvider.get("init.facet.language.en"))
				.withLabel("es", migrationResourcesProvider.get("init.facet.language.es")));

		transaction.add(es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorSmbDocument.extension().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.extension")));

		//		transaction.add(es.newFacetField()
		//				.setUsedByModule(ConstellioESModule.ID)
		//				.setFieldDataStoreCode(es.connectorSmbDocument.parent().getDataStoreCode())
		//				.setTitle(migrationResourcesProvider.get("init.facet.smbFolder")));

		transaction.add(es.newFacetField()
				.setUsedByModule(ConstellioESModule.ID)
				.setFieldDataStoreCode(es.connectorLdapUserDocument.enabled().getDataStoreCode())
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.ldapUserEnabled"))
				//FIXME
				.withLabel("_TRUE_", migrationResourcesProvider.get("init.facet.ldapUserEnabled.true"))
				.withLabel("_FALSE_", migrationResourcesProvider.get("init.facet.ldapUserEnabled.false")));

		return transaction;
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

	private ConnectorField field(String schemaType, String code, MetadataValueType type) {
		String label = migrationResourcesProvider.get(code);
		return new ConnectorField(schemaType + ":" + code, label, type);
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection,
								   MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			generatedComboMigration.applyGeneratedSchemaAlteration(typesBuilder);
			typesBuilder.getDefaultSchema(ConnectorInstance.SCHEMA_TYPE).get(Schemas.TITLE_CODE).setMultiLingual(true);
			typesBuilder.getDefaultSchema(ConnectorType.SCHEMA_TYPE).get(Schemas.TITLE_CODE).setMultiLingual(true);
			new CommonMetadataBuilder().addCommonMetadataToAllExistingSchemas(typesBuilder);
		}

	}

}
