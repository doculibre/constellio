package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.calculators.FolderDecommissioningDateCalculator2;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

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
}
