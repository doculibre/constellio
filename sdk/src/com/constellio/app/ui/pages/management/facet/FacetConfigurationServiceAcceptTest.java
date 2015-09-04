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
package com.constellio.app.ui.pages.management.facet;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationPresenter.AvailableFacetFieldMetadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class FacetConfigurationServiceAcceptTest extends ConstellioTest {
	@Mock AddEditFacetConfigurationView view;
	FacetConfigurationPresenterService service;

	RMTestRecords records = new RMTestRecords(zeCollection);
	private SchemasRecordsServices schemasRecords;
	private RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));

		service = spy(new FacetConfigurationPresenterService(getConstellioFactories(),
				FakeSessionContext.adminInCollection(zeCollection)));
		schemasRecords = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		recordServices = schemasRecords.getModelLayerFactory().newRecordServices();

		recordServices.update(records.getFolder_A04().setKeywords(asList("King Dedede", "La passe de la baleine échouée")));
		recordServices.update(records.getFolder_A07().setKeywords(asList("aKeyword")));
	}

	@Test
	public void whenGetAvailableDataStoreCodeThenReturnGoodValues()
			throws Exception {

		List<AvailableFacetFieldMetadata> availableDataStoreCodes = service.getAvailableDataStoreCodes();
		assertThat(availableDataStoreCodes).extracting("code")
				.containsOnlyOnce("createdById_s", "retentionRuleId_s", "keywords_ss", "copyStatus_s", "borrowed_s", "schema_s")
				.doesNotContain(Schemas.TITLE.getDataStoreCode(), Schemas.CREATED_ON.getDataStoreCode(),
						Schemas.TOKENS.getDataStoreCode(), "title_s", "createdOn_dt", "content_s", "tokens_ss", "username_s",
						"description_txt", "pendingAlerts_ss");

	}

	@Test
	public void whenDataStoreCodeDoesntContainsIdThenAllowsLabel()
			throws Exception {
		assertThat(service.isDataStoreCodeSupportingLabelValues("schema_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("createdById_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("retentionRuleId_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("keywords_ss")).isTrue();
		assertThat(service.isDataStoreCodeSupportingLabelValues("copyStatus_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("borrowed_s")).isTrue();

	}

	@Test
	public void whenValidQueryThenEmpty() {
		Map<Integer, Map<String, String>> values = new HashMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("zeLabel", "*:*");

		Map<String, String> map1 = new HashMap<>();
		map1.put("zeLabel", "*:*");

		values.put(1, map);
		values.put(2, map1);

		assertThat(service.getInvalidQuery(values)).hasSize(0);
	}

	@Test
	public void whenInvalidQueryThenListed() {
		Map<Integer, Map<String, String>> values = new HashMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("zeLabel", "thisisinvalid");

		Map<String, String> map1 = new HashMap<>();
		map1.put("zeLabel", "*:*");

		values.put(1, map);
		values.put(2, map1);

		assertThat(service.getInvalidQuery(values)).hasSize(1);
	}

	@Test
	public void whenGetFieldFacetValuesThenObtainValidRecordValues()
			throws Exception {
		assertThat(service.getFieldFacetValues("keywords_ss"))
				.containsOnly("King Dedede", "La passe de la baleine échouée", "aKeyword");
		assertThat(service.getFieldFacetValues("borrowed_s")).containsOnly("__TRUE__", "__FALSE__");

	}

}
