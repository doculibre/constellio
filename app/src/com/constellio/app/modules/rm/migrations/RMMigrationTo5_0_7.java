package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.calculators.AdministrativeUnitAncestorsCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyStatusCalculator2;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListStatusCalculator2;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.PendingValidationCalculator;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidationFactory;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.data.utils.LangUtils.withoutDuplicates;
import static com.constellio.data.utils.LangUtils.withoutDuplicatesAndNulls;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RMMigrationTo5_0_7 implements MigrationScript {
	public static final String RECENT_FOLDERS = "F";
	public static final String RECENT_DOCUMENTS = "D";
	public static final String CHECKED_OUT_DOCUMENTS = "C";
	public static final String TAXONOMIES = "T";

	@Override
	public String getVersion() {
		return "5.0.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		MetadataSchema taskSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(Task.SCHEMA_TYPE).getDefaultSchema();

		if (!taskSchema.hasMetadataWithCode(RMTask.LINKED_DOCUMENTS)) {
			new SchemaAlterationFor5_0_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}

		updateFormAndDisplayConfigs(collection, appLayerFactory, migrationResourcesProvider);

		setupRoles(collection, appLayerFactory.getModelLayerFactory());

		updateUserProfiles(collection, appLayerFactory.getModelLayerFactory());

		try {
			new FilingSpaceMigration(collection, appLayerFactory).run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		//appLayerFactory.getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);
		setupRMFacets(appLayerFactory, migrationResourcesProvider, collection);
	}

	private void setupRMFacets(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
							   String collection)
			throws RecordServicesException {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		RecordServices recordServices = rm.getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("administrativeUnitId_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.administrativeUnit")));
		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("categoryId_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.category")));
		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("archivisticStatus_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.archivisticStatus")));
		transaction.add(rm.newFacetField().setOrder(3).setFieldDataStoreCode("copyStatus_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.copyStatus")));
		recordServices.execute(transaction);
	}

	private void enableTaskModule(String collection, AppLayerFactory appLayerFactory) {
		TaskModule taskModule = new TaskModule();
		CollectionsListManager collectionsListManager = appLayerFactory.getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager modulesManager = appLayerFactory.getModulesManager();
		if (!modulesManager.isInstalled(taskModule)) {
			try {
				modulesManager.installValidModuleAndGetInvalidOnes(taskModule, collectionsListManager);
			} catch (ConstellioModulesManagerException_ModuleInstallationFailed constellioModulesManagerException_moduleInstallationFailed) {
				throw new RuntimeException(constellioModulesManagerException_moduleInstallationFailed);
			}
		}
		if (!modulesManager.isModuleEnabled(collection, taskModule)) {
			try {
				modulesManager.enableValidModuleAndGetInvalidOnes(collection, taskModule);
			} catch (ConstellioModulesManagerException_ModuleInstallationFailed constellioModulesManagerException_moduleInstallationFailed) {
				throw new RuntimeException(constellioModulesManagerException_moduleInstallationFailed);
			}
		}
	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory,
								   MigrationResourcesProvider migrationResourcesProvider,
								   String collection) {
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "remindReturnBorrowedFolderTemplate.html",
				RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "approvalRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "validationRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.VALIDATION_REQUEST_TEMPLATE_ID);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "alertAvailableTemplate.html",
				RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
	}

	private void addEmailTemplate(AppLayerFactory appLayerFactory,
								  MigrationResourcesProvider migrationResourcesProvider,
								  String collection,
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

	private void updateFormAndDisplayConfigs(String collection, AppLayerFactory appLayerFactory,
											 MigrationResourcesProvider migrationResourcesProvider) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.in(Folder.SCHEMA_TYPE)
				.addToDisplay(Folder.BORROWING_TYPE)
				.beforeMetadata(Folder.LINEAR_SIZE);

		transactionBuilder
				.in(Task.SCHEMA_TYPE)
				.addToForm(RMTask.LINKED_DOCUMENTS, RMTask.LINKED_FOLDERS)
				.atTheEnd();

		transactionBuilder
				.in(Task.SCHEMA_TYPE)
				.addToDisplay(RMTask.ADMINISTRATIVE_UNIT, RMTask.LINKED_DOCUMENTS, RMTask.LINKED_FOLDERS)
				.beforeTheHugeCommentMetadata();

		manager.saveMetadata(manager.getMetadata(collection, ContainerRecord.DEFAULT_SCHEMA, ContainerRecord.STORAGE_SPACE)
				.withInputType(MetadataInputType.LOOKUP));

		//String detailsTab = migrationResourcesProvider.getDefaultLanguageString("init.userTask.details");
		manager.saveMetadata(manager.getMetadata(collection, RMTask.DEFAULT_SCHEMA, RMTask.LINKED_FOLDERS)
				.withMetadataGroup("init.userTask.details"));
		manager.saveMetadata(manager.getMetadata(collection, RMTask.DEFAULT_SCHEMA, RMTask.LINKED_DOCUMENTS)
				.withMetadataGroup("init.userTask.details"));

		transactionBuilder.in(RetentionRule.SCHEMA_TYPE)
				.addToSearchResult(RetentionRule.CODE)
				.beforeMetadata(RetentionRule.TITLE);

		manager.execute(transactionBuilder.build());
	}

	/**
	 * - Add default role to users
	 * - Migrate default tab
	 */
	private void updateUserProfiles(String collection, ModelLayerFactory modelLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchema user = rm.userSchema();
		Metadata startTab = user.getMetadata(User.START_TAB);
		Metadata userRoles = user.getMetadata(User.ROLES);

		Transaction transaction = new Transaction().setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

		LogicalSearchQuery query = new LogicalSearchQuery(from(user).where(startTab).isNotNull().orWhere(userRoles).isNull());
		for (Record record : searchServices.search(query)) {

			String startTabValue = record.get(startTab);

			if (startTabValue != null) {
				switch (startTabValue) {
					case RECENT_FOLDERS:
						transaction.add(record.set(startTab, RMNavigationConfiguration.LAST_VIEWED_FOLDERS));
						break;
					case RECENT_DOCUMENTS:
						transaction.add(record.set(startTab, RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
						break;
					case CHECKED_OUT_DOCUMENTS:
						transaction.add(record.set(startTab, RMNavigationConfiguration.CHECKED_OUT_DOCUMENTS));
						break;
					case TAXONOMIES:
						transaction.add(record.set(startTab, RMNavigationConfiguration.TAXONOMIES));
						break;
				}
			}

			if (record.getList(userRoles).isEmpty()) {
				record.set(userRoles, asList(RMRoles.USER));
			}
		}

		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	class SchemaAlterationFor5_0_7 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_0_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.0.7";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaTypeBuilder adminUnitSchemaType = typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder folderSchemaType = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder documentSchemaType = typesBuilder.getSchemaType(Document.SCHEMA_TYPE);

			//Folder
			MetadataSchemaBuilder adminUnitSchema = typesBuilder.getSchema(AdministrativeUnit.DEFAULT_SCHEMA);
			MetadataSchemaBuilder folderSchema = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA);
			MetadataBuilder folderAdministrativeUnit = folderSchema.get(Folder.ADMINISTRATIVE_UNIT);
			folderSchema.createUndeletable(
					Folder.BORROWING_TYPE).defineAsEnum(BorrowingType.class);

			folderSchema.get(Folder.FILING_SPACE).setEssential(false).setDefaultRequirement(false);
			folderSchema.get(Folder.FILING_SPACE_ENTERED).setEssential(false).setDefaultRequirement(false);
			for (MetadataSchemaBuilder aFolderSchema : typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getAllSchemas()) {
				aFolderSchema.get(Folder.FILING_SPACE_ENTERED).setEnabled(false);
			}

			MetadataBuilder administrativeUnitAncestors = adminUnitSchema.createUndeletable(AdministrativeUnit.ANCESTORS)
					.setType(MetadataValueType.REFERENCE).setMultivalue(true)
					.defineReferencesTo(typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE))
					.defineDataEntry().asCalculated(AdministrativeUnitAncestorsCalculator.class);

			folderSchema.createUndeletable(Folder.ADMINISTRATIVE_UNIT_ANCESTORS)
					.setType(MetadataValueType.REFERENCE).setMultivalue(true).setEssential(true)
					.defineReferencesTo(typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE))
					.defineDataEntry().asCopied(folderAdministrativeUnit, administrativeUnitAncestors);
			folderSchema.get(Folder.COPY_STATUS).defineDataEntry().asCalculated(FolderCopyStatusCalculator2.class);

			folderSchema.createUndeletable(Folder.ALERT_USERS_WHEN_AVAILABLE)
					.setType(MetadataValueType.REFERENCE)
					.setMultivalue(true)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));

			folderSchema.get(Folder.BORROW_USER_ENTERED).setSystemReserved(true);

			// DecommissioningList

			MetadataSchemaBuilder userSchema = typesBuilder.getSchema(User.DEFAULT_SCHEMA);
			MetadataSchemaBuilder decommissioningSchema = typesBuilder.getSchema(DecommissioningList.DEFAULT_SCHEMA);

			decommissioningSchema.createUndeletable(DecommissioningList.VALIDATIONS).setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(DecomListValidationFactory.class).setMultivalue(true);
			decommissioningSchema.createUndeletable(DecommissioningList.PENDING_VALIDATIONS)
					.defineReferencesTo(userSchema).setMultivalue(true)
					.defineDataEntry().asCalculated(PendingValidationCalculator.class);
			decommissioningSchema.get(DecommissioningList.STATUS)
					.defineDataEntry().asCalculated(DecomListStatusCalculator2.class);
			decommissioningSchema.get(DecommissioningList.VALIDATION_DATE).setEssential(false).setEnabled(false);
			decommissioningSchema.get(DecommissioningList.VALIDATION_USER).setEssential(false).setEnabled(false);

			//Document
			MetadataSchemaBuilder documentSchema = typesBuilder.getSchema(Document.DEFAULT_SCHEMA);
			documentSchema.createUndeletable(Document.ALERT_USERS_WHEN_AVAILABLE)
					.setType(MetadataValueType.REFERENCE)
					.setMultivalue(true)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));

			//Task
			MetadataSchemaBuilder taskSchema = typesBuilder.getSchema(Task.DEFAULT_SCHEMA);
			taskSchema.create(RMTask.ADMINISTRATIVE_UNIT).defineTaxonomyRelationshipToType(adminUnitSchemaType);
			taskSchema.create(RMTask.LINKED_FOLDERS).defineReferencesTo(folderSchemaType).setMultivalue(true);
			taskSchema.create(RMTask.LINKED_DOCUMENTS).defineReferencesTo(documentSchemaType).setMultivalue(true);
		}
	}

	private void setupRoles(String collection, ModelLayerFactory modelLayerFactory) {
		Role rgdRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);

		Set<String> newRgdPermissions = new HashSet<>(rgdRole.getOperationPermissions());
		newRgdPermissions.add(CorePermissions.MANAGE_FACETS);
		newRgdPermissions.add(CorePermissions.MANAGE_EXCEL_REPORT);
		newRgdPermissions.add(CorePermissions.MANAGE_LABELS);
		newRgdPermissions.add(CorePermissions.MANAGE_PRINTABLE_REPORT);
		newRgdPermissions.add(CorePermissions.MANAGE_EMAIL_SERVER);

		modelLayerFactory.getRolesManager().updateRole(
				rgdRole.withPermissions(withoutDuplicates(new ArrayList<String>(newRgdPermissions))));
	}

	private static class FilingSpaceMigration {

		private String collection;

		private RMSchemasRecordsServices rm;

		private SearchServices searchServices;

		private RecordServices recordServices;

		private AuthorizationsServices authorizationsServices;

		private Map<ReplacedFilingSpaceKey, String> replacedFilingSpaceMap;

		private MetadataSchemaTypes types;

		private BatchProcessesManager batchProcessesManager;

		private ReindexingServices reindexingServices;

		private FilingSpaceMigration(String collection, AppLayerFactory appLayerFactory) {
			this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
			this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			this.authorizationsServices = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
			this.collection = collection;
			this.replacedFilingSpaceMap = new HashMap<>();
			this.types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
			this.batchProcessesManager = appLayerFactory.getModelLayerFactory().getBatchProcessesManager();
			this.reindexingServices = appLayerFactory.getModelLayerFactory().newReindexingServices();
		}

		private List<Record> search(LogicalSearchCondition condition) {
			return searchServices.search(new LogicalSearchQuery(condition));
		}

		public void run()
				throws Exception {

			Transaction transaction = new Transaction()
					.setSkippingRequiredValuesValidation(true)
					.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION)
					.setSkippingReferenceToLogicallyDeletedValidation(true);
			createNewAdministrativeUnit(transaction);
			moveContainersInNewAdministrativeUnits(transaction);
			moveDecommissioningListInNewAdministrativeUnits(transaction);
			removeFilingSpacesInAdministrativeUnits(transaction);
			recordServices.executeHandlingImpactsAsync(transaction);
			//batchProcessesManager.waitUntilAllFinished();

			moveFoldersToNewAdministrativeUnits();

			boolean hasAdministrativeUnits = searchServices.hasResults(from(rm.administrativeUnit.schemaType()).returnAll());
			if (hasAdministrativeUnits) {
				reindexAll();

				physicallyDeleteFilingSpaces();

			}
		}

		private void reindexAll() {
			//Reindexation that was planned in 5.0.6

			reindexingServices.reindexCollection(collection, ReindexationMode.RECALCULATE_AND_REWRITE);
		}

		private List<String> getUsersWithManagerOrRGDRole() {
			Metadata userRoles = rm.userSchema().get(User.ALL_ROLES);
			return searchServices.searchRecordIds(new LogicalSearchQuery()
					.setCondition(from(rm.userSchemaType()).where(userRoles).isIn(asList(RMRoles.MANAGER, RMRoles.RGD))));
		}

		private void createAuthorizationsForFilingSpaceUsersAndAdministrators() {

			List<String> managersAndRGDs = getUsersWithManagerOrRGDRole();

			for (FilingSpace filingSpace : wrapFilingSpaces(search(from(rm.filingSpaceSchemaType()).returnAll()))) {

				List<String> usersWithReadWrite = withoutDuplicatesAndNulls(filingSpace.getUsers());
				List<String> usersWithReadWriteDelete = withoutDuplicatesAndNulls(filingSpace.getAdministrators());
				List<String> managersInFilingSpace = new ArrayList<>(withoutDuplicatesAndNulls(filingSpace.getAdministrators()));

				managersInFilingSpace.removeAll(managersAndRGDs);

				UniqueIdGenerator uniqueIdGenerator = rm.getModelLayerFactory().getDataLayerFactory()
						.getSecondaryUniqueIdGenerator();
				List<AdministrativeUnit> newUnits = rm.getAdministrativeUnits(getNewUnits(filingSpace.getId()));
				for (AdministrativeUnit newUnit : newUnits) {
					addAuthorizationOn(newUnit, usersWithReadWrite, asList(Role.READ, Role.WRITE), uniqueIdGenerator);
					addAuthorizationOn(newUnit, usersWithReadWriteDelete, asList(Role.READ, Role.WRITE, Role.DELETE),
							uniqueIdGenerator);
					addAuthorizationOn(newUnit, managersInFilingSpace, asList(RMRoles.MANAGER), uniqueIdGenerator);
				}
			}
		}

		private List<FilingSpace> wrapFilingSpaces(List<Record> records) {
			List<FilingSpace> filingSpaces = new ArrayList<>();

			for (Record record : records) {
				filingSpaces.add(new FilingSpace(record, types));
			}

			return filingSpaces;
		}

		private void addAuthorizationOn(AdministrativeUnit newUnit, List<String> users, List<String> roles,
										UniqueIdGenerator uniqueIdGenerator) {
			/*if (!users.isEmpty()) {
				authorizationsServices.add(authorizationInCollection(collection)
						.giving(roles).forPrincipalsIds(users).on(newUnit));
			}*/
		}

		private void physicallyDeleteFilingSpaces() {
			for (FilingSpace filingSpace : wrapFilingSpaces(search(from(rm.filingSpaceSchemaType()).returnAll()))) {
				if (Boolean.TRUE.equals(filingSpace.getWrappedRecord().get(Schemas.LOGICALLY_DELETED_STATUS))) {
					for (AdministrativeUnit newUnit : rm.getAdministrativeUnits(getNewUnits(filingSpace.getId()))) {
						recordServices.logicallyDelete(newUnit.getWrappedRecord(), User.GOD);
					}
				} else {
					recordServices.logicallyDelete(filingSpace.getWrappedRecord(), User.GOD);
				}
				recordServices.physicallyDelete(filingSpace.getWrappedRecord(), User.GOD);
			}
		}

		private void moveFoldersToNewAdministrativeUnits()
				throws RecordServicesException {
			Metadata folderFilingspaceEntered = rm.folder.schema().getMetadata(Folder.FILING_SPACE_ENTERED);

			Iterator<List<Record>> recordsBatchIterator = searchServices.recordsBatchIterator(new LogicalSearchQuery()
					.setCondition(from(rm.folder.schemaType()).where(folderFilingspaceEntered).isNotNull()));

			while (recordsBatchIterator.hasNext()) {
				List<Folder> folders = rm.wrapFolders(recordsBatchIterator.next());

				for (Folder folder : folders) {
					String unit = getNewUnit(folder.getAdministrativeUnitEntered(), folder.getFilingSpaceEntered());
					folder.setAdministrativeUnitEntered(unit).setFilingSpaceEntered((String) null);
				}

				recordServices.executeHandlingImpactsAsync(
						Transaction.wrappers(folders).setSkippingRequiredValuesValidation(true));
			}
			//batchProcessesManager.waitUntilAllFinished();
		}

		private String getNewUnit(String unitId, String filingSpaceId) {
			String newUnit = replacedFilingSpaceMap.get(
					new ReplacedFilingSpaceKey(unitId, filingSpaceId));

			return newUnit == null ? unitId : newUnit;
		}

		private List<String> getNewUnits(String filingSpaceId) {

			List<String> newUnits = new ArrayList<>();

			for (Map.Entry<ReplacedFilingSpaceKey, String> entry : replacedFilingSpaceMap.entrySet()) {
				if (filingSpaceId.equals(entry.getKey().filingSpaceId)) {
					newUnits.add(entry.getValue());
				}
			}

			return newUnits;
		}

		private void moveContainersInNewAdministrativeUnits(Transaction transaction) {
			for (ContainerRecord container : rm.wrapContainerRecords(search(from(rm.containerRecord.schemaType()).returnAll()))) {
				String unit = getNewUnit(container.<String>get("administrativeUnit"), container.getFilingSpace());
				transaction.add((ContainerRecord) container.setFilingSpace((String) null).set("administrativeUnit", unit));
			}
		}

		private void moveDecommissioningListInNewAdministrativeUnits(Transaction transaction) {
			for (DecommissioningList decomList : rm.wrapDecommissioningLists(search(
					from(rm.decommissioningList.schemaType()).returnAll()))) {
				String unit = getNewUnit(decomList.getAdministrativeUnit(), decomList.getFilingSpace());
				transaction.add(decomList.setFilingSpace((String) null).setAdministrativeUnit(unit));
			}
		}

		private void removeFilingSpacesInAdministrativeUnits(Transaction transaction) {
			for (AdministrativeUnit administrativeUnit : rm.wrapAdministrativeUnits(
					search(from(rm.administrativeUnit.schemaType()).returnAll()))) {

				if (!administrativeUnit.getFilingSpaces().isEmpty()) {
					transaction.add(administrativeUnit.setFilingSpaces(new ArrayList<FilingSpace>()));
				}

			}

		}

		private void createNewAdministrativeUnit(Transaction transaction) {
			for (FilingSpace filingSpace : wrapFilingSpaces(search(from(rm.filingSpaceSchemaType()).returnAll()))) {

				List<AdministrativeUnit> units = rm.wrapAdministrativeUnits(search(from(rm.administrativeUnit.schemaType())
						.where(rm.administrativeUnit.filingSpaces()).isEqualTo(filingSpace)));

				for (AdministrativeUnit unit : units) {
					ReplacedFilingSpaceKey key = new ReplacedFilingSpaceKey(unit.getId(), filingSpace.getId());

					AdministrativeUnit newAdministrativeUnit = transaction.add(rm.newAdministrativeUnit());
					newAdministrativeUnit.setParent(unit);
					newAdministrativeUnit.setCode(getNewUnitCode(filingSpace.getCode(), unit.getCode(), units.size() > 1));
					newAdministrativeUnit.setTitle(filingSpace.getTitle());
					newAdministrativeUnit.setDescription(filingSpace.getDescription());

					replacedFilingSpaceMap.put(key, newAdministrativeUnit.getId());
				}
				if (units.isEmpty()) {
					ReplacedFilingSpaceKey key = new ReplacedFilingSpaceKey(null, filingSpace.getId());

					AdministrativeUnit newAdministrativeUnit = transaction.add(rm.newAdministrativeUnit());
					newAdministrativeUnit.setCode(toUniqueCode(filingSpace.getCode()));
					newAdministrativeUnit.setTitle(filingSpace.getTitle());
					newAdministrativeUnit.setDescription(filingSpace.getDescription());

					replacedFilingSpaceMap.put(key, newAdministrativeUnit.getId());
				}
			}

		}

		private String toUniqueCode(String code) {
			if (rm.getAdministrativeUnitWithCode(code) != null) {
				return toUniqueCode(code + " (2)");
			}
			return code;
		}

		String getNewUnitCode(String filingSpaceCode, String unitCode, boolean multipleUnits) {
			if (multipleUnits || rm.getAdministrativeUnitWithCode(filingSpaceCode) != null) {
				return toUniqueCode(unitCode + "-" + filingSpaceCode);
			} else {
				return filingSpaceCode;
			}
		}

	}

	private static class ReplacedFilingSpaceKey {

		private String unitId;

		private String filingSpaceId;

		private ReplacedFilingSpaceKey(String unitId, String filingSpaceId) {
			this.unitId = unitId;
			this.filingSpaceId = filingSpaceId;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

	}

}
