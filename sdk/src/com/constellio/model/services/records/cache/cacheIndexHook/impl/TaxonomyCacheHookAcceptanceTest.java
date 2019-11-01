package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class TaxonomyCacheHookAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void givenUserWithAccessToAnAdministrativeUnit() {



	}

	@Test
	public void validateForAllTestUsersOnAllTestConcepts() {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records).withFoldersAndContainersOfEveryStatus());
		TaxonomyRecordsHookRetriever retriever = getModelLayerFactory().getTaxonomiesSearchServicesCache().getRetriever(zeCollection);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		for (Category category : rm.categoryStream().collect(Collectors.toList())) {
			for (User user : rm.getAllUsers()) {
				boolean hasAccessToCategory = retriever.hasUserAccessToSomethingInSecondaryConcept(
						user, category.getWrappedRecordId(), rm.category.schemaType());
				boolean expectedHasAccessToCategory = visibleRecordsUsingSolr(category, user);

				System.out.println(hasAccessToCategory + " - " + expectedHasAccessToCategory);

				if (hasAccessToCategory != expectedHasAccessToCategory) {
					assertThat(hasAccessToCategory).describedAs("user '" + user.getUsername() + "' read access to something in category '" + category.getCode() + "'")
							.isEqualTo(expectedHasAccessToCategory);
				}
			}
		}

		for (AdministrativeUnit administrativeUnit : rm.administrativeUnitStream().collect(Collectors.toList())) {
			for (User user : rm.getAllUsers()) {
				boolean hasAccessToAdministrativeUnit = retriever.hasUserAccessToSomethingInPrincipalConcept(
						user, administrativeUnit.getWrappedRecordId(), rm.administrativeUnit.schemaType());

				boolean expectedHasAccessToUnit = visibleRecordsUsingSolr(administrativeUnit, user);

				System.out.println(hasAccessToAdministrativeUnit + " - " + expectedHasAccessToUnit);

				if (hasAccessToAdministrativeUnit != expectedHasAccessToUnit) {
					assertThat(hasAccessToAdministrativeUnit).describedAs("user '" + user.getUsername() + "' read access to something in adm. unit '" + administrativeUnit.getCode() + "'")
							.isEqualTo(expectedHasAccessToUnit);
				}
			}
		}

	}

	boolean visibleRecordsUsingSolr(Category category, User user) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(rm.folder.schemaType()).where(Schemas.PATH_PARTS).isEqualTo(category.getId()));
		query.filteredWithUser(user);

		return searchServices.hasResults(query);
	}

	boolean visibleRecordsUsingSolr(AdministrativeUnit unit, User user) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(rm.folder.schemaType()).where(Schemas.PATH_PARTS).isEqualTo(unit.getId()));
		query.filteredWithUser(user);

		return searchServices.hasResults(query);
	}
}
