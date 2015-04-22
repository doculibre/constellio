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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_BenchmarkServiceMustBeEnabled;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_ConfigInvalid;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_ConfigRequired;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_ParameterInvalid;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_ParameterRequired;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.test.RandomWordsIterator;
import com.constellio.data.utils.Octets;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.OptimisticLocking;
import com.constellio.model.services.records.RecordServicesException.UnresolvableOptimisticLockingConflict;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;

public class BenchmarkWebService extends HttpServlet {

	public static final String TEST_RULE_ID = "rule42";

	private static final String CONTENT_TO_ADD_STREAM = "BenchmarkWebService-ContentToAdd";

	public static final String COLLECTION = "benchmarkCollection";
	static final String NUMBER_OF_ROOT_CATEGORIES = "numberOfRootCategories";
	static final String NUMBER_OF_LEVEL1_CATEGORIES = "numberOfLevel1Categories";
	static final String NUMBER_OF_LEVEL2_CATEGORIES = "numberOfLevel2Categories";
	static final String NUMBER_OF_LEVEL1_ADMINISTRATIVE_UNITS = "numberOfLevel1AdministrativeUnits";
	static final String NUMBER_OF_LEVEL2_ADMINISTRATIVE_UNITS = "numberOfLevel2AdministrativeUnits";
	static final String NUMBER_OF_FOLDERS = "numberOfFolders";
	static final String CONTENT_SAMPLES = "contentSamples";
	static final String ACTION_PARAMETER = "action";
	static final String QUANTITY_PARAMETER = "qty";
	static final String FLUSH_WITHIN_PARAMETER = "flushWithinMS";
	static final String LEVELS_PARAMETER = "levels";
	static final String SCHEMA = "schema";

	static final String ACTION_SEARCH = "search";
	static final String ACTION_OPEN = "open";
	static final String ACTION_NAVIGATE = "navigate";
	static final String ACTION_ADD_FOLDER = "addRecords";
	static final String ACTION_UPDATE_FOLDERS = "updateRecords";
	static final String ACTION_ADD_CONTENT = "addContent";
	static final String SIZE_IN_OCTETS_PARAMETER = "sizeInOctets";

	static List<String> level1And2CategoryIds;
	static List<Record> administrativeUnits;

	static AtomicInteger addedFolderCounter = new AtomicInteger();
	static AtomicInteger serviceCallCounter = new AtomicInteger();

	static BenchmarkWebServiceSetup setup = new BenchmarkWebServiceSetup();

	static List<File> allPdfFiles;

	private static synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		super.init(config);

		setup = newTestSetup();

