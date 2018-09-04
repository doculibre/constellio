package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo8_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		// Changement de libélé
		new RMMigrationTo8_1.SchemaAlterationFor8_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor8_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "8.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(Printable.DEFAULT_SCHEMA).get(Printable.TITLE).setMultiLingual(true);
			//For i18n purposes
		}
	}

	public class RMMigrationTo8_1_0_2 implements MigrationScript {
		private String collection;

		private MigrationResourcesProvider migrationResourcesProvider;

		private AppLayerFactory appLayerFactory;

	=======
	import MetadataSchemasAlterationHelper;
	import MigrationHelper;
	import MigrationResourcesProvider;
	import MigrationScript;
	import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDepositDatesCalculator2;
	import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDestructionDatesCalculator2;
	import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator2;
	import com.constellio.app.modules.rm.wrappers.Folder;
	import AppLayerFactory;
	import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
	import MetadataSchemaTypesBuilder;

	public class RMMigrationTo8_1_0_1 extends MigrationHelper implements MigrationScript {
	>>>>>>> Added configuration to calculate deposit/destruction dates based on expected transfer date
		@Override
		public String getVersion() {
			return "8.1.0.1";
		}

		@Override
		public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
	<<<<<<< HEAD
							AppLayerFactory appLayerFactory)
				throws Exception {
			this.collection = collection;
			this.migrationResourcesProvider = migrationResourcesProvider;
			this.appLayerFactory = appLayerFactory;

			reloadEmailTemplates();

		}

		private void reloadEmailTemplates() {
			if (appLayerFactory.getModelLayerFactory().getCollectionsListManager()
					.getCollectionLanguages(collection).get(0).equals("en")) {
				reloadEmailTemplate("alertAvailableTemplate_en.html", RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
				reloadEmailTemplate("alertBorrowedTemplate_en.html", RMEmailTemplateConstants.ALERT_BORROWED_ACCEPTED);
				reloadEmailTemplate("alertBorrowedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_BORROWED_DENIED);
				reloadEmailTemplate("alertBorrowingExtendedTemplate_en.html",
						RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_ACCEPTED);
				reloadEmailTemplate("alertBorrowingExtendedTemplateDenied_en.html",
						RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_DENIED);
				reloadEmailTemplate("alertReactivatedTemplate_en.html", RMEmailTemplateConstants.ALERT_REACTIVATED_ACCEPTED);
				reloadEmailTemplate("alertReactivatedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_REACTIVATED_DENIED);
				reloadEmailTemplate("alertReturnedTemplate_en.html", RMEmailTemplateConstants.ALERT_RETURNED_ACCEPTED);
				reloadEmailTemplate("alertReturnedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_RETURNED_DENIED);
				reloadEmailTemplate("alertWhenDecommissioningListCreatedTemplate_en.html",
						RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
				reloadEmailTemplate("approvalRequestForDecomListTemplate.html",
						RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID);
			} else {
				reloadEmailTemplate("alertAvailableTemplate.html", RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
				reloadEmailTemplate("alertBorrowedTemplate.html", RMEmailTemplateConstants.ALERT_BORROWED_ACCEPTED);
				reloadEmailTemplate("alertBorrowedTemplateDenied.html", RMEmailTemplateConstants.ALERT_BORROWED_DENIED);
				reloadEmailTemplate("alertBorrowingExtendedTemplate.html",
						RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_ACCEPTED);
				reloadEmailTemplate("alertBorrowingExtendedTemplateDenied.html",
						RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_DENIED);
				reloadEmailTemplate("alertReactivatedTemplate.html", RMEmailTemplateConstants.ALERT_REACTIVATED_ACCEPTED);
				reloadEmailTemplate("alertReactivatedTemplateDenied.html", RMEmailTemplateConstants.ALERT_REACTIVATED_DENIED);
				reloadEmailTemplate("alertReturnedTemplate.html", RMEmailTemplateConstants.ALERT_RETURNED_ACCEPTED);
				reloadEmailTemplate("alertReturnedTemplateDenied.html", RMEmailTemplateConstants.ALERT_RETURNED_DENIED);
				reloadEmailTemplate("alertWhenDecommissioningListCreatedTemplate.html",
						RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
				reloadEmailTemplate("approvalRequestForDecomListTemplate.html",
						RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID);
				reloadEmailTemplate("validationRequestForDecomListTemplate.html",
						RMEmailTemplateConstants.VALIDATION_REQUEST_TEMPLATE_ID);
			}
		}

		private void reloadEmailTemplate(final String templateFileName, final String templateId) {
			final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

			try {
				appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
						.replaceCollectionTemplate(templateId, collection, templateInputStream);
			} catch (IOException | ConfigManagerException.OptimisticLockingConfiguration e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(templateInputStream);
	=======
							AppLayerFactory appLayerFactory) throws Exception {
			new RMMigrationTo8_1_0_1.SchemaAlterationFor8_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}

		class SchemaAlterationFor8_1 extends MetadataSchemasAlterationHelper {

			protected SchemaAlterationFor8_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
				super(collection, migrationResourcesProvider, appLayerFactory);
			}

			@Override
			protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
				MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);

				folderSchema.get(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedTransferDatesCalculator2.class);
				folderSchema.get(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedDepositDatesCalculator2.class);
				folderSchema.get(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedDestructionDatesCalculator2.class);
	>>>>>>> Added configuration to calculate deposit/destruction dates based on expected transfer date
			}
		}
	}
		}
