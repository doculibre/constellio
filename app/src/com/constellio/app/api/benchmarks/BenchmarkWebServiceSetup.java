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
package com.constellio.app.api.benchmarks;

import static com.constellio.app.api.benchmarks.BenchmarkWebService.TEST_RULE_ID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.joda.time.LocalDate;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.test.RandomWordsIterator;
import com.constellio.data.utils.Octets;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;

public class BenchmarkWebServiceSetup {

	public static String USER_WITH_ONE_PERCENT_ACCESS = "benckmarkTestUser_1pct";
	public static String USER_WITH_FIVE_PERCENT_ACCESS = "benckmarkTestUser_5pct";
	public static String USER_WITH_TEN_PERCENT_ACCESS = "benckmarkTestUser_10pct";
	public static String USER_WITH_TWENTY_PERCENT_ACCESS = "benckmarkTestUser_20pct";
	public static String USER_WITH_FIFTY_PERCENT_ACCESS = "benckmarkTestUser_50pct";

	static ThreadLocal<Random> randoms = new ThreadLocal<>();
	static ThreadLocal<RandomWordsIterator> randomFrenchWordsIterators = new ThreadLocal<RandomWordsIterator>();
	static ThreadLocal<RandomWordsIterator> randomEnglishWordsIterators = new ThreadLocal<RandomWordsIterator>();
	RandomWordsIterator mainFrenchWordsIterators;
	RandomWordsIterator mainEnglishWordsIterators;

	FilingSpace filingSpace;
	List<AdministrativeUnit> administrativeUnits;

