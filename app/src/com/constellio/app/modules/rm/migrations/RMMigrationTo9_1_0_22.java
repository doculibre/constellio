package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.io.IOException;
import java.io.InputStream;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class RMMigrationTo9_1_0_22 implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "9.1.0.22";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_0_22(collection, migrationResourcesProvider, appLayerFactory).migrate();

		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		addEmailTemplates();
	}

	private class SchemaAlterationFor9_1_0_22 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_0_22(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder decomListSchema = typesBuilder.getSchemaType(DecommissioningList.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaTypeBuilder userSchemaType = type(User.SCHEMA_TYPE);

			decomListSchema.createUndeletable(DecommissioningList.VALIDATION_REQUESTER).setType(REFERENCE)
					.defineReferencesTo(userSchemaType);
		}
	}

	private void addEmailTemplates() {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("fr")) {
			addEmailTemplates("approvalRequestApprovedTemplate.html",
					RMEmailTemplateConstants.APPROVAL_REQUEST_APPROVED_TEMPLATE_ID);

			addEmailTemplates("validationRequestValidatedTemplate.html",
					RMEmailTemplateConstants.VALIDATION_REQUEST_VALIDATED_TEMPLATE_ID);
		} else {
			addEmailTemplates("approvalRequestApprovedTemplate_en.html",
					RMEmailTemplateConstants.APPROVAL_REQUEST_APPROVED_TEMPLATE_ID);

			addEmailTemplates("validationRequestValidatedTemplate_en.html",
					RMEmailTemplateConstants.VALIDATION_REQUEST_VALIDATED_TEMPLATE_ID);
		}
	}

	private void addEmailTemplates(String templateFileName, String templateId) {
		EmailTemplatesManager emailTemplateManager = appLayerFactory.getModelLayerFactory()
				.getEmailTemplatesManager();
		try (InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName)) {
			emailTemplateManager.addCollectionTemplateIfInexistent(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		}
	}
}