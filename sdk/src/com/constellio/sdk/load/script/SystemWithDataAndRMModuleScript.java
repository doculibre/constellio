package com.constellio.sdk.load.script;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.BigFileEntry;
import com.constellio.data.utils.BigFileFolderIterator;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.EventFactory;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.load.script.utils.LinkableIdsList;
import com.constellio.sdk.load.script.utils.LinkableRecordsList;
import demo.DemoInitScript;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.records.wrappers.EventType.VIEW;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class SystemWithDataAndRMModuleScript implements DemoInitScript {

	static int documentsBatch = 100;

	File bigFilesFolder;
	int subFoldersPerFolder = 0;
	int subSubFoldersPerFolder = 0;
	int numberOfRootFolders = 0;
	int numberOfDocuments = 0;
	List<String> collections = new ArrayList<>();

	BigFileFolderIterator contentIterator;
	UserPreparator userPreparator;
	TaxonomyPreparator categoriesTaxonomy;
	PrincipalTaxonomyPreparator administrativeUnitsTaxonomy;

	public File getBigFilesFolder() {
		return bigFilesFolder;
	}

	public void setBigFilesFolder(File bigFilesFolder) {
		this.bigFilesFolder = bigFilesFolder;
	}

	public int getNumberOfRootFolders() {
		return numberOfRootFolders;
	}

	public void setNumberOfRootFolders(int numberOfRootFolders) {
		this.numberOfRootFolders = numberOfRootFolders;
	}

	public UserPreparator getUserPreparator() {
		return userPreparator;
	}

	public void setUserPreparator(UserPreparator userPreparator) {
		this.userPreparator = userPreparator;
	}

	public int getSubFoldersPerFolder() {
		return subFoldersPerFolder;
	}

	public void setSubFoldersPerFolder(int subFoldersPerFolder) {
		this.subFoldersPerFolder = subFoldersPerFolder;
	}

	public int getSubSubFoldersPerFolder() {
		return subSubFoldersPerFolder;
	}

	public void setSubSubFoldersPerFolder(int subSubFoldersPerFolder) {
		this.subSubFoldersPerFolder = subSubFoldersPerFolder;
	}

	public int getNumberOfDocuments() {
		return numberOfDocuments;
	}

	public void setNumberOfDocuments(int numberOfDocuments) {
		this.numberOfDocuments = numberOfDocuments;
	}

	public List<String> getCollections() {
		return collections;
	}

	public void setCollections(List<String> collections) {
		this.collections = collections;
	}

	public TaxonomyPreparator getCategoriesTaxonomy() {
		return categoriesTaxonomy;
	}

	public void setCategoriesTaxonomy(TaxonomyPreparator categoriesTaxonomy) {
		this.categoriesTaxonomy = categoriesTaxonomy;
	}

	public PrincipalTaxonomyPreparator getAdministrativeUnitsTaxonomy() {
		return administrativeUnitsTaxonomy;
	}

	public void setAdministrativeUnitsTaxonomy(PrincipalTaxonomyPreparator administrativeUnitsTaxonomy) {
		this.administrativeUnitsTaxonomy = administrativeUnitsTaxonomy;
	}

	@Override
	public void setup(AppLayerFactory appLayerFactory, ModelLayerFactory modelLayerFactory)
			throws Exception {

		if (collections.isEmpty()) {
			throw new RuntimeException("Parameter 'collections' is required");
		}

		if (categoriesTaxonomy == null) {
			throw new RuntimeException("Parameter 'categoriesTaxonomy' is required");
		}

		if (administrativeUnitsTaxonomy == null) {
			throw new RuntimeException("Parameter 'administrativeUnitsTaxonomy' is required");
		}

		for (String collection : collections) {
			givenCollectionWithRMModuleAndUsers(appLayerFactory, collection, collection);
		}

		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		contentIterator = new BigFileFolderIterator(bigFilesFolder, ioServices, "bigFileITerator");

		List<String> groupCodes = new ArrayList<>();
		UserServices userServices = modelLayerFactory.newUserServices();
		PasswordFileAuthenticationService authenticationService = modelLayerFactory.getPasswordFileAuthenticationService();
		for (GroupAddUpdateRequest group : userPreparator.createGroups()) {
			userServices.execute(group);
			groupCodes.add(group.getCode());
		}

		for (com.constellio.model.services.users.UserAddUpdateRequest user : userPreparator.createUsers(groupCodes)) {
			userServices.execute(user);
			authenticationService.changePassword(user.getUsername(), "password");

		}

		for (String collection : collections) {
			setupCollection(collection, appLayerFactory);
		}

		BigVaultServer bigVaultServer = modelLayerFactory.getDataLayerFactory().getRecordsVaultServer();
		bigVaultServer.addAll(new BigVaultServerTransaction(RecordsFlushing.NOW()).setDeletedQueries(asList("type_s:marker")));
	}

	@Override
	public List<InstallableModule> getModules() {
		ConstellioRMModule rmModule = new ConstellioRMModule();
		return asList((InstallableModule) rmModule);
	}

	private void givenCollectionWithRMModuleAndUsers(AppLayerFactory appLayerFactory, String collection, String title)
			throws Exception {

		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		//CREATE COLLECTION
		Record collectionRecord = appLayerFactory.getCollectionsManager()
				.createCollection(collection, asList("fr"));
		modelLayerFactory.newRecordServices().update(collectionRecord.set(Schemas.TITLE, title));

		//SETUP MODULES
		appLayerFactory.getModulesManager().enableValidModuleAndGetInvalidOnes(collection, new ConstellioRMModule());

	}

	private void setupCollection(String collection, AppLayerFactory appLayerFactory)
			throws Exception {

		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		User user = modelLayerFactory.newUserServices().getUserInCollection("admin", collection);
		modelLayerFactory.newRecordServices().update(user.setUserRoles(asList(RMRoles.RGD)).setCollectionAllAccess(true));

		LogicalSearchQuery allUsersQuery = new LogicalSearchQuery(from(rm.userSchemaType()).returnAll());
		LogicalSearchQuery allGroupsQuery = new LogicalSearchQuery(from(rm.groupSchemaType()).returnAll());
		LogicalSearchQuery allRulesQuery = new LogicalSearchQuery(from(rm.retentionRule.schemaType()).returnAll());

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LinkableRecordsList<User> users = new LinkableRecordsList<>(rm.wrapUsers(searchServices.search(allUsersQuery)));
		LinkableIdsList userIds = new LinkableIdsList(searchServices.searchRecordIds(allUsersQuery));
		LinkableIdsList groupIds = new LinkableIdsList(searchServices.searchRecordIds(allGroupsQuery));

		Transaction transaction = new Transaction().setOptimisticLockingResolution(EXCEPTION);
		List<RecordWrapper> categories = prepareTaxonomy(rm, transaction, categoriesTaxonomy);
		modelLayerFactory.newRecordServices().execute(transaction);

		transaction = new Transaction().setOptimisticLockingResolution(EXCEPTION);
		List<RecordWrapper> administrativeUnits = prepareTaxonomy(rm, transaction, administrativeUnitsTaxonomy);
		modelLayerFactory.newRecordServices().execute(transaction);

		for (RecordWrapper recordWrapper : administrativeUnits) {
			administrativeUnitsTaxonomy.setupAuthorizations(rm, recordWrapper, userIds, groupIds);
		}

		LinkableIdsList categoriesIds = LinkableIdsList.forRecords(categories);
		LinkableIdsList administrativeUnitsIds = LinkableIdsList.forRecords(administrativeUnits);
		String ruleId = modelLayerFactory.newSearchServices().searchRecordIds(allRulesQuery).get(0);
		LinkableIdsList folderIds = createFolders(rm, ruleId, categoriesIds, administrativeUnitsIds, users);
		createDocuments(rm, folderIds, users);
	}

	private void createDocuments(RMSchemasRecordsServices rm, LinkableIdsList folderIds,
								 LinkableRecordsList<User> users) {

		EventFactory eventFactory = new EventFactory(rm.getModelLayerFactory());
		User admin = rm.getModelLayerFactory().newUserServices().getUserInCollection("admin", rm.getCollection());

		RecordServices recordServices = rm.getModelLayerFactory().newRecordServices();
		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions();
		options.withRecordsPerBatch(1000);
		options.withNumberOfThreads(4);
		BulkRecordTransactionHandler transactionHandler = new BulkRecordTransactionHandler(recordServices, "addDocuments",
				options);

		ThreadList<DocumentSavehread> threadList = new ThreadList<>();
		AtomicInteger addedCount = new AtomicInteger();
		for (int i = 0; i < 10; i++) {
			threadList.addAndStart(new DocumentSavehread(addedCount, numberOfDocuments, contentIterator, rm, folderIds, users,
					transactionHandler));
		}
		try {
			threadList.joinAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		transactionHandler.closeAndJoin();
	}

	private class DocumentSavehread extends Thread {

		private int requiredCount;
		private AtomicInteger addedCount = new AtomicInteger();

		private BigFileFolderIterator bigFileFolderIterator;

		private RMSchemasRecordsServices rm;

		private LinkableIdsList folderIds;
		private LinkableRecordsList<User> users;

		private ContentManager contentManager;

		private BulkRecordTransactionHandler transactionHandler;

		private EventFactory eventFactory;
		private User admin;

		public DocumentSavehread(AtomicInteger addedCount, int requiredCount,
								 BigFileFolderIterator bigFileFolderIterator,
								 RMSchemasRecordsServices rm, LinkableIdsList folderIds,
								 LinkableRecordsList<User> users,
								 BulkRecordTransactionHandler transactionHandler) {
			this.addedCount = addedCount;
			this.requiredCount = requiredCount;
			this.bigFileFolderIterator = bigFileFolderIterator;
			this.rm = rm;
			this.folderIds = folderIds;
			this.users = users;
			this.transactionHandler = transactionHandler;
			this.contentManager = rm.getModelLayerFactory().getContentManager();

			this.eventFactory = new EventFactory(rm.getModelLayerFactory());
			this.admin = rm.getModelLayerFactory().newUserServices().getUserInCollection("admin", rm.getCollection());
		}

		@Override
		public void run() {
			while (addedCount.get() < requiredCount) {
				List<BigFileEntry> entries = new ArrayList<>();
				Folder folder = rm.getFolder(folderIds.next());
				User createdBy = users.next();
				synchronized (bigFileFolderIterator) {
					for (int i = 0; i < documentsBatch; i++) {
						entries.add(bigFileFolderIterator.next());
					}
				}

				List<Record> records = new ArrayList<>();
				for (int i = 0; i < documentsBatch; i++) {

					BigFileEntry entry = contentIterator.next();
					InputStream in = new ByteArrayInputStream(entry.getBytes());
					ContentVersionDataSummary contentVersionDataSummary;
					try {
						UploadOptions options = new UploadOptions().setHandleDeletionOfUnreferencedHashes(false);
						contentVersionDataSummary = contentManager.upload(in, options).getContentVersionDataSummary();
						;
					} finally {
						IOUtils.closeQuietly(in);
					}

					Document document = rm.newDocument()
							.setFolder(folder)
							.setFormCreatedBy(createdBy)
							.setFormCreatedOn(new LocalDateTime())
							.setTitle(entry.getFileName())
							.setContent(contentManager.createMajor(createdBy, entry.getFileName(), contentVersionDataSummary));
					records.add(document.getWrappedRecord());

					Record documentRecord = document.getWrappedRecord();
					records.add(eventFactory.newRecordEvent(documentRecord, users.next(), VIEW).getWrappedRecord());
					records.add(eventFactory.newRecordEvent(documentRecord, users.next(), VIEW).getWrappedRecord());
					records.add(eventFactory.newRecordEvent(documentRecord, users.next(), VIEW).getWrappedRecord());
				}

				List<Record> dependOn = asList(folder.getWrappedRecord());

				transactionHandler.append(records, dependOn);

				addedCount.addAndGet(documentsBatch);
			}
		}
	}

	private LinkableIdsList createFolders(RMSchemasRecordsServices rm, String ruleId, LinkableIdsList categoriesIds,
										  LinkableIdsList administrativeUnitsIds, LinkableRecordsList<User> users) {

		Map<String, User> userCache = new HashMap<>();

		EventFactory eventFactory = new EventFactory(rm.getModelLayerFactory());
		List<String> folderIds = new ArrayList<>();
		RecordServices recordServices = rm.getModelLayerFactory().newRecordServices();
		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions();
		//options.withRecordsPerBatch(1 + subFoldersPerFolder + subFoldersPerFolder * subSubFoldersPerFolder);
		BulkRecordTransactionHandler transactionHandler = new BulkRecordTransactionHandler(recordServices, "addFolders", options);

		for (int i = 0; i < numberOfRootFolders; i++) {
			List<Record> foldersToAppend = new ArrayList<>();
			Folder folder = rm.newFolder()
					.setTitle("Folder " + i)
					.setAdministrativeUnitEntered(administrativeUnitsIds.next())
					.setOpenDate(new LocalDate())
					.setCategoryEntered(categoriesIds.next())
					.setRetentionRuleEntered(ruleId)
					.setCopyStatusEntered(CopyType.PRINCIPAL);

			folderIds.add(folder.getId());
			foldersToAppend.add(folder.getWrappedRecord());

			for (int j = 0; j < subFoldersPerFolder; j++) {
				Folder subFolder = rm.newFolder()
						.setTitle("Folder " + i + "-" + j)
						.setParentFolder(folder)
						.setCopyStatusEntered(CopyType.PRINCIPAL)
						.setOpenDate(new LocalDate());

				folderIds.add(subFolder.getId());
				foldersToAppend.add(subFolder.getWrappedRecord());

				for (int k = 0; k < subSubFoldersPerFolder; k++) {
					Folder subSubFolder = rm.newFolder()
							.setTitle("Folder " + i + "-" + j + "-" + k)
							.setParentFolder(subFolder)
							.setCopyStatusEntered(CopyType.PRINCIPAL)
							.setOpenDate(new LocalDate());

					folderIds.add(subSubFolder.getId());
					foldersToAppend.add(subSubFolder.getWrappedRecord());
				}
			}

			transactionHandler.append(foldersToAppend);
			for (Record folderRecord : foldersToAppend) {
				transactionHandler.append(eventFactory.newRecordEvent(folderRecord, users.next(), VIEW).getWrappedRecord());
				transactionHandler.append(eventFactory.newRecordEvent(folderRecord, users.next(), VIEW).getWrappedRecord());
				transactionHandler.append(eventFactory.newRecordEvent(folderRecord, users.next(), VIEW).getWrappedRecord());
			}

		}

		transactionHandler.closeAndJoin();

		return new LinkableIdsList(folderIds);
	}

	private List<RecordWrapper> prepareTaxonomy(RMSchemasRecordsServices rm, Transaction transaction,
												TaxonomyPreparator taxonomy) {
		List<RecordWrapper> recordWrappers = new ArrayList<>();

		taxonomy.init(rm, transaction);

		Stack<Integer> stack = new Stack<>();
		List<RecordWrapper> rootConcepts = taxonomy.createRootConcepts(rm);
		recordWrappers.addAll(rootConcepts);
		for (int i = 0; i < rootConcepts.size(); i++) {
			stack.push(i);
			RecordWrapper recordWrapper = rootConcepts.get(i);
			transaction.add(recordWrapper);
			prepareTaxonomy(rm, transaction, recordWrapper, taxonomy, stack, recordWrappers);
			stack.pop();
		}
		return recordWrappers;
	}

	private void prepareTaxonomy(RMSchemasRecordsServices rm, Transaction transaction, RecordWrapper record,
								 TaxonomyPreparator taxonomy, Stack<Integer> stack,
								 List<RecordWrapper> recordWrappers) {

		List<RecordWrapper> childConcepts = taxonomy.createChildConcepts(rm, record, stack);
		recordWrappers.addAll(childConcepts);
		for (int i = 0; i < childConcepts.size(); i++) {
			stack.push(i);
			RecordWrapper recordWrapper = childConcepts.get(i);
			transaction.add(recordWrapper);
			prepareTaxonomy(rm, transaction, recordWrapper, taxonomy, stack, recordWrappers);
			stack.pop();
		}
	}

}
