package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.STANDBY;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static java.util.Arrays.asList;

public class TasksMigrationCombo implements ComboMigrationScript {
	@Override
	public List<MigrationScript> getVersions() {
		List<MigrationScript> scripts = new ArrayList<>();
		scripts.add(new TasksMigrationTo5_0_7());
		scripts.add(new TasksMigrationTo5_1_2());
		scripts.add(new TasksMigrationTo5_1_3());
		scripts.add(new TasksMigrationTo6_0());
		scripts.add(new TasksMigrationTo6_5_33());
		scripts.add(new TasksMigrationTo7_0());
		scripts.add(new TasksMigrationTo7_2());
		scripts.add(new TasksMigrationTo7_5());
		scripts.add(new TasksMigrationTo7_5_0_1());
		scripts.add(new TasksMigrationTo7_6_1());
		scripts.add(new TasksMigrationTo7_6_3());
		scripts.add(new TasksMigrationTo7_6_6());
		scripts.add(new TasksMigrationTo7_6_6_1());
		scripts.add(new TasksMigrationTo7_7());
		scripts.add(new TasksMigrationTo7_7_3());
		scripts.add(new TasksMigrationTo7_7_4());
		scripts.add(new TasksMigrationTo7_7_4_1());
		scripts.add(new TasksMigrationTo8_1_2());
		scripts.add(new TasksMigrationTo8_1_4());
		scripts.add(new TasksMigrationTo8_1_5());
		scripts.add(new TasksMigrationTo8_2_42());
		scripts.add(new TasksMigrationTo8_3_1());
		scripts.add(new TasksMigrationTo9_0());
		scripts.add(new TasksMigrationTo9_0_1_1());
		scripts.add(new TasksMigrationTo9_0_3());
		scripts.add(new TasksMigrationTo9_0_4());
		scripts.add(new TasksMigrationTo9_1());
		scripts.add(new TasksMigrationTo9_1_0_1());
		scripts.add(new TasksMigrationTo9_1_0_2());
		scripts.add(new TasksMigrationTo9_1_0_3());
		scripts.add(new TasksMigrationTo9_2_0());
		scripts.add(new TasksMigrationTo9_2_3());
		scripts.add(new TasksMigrationFrom9_3_UpdateTokensCalculator());

		return scripts;
	}

	@Override
	public String getVersion() {
		return "combo";
	}

