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
package com.constellio.model.services.taxonomies;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;

public class TaxonomiesSearchServices_VisibleTreesAcceptTest extends ConstellioTest {

	String subFolderId;

	User alice;
	DecommissioningService decommissioningService;
	TaxonomiesSearchServices service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	MetadataSchemasManager manager;
	RecordServices recordServices;
	String document1InA16, document2InA16, document3InA16;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		service = getModelLayerFactory().newTaxonomiesSearchService();
		decommissioningService = new DecommissioningService(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential userCredential = userServices.getUserCredential(aliceWonderland);
		userServices.addUserToCollection(userCredential, zeCollection);
		alice = userServices.getUserInCollection(aliceWonderland, zeCollection);
		manager = getModelLayerFactory().getMetadataSchemasManager();

		DecommissioningService service = new DecommissioningService(zeCollection, getModelLayerFactory());

		Folder subfolder = service.newSubFolderIn(records.getFolder_A16());
		subfolder.setTitle("Sous-dossier");
		recordServices.add(subfolder);
		subFolderId = subfolder.getId();

		List<String> documentsInA16 = getFolderDocuments(records.folder_A16);
		document1InA16 = documentsInA16.get(0);
		document2InA16 = documentsInA16.get(1);
		document3InA16 = documentsInA16.get(2);

		for (String documentId : getFolderDocuments(records.folder_A17)) {
			Record document = recordServices.getDocumentById(documentId);
			recordServices.logicallyDelete(document, User.GOD);
		}

		for (String documentId : getFolderDocuments(records.folder_A18)) {
			Record document = recordServices.getDocumentById(documentId);
			recordServices.logicallyDelete(document, User.GOD);
		}
	}

