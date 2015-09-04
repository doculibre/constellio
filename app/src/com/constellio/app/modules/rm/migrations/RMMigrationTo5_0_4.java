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
package com.constellio.app.modules.rm.migrations;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.model.calculators.ContainerRecordTreeVisibilityCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderTreeVisibilityCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_0_4 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.0.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new SchemaAlterationFor5_0_4(collection, migrationResourcesProvider, appLayerFactory).migrate();

		createEmailDocumentType(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);
		setupDisplayConfigForEmail(collection, appLayerFactory);
	}

	private void setupDisplayConfigForEmail(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		SchemaDisplayConfig schemaFormEmailConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, Email.SCHEMA),
				Document.TYPE,
				Schemas.TITLE.getLocalCode(),
				Document.CONTENT,
				Document.FOLDER,
				Document.KEYWORDS,
				Email.EMAIL_TO,
				Email.EMAIL_FROM,
				Email.EMAIL_IN_NAME_OF,
				Email.EMAIL_CC_TO,
				Email.EMAIL_BCC_TO,
				Email.EMAIL_OBJECT,
				Email.EMAIL_ATTACHMENTS_LIST,
				Email.EMAIL_SENT_ON,
				Email.EMAIL_RECEIVED_ON,
				Document.COMPANY,
				Email.SUBJECT_TO_BROADCAST_RULE,
				Document.AUTHOR,
				Document.COMMENTS);
		SchemaDisplayConfig schemaDisplayEmailConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, Email.SCHEMA),
				Document.TYPE,
				Schemas.TITLE.getLocalCode(),
				Document.CONTENT,
				Document.TYPE,
				Document.FOLDER,
				Document.KEYWORDS,
				Email.EMAIL_TO,
				Email.EMAIL_FROM,
				Email.EMAIL_IN_NAME_OF,
				Email.EMAIL_CC_TO,
				Email.EMAIL_BCC_TO,
				Email.EMAIL_OBJECT,
				Email.EMAIL_ATTACHMENTS_LIST,
				Email.EMAIL_SENT_ON,
				Email.EMAIL_RECEIVED_ON,
				Document.COMPANY,
				Email.SUBJECT_TO_BROADCAST_RULE,
				Document.AUTHOR,
				Email.EMAIL_CONTENT,
				Document.COMMENTS);
		transaction.add(
				schemaDisplayEmailConfig.withFormMetadataCodes(schemaFormEmailConfig.getFormMetadataCodes()));
		manager.execute(transaction);
	}

	private void createEmailDocumentType(String collection, ModelLayerFactory modelLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {
		Transaction transaction = new Transaction();

		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);

		if (schemas.getDocumentTypeByCode(DocumentType.EMAIL_DOCUMENT_TYPE) == null) {
			transaction.add(schemas.newDocumentType().setCode(DocumentType.EMAIL_DOCUMENT_TYPE)
					.setTitle($("DocumentType.emailDocumentType")).setLinkedSchema(Email.SCHEMA));
		}

		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

	}

	class SchemaAlterationFor5_0_4 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor5_0_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.0.4";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			createCustomSchemaEmail(typesBuilder);

			enableCodeMetadata(typesBuilder);
			enableTitleMetadata(typesBuilder);
		}

		private void createCustomSchemaEmail(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder documentType = typesBuilder.getSchemaType(Email.SCHEMA_TYPE);

			MetadataSchemaBuilder emailSchema = documentType.createCustomSchema("email");

			emailSchema.createUndeletable(Email.EMAIL_TO).setType(MetadataValueType.STRING).setMultivalue(true);
			emailSchema.createUndeletable(Email.EMAIL_FROM).setType(MetadataValueType.STRING);
			emailSchema.createUndeletable(Email.EMAIL_IN_NAME_OF).setType(MetadataValueType.STRING);
			emailSchema.createUndeletable(Email.EMAIL_CC_TO).setType(MetadataValueType.STRING).setMultivalue(true);
			emailSchema.createUndeletable(Email.EMAIL_BCC_TO).setType(MetadataValueType.STRING).setMultivalue(true);
			emailSchema.createUndeletable(Email.EMAIL_ATTACHMENTS_LIST).setType(MetadataValueType.STRING).setMultivalue(true);
			emailSchema.createUndeletable(Email.EMAIL_OBJECT).setType(MetadataValueType.STRING);
			emailSchema.createUndeletable(Email.EMAIL_COMPANY).setType(MetadataValueType.STRING);
			emailSchema.createUndeletable(Email.EMAIL_CONTENT).setType(MetadataValueType.TEXT);
			emailSchema.createUndeletable(Email.EMAIL_SENT_ON).setType(MetadataValueType.DATE_TIME);
			emailSchema.createUndeletable(Email.EMAIL_RECEIVED_ON).setType(MetadataValueType.DATE_TIME);

			emailSchema.createUndeletable(Email.SUBJECT_TO_BROADCAST_RULE).setType(MetadataValueType.BOOLEAN);
			activateVisibilityCalculator(typesBuilder);
		}

		private void enableCodeMetadata(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(FolderType.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(FolderType.CODE).setEnabled(true);

			schema = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(DocumentType.CODE).setEnabled(true);

			schema = typesBuilder.getSchemaType(MediumType.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(MediumType.CODE).setEnabled(true);

			schema = typesBuilder.getSchemaType(StorageSpaceType.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(StorageSpaceType.CODE).setEnabled(true);

			schema = typesBuilder.getSchemaType(ContainerRecordType.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(ContainerRecordType.CODE).setEnabled(true);

			schema = typesBuilder.getSchemaType(ContainerRecordType.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(ContainerRecordType.CODE).setEnabled(true);

			schema = typesBuilder.getSchemaType(FilingSpace.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(FilingSpace.CODE).setEnabled(true).setUniqueValue(false);
		}

		private void enableTitleMetadata(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true);

			schema = typesBuilder.getSchemaType(Category.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true);

			schema = typesBuilder.getSchemaType(FilingSpace.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true);

			schema = typesBuilder.getSchemaType(StorageSpace.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true);

			schema = typesBuilder.getSchemaType(UniformSubdivision.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true);
		}

		private void activateVisibilityCalculator(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			MetadataBuilder folderVisibility = schema.getMetadata(CommonMetadataBuilder.VISIBLE_IN_TREES)
					.defineDataEntry().asCalculated(FolderTreeVisibilityCalculator.class);

			schema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(CommonMetadataBuilder.VISIBLE_IN_TREES)
					.defineDataEntry().asCopied(schema.getMetadata(Document.FOLDER), folderVisibility);

			schema = typesBuilder.getSchemaType(ContainerRecord.SCHEMA_TYPE).getDefaultSchema();
			schema.getMetadata(CommonMetadataBuilder.VISIBLE_IN_TREES)
					.defineDataEntry().asCalculated(ContainerRecordTreeVisibilityCalculator.class);
		}
	}
}
