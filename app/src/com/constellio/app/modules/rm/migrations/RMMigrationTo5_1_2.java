package com.constellio.app.modules.rm.migrations;

import static com.constellio.data.utils.LangUtils.withoutDuplicates;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

public class RMMigrationTo5_1_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupRoles(collection, appLayerFactory.getModelLayerFactory());
		addExtractors(collection, appLayerFactory);
		reloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);
	}

	private void setupRoles(String collection, ModelLayerFactory modelLayerFactory) {
		RolesManager rolesManager = modelLayerFactory.getRolesManager();

		Role manager = rolesManager.getRole(collection, RMRoles.MANAGER);
		Role rgd = rolesManager.getRole(collection, RMRoles.RGD);
		Role administrator = rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR);

		List<String> managerPermissions = new ArrayList<>(manager.getOperationPermissions());
		managerPermissions.add(RMPermissionsTo.MODIFY_OPENING_DATE_FOLDER);

		List<String> newRgdPermissions = new ArrayList<>(rgd.getOperationPermissions());
		newRgdPermissions.add(RMPermissionsTo.MODIFY_OPENING_DATE_FOLDER);

		List<String> newAdministratorPermissions = new ArrayList<>(administrator.getOperationPermissions());
		newAdministratorPermissions.addAll(RMPermissionsTo.PERMISSIONS.getAll());

		rolesManager.updateRole(manager.withPermissions(withoutDuplicates(managerPermissions)));
		rolesManager.updateRole(rgd.withPermissions(withoutDuplicates(newRgdPermissions)));
		rolesManager.updateRole(administrator.withPermissions(withoutDuplicates(newAdministratorPermissions)));
	}

	private void addExtractors(String collection, AppLayerFactory appLayerFactory) {
		MetadataSchemasManager schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();

		schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder documentSchema = types.getSchema(Document.DEFAULT_SCHEMA);
				documentSchema.get(Document.KEYWORDS).getPopulateConfigsBuilder().setProperties(asList(Document.KEYWORDS));
				documentSchema.get(Document.AUTHOR).getPopulateConfigsBuilder().setProperties(asList(Document.AUTHOR));
				documentSchema.get(Document.COMPANY).getPopulateConfigsBuilder().setProperties(asList(Document.COMPANY));
				documentSchema.get(Document.SUBJECT).getPopulateConfigsBuilder().setProperties(asList(Document.SUBJECT));
				documentSchema.get(Document.TITLE).getPopulateConfigsBuilder().setProperties(asList(Document.TITLE));

				MetadataSchemaBuilder emailSchema = types.getSchema(Email.SCHEMA);
				emailSchema.get(Email.TITLE).getPopulateConfigsBuilder().setProperties(asList("subject"));
				emailSchema.get(Email.EMAIL_OBJECT).getPopulateConfigsBuilder().setProperties(asList("subject"));
				emailSchema.get(Email.EMAIL_FROM).getPopulateConfigsBuilder().setProperties(asList("from"));
				emailSchema.get(Email.EMAIL_BCC_TO).getPopulateConfigsBuilder().setProperties(asList("bcc"));
				emailSchema.get(Email.EMAIL_CC_TO).getPopulateConfigsBuilder().setProperties(asList("cc"));
				emailSchema.get(Email.EMAIL_TO).getPopulateConfigsBuilder().setProperties(asList("to"));

				MetadataSchemaBuilder userDocument = types.getSchema(UserDocument.DEFAULT_SCHEMA);
				userDocument.get(UserDocument.TITLE).getPopulateConfigsBuilder().setProperties(asList("subject", "title"));
			}
		});
	}

	class SchemaAlterationFor5_1_2 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_1_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder retentionSchemaType = typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE);
			retentionSchemaType.getDefaultSchema().getMetadata(RetentionRule.KEYWORDS).setSearchable(true);
		}
	}

	private void reloadEmailTemplates(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection) {
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "remindReturnBorrowedFolderTemplate.html",
				RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "approvalRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "validationRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.VALIDATION_REQUEST_TEMPLATE_ID);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "alertAvailableTemplate.html",
				RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
	}

	private void reloadEmailTemplate(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection,
			String templateFileName, String templateId) {
		InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);
		EmailTemplatesManager emailTemplateManager = appLayerFactory.getModelLayerFactory()
				.getEmailTemplatesManager();
		try {
			emailTemplateManager.replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}
}
