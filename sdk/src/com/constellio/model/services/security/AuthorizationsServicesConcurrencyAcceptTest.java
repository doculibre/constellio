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
package com.constellio.model.services.security;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.Records;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.setups.Users;

@LoadTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorizationsServicesConcurrencyAcceptTest extends ConstellioTest {

	String ZE_ROLE = "zeRoleCode";

	SecurityAcceptanceTestSetup setup = new SecurityAcceptanceTestSetup(zeCollection);

	MetadataSchemasManager schemasManager;
	SearchServices searchServices;
	RecordServices recordServices;
	TaxonomiesManager taxonomiesManager;
	AuthorizationsServices authorizationsServices;

	Records records;
	Users users = new Users();
	RolesManager roleManager;

	@Before
	public void setUp()
			throws Exception {
		recordServices = getModelLayerFactory().newRecordServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		searchServices = getModelLayerFactory().newSearchServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		roleManager = getModelLayerFactory().getRolesManager();

		givenCollection("zeCollection");
		defineSchemasManager().using(setup);

		taxonomiesManager.addTaxonomy(setup.getTaxonomy1(), schemasManager);
		taxonomiesManager.addTaxonomy(setup.getTaxonomy2(), schemasManager);

		records = setup.givenRecords(recordServices);
		users.setUp(getModelLayerFactory().newUserServices());
	}

	@Test
	public void test01()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test02()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test03()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test04()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test05()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test06()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test07()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test08()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test09()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test10()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test11()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test12()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test13()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test14()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test15()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test16()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test17()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test18()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test19()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test20()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test21()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test22()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test23()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test24()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test25()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test26()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test27()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test28()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test29()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test30()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test31()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test32()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test33()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test34()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test35()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test36()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test37()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test38()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test39()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test40()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test41()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test42()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test43()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test44()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test45()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test46()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test47()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test48()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test49()
			throws Exception {
		doTheTest();
	}

	@Test
	public void test50()
			throws Exception {
		doTheTest();
	}

	private void doTheTest()
			throws Exception {
		taxonomiesManager.setPrincipalTaxonomy(setup.getTaxonomy1(), schemasManager);

		List<String> roles = Arrays.asList(Role.READ, Role.WRITE, Role.DELETE);

		addAuthorizationWithoutDetaching(roles, Arrays.asList(users.legendsIn(zeCollection).getId()),
				Arrays.asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, Arrays.asList(users.heroesIn(zeCollection).getId()),
				Arrays.asList(records.folder2().getId()));
		addAuthorizationWithoutDetaching(roles, Arrays.asList(users.gandalfIn(zeCollection).getId()),
				Arrays.asList(records.taxo1_category1().getId()));

		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.gandalfIn(zeCollection));

		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId(), records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
	}

	private void addZeUltimateAuthorization()
			throws RolesManagerRuntimeException {
		// This authorization is only valid for Taxo1.
		// This problem will fix itself when the collections are added.

		List<String> roles = Arrays.asList(Role.READ, Role.WRITE, Role.DELETE);

		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, null, null, zeCollection);
		List<String> grantedToPrincipals = Arrays.asList(users.chuckNorrisIn(zeCollection).getId());
		List<String> grantedOnRecords = Arrays.asList(records.taxo1_fond1().getId());

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);
	}

	private List<Record> findRecords(LogicalSearchCondition condition, User user) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		query.filteredWithUser(user);
		return searchServices.search(query);
	}

	private List<String> findAllFoldersAndDocumentsWithWritePermission(User user) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(setup.folderSchema.instance()).returnAll());
		query.filteredWithUserWrite(user);
		List<String> recordIds = searchServices.searchRecordIds(query);

		query.setCondition(from(setup.documentSchema.instance()).returnAll());
		recordIds.addAll(searchServices.searchRecordIds(query));
		return recordIds;
	}

	private List<String> findAllFoldersAndDocuments(User user) {
		List<String> recordIds = new ArrayList<>();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(setup.folderSchema.instance()).returnAll());
		query.filteredWithUser(user);
		recordIds.addAll(searchServices.searchRecordIds(query));

		query = new LogicalSearchQuery();
		query.setCondition(from(setup.documentSchema.instance()).returnAll());
		query.filteredWithUser(user);
		recordIds.addAll(searchServices.searchRecordIds(query));
		return recordIds;
	}

	private Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, null, null, zeCollection);

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);
		return authorization;
	}

	private Authorization addAuthorizationDetaching(List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.DETACH, null);
		return authorization;
	}

	private void addAuthorizationForDates(List<String> roles, List<String> grantedToPrincipals, List<String> grantedOnRecords,
			LocalDate startDate, LocalDate endDate) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, startDate, endDate, zeCollection);

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);
	}

	private void modifyAuthorizationWithoutDetaching(Authorization authorization) {
		authorizationsServices.modify(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);
	}

}