		if ("true".equals(System.getProperty("benchmarkServiceEnabled"))) {
			int numberOfRootCategories = getRequiredIntegerConfig(config, NUMBER_OF_ROOT_CATEGORIES);
			int numberOfLevel1Categories = getRequiredIntegerConfig(config, NUMBER_OF_LEVEL1_CATEGORIES);
			int numberOfLevel2Categories = getRequiredIntegerConfig(config, NUMBER_OF_LEVEL2_CATEGORIES);
			int numberOfLevel1AdministrativeUnits = getRequiredIntegerConfig(config, NUMBER_OF_LEVEL1_ADMINISTRATIVE_UNITS);
			int numberOfLevel2AdministrativeUnits = getRequiredIntegerConfig(config, NUMBER_OF_LEVEL2_ADMINISTRATIVE_UNITS);
			int numberOfFolders = getRequiredIntegerConfig(config, NUMBER_OF_FOLDERS);
			String contentSamplesFilepath = getRequiredConfig(config, CONTENT_SAMPLES);

			setup.initializeCollectionIfRequired(numberOfRootCategories, numberOfLevel1Categories, numberOfLevel2Categories,
					numberOfLevel1AdministrativeUnits, numberOfLevel2AdministrativeUnits, numberOfFolders, COLLECTION);

			setCategoriesAndClassificationIdsList();

			IOFileFilter fileFilter = new IOFileFilter() {

				@Override
				public boolean accept(File file) {
					return true;
				}

				@Override
				public boolean accept(File dir, String name) {
					return true;
				}
			};

			File contentSamplesFile = new File(contentSamplesFilepath);
			if (!contentSamplesFile.exists()) {
				throw new RuntimeException("Folder '" + contentSamplesFilepath + "' does not exist");
			}
			Collection<File> files = FileUtils.listFiles(contentSamplesFile, fileFilter, fileFilter);
			if (files == null || files.isEmpty()) {
				throw new RuntimeException("Folder '" + contentSamplesFilepath + "' is empty");
			}
			allPdfFiles = new ArrayList<>(files);
		}

	}

	private void setCategoriesAndClassificationIdsList() {
		SearchServices searchServices = getSearchServices();
		MetadataSchema categorySchema = getMetadataSchemasManager().getSchemaTypes(COLLECTION).getSchema(Category.DEFAULT_SCHEMA);
		MetadataSchema administrativeUnitSchema = getMetadataSchemasManager().getSchemaTypes(COLLECTION).getSchema(
				AdministrativeUnit.DEFAULT_SCHEMA);
		Metadata filingSpaceMetadata = administrativeUnitSchema.getMetadata(AdministrativeUnit.FILING_SPACES);
		Metadata parentCategoryMetadata = categorySchema.getMetadata("parent");
		level1And2CategoryIds = searchServices.searchRecordIds(new LogicalSearchQuery(
				LogicalSearchQueryOperators.from(categorySchema).where(parentCategoryMetadata).isNotNull()));
		administrativeUnits = searchServices.search(new LogicalSearchQuery(
				LogicalSearchQueryOperators.from(administrativeUnitSchema).where(filingSpaceMetadata).isNotNull()));
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (!"true".equals(System.getProperty("benchmarkServiceEnabled"))) {
			throw new BenchmarkWebServiceRuntimeException_BenchmarkServiceMustBeEnabled();
		}

		try {
			handleGet(request, response);
		} catch (UnresolvableOptimisticLockingConflict | OptimisticLocking e) {
			//Normal scenario, this does not fail the benchmark
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void handleGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, RecordServicesException {

		int serviceCall = serviceCallCounter.incrementAndGet();
		String action = getRequiredParameter(request, ACTION_PARAMETER);

		if (ACTION_SEARCH.equals(action)) {
			doSearch(request, response, serviceCall);

		} else if (ACTION_ADD_FOLDER.equals(action)) {
			doAddFolder(request, response, serviceCall);

		} else if (ACTION_UPDATE_FOLDERS.equals(action)) {
			doUpdateRecords(request, response, serviceCall);

		} else if (ACTION_NAVIGATE.equals(action)) {
			doNavigate(request, response, serviceCall);

		} else if (ACTION_ADD_CONTENT.equals(action)) {
			doAddContent(request, response, serviceCall);

		} else if (ACTION_OPEN.equals(action)) {
			open(request, response, serviceCall);

		} else {
			throw new BenchmarkWebServiceRuntimeException_ParameterInvalid(ACTION_PARAMETER);
		}
	}

	void doSearch(HttpServletRequest request, HttpServletResponse response, int serviceCall)
			throws ServletException, IOException {
		int qty = getRequiredIntegerParameter(request, QUANTITY_PARAMETER);
		MetadataSchema folderSchema = getMetadataSchemasManager().getSchemaTypes(COLLECTION).getSchema("folder_default");
		Metadata categoryMetadata = folderSchema.getMetadata("category");
		SearchServices searchServices = getSearchServices();
		String categoryId = level1And2CategoryIds.get(serviceCall % level1And2CategoryIds.size());
		LogicalSearchCondition condition = from(folderSchema).where(categoryMetadata).isEqualTo(categoryId);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchemaTitle());
		query.setNumberOfRows(qty);
		searchServices.search(query);

	}

	private UserCredential getTestUserCredential(String username) {
		return getUserServices().getUser(username);
	}

	private User getTestUser(String username) {
		return getUserServices().getUserInCollection(username, COLLECTION);
	}

	void doNavigate(HttpServletRequest request, HttpServletResponse response, int serviceCall)
			throws ServletException, IOException {
		User user = getUserParameter(request);

		TaxonomiesSearchServices taxonomiesSearchServices = getTaxonomiesSearchServices();
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();
		options.setRows(25);

		List<Record> rootRecords = taxonomiesSearchServices.getVisibleRootConcept(user, COLLECTION, "plan", options);

		if (!rootRecords.isEmpty()) {
			List<Record> level1Records = taxonomiesSearchServices
					.getVisibleChildConcept(user, "plan", rootRecords.get(0), options);
			//			if (!level1Records.isEmpty()) {
			//				List<Record> level2Records = taxonomiesSearchServices.getVisibleChildConcept(user, level1Records.get(0), options);
			//			}
		}

	}

	private User getUserParameter(HttpServletRequest request) {
		String username = getRequiredParameter(request, "user");
		return getUserServices().getUserInCollection(username, COLLECTION);
	}

	void open(HttpServletRequest request, HttpServletResponse response, int serviceCall) {
		RecordServices recordServices = getConstellioFactories().getModelLayerFactory().newRecordServices();
		String idWithTooMuchZeros = "0000000000" + serviceCall % 1000000;
		recordServices.getDocumentById(idWithTooMuchZeros.substring(idWithTooMuchZeros.length() - 11));
	}

	void doAddFolder(HttpServletRequest request, HttpServletResponse response, int serviceCall)
			throws ServletException, IOException, RecordServicesException {
		int addedFolder = addedFolderCounter.incrementAndGet();
		int qty = getRequiredIntegerParameter(request, QUANTITY_PARAMETER);
		int sizeInOctets = getRequiredIntegerParameter(request, SIZE_IN_OCTETS_PARAMETER);

		MetadataSchemaTypes schemaTypes = getSchemaTypes(COLLECTION);
		MetadataSchema folderSchema = schemaTypes.getSchema("folder_default");
		RecordsFlushing recordsFlushing = getRecordsFlushing(request);

		List<Record> records = new ArrayList<>();
		for (int i = 0; i < qty; i++) {
			int counter = addedFolder + i;

			String categoryId = level1And2CategoryIds.get(counter % level1And2CategoryIds.size());
			AdministrativeUnit administrativeUnit = new AdministrativeUnit(
					administrativeUnits.get(counter % administrativeUnits.size()), schemaTypes);

			Record newFolderRecord = getRecordServices().newRecordWithSchema(folderSchema);
			Folder newFolder = new Folder(newFolderRecord, schemaTypes);
			newFolder.setCategoryEntered(categoryId);
			newFolder.setRetentionRuleEntered(TEST_RULE_ID);
			newFolder.setCopyStatusEntered(CopyType.PRINCIPAL);
			newFolder.setOpenDate(new LocalDate(2010, 1, 1));
			newFolder.setFilingSpaceEntered(administrativeUnit.getFilingSpaces().get(0));
			newFolder.setAdministrativeUnitEntered(administrativeUnit);

			setRecordMetadatas(newFolderRecord, folderSchema, Octets.octets(sizeInOctets), addedFolder + i);
			records.add(newFolderRecord);
		}
		Transaction transaction = new Transaction(records);
		transaction.setRecordFlushing(recordsFlushing);
		getRecordServices().execute(transaction);
	}

	void doUpdateRecords(HttpServletRequest request, HttpServletResponse response, int serviceCall)
			throws ServletException, IOException, RecordServicesException {

		MetadataSchemaTypes schemaTypes = getSchemaTypes(COLLECTION);
		MetadataSchema schema = schemaTypes.getSchema("folder_default");
		int sizeInOctets = getRequiredIntegerParameter(request, SIZE_IN_OCTETS_PARAMETER);
		int qty = getRequiredIntegerParameter(request, QUANTITY_PARAMETER);
		RecordsFlushing recordsFlushing = getRecordsFlushing(request);

		List<Record> records = getRandomFolders(qty, serviceCall);
		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			setRecordMetadatas(record, schema, Octets.octets(sizeInOctets), serviceCall + i);
		}
		Transaction transaction = new Transaction(records);
		transaction.setRecordFlushing(recordsFlushing);
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		getRecordServices().execute(transaction);

	}

	RecordsFlushing getRecordsFlushing(HttpServletRequest request) {
		int flushWithin = getRequiredIntegerParameter(request, FLUSH_WITHIN_PARAMETER);
		return flushWithin == 0 ? RecordsFlushing.NOW : RecordsFlushing.WITHIN_MILLISECONDS(flushWithin);
	}

	private void setRecordMetadatas(Record record, MetadataSchema schema, Octets octets, int counter) {
		Metadata description = schema.getMetadata("description");

		RandomWordsIterator wordsIterator = setup.getRandomWordsIterator(counter);

		record.set(Schemas.TITLE, wordsIterator.nextWords(8));
		record.set(description, wordsIterator.nextWordsOfLength(octets));
	}

	void doAddContent(HttpServletRequest request, HttpServletResponse response, int serviceCall)
			throws ServletException, IOException {

		IOServices ioServices = getIOServices();
		RecordsFlushing recordsFlushing = getRecordsFlushing(request);
		User user = getUserServices().getUserInCollection("admin", COLLECTION);

		List<Record> records = getRandomFolders(1, serviceCall);
		if (!records.isEmpty()) {

			ContentManager contentManager = getContentServices();
			MetadataSchemaTypes types = getSchemaTypes(COLLECTION);
			MetadataSchema documentSchema = types.getSchema(Document.DEFAULT_SCHEMA);
			Document document = new Document(getRecordServices().newRecordWithSchema(documentSchema), types);

			int qtyOfFiles = allPdfFiles.size();
			int index = setup.getRandom().nextInt(qtyOfFiles);
			File file = allPdfFiles.get(index);
			BufferedInputStream inputStream = new BufferedInputStream(ioServices.newFileInputStream(file, CONTENT_TO_ADD_STREAM));
			ContentVersionDataSummary dataSummary = getContentServices().upload(inputStream);
			document.setTitle(file.getName());
			document.setContent(contentManager.createMajor(user, file.getName(), dataSummary));
			document.setFolder(records.get(0).getId());

			Transaction transaction = new Transaction(asList(document.getWrappedRecord()));
			transaction.setRecordFlushing(recordsFlushing);
			transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
			try {
				getRecordServices().execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

			IOUtils.closeQuietly(inputStream);
		}
	}

	List<Record> getRandomFolders(int qty, int counter) {
		MetadataSchema folderSchema = getMetadataSchemasManager().getSchemaTypes(COLLECTION).getSchema("folder_default");
		Metadata categoryMetadata = folderSchema.getMetadata(Folder.CATEGORY_ENTERED);
		SearchServices searchServices = getSearchServices();
		String categoryId = level1And2CategoryIds.get(counter % level1And2CategoryIds.size());
		LogicalSearchCondition condition = from(folderSchema).where(categoryMetadata).isEqualTo(categoryId);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setNumberOfRows(qty);
		return searchServices.search(query);
	}

	int getRequiredIntegerParameter(HttpServletRequest request, String name) {
		String value = getRequiredParameter(request, name);
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw new BenchmarkWebServiceRuntimeException_ParameterInvalid(name);
		}
	}

	int getRequiredIntegerConfig(ServletConfig config, String name) {
		String value = getRequiredConfig(config, name);
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw new BenchmarkWebServiceRuntimeException_ConfigInvalid(name);
		}
	}

	String getRequiredParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (StringUtils.isBlank(value)) {
			throw new BenchmarkWebServiceRuntimeException_ParameterRequired(name);
		}
		return value;
	}

	String getRequiredConfig(ServletConfig config, String name) {
		String value = config.getInitParameter(name);
		if (StringUtils.isBlank(value)) {
			throw new BenchmarkWebServiceRuntimeException_ConfigRequired(name);
		}
		return value;
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

	IOServices getIOServices() {
		return getConstellioFactories().getModelLayerFactory().getIOServicesFactory().newIOServices();
	}

	CollectionsManager getCollectionsServices() {
		return getConstellioFactories().getAppLayerFactory().getCollectionsManager();
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

	TaxonomiesManager getTaxonomiesManager() {
		return getConstellioFactories().getModelLayerFactory().getTaxonomiesManager();
	}

	TaxonomiesSearchServices getTaxonomiesSearchServices() {
		return getConstellioFactories().getModelLayerFactory().newTaxonomiesSearchService();
	}

	UserServices getUserServices() {
		return getConstellioFactories().getModelLayerFactory().newUserServices();
	}

	public BenchmarkWebServiceSetup newTestSetup() {
		return new BenchmarkWebServiceSetup();
	}
}