	GeneratedTasksMigrationCombo generatedComboMigration;

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		generatedComboMigration = new GeneratedTasksMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		generatedComboMigration.applyGeneratedRoles();
		generatedComboMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);

		Transaction transaction = new Transaction();
		final String standById = createRecordTransaction(
				collection, migrationResourcesProvider, appLayerFactory, types, transaction);
		recordServices.execute(transaction);

		//TODO Uncomment after supporting up to 7.2 in migration combo
		modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(Task.DEFAULT_SCHEMA + "_" + Task.STATUS).setDefaultValue(standById);
			}
		});
	}

	private String createRecordTransaction(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory, MetadataSchemaTypes types,
										   Transaction transaction) {

		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(collection, appLayerFactory);

		String standByCode = STANDBY_CODE;
		String inProcessCode = migrationResourcesProvider.getDefaultLanguageString("TaskStatusType.I");
		String finishedCode = migrationResourcesProvider.getDefaultLanguageString("TaskStatusType.F");
		String closedCode = CLOSED_CODE;

		String standbyId =
				transaction.add(schemas.newTaskStatus().setCode(standByCode)
						.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.STitle"))
						.setStatusType(STANDBY)).getId();
		transaction.add(schemas.newTaskStatus().setCode(inProcessCode)
				.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.ITitle"))
				.setStatusType(IN_PROGRESS));
		transaction.add(schemas.newTaskStatus().setCode(finishedCode)
				.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.FTitle"))
				.setStatusType(FINISHED));
		transaction.add(schemas.newTaskStatus().setCode(closedCode)
				.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.CTitle"))
				.setStatusType(CLOSED));

		return standbyId;
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection,
								   MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			//This module is fixing problems in other module
			//			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
			//				for (MetadataBuilder metadata : typeBuilder.getAllMetadatas()) {
			//					if (metadata.getLocalCode().equals("comments")) {
			//						metadata.setTypeWithoutValidation(MetadataValueType.STRUCTURE);
			//					}
			//				}
			//			}
			//
			//			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
			//				MetadataSchemaBuilder schemaBuilder = typeBuilder.getDefaultSchema();
			//				if (schemaBuilder.hasMetadata("description")) {
			//					schemaBuilder.get("description").setEnabled(true).setEssentialInSummary(true);
			//				}
			//			}
			//
			//			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
			//				for (MetadataBuilder metadataBuilder : typeBuilder.getDefaultSchema().getMetadatas()) {
			//					if ("code".equals(metadataBuilder.getLocalCode())) {
			//						metadataBuilder.setUniqueValue(true);
			//						metadataBuilder.setDefaultRequirement(true);
			//					}
			//				}
			//			}

			generatedComboMigration.applyGeneratedSchemaAlteration(typesBuilder);

			for (String metadata : asList("ddvTaskStatus_default_description", "ddvTaskStatus_default_title",
					"ddvTaskType_default_description", "ddvTaskType_default_title")) {
				typesBuilder.getMetadata(metadata).setMultiLingual(true);
			}
			new CommonMetadataBuilder().addCommonMetadataToAllExistingSchemas(typesBuilder);
		}

	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory,
								   MigrationResourcesProvider migrationResourcesProvider,
								   String collection) {

		String taskReminderTemplate;
		String subTasksModificationTemplate;
		String taskAssigneeModificationTemplate;
		String taskAssigneeToYouTemplate;
		String taskDeletionTemplate;
		String taskStatusModificationTemplate;
		String taskStatusModificationToCompletedTemplate;
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("en")) {
			taskReminderTemplate = "taskReminderTemplate_en.html";
			subTasksModificationTemplate = "subTasksModificationTemplate_en.html";
			taskAssigneeModificationTemplate = "taskAssigneeModificationTemplate_en.html";
			taskAssigneeToYouTemplate = "taskAssigneeToYouTemplate_en.html";
			taskStatusModificationTemplate = "taskStatusModificationTemplate_en.html";
			taskStatusModificationToCompletedTemplate = "taskStatusModificationToCompletedTemplate_en.html";
			taskDeletionTemplate = "taskDeletionTemplate_en.html";
		} else {
			taskReminderTemplate = "taskReminderTemplate.html";
			subTasksModificationTemplate = "subTasksModificationTemplate.html";
			taskAssigneeModificationTemplate = "taskAssigneeModificationTemplate.html";
			taskAssigneeToYouTemplate = "taskAssigneeToYouTemplate.html";
			taskStatusModificationTemplate = "taskStatusModificationTemplate.html";
			taskStatusModificationToCompletedTemplate = "taskStatusModificationToCompletedTemplate.html";
			taskDeletionTemplate = "taskDeletionTemplate.html";
		}
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, taskAssigneeModificationTemplate,
				TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection,
				taskStatusModificationToCompletedTemplate,
				TasksEmailTemplates.TASK_COMPLETED);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, taskDeletionTemplate,
				TasksEmailTemplates.TASK_DELETED);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, taskStatusModificationTemplate,
				TasksEmailTemplates.TASK_STATUS_MODIFIED);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, subTasksModificationTemplate,
				TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, taskReminderTemplate,
				TasksEmailTemplates.TASK_REMINDER);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, taskAssigneeToYouTemplate,
				TasksEmailTemplates.TASK_ASSIGNED_TO_YOU);

	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory,
								   MigrationResourcesProvider migrationResourcesProvider,
								   String collection,
								   String templateFileName, String templateId) {
		InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);
		EmailTemplatesManager emailTemplateManager = appLayerFactory.getModelLayerFactory()
				.getEmailTemplatesManager();
		try {
			emailTemplateManager.addCollectionTemplateIfInexistent(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}
}
