package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.model.calculators.FolderTokensOfHierarchyCalculator;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS_OF_HIERARCHY;

/**
 * Created by Charles Blanchette on 2017-03-22.
 */
public class RMMigrationTo7_6_6 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {
		reloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);
		new SchemaAlterationFor7_6_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	public static void reloadEmailTemplates(AppLayerFactory appLayerFactory,
											MigrationResourcesProvider migrationResourcesProvider,
											String collection) {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("en")) {
			reloadEmailTemplate("alertBorrowedTemplate_en.html", RMEmailTemplateConstants.ALERT_BORROWED_ACCEPTED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplate_en.html", RMEmailTemplateConstants.ALERT_RETURNED_ACCEPTED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplate_en.html", RMEmailTemplateConstants.ALERT_REACTIVATED_ACCEPTED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplate_en.html",
					RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_ACCEPTED, appLayerFactory, migrationResourcesProvider,
					collection);
			reloadEmailTemplate("alertBorrowedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_BORROWED_DENIED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_RETURNED_DENIED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_REACTIVATED_DENIED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplateDenied_en.html",
					RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_DENIED, appLayerFactory, migrationResourcesProvider,
					collection);
		} else {
			reloadEmailTemplate("alertBorrowedTemplate.html", RMEmailTemplateConstants.ALERT_BORROWED_ACCEPTED, appLayerFactory,
					migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplate.html", RMEmailTemplateConstants.ALERT_RETURNED_ACCEPTED, appLayerFactory,
					migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplate.html", RMEmailTemplateConstants.ALERT_REACTIVATED_ACCEPTED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplate.html", RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_ACCEPTED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowedTemplateDenied.html", RMEmailTemplateConstants.ALERT_BORROWED_DENIED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplateDenied.html", RMEmailTemplateConstants.ALERT_RETURNED_DENIED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplateDenied.html", RMEmailTemplateConstants.ALERT_REACTIVATED_DENIED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplateDenied.html",
					RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_DENIED, appLayerFactory, migrationResourcesProvider,
					collection);
		}
	}

	private static void reloadEmailTemplate(final String templateFileName, final String templateId,
											AppLayerFactory appLayerFactory,
											MigrationResourcesProvider migrationResourcesProvider, String collection) {
		final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | ConfigManagerException.OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}

	class SchemaAlterationFor7_6_6 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).setSmallCode("f");
			typesBuilder.getSchemaType(Document.SCHEMA_TYPE).setSmallCode("d");
			typesBuilder.getSchemaType(Task.SCHEMA_TYPE).setSmallCode("t");
			typesBuilder.getSchemaType(ContainerRecord.SCHEMA_TYPE).setSmallCode("c");

//			if (typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(Schemas.NON_TAXONOMY_AUTHORIZATIONS.getLocalCode())
//						.getType() == MetadataValueType.REFERENCE) {

				typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(Schemas.TOKENS.getLocalCode())
						.defineDataEntry().asCalculated(TokensCalculator4.class);

				typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Schemas.TOKENS.getLocalCode())
						.defineDataEntry().asCalculated(TokensCalculator4.class);

				MetadataBuilder folderParent = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.PARENT_FOLDER);
				MetadataBuilder documentFolder = typesBuilder.getSchema(Document.DEFAULT_SCHEMA).get(Document.FOLDER);
				MetadataBuilder documentTokensHierarchy = typesBuilder.getSchema(Document.DEFAULT_SCHEMA)
						.get(TOKENS_OF_HIERARCHY);

				MetadataBuilder tokensHierarchy = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).getMetadata(TOKENS_OF_HIERARCHY)
						.defineDataEntry().asCalculated(FolderTokensOfHierarchyCalculator.class);

				if (!typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).hasMetadata(Folder.SUB_FOLDERS_TOKENS)) {
					typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).create(Folder.SUB_FOLDERS_TOKENS)
							.setType(STRING).setMultivalue(true)
							.defineDataEntry().asUnion(folderParent, tokensHierarchy);

					typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).create(Folder.DOCUMENTS_TOKENS)
							.setType(STRING).setMultivalue(true)
							.defineDataEntry().asUnion(documentFolder, documentTokensHierarchy);
				}
			//}
			MetadataSchemaTypeBuilder builder = typesBuilder.createNewSchemaTypeWithSecurity(BagInfo.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultBagInfoSchema = builder.getDefaultSchema();

			defaultBagInfoSchema.create(BagInfo.ARCHIVE_TITLE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.NOTE).setType(MetadataValueType.TEXT);
			defaultBagInfoSchema.create(BagInfo.IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.ID_ORGANISME_VERSEUR_OU_DONATEUR).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.ADRESSE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.REGION_ADMINISTRATIVE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.ENTITE_RESPONSABLE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.IDENTIFICATION_RESPONSABLE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.COURRIEL_RESPONSABLE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.NUMERO_TELEPHONE_RESPONSABLE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.DESCRIPTION_SOMMAIRE).setType(MetadataValueType.TEXT);
			defaultBagInfoSchema.create(BagInfo.CATEGORIE_DOCUMENT).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.METHODE_TRANSFERE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.RESTRICTION_ACCESSIBILITE).setType(MetadataValueType.STRING);
			defaultBagInfoSchema.create(BagInfo.ENCODAGE).setType(MetadataValueType.STRING);

		}
	}
}