	private static synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	List<String> initializeCollectionIfRequired(int numberOfRootCategories, int numberOfLevel1Categories,
			int numberOfLevel2Categories,
			int numberOfLevel1AdministrativeUnits, int numberOfLevel2AdministrativeUnits, int numberOfFolders,
			String collection) {

		File dictionaryFolder = getFoldersLocator().getDict();
		mainFrenchWordsIterators = RandomWordsIterator.createFor(new File(dictionaryFolder, "fr_FR_avec_accents.dic"));
		mainEnglishWordsIterators = RandomWordsIterator.createFor(new File(dictionaryFolder, "en_US.dic"));

		CollectionsManager collectionsManager = getCollectionsServices();
		if (!collectionsManager.getCollectionCodes().contains(collection)) {

			collectionsManager.createCollectionInCurrentVersion(collection, asList("fr", "en"));

			ConstellioModulesManagerImpl modulesManager = (ConstellioModulesManagerImpl) getModulesManager();
			InstallableModule module = new ConstellioRMModule();

			try {
				modulesManager.installModule(module, getCollectionsListManager());
				modulesManager.enableModule(collection, module);
				getMigrationServices().migrate(null);

			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new RuntimeException(optimisticLockingConfiguration);
			}

			setupUsers(collection);

			try {
				List<Record> level1And2CategoriesIds = createCategories(numberOfRootCategories, numberOfLevel1Categories,
						numberOfLevel2Categories, collection);

				createClassificationStations(numberOfLevel1AdministrativeUnits,
						numberOfLevel2AdministrativeUnits, collection);

				createFolders(level1And2CategoriesIds, filingSpace, administrativeUnits, numberOfFolders, collection);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

		MetadataSchema categorySchema = getMetadataSchemasManager().getSchemaTypes(collection).getSchema(Category.DEFAULT_SCHEMA);
		Metadata parentCategory = categorySchema.getMetadata(Category.PARENT);
		SearchServices searchServices = getSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery(from(categorySchema).where(parentCategory).isNotNull());
		return searchServices.searchRecordIds(query);
	}

	private void setupUsers(String collection) {
		setupUser(USER_WITH_ONE_PERCENT_ACCESS, collection);
		setupUser(USER_WITH_FIVE_PERCENT_ACCESS, collection);
		setupUser(USER_WITH_TEN_PERCENT_ACCESS, collection);
		setupUser(USER_WITH_TWENTY_PERCENT_ACCESS, collection);
		setupUser(USER_WITH_FIFTY_PERCENT_ACCESS, collection);

		UserServices userServices = getConstellioFactories().getModelLayerFactory().newUserServices();
		userServices.addUserToCollection(userServices.getUser("admin"), collection);
	}

	private void setupUser(String username, String collection) {
		UserServices userServices = getConstellioFactories().getModelLayerFactory().newUserServices();

		UserCredential user = new UserCredential(username, username.split("_")[0], username.split("_")[1],
				username + "@constellio.com", new ArrayList<String>(), new ArrayList<String>(), UserCredentialStatus.ACTIVE, "");

		userServices.addUpdateUserCredential(user);
		userServices.addUserToCollection(user, collection);
	}

	private List<Record> createCategories(int numberOfRootCategories, int numberOfLevel1Categories, int numberOfLevel2Categories,
			String collection)
			throws RecordServicesException {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection,
				getConstellioFactories().getModelLayerFactory());
		RecordServices recordServices = getRecordServices();
		MetadataSchema categorySchema = getMetadataSchemasManager().getSchemaTypes(collection).getSchema(Category.DEFAULT_SCHEMA);
		Metadata categoryParent = categorySchema.getMetadata(Category.PARENT);
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

		CopyRetentionRule principal888_5_C = CopyRetentionRule.newPrincipal(asList(rm.PA(), rm.DM()), "888-5-C");
		principal888_5_C.setContentTypesComment("R1");
		principal888_5_C.setActiveRetentionComment("R2");
		CopyRetentionRule secondary888_0_D = CopyRetentionRule.newSecondary(asList(rm.PA(), rm.DM()), "888-0-D");
		secondary888_0_D.setSemiActiveRetentionComment("R3");
		secondary888_0_D.setInactiveDisposalComment("R4");

		RetentionRule rule = transaction.add(rm.newRetentionRuleWithId(TEST_RULE_ID)).setCode("1").setTitle("Rule #1")
				.setApproved(true).setResponsibleAdministrativeUnits(true)
				.setCopyRetentionRules(asList(principal888_5_C, secondary888_0_D)).setKeywords(asList("Rule #1"))
				.setCorpus("Corpus  Rule 1").setDescription("Description Rule 1")
				.setJuridicReference("Juridic reference Rule 1").setGeneralComment("General Comment Rule 1")
				.setCopyRulesComment(asList("R1:comment1", "R2:comment2", "R3:comment3", "R4:comment4"));
		//				.setDocumentTypesDetails(asList(
		//						new RetentionRuleDocumentType(type1.getId()),
		//						new RetentionRuleDocumentType(type2.getId()),
		//						new RetentionRuleDocumentType(type3.getId())));

		List<Record> level1And2CategoriesIds = new ArrayList<>();
		for (int i = 0; i < numberOfRootCategories; i++) {
			Category level0Category = rm.newCategory();

			level0Category.setCode("" + i);
			level0Category.setTitle("Category #" + i);
			level0Category.setRetentionRules(asList(TEST_RULE_ID));
			transaction.add(level0Category);

			for (int j = 0; j < numberOfLevel1Categories; j++) {
				Category level1Category = rm.newCategory();

				String codeL1 = i + "_" + j;
				level1Category.setCode(codeL1);
				level1Category.setTitle(codeL1);
				level1Category.setParent(level0Category);
				level1Category.setRetentionRules(asList(TEST_RULE_ID));
				transaction.add(level1Category);
				level1And2CategoriesIds.add(level1Category.getWrappedRecord());

				for (int k = 0; k < numberOfLevel2Categories; k++) {
					Category level2Category = rm.newCategory();
					String codeL2 = i + "_" + j + "_" + k;

					level2Category.setCode(codeL2);
					level2Category.setTitle("Category #" + codeL2);
					level2Category.setParent(level1Category);
					level2Category.setRetentionRules(asList(TEST_RULE_ID));
					transaction.add(level2Category);
					level1And2CategoriesIds.add(level2Category.getWrappedRecord());
				}

			}
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return level1And2CategoriesIds;
	}

	private void createClassificationStations(int numberOfLevel1AdministrativeUnits,
			int numberOfLevel2AdministrativeUnits,
			String collection)
			throws RecordServicesException {
		RecordServices recordServices = getRecordServices();
		MetadataSchemaTypes types = getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchema administrativeUnitSchema = types.getSchema(AdministrativeUnit.DEFAULT_SCHEMA);
		MetadataSchema filingSpaceSchema = types.getSchema(FilingSpace.DEFAULT_SCHEMA);

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		administrativeUnits = new ArrayList<>();

		List<String> onePercentAdministrativeUnits = new ArrayList<>();
		List<String> fivePercentAdministrativeUnits = new ArrayList<>();
		List<String> tenPercentAdministrativeUnits = new ArrayList<>();
		List<String> twentyPercentAdministrativeUnits = new ArrayList<>();
		List<String> fiftyPercentAdministrativeUnits = new ArrayList<>();

		filingSpace = new FilingSpace(getRecordServices().newRecordWithSchema(filingSpaceSchema), types);
		filingSpace.setTitle("Ze filing space");
		filingSpace.setCode("space");
		transaction.add(filingSpace);

		int counter = 0;
		for (int i = 0; i < numberOfLevel1AdministrativeUnits; i++) {
			AdministrativeUnit administrativeUnit = new AdministrativeUnit(
					getRecordServices().newRecordWithSchema(administrativeUnitSchema), types);

			administrativeUnit.setCode("" + i);
			administrativeUnit.setTitle("Administrative unit #" + i);

			transaction.add(administrativeUnit);

			for (int j = 0; j < numberOfLevel2AdministrativeUnits; j++) {

				AdministrativeUnit level2AdministrativeUnit = new AdministrativeUnit(
						getRecordServices().newRecordWithSchema(administrativeUnitSchema), types);

				String code = i + "_" + j;
				level2AdministrativeUnit.setCode(code);
				level2AdministrativeUnit.setTitle("Administrative unit #" + code);
				level2AdministrativeUnit.setFilingSpaces(Arrays.asList(filingSpace.getId()));
				level2AdministrativeUnit.setParent(administrativeUnit);

				transaction.add(level2AdministrativeUnit);
				administrativeUnits.add(level2AdministrativeUnit);

				if (counter % 100 == 0) {
					onePercentAdministrativeUnits.add(level2AdministrativeUnit.getId());
				}
				if (counter % 20 == 0) {
					fivePercentAdministrativeUnits.add(level2AdministrativeUnit.getId());
				}
				if (counter % 10 == 0) {
					tenPercentAdministrativeUnits.add(level2AdministrativeUnit.getId());
				}
				if (counter % 5 == 0) {
					twentyPercentAdministrativeUnits.add(level2AdministrativeUnit.getId());
				}
				if (counter % 2 == 0) {
					fiftyPercentAdministrativeUnits.add(level2AdministrativeUnit.getId());
				}
				counter++;
			}
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		UserServices userServices = getConstellioFactories().getModelLayerFactory().newUserServices();
		AuthorizationsServices authServices = getConstellioFactories().getModelLayerFactory().newAuthorizationsServices();
		RolesManager rolesManager = getConstellioFactories().getModelLayerFactory().getRolesManager();

		//		Role userRole = rolesManager.getRole(collection, RMRoles.USER);

		authServices.add(new Authorization(AuthorizationDetails.create("1pct", Arrays.asList(Role.WRITE), collection),
				Arrays.asList(userServices.getUserInCollection(USER_WITH_ONE_PERCENT_ACCESS, collection).getId()),
				onePercentAdministrativeUnits), CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		authServices.add(new Authorization(AuthorizationDetails.create("5pct", Arrays.asList(Role.WRITE), collection),
				Arrays.asList(userServices.getUserInCollection(USER_WITH_FIVE_PERCENT_ACCESS, collection).getId()),
				fivePercentAdministrativeUnits), CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		authServices.add(new Authorization(AuthorizationDetails.create("10pct", Arrays.asList(Role.WRITE), collection),
				Arrays.asList(userServices.getUserInCollection(USER_WITH_TEN_PERCENT_ACCESS, collection).getId()),
				tenPercentAdministrativeUnits), CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		authServices.add(new Authorization(AuthorizationDetails.create("20pct", Arrays.asList(Role.WRITE), collection),
				Arrays.asList(userServices.getUserInCollection(USER_WITH_TWENTY_PERCENT_ACCESS, collection).getId()),
				twentyPercentAdministrativeUnits), CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		authServices.add(new Authorization(AuthorizationDetails.create("50pct", Arrays.asList(Role.WRITE), collection),
				Arrays.asList(userServices.getUserInCollection(USER_WITH_FIFTY_PERCENT_ACCESS, collection).getId()),
				fiftyPercentAdministrativeUnits), CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		BatchProcessesManager manager = getConstellioFactories().getModelLayerFactory().getBatchProcessesManager();
		for (BatchProcess batchProcess : manager.getAllNonFinishedBatchProcesses()) {
			while (manager.get(batchProcess.getId()).getStatus() != BatchProcessStatus.FINISHED) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

		}

		recordServices.refresh(administrativeUnits);
	}

	private void createFolders(List<Record> level1And2Categories, FilingSpace filingSpace,
			List<AdministrativeUnit> administrativeUnits,
			int numberOfFoldersInEachSubCategory, String collection) {

		RecordServices recordServices = getRecordServices();
		MetadataSchemaTypes schemaTypes = getSchemaTypes(collection);
		MetadataSchema categorySchema = schemaTypes.getSchema(Category.DEFAULT_SCHEMA);
		Metadata categoryRulesMetadata = categorySchema.getMetadata(Category.RETENTION_RULES);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, getConstellioFactories().getModelLayerFactory());

		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions().withRecordsPerBatch(5000);
		BulkRecordTransactionHandler bulkTransactionsHandler = new BulkRecordTransactionHandler(recordServices,
				"benchmarkRecordsImport", options);

		try {
			for (int i = 0; i < numberOfFoldersInEachSubCategory; i++) {

				Record category = level1And2Categories.get(i % level1And2Categories.size());
				List<String> categoryRules = category.getList(categoryRulesMetadata);
				AdministrativeUnit administrativeUnit = administrativeUnits.get(i % administrativeUnits.size());

				RandomWordsIterator randomWordsIterator = getRandomWordsIterator(i);
				Folder folder = rm.newFolder();
				folder.setCategoryEntered(category);
				folder.setRetentionRuleEntered(categoryRules.get(0));
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setFilingSpaceEntered(filingSpace);
				folder.setOpenDate(new LocalDate(2010, 1, 1));
				folder.setAdministrativeUnitEntered(administrativeUnit);
				folder.setTitle(randomWordsIterator.nextWords(10));
				folder.setDescription(randomWordsIterator.nextWordsOfLength(Octets.kilooctets(2)));
				bulkTransactionsHandler.append(Arrays.asList(folder.getWrappedRecord()),
						Arrays.asList(category, filingSpace.getWrappedRecord(), administrativeUnit.getWrappedRecord()));

			}
		} finally {

			bulkTransactionsHandler.closeAndJoin();
		}

	}

	RandomWordsIterator getRandomWordsIterator(int serviceCall) {
		if (serviceCall % 2 == 1) {
			return getFrenchRandomWordsIterator();
		} else {
			return getEnglishRandomWordsIterator();
		}
	}

	private RandomWordsIterator getEnglishRandomWordsIterator() {
		RandomWordsIterator iterator = randomEnglishWordsIterators.get();
		if (iterator == null) {
			iterator = mainEnglishWordsIterators.createCopy();
		}
		randomEnglishWordsIterators.set(iterator);
		return iterator;
	}

	private RandomWordsIterator getFrenchRandomWordsIterator() {
		RandomWordsIterator iterator = randomFrenchWordsIterators.get();
		if (iterator == null) {
			iterator = mainFrenchWordsIterators.createCopy();
		}
		randomFrenchWordsIterators.set(iterator);
		return iterator;
	}

	MetadataSchemaTypes getSchemaTypes(String collection) {
		return getMetadataSchemasManager().getSchemaTypes(collection);
	}

	MetadataSchemasManager getMetadataSchemasManager() {
		return getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager();
	}

	SearchServices getSearchServices() {
		return getConstellioFactories().getModelLayerFactory().newSearchServices();
	}

	FreeTextSearchServices getFreeTextSearchServices() {
		return getConstellioFactories().getModelLayerFactory().newFreeTextSearchServices();
	}

	RecordServices getRecordServices() {
		return getConstellioFactories().getModelLayerFactory().newRecordServices();
	}

	ContentManager getContentServices() {
		return getConstellioFactories().getModelLayerFactory().getContentManager();
	}

	CollectionsManager getCollectionsServices() {
		return getConstellioFactories().getAppLayerFactory().getCollectionsManager();
	}

	CollectionsListManager getCollectionsListManager() {
		return getConstellioFactories().getModelLayerFactory().getCollectionsListManager();
	}

	FoldersLocator getFoldersLocator() {
		return getConstellioFactories().getFoldersLocator();
	}

	ConstellioPluginManager getPluginManager() {
		return getConstellioFactories().getAppLayerFactory().getPluginManager();
	}

	ConstellioModulesManager getModulesManager() {
		return getConstellioFactories().getAppLayerFactory().getModulesManager();
	}

	MigrationServices getMigrationServices() {
		return getConstellioFactories().getAppLayerFactory().newMigrationServices();
	}

	TaxonomiesManager getTaxonomiesManager() {
		return getConstellioFactories().getModelLayerFactory().getTaxonomiesManager();
	}

	public User getTestUser(String username, String collection) {
		return null;
	}

	public Random getRandom() {
		Random random = randoms.get();
		if (random == null) {
			random = new Random();
			randoms.set(random);
		}
		return random;
	}
}
