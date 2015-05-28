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
package com.constellio.model.services.records.reindexing;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSchemaAutocomplete;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.Users;

public class ReindexingServicesOneSchemaWithPopulatedFieldsAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = shishOClock.plusHours(5);

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	//TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	RecordServices recordServices;
	ReindexingServices reindexingServices;
	RecordDao recordDao;

	Users users = new Users();
	String dakotaId;

	@Before
	public void setup()
			throws Exception {
		givenDisabledAfterTestValidations();
		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
		recordDao = getDataLayerFactory().newRecordDao();

		givenCollection(zeCollection).withAllTestUsers().andUsersWithWriteAccess(dakota);
		users.setUp(getModelLayerFactory().newUserServices());
		defineSchemasManager().using(schemas
				.withAStringMetadata(whichIsSchemaAutocomplete)
				.withALargeTextMetadata(whichIsSearchable));
	}

	@Test
	public void whenReindexingThenRefreshCopiedAndCalculatedMetadatas_1()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.stringMetadata(), "AC42")
				.set(zeSchema.largeTextMetadata(), "Il y a un serpent dans ma botte");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.stringMetadata(), "AC666")
				.set(zeSchema.largeTextMetadata(), "Votre manque de foi me consterne");

		recordServices.execute(transaction);

		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE, 1);
		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

		reindexingServices.reindexCollections(ReindexationMode.REWRITE, 1);
		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE, 1);
		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

	}

	private List<String> freeTextSearch(String terms) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		ConditionTemplateFactory factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);
		ConditionTemplate conditionTemplate = factory.searchFieldHasAnalyzedValue(terms);
		LogicalSearchCondition condition = from(zeSchema.instance()).where(conditionTemplate);
		return searchServices.searchRecordIds(new LogicalSearchQuery(condition));
	}

	private List<String> autocompleteSearch(String terms) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		ConditionTemplate conditionTemplate = ConditionTemplateFactory.autocompleteFieldMatching(terms);
		LogicalSearchCondition condition = from(zeSchema.instance()).where(conditionTemplate);
		return searchServices.searchRecordIds(new LogicalSearchQuery(condition));
	}

}
