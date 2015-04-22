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
package com.constellio.app.ui.framework.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class RecordLookupInputDataProviderAcceptTest extends ConstellioTest {

	String ua_record1 = "ua_record1";
	String ua_record2 = "ua_record2";
	String ua_record3 = "ua_record3";
	String ua_record4 = "ua_record4";
	String ua_record5 = "ua_record5";
	String ua_record6 = "ua_record6";
	String ua_record7 = "ua_record7";

	String c_record1 = "c_record1";
	String c_record2 = "c_record2";
	String c_record3 = "c_record3";
	String c_record4 = "c_record4";
	String c_record5 = "c_record5";
	String c_record6 = "c_record6";
	String c_record7 = "c_record7";

	String s_record1 = "s_record1";
	String s_record2 = "s_record2";
	String s_record3 = "s_record3";
	String s_record4 = "s_record4";
	String s_record5 = "s_record5";
	String s_record6 = "s_record6";
	String s_record7 = "s_record7";

	RMSchemasRecordsServices schemas;
	RecordTextInputDataProvider dataProvider;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection, Arrays.asList("fr", "en")).withConstellioRMModule().withAllTestUsers()
				.andUsersWithReadAccess(dakota);
		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		Transaction transaction = new Transaction();

		transaction.add(schemas.newAdministrativeUnitWithId(ua_record1).setCode("10").setTitle("Gestion DocuLibre"));
		transaction.add(schemas.newAdministrativeUnitWithId(ua_record2).setCode("11").setTitle("Direction générale"));
		transaction.add(schemas.newAdministrativeUnitWithId(ua_record3).setCode("20").setTitle("Ressources humaines"));
		transaction.add(schemas.newAdministrativeUnitWithId(ua_record4).setCode("30").setTitle("Ressources financières"));
		transaction.add(schemas.newAdministrativeUnitWithId(ua_record5).setCode("S1").setTitle("Secrétariat"));
		transaction.add(schemas.newAdministrativeUnitWithId(ua_record6).setCode("AB-20").setTitle("Records management"));

		transaction.add(schemas.newCategoryWithId(c_record1).setCode("10").setTitle("Gestion DocuLibre"));
		transaction.add(schemas.newCategoryWithId(c_record2).setCode("11").setTitle("Direction générale"));
		transaction.add(schemas.newCategoryWithId(c_record3).setCode("20").setTitle("Ressources humaines"));
		transaction.add(schemas.newCategoryWithId(c_record4).setCode("30").setTitle("Ressources financières"));
		transaction.add(schemas.newCategoryWithId(c_record5).setCode("S1").setTitle("Secrétariat"));
		transaction.add(schemas.newCategoryWithId(c_record6).setCode("AB-20").setTitle("Records management"));

		transaction.add(schemas.newFilingSpaceWithId(s_record1).setCode("10").setTitle("Gestion DocuLibre"));
		transaction.add(schemas.newFilingSpaceWithId(s_record2).setCode("11").setTitle("Direction générale"));
		transaction.add(schemas.newFilingSpaceWithId(s_record3).setCode("20").setTitle("Ressources humaines"));
		transaction.add(schemas.newFilingSpaceWithId(s_record4).setCode("30").setTitle("Ressources financières"));
		transaction.add(schemas.newFilingSpaceWithId(s_record5).setCode("S1").setTitle("Secrétariat"));
		transaction.add(schemas.newFilingSpaceWithId(s_record6).setCode("AB-20").setTitle("Records management"));
		transaction.add(schemas.newFilingSpaceWithId(s_record7).setCode("A").setTitle("Room A"));

		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	@Test
	public void whenSearchingAdministrativeUnitThenValidSearchResults()
			throws Exception {

		User dakota = getModelLayerFactory().newUserServices().getUserInCollection(this.dakota, zeCollection);

		RecordTextInputDataProvider dataProvider = dataProviderForSchemaType(AdministrativeUnit.SCHEMA_TYPE);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "1", 0, 10))).containsOnly(ua_record1, ua_record2);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "10", 0, 10))).containsOnly(ua_record1);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "R", 0, 10)))
				.containsOnly(ua_record3, ua_record4, ua_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Res", 0, 10))).containsOnly(ua_record3, ua_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Secre", 0, 10))).containsOnly(ua_record5);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Rés", 0, 10))).containsOnly(ua_record3, ua_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "m", 0, 10))).containsOnly(ua_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "man", 0, 10))).containsOnly(ua_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "ge", 0, 10))).containsOnly(ua_record1, ua_record2);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "AB-20", 0, 10))).containsOnly(ua_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "AB-20 ", 0, 10))).containsOnly(ua_record6);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "financiè", 0, 10))).containsOnly(ua_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Ressources financières", 0, 10)))
				.containsOnly(ua_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Ressources financieres", 0, 10)))
				.containsOnly(ua_record4);

	}

	@Test
	public void whenSearchingCategoriesThenValidSearchResults()
			throws Exception {

		User dakota = getModelLayerFactory().newUserServices().getUserInCollection(this.dakota, zeCollection);

		RecordTextInputDataProvider dataProvider = dataProviderForSchemaType(Category.SCHEMA_TYPE);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "1", 0, 10))).containsOnly(c_record1, c_record2);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "10", 0, 10))).containsOnly(c_record1);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "R", 0, 10)))
				.containsOnly(c_record3, c_record4, c_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Res", 0, 10))).containsOnly(c_record3, c_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Secre", 0, 10))).containsOnly(c_record5);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Rés", 0, 10))).containsOnly(c_record3, c_record4);
	}

	@Test
	public void whenSearchingCategoriesWithoutAccessThenValidSearchResults()
			throws Exception {

		User edouard = getModelLayerFactory().newUserServices().getUserInCollection(this.edouard, zeCollection);

		RecordTextInputDataProvider dataProvider = dataProviderForSchemaType(Category.SCHEMA_TYPE);

		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "1", 0, 10))).containsOnly(c_record1, c_record2);
		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "10", 0, 10))).containsOnly(c_record1);
		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "R", 0, 10)))
				.containsOnly(c_record3, c_record4, c_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "Res", 0, 10))).containsOnly(c_record3, c_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "Secre", 0, 10))).containsOnly(c_record5);
		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "Rés", 0, 10))).containsOnly(c_record3, c_record4);
	}

	@Test
	public void whenSearchingAdministrativeUnitWithUserWithoutAccessThenFindNoResults()
			throws Exception {

		User edouard = getModelLayerFactory().newUserServices().getUserInCollection(this.edouard, zeCollection);

		RecordTextInputDataProvider dataProvider = dataProviderForSchemaType(AdministrativeUnit.SCHEMA_TYPE);

		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "1", 0, 10))).isEmpty();
		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "10", 0, 10))).isEmpty();
		assertThat(idsOf(dataProvider.searchAutocompletField(edouard, "R", 0, 10))).isEmpty();

	}

	@Test
	public void whenSearchingFilingSpacesThenValidSearchResults()
			throws Exception {

		User dakota = getModelLayerFactory().newUserServices().getUserInCollection(this.dakota, zeCollection);

		RecordTextInputDataProvider dataProvider = dataProviderForSchemaType(FilingSpace.SCHEMA_TYPE);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "1", 0, 10))).containsOnly(s_record1, s_record2);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "10", 0, 10))).containsOnly(s_record1);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "R", 0, 10)))
				.containsOnly(s_record3, s_record4, s_record6, s_record7);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Res", 0, 10))).containsOnly(s_record3, s_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Secre", 0, 10))).containsOnly(s_record5);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Rés", 0, 10))).containsOnly(s_record3, s_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "m", 0, 10))).containsOnly(s_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "man", 0, 10))).containsOnly(s_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "ge", 0, 10))).containsOnly(s_record1, s_record2);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "AB-20", 0, 10))).containsOnly(s_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "AB-20 ", 0, 10))).containsOnly(s_record6);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "A ", 0, 10))).containsOnly(s_record7);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Room A", 0, 10))).containsOnly(s_record7);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "financiè", 0, 10))).containsOnly(s_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Ressources financières", 0, 10)))
				.containsOnly(s_record4);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "Ressources financieres", 0, 10)))
				.containsOnly(s_record4);

	}

	@Test
	public void whenSearchingFilingSpacesWithUserWithoutAccessesThenAllValidSearchResults()
			throws Exception {

		User dakota = getModelLayerFactory().newUserServices().getUserInCollection(this.dakota, zeCollection);

		RecordTextInputDataProvider dataProvider = dataProviderForSchemaType(FilingSpace.SCHEMA_TYPE);

		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "1", 0, 10))).containsOnly(s_record1, s_record2);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "10", 0, 10))).containsOnly(s_record1);
		assertThat(idsOf(dataProvider.searchAutocompletField(dakota, "R", 0, 10)))
				.containsOnly(s_record3, s_record4, s_record6, s_record7);
	}

	//-------------------------------------------

	private RecordTextInputDataProvider dataProviderForSchemaType(String schemaType) {
		SessionContext sessionContext = FakeSessionContext.dakotaInCollection(zeCollection);
		return new RecordTextInputDataProvider(getConstellioFactories(), sessionContext, schemaType);
	}

	private List<String> idsOf(SPEQueryResponse response) {
		List<String> ids = new ArrayList<>();

		for (Record result : response.getRecords()) {
			ids.add(result.getId());
		}

		return ids;
	}
}
