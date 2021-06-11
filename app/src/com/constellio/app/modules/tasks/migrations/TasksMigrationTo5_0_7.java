package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.modules.tasks.model.calculators.TaskFollowersCalculator;
import com.constellio.app.modules.tasks.model.calculators.TaskNextReminderOnCalculator;
import com.constellio.app.modules.tasks.model.calculators.TaskTokensCalculator;
import com.constellio.app.modules.tasks.model.validators.TaskStatusValidator;
import com.constellio.app.modules.tasks.model.validators.TaskValidator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollowerFactory;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminderFactory;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.PercentageValidator;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.constellio.app.entities.schemasDisplay.enums.MetadataInputType.DROPDOWN;
import static com.constellio.app.entities.schemasDisplay.enums.MetadataInputType.HIDDEN;
import static com.constellio.app.entities.schemasDisplay.enums.MetadataInputType.LOOKUP;
import static com.constellio.app.entities.schemasDisplay.enums.MetadataInputType.RICHTEXT;
import static com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.STANDBY;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.model.entities.records.wrappers.RecordWrapper.TITLE;
import static com.constellio.model.entities.records.wrappers.ValueListItem.CODE;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static java.util.Arrays.asList;

public class TasksMigrationTo5_0_7 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.0.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {

		if (!appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.hasType(RMTask.SCHEMA_TYPE)) {

			new TaskStatusSchemaAlterationFor5_0_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
			createTaskStatusTypes(collection, appLayerFactory, migrationResourcesProvider);
			TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
			TaskStatus standbyStatus = tasksSchemas.getTaskStatusWithCode(STANDBY_CODE);
			new TaskSchemaAlterationFor5_0_7(collection, migrationResourcesProvider, appLayerFactory, standbyStatus).migrate();
			setupDisplayConfig(collection, appLayerFactory, migrationResourcesProvider);
		}
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory,
									MigrationResourcesProvider migrationResourcesProvider) {

		Language language = migrationResourcesProvider.getLanguage();

		String definitionTab = "default:init.userTask.definition";
		String filesTab = "init.userTask.details";
		String assignmentTab = "init.userTask.assignment";
		String remindersTab = "init.userTask.remindersTab";
		String followersTab = "init.userTask.followersTab";

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		SchemaTypeDisplayConfig taskSchemaType = manager.getType(collection, Task.SCHEMA_TYPE);
		SchemaDisplayConfig taskSchema = manager.getSchema(collection, Task.DEFAULT_SCHEMA);

		transaction.add(taskSchemaType
				.withMetadataGroup(migrationResourcesProvider.getLanguageMap(asList(
						definitionTab, filesTab, assignmentTab, remindersTab, followersTab)))
				.withAdvancedSearchStatus(true).withSimpleSearchStatus(true));

		transaction.add(taskSchema
				.withSearchResultsMetadataCodes(asList(
						Task.DEFAULT_SCHEMA + "_" + Schemas.TITLE,
						Task.DEFAULT_SCHEMA + "_" + Task.STATUS,
						Task.DEFAULT_SCHEMA + "_" + Task.ASSIGNEE,
						Task.DEFAULT_SCHEMA + "_" + Task.DUE_DATE)));

		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.CONTENTS)
				.withMetadataGroup(filesTab).withInputType(MetadataInputType.CONTENT).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.DESCRIPTION)
				.withInputType(RICHTEXT).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.ASSIGNEE_USERS_CANDIDATES)
				.withMetadataGroup(assignmentTab).withInputType(LOOKUP).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.ASSIGNEE_GROUPS_CANDIDATES)
				.withMetadataGroup(assignmentTab).withInputType(LOOKUP).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.ASSIGNEE)
				.withMetadataGroup(assignmentTab).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.STATUS)
				.withInputType(LOOKUP).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.ASSIGNER)
				.withInputType(HIDDEN).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.ASSIGNED_ON)
				.withInputType(HIDDEN).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.END_DATE)
				.withInputType(HIDDEN).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, TaskStatus.DEFAULT_SCHEMA, TaskStatus.STATUS_TYPE)
				.withInputType(DROPDOWN).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.REMINDERS)
				.withMetadataGroup(remindersTab));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.TASK_FOLLOWERS)
				.withMetadataGroup(followersTab));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.PARENT_TASK)
				.withInputType(HIDDEN).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.START_DATE)
				.withInputType(HIDDEN).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.TYPE)
				.withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.TITLE)
				.withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.DUE_DATE)
				.withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.PARENT_TASK_DUE_DATE)
				.withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.FOLLOWERS_IDS)
				.withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.PROGRESS_PERCENTAGE)
				.withVisibleInAdvancedSearchStatus(true));

		manager.execute(transaction);

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);
		transactionBuilder
				.in(Task.SCHEMA_TYPE)
				.addToSearchResult(Task.DUE_DATE, Task.ASSIGNEE)
				.atTheEnd();
		manager.execute(transactionBuilder.build());

	}

	private void createTaskStatusTypes(String collection, AppLayerFactory appLayerFactory,
									   MigrationResourcesProvider migrationResourcesProvider) {
		Transaction transaction = new Transaction();
		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(collection, appLayerFactory);

		String standByCode = STANDBY_CODE;
		String inProcessCode = migrationResourcesProvider.getDefaultLanguageString("TaskStatusType.I");
		String finishedCode = migrationResourcesProvider.getDefaultLanguageString("TaskStatusType.F");
		String closedCode = CLOSED_CODE;

		transaction.add(schemas.newTaskStatus().setCode(standByCode)
				.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.STitle"))
				.setStatusType(STANDBY));
		transaction.add(schemas.newTaskStatus().setCode(inProcessCode)
				.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.ITitle"))
				.setStatusType(IN_PROGRESS));
		transaction.add(schemas.newTaskStatus().setCode(finishedCode)
				.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.FTitle"))
				.setStatusType(FINISHED));
		transaction.add(schemas.newTaskStatus().setCode(closedCode)
				.setTitles(migrationResourcesProvider.getLanguagesString("TaskStatusType.CTitle"))
				.setStatusType(CLOSED));

		try {
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory,
								   MigrationResourcesProvider migrationResourcesProvider,
								   String collection) {
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskAssigneeModificationTemplate.html",
				TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection,
				"taskStatusModificationToCompletedTemplate.html",
				TasksEmailTemplates.TASK_COMPLETED);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskDeletionTemplate.html",
				TasksEmailTemplates.TASK_DELETED);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskStatusModificationTemplate.html",
				TasksEmailTemplates.TASK_STATUS_MODIFIED);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "subTasksModificationTemplate.html",
				TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskReminderTemplate.html",
				TasksEmailTemplates.TASK_REMINDER);
	}

	private void addEmailTemplate(AppLayerFactory appLayerFactory,
								  MigrationResourcesProvider migrationResourcesProvider,
								  String collection,
								  String templateFileName, String templateId) {
		InputStream remindReturnBorrowedFolderTemplate = migrationResourcesProvider.getStream(templateFileName);
		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.addCollectionTemplateIfInexistent(templateId, collection, remindReturnBorrowedFolderTemplate);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(remindReturnBorrowedFolderTemplate);
		}
	}

	private class TaskStatusSchemaAlterationFor5_0_7 extends MetadataSchemasAlterationHelper {
		public TaskStatusSchemaAlterationFor5_0_7(String collection,
												  MigrationResourcesProvider migrationResourcesProvider,
												  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			createTaskSchemaTypes(typesBuilder);
		}

		private void createTaskSchemaTypes(MetadataSchemaTypesBuilder typesBuilder) {
			createTaskStatusType();
		}

		private MetadataSchemaTypeBuilder createTaskStatusType() {
			MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
					.createValueListItemSchema(TaskStatus.SCHEMA_TYPE, migrationResourcesProvider.getLanguagesString("Statut"),
							codeMetadataRequiredAndUnique())
					.setSecurity(false);
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
			defaultSchema.defineValidators().add(TaskStatusValidator.class);
			defaultSchema.getMetadata(CODE).setUniqueValue(true).setUnmodifiable(true);
			defaultSchema.getMetadata(TITLE).setMultiLingual(true);
			defaultSchema.createUndeletable(TaskStatus.STATUS_TYPE).defineAsEnum(TaskStatusType.class)
					.setDefaultRequirement(true);

			return schemaType;
		}
	}

	private class TaskSchemaAlterationFor5_0_7 extends MetadataSchemasAlterationHelper {
		TaskStatus standbyStatus;

		public TaskSchemaAlterationFor5_0_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory, TaskStatus standbyStatus) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			this.standbyStatus = standbyStatus;
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			List<String> language = appLayerFactory.getCollectionsManager().getCollectionLanguages(collection);
			MetadataSchemaBuilder taskType = createTaskTypeSchemaType(typesBuilder);
			createTaskSchemaType(typesBuilder, taskType);
		}

		private MetadataSchemaBuilder createTaskTypeSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
			Map<Language, String> labels = migrationResourcesProvider.getLanguagesString("init.ddvTaskType");
			MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(typesBuilder)
					.createValueListItemSchema(TaskType.SCHEMA_TYPE, labels, codeMetadataRequiredAndUnique())
					.setSecurity(false);
			MetadataSchemaBuilder schema = schemaType.getDefaultSchema();
			schema.create(TaskType.LINKED_SCHEMA).setType(STRING);
			return schema;
		}

		private MetadataSchemaTypeBuilder createTaskSchemaType(MetadataSchemaTypesBuilder typesBuilder,
															   MetadataSchemaBuilder taskType) {
			MetadataSchemaTypeBuilder taskStatusType = typesBuilder.getSchemaType(TaskStatus.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder schemaType = types().createNewSchemaTypeWithSecurity(Task.SCHEMA_TYPE).setSecurity(true).setSmallCode("t");
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
			defaultSchema.defineValidators().add(TaskValidator.class);

			MetadataSchemaBuilder userSchemaBuilder = typesBuilder.getDefaultSchema(User.SCHEMA_TYPE);
			defaultSchema.createUndeletable(Task.TYPE).defineReferencesTo(taskType);
			defaultSchema.createUndeletable(Task.ASSIGNEE).defineReferencesTo(userSchemaBuilder);
			defaultSchema.createUndeletable(Task.ASSIGNER).defineReferencesTo(userSchemaBuilder);
			defaultSchema.createUndeletable(Task.ASSIGNEE_GROUPS_CANDIDATES)
					.defineReferencesTo(typesBuilder.getDefaultSchema(Group.SCHEMA_TYPE)).setMultivalue(true);
			defaultSchema.createUndeletable(Task.ASSIGNEE_USERS_CANDIDATES).defineReferencesTo(
					typesBuilder.getDefaultSchema(User.SCHEMA_TYPE)).setMultivalue(true);
			defaultSchema.createUndeletable(Task.ASSIGNED_ON).setType(DATE);
			defaultSchema.createUndeletable(Task.FOLLOWERS_IDS).defineReferencesTo(userSchemaBuilder).setMultivalue(true)
					.defineDataEntry()
					.asCalculated(TaskFollowersCalculator.class);
			defaultSchema.createUndeletable(Task.TASK_FOLLOWERS).setType(STRUCTURE).setMultivalue(true).defineStructureFactory(
					TaskFollowerFactory.class);
			defaultSchema.createUndeletable(Task.DESCRIPTION).setType(TEXT).setSearchable(true);
			defaultSchema.createUndeletable(Task.CONTENTS).setType(CONTENT).setMultivalue(true).setSearchable(true);
			defaultSchema.createUndeletable(Task.NEXT_REMINDER_ON).setType(DATE).defineDataEntry().asCalculated(
					TaskNextReminderOnCalculator.class);
			defaultSchema.createUndeletable(Task.REMINDERS).setType(STRUCTURE).setMultivalue(true).defineStructureFactory(
					TaskReminderFactory.class);
			defaultSchema.createUndeletable(Task.START_DATE).setType(DATE);
			MetadataBuilder dueDate = defaultSchema.createUndeletable(Task.DUE_DATE).setType(DATE);
			defaultSchema.createUndeletable(Task.END_DATE).setType(DATE);
			defaultSchema.createUndeletable(Task.STATUS).defineReferencesTo(taskStatusType).setDefaultRequirement(true)
					.setDefaultValue(standbyStatus.getId());
			defaultSchema.createUndeletable(Task.PROGRESS_PERCENTAGE).setType(NUMBER).addValidator(PercentageValidator.class);
			MetadataBuilder parent = defaultSchema.createUndeletable(Task.PARENT_TASK)
					.defineChildOfRelationshipToType(schemaType);
			defaultSchema.createUndeletable(Task.PARENT_TASK_DUE_DATE).setType(DATE).defineDataEntry().asCopied(
					parent, dueDate);
			defaultSchema.createUndeletable(Task.COMMENTS).setType(STRUCTURE).defineStructureFactory(CommentFactory.class)
					.setMultivalue(true);
			defaultSchema.get(Schemas.TITLE.getLocalCode()).setDefaultRequirement(true);
			defaultSchema.get(Schemas.TOKENS.getLocalCode()).defineDataEntry().asCalculated(TaskTokensCalculator.class);
			return schemaType;
		}
	}
}
