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

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class BenchmarkWebServiceAcceptTest extends ConstellioTest {

	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;
	@Mock ServletConfig config;
	BenchmarkWebService benchmarkWebService;
	File contentSamplesTempFolder;
	private boolean foldersWithBananaTitle;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(ConstellioPluginManager.class);
		List<InstallableModule> modules = asList((InstallableModule) new ConstellioRMModule());
		when(getAppLayerFactory().getPluginManager().getPlugins(InstallableModule.class)).thenReturn(modules);

		System.setProperty("benchmarkServiceEnabled", "true");
		benchmarkWebService = new BenchmarkWebService();

		contentSamplesTempFolder = newTempFolder();
		FileUtils.write(new File(contentSamplesTempFolder, "content.txt"), "The content data");

	}

	@Test
	public void whenBenchmarkAreCalculatedThenServicesAreWorkingCorrectly()
			throws Exception {

		assertThat(getModelLayerFactory().getCollectionsListManager().getCollections()).isEmpty();

		when(config.getInitParameter(BenchmarkWebService.NUMBER_OF_ROOT_CATEGORIES)).thenReturn("1");
		when(config.getInitParameter(BenchmarkWebService.NUMBER_OF_LEVEL1_CATEGORIES)).thenReturn("2");
		when(config.getInitParameter(BenchmarkWebService.NUMBER_OF_LEVEL2_CATEGORIES)).thenReturn("4");
		when(config.getInitParameter(BenchmarkWebService.NUMBER_OF_LEVEL1_ADMINISTRATIVE_UNITS)).thenReturn("5");
		when(config.getInitParameter(BenchmarkWebService.NUMBER_OF_LEVEL2_ADMINISTRATIVE_UNITS)).thenReturn("10");
		when(config.getInitParameter(BenchmarkWebService.NUMBER_OF_FOLDERS)).thenReturn("200");
		when(config.getInitParameter(BenchmarkWebService.CONTENT_SAMPLES)).thenReturn(contentSamplesTempFolder.getAbsolutePath());
		benchmarkWebService.init(config);

		int expectedInitialNumberOfFolders = 200;

		assertThat(getModelLayerFactory().getCollectionsListManager().getCollections()).containsOnly(
				BenchmarkWebService.COLLECTION);
		assertThat(countFolders()).isEqualTo(200);
		assertThat(countCategories()).isEqualTo(1 + (1 * 2) + (1 * 2 * 4));
		assertThat(countClassificationStations()).isEqualTo(55);
		assertThatEachLevel1And2CategoriesHaveNumberOfFolders(20);
		assertThatEachLevel2AdministrativeUnitsHaveNumberOfFolders(4);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_ONE_PERCENT_ACCESS, 4);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_FIVE_PERCENT_ACCESS, 12);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_TEN_PERCENT_ACCESS, 20);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_TWENTY_PERCENT_ACCESS, 40);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_FIFTY_PERCENT_ACCESS, 100);

		add100FoldersOf200Octets();
		getModelLayerFactory().newRecordServices().flush();
		assertThat(countFolders()).isEqualTo(100 + expectedInitialNumberOfFolders);
		assertThatEachLevel1And2CategoriesHaveNumberOfFolders(30);
		assertThatEachLevel2AdministrativeUnitsHaveNumberOfFolders(6);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_ONE_PERCENT_ACCESS, 6);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_FIVE_PERCENT_ACCESS, 18);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_TEN_PERCENT_ACCESS, 30);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_TWENTY_PERCENT_ACCESS, 60);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_FIFTY_PERCENT_ACCESS, 150);

		add150FoldersOf200Octets();
		assertThat(countFolders()).isEqualTo(250 + expectedInitialNumberOfFolders);
		assertThatEachLevel1And2CategoriesHaveNumberOfFolders(45);
		assertThatEachLevel2AdministrativeUnitsHaveNumberOfFolders(9);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_ONE_PERCENT_ACCESS, 9);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_FIVE_PERCENT_ACCESS, 27);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_TEN_PERCENT_ACCESS, 45);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_TWENTY_PERCENT_ACCESS, 90);
		assertThatUserHasAccessToNumberOfFolders(BenchmarkWebServiceSetup.USER_WITH_FIFTY_PERCENT_ACCESS, 225);

		changeAllFoldersTitleToBanana();
		assertThat(getFoldersWithBananaTitle()).isEqualTo(250 + expectedInitialNumberOfFolders);
		update2Folders();
		assertThat(getFoldersWithBananaTitle()).isEqualTo(248 + expectedInitialNumberOfFolders);

	}

	private void assertThatEachLevel1And2CategoriesHaveNumberOfFolders(int expectedNumberOfFolders) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchema folderSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(Folder.DEFAULT_SCHEMA);
		MetadataSchema schema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(Category.DEFAULT_SCHEMA);
		Metadata parentMetadata = schema.getMetadata(Category.PARENT);
		Metadata folderCategoryMetadata = folderSchema.getMetadata(Folder.CATEGORY_ENTERED);

		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		LogicalSearchCondition allRecords = LogicalSearchQueryOperators.from(schema).where(parentMetadata).isNotNull();
		for (Record record : searchServices.search(new LogicalSearchQuery(allRecords))) {

			LogicalSearchCondition allFolders = LogicalSearchQueryOperators.from(folderSchema).where(folderCategoryMetadata)
					.isEqualTo(record);
			assertThat(searchServices.getResultsCount(allFolders)).isEqualTo(expectedNumberOfFolders);
		}
	}

	private void assertThatUserHasAccessToNumberOfFolders(String username, long expectedNumberOfFolders) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		UserServices userServices = getModelLayerFactory().newUserServices();
		User user = userServices.getUserInCollection(username, BenchmarkWebService.COLLECTION);
		MetadataSchema folderSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(Folder.DEFAULT_SCHEMA);
		LogicalSearchCondition allFolders = LogicalSearchQueryOperators.from(folderSchema).returnAll();
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(allFolders).filteredWithUser(user)))
				.isEqualTo(expectedNumberOfFolders);
	}

	private void assertThatEachLevel2AdministrativeUnitsHaveNumberOfFolders(int expectedNumberOfFolders) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchema folderSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(Folder.DEFAULT_SCHEMA);
		MetadataSchema administrativeUnitSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(AdministrativeUnit.DEFAULT_SCHEMA);
		Metadata folderAdministrativeUnit = folderSchema.getMetadata(Folder.ADMINISTRATIVE_UNIT);
		Metadata administrativeUnitParent = administrativeUnitSchema.getMetadata(AdministrativeUnit.PARENT);
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		LogicalSearchCondition allLeve2AdministrativeUnitsCondition = LogicalSearchQueryOperators.from(administrativeUnitSchema)
				.where(administrativeUnitParent).isNotNull();
		for (Record record : searchServices.search(new LogicalSearchQuery(allLeve2AdministrativeUnitsCondition))) {
			LogicalSearchCondition allFolders = LogicalSearchQueryOperators.from(folderSchema)
					.where(folderAdministrativeUnit).isEqualTo(record);
			assertThat(searchServices.getResultsCount(allFolders)).isEqualTo(expectedNumberOfFolders);
		}
	}

	private void add150FoldersOf200Octets()
			throws ServletException, IOException {
		request = mock(HttpServletRequest.class);
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn(BenchmarkWebService.ACTION_ADD_FOLDER);
		when(request.getParameter(BenchmarkWebService.FLUSH_WITHIN_PARAMETER)).thenReturn("0");
		when(request.getParameter(BenchmarkWebService.QUANTITY_PARAMETER)).thenReturn("150");
		when(request.getParameter(BenchmarkWebService.SIZE_IN_OCTETS_PARAMETER)).thenReturn("200");
		benchmarkWebService.doGet(request, response);
	}

	private void add100FoldersOf200Octets()
			throws ServletException, IOException {
		request = mock(HttpServletRequest.class);
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn(BenchmarkWebService.ACTION_ADD_FOLDER);
		when(request.getParameter(BenchmarkWebService.FLUSH_WITHIN_PARAMETER)).thenReturn("1000");
		when(request.getParameter(BenchmarkWebService.QUANTITY_PARAMETER)).thenReturn("100");
		when(request.getParameter(BenchmarkWebService.SIZE_IN_OCTETS_PARAMETER)).thenReturn("200");
		benchmarkWebService.doGet(request, response);
	}

	private void update2Folders()
			throws ServletException, IOException {
		request = mock(HttpServletRequest.class);
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn(BenchmarkWebService.ACTION_UPDATE_FOLDERS);
		when(request.getParameter(BenchmarkWebService.FLUSH_WITHIN_PARAMETER)).thenReturn("1000");
		when(request.getParameter(BenchmarkWebService.QUANTITY_PARAMETER)).thenReturn("2");
		when(request.getParameter(BenchmarkWebService.SIZE_IN_OCTETS_PARAMETER)).thenReturn("200");
		benchmarkWebService.doGet(request, response);
		getModelLayerFactory().newRecordServices().flush();
	}

	private void changeAllFoldersTitleToBanana()
			throws RecordServicesException {
		MetadataSchema defaultSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(
						Folder.DEFAULT_SCHEMA);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(defaultSchema).returnAll();
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(new LogicalSearchQuery(condition))) {
			transaction.add(record.set(Schemas.TITLE, "banana"));
		}
		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private long countCategories() {

		MetadataSchema defaultSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(
						Category.DEFAULT_SCHEMA);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(defaultSchema).returnAll();
		return searchServices.getResultsCount(condition);
	}

	private long countClassificationStations() {

		MetadataSchemaType admUnits = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(admUnits).returnAll();
		return searchServices.getResultsCount(condition);
	}

	private long countFolders() {

		MetadataSchema folderSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(Folder.DEFAULT_SCHEMA);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchema).returnAll();
		return searchServices.getResultsCount(condition);
	}

	public long getFoldersWithBananaTitle() {
		MetadataSchema defaultSchema = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(BenchmarkWebService.COLLECTION).getSchema(Folder.DEFAULT_SCHEMA);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(defaultSchema).where(Schemas.TITLE)
				.isEqualTo("banana");
		return searchServices.getResultsCount(condition);
	}
}