	private List<String> getFolderDocuments(String id) {
		return getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery()
				.sortAsc(Schemas.TITLE).setCondition(from(rm.documentSchemaType()).where(rm.documentFolder()).isEqualTo(id)));
	}

	//	@Test
	//	public void givenUserCreateNewTaxonomyAndAssignRecordsToItThenRecordsVisibleInTheTaxonomyTree()
	//			throws Exception {
	//
	//		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));
	//
	//		ValueListServices services = new ValueListServices(getModelLayerFactory(), zeCollection);
	//		String type = services.createTaxonomy("ZE").getSchemaTypes().get(0);
	//
	//		MetadataSchemaTypesBuilder types = manager.modifyTo(zeCollection);
	//		types.getSchema(Folder.SCHEMA_TYPE).create("ZeMetadata").defineTaxonomyRelationshipToType(types.getSchemaType(type));
	//		manager.saveUpdateSchemaTypes(types);
	//
	//		HierarchicalValueListItem item = rm.newHierarchicalValueListItem(type + "_default");
	//
	//	}

	@Test
	public void whenDakotaIsNavigatingATaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.categoryId_X)
				.has(recordsInOrder(records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.categoryId_X100)
				.has(recordsInOrder(records.categoryId_X110, records.categoryId_X120, records.folder_A16, records.folder_A17,
						records.folder_A18, records.folder_B06))
				.has(recordsWithChildren(records.categoryId_X110, records.categoryId_X120, records.folder_A16,
						records.folder_B06));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.folder_A16)
				.has(recordsInOrder(document1InA16, document2InA16, document3InA16, subFolderId))
				.has(noRecordsWithChildren());
	}

	@Test
	public void whenAdminIsNavigatingATaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X)
				.has(recordsInOrder(records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06"));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z)
				.has(recordsInOrder(records.categoryId_Z100))
				.has(recordsWithChildren(records.categoryId_Z100));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z100)
				.has(recordsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(recordsWithChildren(records.categoryId_Z110, records.categoryId_Z120));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z110)
				.has(recordsInOrder(records.categoryId_Z112))
				.has(recordsWithChildren(records.categoryId_Z112));

	}

	@Test
	public void whenNavigatingByIntervalThenGetGoodResults()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 0, 2)
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 0, 1)
				.has(recordsInOrder(records.categoryId_X))
				.has(recordsWithChildren(records.categoryId_X));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 1, 1)
				.has(recordsInOrder(records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_Z));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06"));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 10)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06"));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 7)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06"));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 3)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16"));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 1, 4)
				.has(recordsInOrder("categoryId_X120", "A16", "A17", "A18"))
				.has(recordsWithChildren("categoryId_X120", "A16"));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.folder_A16, 0, 5)
				.has(recordsInOrder(document1InA16, document2InA16, document3InA16, subFolderId))
				.has(noRecordsWithChildren());

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.folder_A16, 0, 1)
				.has(recordsInOrder(document1InA16))
				.has(noRecordsWithChildren());

	}
	// -------

	private Condition<? super List<TaxonomySearchRecord>> recordsInOrder(String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> values) {
				List<String> valueIds = new ArrayList<>();
				for (TaxonomySearchRecord value : values) {
					valueIds.add(value.getRecord().getId());
				}
				assertThat(valueIds).isEqualTo(idsList);
				return true;
			}
		};
	}

	private Condition<? super List<TaxonomySearchRecord>> noRecordsWithChildren() {
		return recordsWithChildren();
	}

	private Condition<? super List<TaxonomySearchRecord>> recordsWithChildren(String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> values) {
				List<String> valueIds = new ArrayList<>();
				for (TaxonomySearchRecord value : values) {
					if (value.hasChildren()) {
						valueIds.add(value.getRecord().getId());
					}
				}
				assertThat(valueIds).isEqualTo(idsList);
				return true;
			}
		};
	}

	private Condition<? super List<TaxonomySearchRecord>> validOrder() {
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> values) {

				List<Record> actualRecords = new ArrayList<>();
				List<Record> recordsInExpectedOrder = new ArrayList<>();

				for (TaxonomySearchRecord value : values) {
					actualRecords.add(value.getRecord());
					recordsInExpectedOrder.add(value.getRecord());
				}

				final List<String> typesOrder = asList(Category.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						ContainerRecord.SCHEMA_TYPE, Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE);

				Collections.sort(recordsInExpectedOrder, new Comparator<Record>() {
					@Override
					public int compare(Record r1, Record r2) {

						int r1TypeIndex = typesOrder.indexOf(new SchemaUtils().getSchemaTypeCode(r1.getSchemaCode()));
						int r2TypeIndex = typesOrder.indexOf(new SchemaUtils().getSchemaTypeCode(r2.getSchemaCode()));

						if (r1TypeIndex != r2TypeIndex) {
							return new Integer(r1TypeIndex).compareTo(r2TypeIndex);

						} else {
							String code1 = r1.get(Schemas.CODE);
							String code2 = r2.get(Schemas.CODE);
							if (code1 != null && code2 != null) {
								return code1.compareTo(code2);

							} else if (code1 != null && code2 == null) {
								return 1;
							} else if (code1 == null && code2 != null) {
								return -1;
							} else {

								String title1 = r1.get(Schemas.TITLE);
								String title2 = r2.get(Schemas.TITLE);
								if (title1 == null) {
									return -1;
								} else {
									return title1.compareTo(title2);
								}
							}

						}

					}
				});

				assertThat(actualRecords).isEqualTo(recordsInExpectedOrder);
				return true;
			}
		};
	}

	private Condition<? super List<TaxonomySearchRecord>> unlinkable(final String... ids) {
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> records) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : records) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					if (foundRecord == null) {
						throw new RuntimeException("Record not found : " + id);
					} else {
						assertThat(foundRecord.isLinkable()).isFalse();
					}

				}

				return true;
			}
		};
	}

	private Condition<? super List<TaxonomySearchRecord>> linkable(final String... ids) {
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> records) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : records) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					if (foundRecord == null) {
						throw new RuntimeException("Record not found : " + id);
					} else {
						assertThat(foundRecord.isLinkable()).isTrue();
					}

				}

				return true;
			}
		};
	}

	private void givenUserHasReadAccessTo(String... ids) {

		Authorization authorization = new Authorization();
		authorization.setDetail(AuthorizationDetails.create("zeAuthorization", asList(Role.READ), zeCollection));
		authorization.setGrantedOnRecords(asList(ids));
		authorization.setGrantedToPrincipals(asList(alice.getId()));
		getModelLayerFactory().newAuthorizationsServices().add(
				authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		alice = getModelLayerFactory().newUserServices().getUserInCollection(aliceWonderland, zeCollection);
		System.out.println(alice.getTokens());
		System.out.println(alice.getTokens());
	}

	private ConditionTemplate withoutFilters = null;

	private ListAssert<TaxonomySearchRecord> assertThatRootWhenUserNavigateUsingPlanTaxonomy(User user) {
		return assertThatRootWhenUserNavigateUsingPlanTaxonomy(user, 0, Integer.MAX_VALUE);
	}

	private ListAssert<TaxonomySearchRecord> assertThatRootWhenUserNavigateUsingPlanTaxonomy(User user, int start, int rows) {
		LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
				user, zeCollection, CLASSIFICATION_PLAN, new TaxonomiesSearchOptions().setStartRow(start).setRows(rows));

		if (rows == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response.getRecords());
	}

	private ListAssert<TaxonomySearchRecord> assertThatChildWhenUserNavigateUsingPlanTaxonomy(User user, String category) {
		return assertThatChildWhenUserNavigateUsingPlanTaxonomy(user, category, 0, Integer.MAX_VALUE);
	}

	private ListAssert<TaxonomySearchRecord> assertThatChildWhenUserNavigateUsingPlanTaxonomy(User user, String category,
			int start, int rows) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service
				.getVisibleChildConceptResponse(user, CLASSIFICATION_PLAN, inRecord,
						new TaxonomiesSearchOptions().setStartRow(start).setRows(rows));

		if (rows == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response.getRecords());
	}

}
