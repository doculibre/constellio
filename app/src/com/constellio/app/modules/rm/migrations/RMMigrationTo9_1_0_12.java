package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCheckedOutDateCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo9_1_0_12 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.0.12";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_1_0_12(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_1_0_12 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_1_0_12(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder document = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);

			if (!document.hasMetadata(Document.IS_CHECKOUT_ALERT_SENT)) {
				document.createUndeletable(Document.IS_CHECKOUT_ALERT_SENT).setType(MetadataValueType.BOOLEAN)
						.setDefaultValue(false).setSystemReserved(true);
				addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "alertBorrowingPeriodEndedTemplate.html",
						RMEmailTemplateConstants.ALERT_BORROWING_PERIOD_ENDED);
			}

			if (!document.hasMetadata(Document.CONTENT_CHECKED_OUT_DATE)) {
				document.createUndeletable(Document.CONTENT_CHECKED_OUT_DATE)
						.setType(MetadataValueType.DATE_TIME)
						.defineDataEntry().asCalculated(DocumentCheckedOutDateCalculator.class);
			}

		}

		private void addEmailTemplate(AppLayerFactory appLayerFactory,
									  MigrationResourcesProvider migrationResourcesProvider, String collection,
									  String templateFileName, String templateId) {
			InputStream remindReturnBorrowedFolderTemplate = migrationResourcesProvider.getStream(templateFileName);
			try {
				appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
						.addCollectionTemplateIfInexistent(templateId, collection, remindReturnBorrowedFolderTemplate);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (ConfigManagerException.OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new RuntimeException(optimisticLockingConfiguration);
			} finally {
				IOUtils.closeQuietly(remindReturnBorrowedFolderTemplate);
			}
		}

	}
}
