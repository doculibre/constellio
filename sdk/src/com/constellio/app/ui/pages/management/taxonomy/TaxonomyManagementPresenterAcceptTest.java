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
package com.constellio.app.ui.pages.management.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class TaxonomyManagementPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records;
	@Mock TaxonomyManagementView view;
	@Mock SessionContext sessionContext;
	TaxonomyManagementPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);

		presenter = new TaxonomyManagementPresenter(view);
	}

	@Test
	public void whenLoadingForRootThenNoCurrentConceptAndRightValues() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		String params = ParamUtils.addParams(null, paramsMap);

		presenter.forParams(params);

		assertThat(presenter.conceptId).isNull();
		assertThat(presenter.taxonomy.getCode()).isEqualTo(RMTaxonomies.CLASSIFICATION_PLAN);
	}

	@Test
	public void whenLoadingForLevelOneConceptThenAllValuesAreRight() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, records.categoryId_X);
		String params = ParamUtils.addParams(null, paramsMap);

		presenter.forParams(params);

		assertThat(presenter.conceptId).isEqualTo(records.categoryId_X);
		assertThat(presenter.taxonomy.getCode()).isEqualTo(RMTaxonomies.CLASSIFICATION_PLAN);
	}

	@Test
	public void whenGettingDataProvidersThenExpectedRecordsProvided() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		List<RecordVODataProvider> dataProviders = presenter.getDataProviders();

		RecordVODataProvider dataProvider = dataProviders.get(0);
		assertThat(dataProvider.getSchema().getCode()).isEqualTo(Category.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider)).containsOnly(records.categoryId_X, records.categoryId_Z);
	}

	@Test
	public void whenGetRetentionRulesThenOk()
			throws Exception {

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.ADMINISTRATIVE_UNITS);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, "unitId_12");
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		assertThat(presenter.getRetentionRules()).hasSize(3);
		assertThat(presenter.getRetentionRules()).containsOnly("ruleId_1", "ruleId_2", "ruleId_4");
	}

	@Test
	public void whenGetAdministrativeUnitsFoldersDataProviderAndNumberOfFoldersThenOk()
			throws Exception {

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.ADMINISTRATIVE_UNITS);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, "unitId_12");
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		assertThat(presenter.getNumberOfFolders()).isEqualTo("10");
		assertThat(presenter.newAdministrativeUnitsFoldersDataProvider().size()).isEqualTo(10);

	}

	@Test
	public void whenGetClassificationPlansFoldersDataProviderAndNumberOfFoldersThenOk()
			throws Exception {

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, "categoryId_Z112");
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		assertThat(presenter.getNumberOfFolders()).isEqualTo("5");
		assertThat(presenter.newClassificationPlansFoldersDataProvider().size()).isEqualTo(5);

	}

	private List<String> getRecordIdsFromDataProvider(RecordVODataProvider dataProvider) {
		List<String> IDs = new ArrayList<>();
		for (RecordVO recordVO : dataProvider.listRecordVOs(0, dataProvider.size())) {
			IDs.add(recordVO.getId());
		}
		return IDs;
	}

}
