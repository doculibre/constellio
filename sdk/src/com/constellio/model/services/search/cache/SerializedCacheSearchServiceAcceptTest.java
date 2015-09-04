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
package com.constellio.model.services.search.cache;

import static com.constellio.model.services.records.cache.CacheConfig.permanentCache;
import static com.constellio.model.services.records.cache.CacheConfig.volatileCache;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInExceptEvents;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.startingWithText;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.StatsBigVaultServerExtension;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class SerializedCacheSearchServiceAcceptTest extends ConstellioTest {

	StatsBigVaultServerExtension queriesListener;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = zeCollectionSchemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = zeCollectionSchemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = zeCollectionSchemas.new ThirdSchemaMetadatas();

	SerializableSearchCache cache = new SerializableSearchCache();
	SerializedCacheSearchService searchServices;

	LogicalSearchCondition fromAllSchemas, fromAllSchemasWhereNumberIs42, fromZeSchema;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection);
		defineSchemasManager().using(zeCollectionSchemas.withAStringMetadata().withANumberMetadata());

		searchServices = new SerializedCacheSearchService(getModelLayerFactory(), cache);

		givenTestRecords();

		fromAllSchemas = fromAllSchemasInExceptEvents(zeCollection).where(Schemas.SCHEMA).isNotEqual("collection_default")
				.andWhere(Schemas.SCHEMA).isNot(startingWithText("facet"));
		fromAllSchemasWhereNumberIs42 = fromAllSchemasIn(zeCollection).where(Schemas.SCHEMA).isNotEqual("collection_default")
				.andWhere(Schemas.SCHEMA).isNot(startingWithText("facet")).andWhere(zeSchema.numberMetadata()).isEqualTo(42);
		fromZeSchema = from(zeSchema.instance()).returnAll();

		RecordsCache collectionCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		collectionCache.configureCache(volatileCache(zeSchema.type(), 10));
		collectionCache.configureCache(permanentCache(anotherSchema.type()));
		collectionCache.configureCache(permanentCache(thirdSchema.type()));

		DataLayerSystemExtensions extensions = getDataLayerFactory().getExtensions().getSystemWideExtensions();
		queriesListener = new StatsBigVaultServerExtension();
		extensions.bigVaultServerExtension.add(queriesListener);
	}

	@Test
	public void givenQuerySortIsModifiedThenInvalidated()
			throws Exception {

		SPEQueryResponse response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE));
		assertThat(response.getNumFound()).isEqualTo(12);
		assertThat(response.getRecords()).hasSize(12);
		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_3"));
		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_1"));
		assertThat(response.getRecords().get(4)).isEqualTo(record("anotherSchema_1"));
		assertThat(response.getRecords().get(5)).isEqualTo(record("thirdSchema_1"));
		assertThat(response.getRecords().get(6)).isEqualTo(record("anotherSchema_2"));
		assertThat(response.getRecords().get(7)).isEqualTo(record("anotherSchema_3"));
		assertThat(response.getRecords().get(8)).isEqualTo(record("thirdSchema_2"));
		assertThat(response.getRecords().get(9)).isEqualTo(record("zeSchema_3"));
		assertThat(response.getRecords().get(10)).isEqualTo(record("anotherSchema_4"));
		assertThat(response.getRecords().get(11)).isEqualTo(record("thirdSchema_4"));

		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortDesc(Schemas.TITLE));
		assertThat(response.getNumFound()).isEqualTo(12);
		assertThat(response.getRecords()).hasSize(12);
		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_4"));
		assertThat(response.getRecords().get(6)).isEqualTo(record("thirdSchema_1"));
		assertThat(response.getRecords().get(10)).isEqualTo(record("zeSchema_4"));

		response = searchServices.query(new LogicalSearchQuery(fromAllSchemasWhereNumberIs42).sortDesc(Schemas.TITLE));
		assertThat(response.getNumFound()).isEqualTo(2);
		assertThat(response.getRecords()).hasSize(2);
		assertThat(response.getRecords().get(0)).isEqualTo(record("zeSchema_3"));
		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_4"));

		response = searchServices.query(new LogicalSearchQuery(fromAllSchemasWhereNumberIs42).sortAsc(Schemas.TITLE));
		assertThat(response.getNumFound()).isEqualTo(2);
		assertThat(response.getRecords()).hasSize(2);
		assertThat(response.getRecords().get(0)).isEqualTo(record("zeSchema_4"));
		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_3"));

		response = searchServices.query(new LogicalSearchQuery(fromZeSchema).sortAsc(Schemas.TITLE));
		assertThat(response.getNumFound()).isEqualTo(4);
		assertThat(response.getRecords()).hasSize(4);
		assertThat(response.getRecords().get(0)).isEqualTo(record("zeSchema_4"));
		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_2"));
		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_1"));
		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_3"));

		response = searchServices.query(new LogicalSearchQuery(fromZeSchema).sortDesc(Schemas.TITLE));
		assertThat(response.getNumFound()).isEqualTo(4);
		assertThat(response.getRecords()).hasSize(4);
		assertThat(response.getRecords().get(0)).isEqualTo(record("zeSchema_3"));
		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_1"));
		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_4"));

		response = searchServices.query(new LogicalSearchQuery(fromZeSchema).sortDesc(zeSchema.stringMetadata()));
		assertThat(response.getNumFound()).isEqualTo(4);
		assertThat(response.getRecords()).hasSize(4);
		assertThat(response.getRecords().get(0)).isEqualTo(record("zeSchema_3"));
		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_1"));

	}

	@Test
	public void givenReusingTheSameResponseThenUseCache()
			throws Exception {
		queriesListener.clear();
		SPEQueryResponse response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 3);
		assertThat(response.getNumFound()).isEqualTo(12);
		assertThat(response.getRecords()).hasSize(12);

		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_3"));
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(queriesListener.queries).hasSize(1);

		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_1"));
		assertThat(queriesListener.queries).hasSize(2);
		assertThat(response.getRecords().get(4)).isEqualTo(record("anotherSchema_1"));
		assertThat(queriesListener.queries).hasSize(2);
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 4);
		assertThat(response.getRecords().get(5)).isEqualTo(record("thirdSchema_1"));
		assertThat(queriesListener.queries).hasSize(2);

		assertThat(response.getRecords().get(6)).isEqualTo(record("anotherSchema_2"));
		assertThat(queriesListener.queries).hasSize(3);
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 1);
		assertThat(response.getRecords().get(7)).isEqualTo(record("anotherSchema_3"));
		assertThat(queriesListener.queries).hasSize(3);
		assertThat(response.getRecords().get(8)).isEqualTo(record("thirdSchema_2"));
		assertThat(queriesListener.queries).hasSize(3);

		assertThat(response.getRecords().get(9)).isEqualTo(record("zeSchema_3"));
		assertThat(queriesListener.queries).hasSize(3);
		assertThat(response.getRecords().get(10)).isEqualTo(record("anotherSchema_4"));
		assertThat(queriesListener.queries).hasSize(4);
		assertThat(response.getRecords().get(11)).isEqualTo(record("thirdSchema_4"));
		assertThat(queriesListener.queries).hasSize(5);

		assertThat(response.getRecords().get(12)).isNull();
		assertThat(queriesListener.queries).hasSize(6);

	}

	@Test
	public void givenHardcoreUsageThenUseCache()
			throws Exception {
		//This class was created to respond to this extreme usecase
		queriesListener.clear();
		SPEQueryResponse response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(response.getNumFound()).isEqualTo(12);
		assertThat(response.getRecords()).hasSize(12);

		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_3"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_4"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_2"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_1"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(4)).isEqualTo(record("anotherSchema_1"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(5)).isEqualTo(record("thirdSchema_1"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(6)).isEqualTo(record("anotherSchema_2"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(7)).isEqualTo(record("anotherSchema_3"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(8)).isEqualTo(record("thirdSchema_2"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(9)).isEqualTo(record("zeSchema_3"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(10)).isEqualTo(record("anotherSchema_4"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(response.getRecords().get(11)).isEqualTo(record("thirdSchema_4"));
		response = searchServices.query(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);

		assertThat(queriesListener.queries).hasSize(1);

	}

	@Test
	public void whenIterateNormallyOverALazyRecordListWithALargeBufferThenOnlyOneQuery()
			throws Exception {
		//This class was created to respond to this extreme usecase
		queriesListener.clear();
		List<Record> records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records).hasSize(12);

		assertThat(records.get(0)).isEqualTo(record("thirdSchema_3"));
		assertThat(records.get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(records.get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(records.get(3)).isEqualTo(record("zeSchema_1"));
		assertThat(records.get(4)).isEqualTo(record("anotherSchema_1"));
		assertThat(records.get(5)).isEqualTo(record("thirdSchema_1"));
		assertThat(records.get(6)).isEqualTo(record("anotherSchema_2"));
		assertThat(records.get(7)).isEqualTo(record("anotherSchema_3"));
		assertThat(records.get(8)).isEqualTo(record("thirdSchema_2"));
		assertThat(records.get(9)).isEqualTo(record("zeSchema_3"));
		assertThat(records.get(10)).isEqualTo(record("anotherSchema_4"));
		assertThat(records.get(11)).isEqualTo(record("thirdSchema_4"));
		assertThat(queriesListener.queries).hasSize(1);

		assertThat(records.get(0)).isEqualTo(record("thirdSchema_3"));
		assertThat(records.get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(records.get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(records.get(3)).isEqualTo(record("zeSchema_1"));
		assertThat(records.get(4)).isEqualTo(record("anotherSchema_1"));
		assertThat(records.get(5)).isEqualTo(record("thirdSchema_1"));
		assertThat(records.get(6)).isEqualTo(record("anotherSchema_2"));
		assertThat(records.get(7)).isEqualTo(record("anotherSchema_3"));
		assertThat(records.get(8)).isEqualTo(record("thirdSchema_2"));
		assertThat(records.get(9)).isEqualTo(record("zeSchema_3"));
		assertThat(records.get(10)).isEqualTo(record("anotherSchema_4"));
		assertThat(records.get(11)).isEqualTo(record("thirdSchema_4"));
		assertThat(queriesListener.queries).hasSize(1);

	}

	@Test
	public void whenIterateNormallyAndTwiceOverALazyRecordListWithASmallBufferThenQueriesForEachBatchAndNoMoreQueriesForSecondIteration()
			throws Exception {
		//This class was created to respond to this extreme usecase
		queriesListener.clear();
		List<Record> records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 4);
		assertThat(records).hasSize(12);

		assertThat(records.get(0)).isEqualTo(record("thirdSchema_3"));
		assertThat(records.get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(records.get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(records.get(3)).isEqualTo(record("zeSchema_1"));
		assertThat(records.get(4)).isEqualTo(record("anotherSchema_1"));
		assertThat(records.get(5)).isEqualTo(record("thirdSchema_1"));
		assertThat(records.get(6)).isEqualTo(record("anotherSchema_2"));
		assertThat(records.get(7)).isEqualTo(record("anotherSchema_3"));
		assertThat(records.get(8)).isEqualTo(record("thirdSchema_2"));
		assertThat(records.get(9)).isEqualTo(record("zeSchema_3"));
		assertThat(records.get(10)).isEqualTo(record("anotherSchema_4"));
		assertThat(records.get(11)).isEqualTo(record("thirdSchema_4"));
		assertThat(queriesListener.queries).hasSize(3);

		assertThat(records.get(0)).isEqualTo(record("thirdSchema_3"));
		assertThat(records.get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(records.get(2)).isEqualTo(record("zeSchema_2"));
		assertThat(records.get(3)).isEqualTo(record("zeSchema_1"));
		assertThat(records.get(4)).isEqualTo(record("anotherSchema_1"));
		assertThat(records.get(5)).isEqualTo(record("thirdSchema_1"));
		assertThat(records.get(6)).isEqualTo(record("anotherSchema_2"));
		assertThat(records.get(7)).isEqualTo(record("anotherSchema_3"));
		assertThat(records.get(8)).isEqualTo(record("thirdSchema_2"));
		assertThat(records.get(9)).isEqualTo(record("zeSchema_3"));
		assertThat(records.get(10)).isEqualTo(record("anotherSchema_4"));
		assertThat(records.get(11)).isEqualTo(record("thirdSchema_4"));
		assertThat(queriesListener.queries).hasSize(3);

	}

	@Test
	public void givenHighlightingIsEnabledWhenIteratingWithLargeBufferASecondTimeThenReuseCache()
			throws Exception {
		//This class was created to respond to this extreme usecase
		queriesListener.clear();
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE)
				.setHighlighting(true).setFreeTextQuery("banane");
		SPEQueryResponse response = searchServices.query(query, 12);
		assertThat(response.getRecords()).hasSize(12);

		iterateOverAllSchemasQueryByAscTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(1);

		iterateOverAllSchemasQueryByAscTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(1);

	}

	@Test
	public void givenHighlightingIsEnabledWhenIteratingWithSmallBufferASecondTimeThenReuseCache()
			throws Exception {
		//This class was created to respond to this extreme usecase
		queriesListener.clear();
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE)
				.setHighlighting(true).setFreeTextQuery("banane");
		SPEQueryResponse response = searchServices.query(query, 4);
		assertThat(response.getRecords()).hasSize(12);

		iterateOverAllSchemasQueryByAscTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(3);

		iterateOverAllSchemasQueryByAscTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(3);

		query.clearSort();
		query.sortDesc(Schemas.TITLE);

		response = searchServices.query(query, 4);
		assertThat(response.getRecords()).hasSize(12);

		iterateOverAllSchemasQueryByDescTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(6);

		response = searchServices.query(query, 4);
		iterateOverAllSchemasQueryByDescTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(6);

	}

	@Test
	public void givenHighlightingIsEnabledWhenQueryingWithModifiedFreeTextThenInvalidatePreviousHighlightinh()
			throws Exception {
		//This class was created to respond to this extreme usecase
		queriesListener.clear();
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE)
				.setHighlighting(true).setFreeTextQuery("banane");
		SPEQueryResponse response = searchServices.query(query, 4);
		assertThat(response.getRecords()).hasSize(12);

		iterateOverAllSchemasQueryByAscTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(3);

		iterateOverAllSchemasQueryByAscTitleAndValidateRecordAndHighlighting(response);
		assertThat(queriesListener.queries).hasSize(3);

		query.setFreeTextQuery("pomme");
		query.clearSort();
		query.sortDesc(Schemas.TITLE);

		response = searchServices.query(query, 4);

		assertThat(response.getRecords()).hasSize(12);

		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_4"));
		assertThat(response.getHighlighting("thirdSchema_4").get("search_txt_fr").get(0))
				.contains("<em>pomme</em> banane thirdSchema_4");

		assertThat(response.getRecords().get(1)).isEqualTo(record("anotherSchema_4"));
		assertThat(response.getHighlighting("anotherSchema_4").get("search_txt_fr").get(0))
				.contains("<em>pomme</em> banane anotherSchema_4");

		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_3"));
		assertThat(response.getHighlighting("zeSchema_3").get("search_txt_fr").get(0))
				.contains("<em>pomme</em> banane zeSchema_3");

		assertThat(response.getRecords().get(3)).isEqualTo(record("thirdSchema_2"));
		assertThat(response.getHighlighting("thirdSchema_2").get("search_txt_fr").get(0))
				.contains("<em>pomme</em> banane thirdSchema_2");

		assertThat(response.getRecords().get(10)).isEqualTo(record("zeSchema_4"));
		assertThat(response.getHighlighting("zeSchema_4").get("search_txt_fr").get(0))
				.contains("<em>pomme</em> banane zeSchema_4");

		assertThat(response.getRecords().get(11)).isEqualTo(record("thirdSchema_3"));
		assertThat(response.getHighlighting("thirdSchema_3").get("search_txt_fr").get(0))
				.contains("<em>pomme</em> banane thirdSchema_3");

		assertThat(queriesListener.queries).hasSize(5);
	}

	private void iterateOverAllSchemasQueryByAscTitleAndValidateRecordAndHighlighting(SPEQueryResponse response) {
		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_3"));
		assertThat(response.getHighlighting("thirdSchema_3").get("search_txt_fr").get(0))
				.contains("Aigle pomme <em>banane</em> thirdSchema_3");

		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_4"));
		assertThat(response.getHighlighting("zeSchema_4").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> zeSchema_4");

		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_1"));
		assertThat(response.getHighlighting("zeSchema_1").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> zeSchema_1");

		assertThat(response.getRecords().get(4)).isEqualTo(record("anotherSchema_1"));
		assertThat(response.getHighlighting("anotherSchema_1").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_1");

		assertThat(response.getRecords().get(5)).isEqualTo(record("thirdSchema_1"));
		assertThat(response.getHighlighting("thirdSchema_1").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> thirdSchema_1");

		assertThat(response.getRecords().get(6)).isEqualTo(record("anotherSchema_2"));
		assertThat(response.getHighlighting("anotherSchema_2").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_2");

		assertThat(response.getRecords().get(7)).isEqualTo(record("anotherSchema_3"));
		assertThat(response.getHighlighting("anotherSchema_3").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_3");

		assertThat(response.getRecords().get(8)).isEqualTo(record("thirdSchema_2"));
		assertThat(response.getHighlighting("thirdSchema_2").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> thirdSchema_2");

		assertThat(response.getRecords().get(9)).isEqualTo(record("zeSchema_3"));
		assertThat(response.getHighlighting("zeSchema_3").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> zeSchema_3");

		assertThat(response.getRecords().get(10)).isEqualTo(record("anotherSchema_4"));
		assertThat(response.getHighlighting("anotherSchema_4").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_4");

		assertThat(response.getRecords().get(11)).isEqualTo(record("thirdSchema_4"));
		assertThat(response.getHighlighting("thirdSchema_4").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> thirdSchema_4");
	}

	private void iterateOverAllSchemasQueryByDescTitleAndValidateRecordAndHighlighting(SPEQueryResponse response) {
		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_4"));
		assertThat(response.getHighlighting("thirdSchema_4").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> thirdSchema_4");

		assertThat(response.getRecords().get(1)).isEqualTo(record("anotherSchema_4"));
		assertThat(response.getHighlighting("anotherSchema_4").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_4");

		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_3"));
		assertThat(response.getHighlighting("zeSchema_3").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> zeSchema_3");

		assertThat(response.getRecords().get(3)).isEqualTo(record("thirdSchema_2"));
		assertThat(response.getHighlighting("thirdSchema_2").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> thirdSchema_2");

		assertThat(response.getRecords().get(4)).isEqualTo(record("anotherSchema_3"));
		assertThat(response.getHighlighting("anotherSchema_3").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_3");

		assertThat(response.getRecords().get(5)).isEqualTo(record("anotherSchema_2"));
		assertThat(response.getHighlighting("anotherSchema_2").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_2");

		assertThat(response.getRecords().get(6)).isEqualTo(record("thirdSchema_1"));
		assertThat(response.getHighlighting("thirdSchema_1").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> thirdSchema_1");

		assertThat(response.getRecords().get(7)).isEqualTo(record("anotherSchema_1"));
		assertThat(response.getHighlighting("anotherSchema_1").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> anotherSchema_1");

		assertThat(response.getRecords().get(8)).isEqualTo(record("zeSchema_1"));
		assertThat(response.getHighlighting("zeSchema_1").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> zeSchema_1");

		assertThat(response.getRecords().get(10)).isEqualTo(record("zeSchema_4"));
		assertThat(response.getHighlighting("zeSchema_4").get("search_txt_fr").get(0))
				.contains("pomme <em>banane</em> zeSchema_4");

		assertThat(response.getRecords().get(11)).isEqualTo(record("thirdSchema_3"));
		assertThat(response.getHighlighting("thirdSchema_3").get("search_txt_fr").get(0))
				.contains("Aigle pomme <em>banane</em> thirdSchema_3");
	}

	@Test
	public void givenHardcoreUsageOfRecordsListThenUseCache()
			throws Exception {
		//This class was created to respond to this extreme usecase
		queriesListener.clear();
		List<Record> records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records).hasSize(12);
		assertThat(records.get(0)).isEqualTo(record("thirdSchema_3"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(1)).isEqualTo(record("zeSchema_4"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(2)).isEqualTo(record("zeSchema_2"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(3)).isEqualTo(record("zeSchema_1"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(4)).isEqualTo(record("anotherSchema_1"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(5)).isEqualTo(record("thirdSchema_1"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(6)).isEqualTo(record("anotherSchema_2"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(7)).isEqualTo(record("anotherSchema_3"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(8)).isEqualTo(record("thirdSchema_2"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(9)).isEqualTo(record("zeSchema_3"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(10)).isEqualTo(record("anotherSchema_4"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(records.get(11)).isEqualTo(record("thirdSchema_4"));

		records = searchServices.search(new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE), 15);
		assertThat(queriesListener.queries).hasSize(1);

	}

	// ****
	//This service is not supporting facets yet, here is a test for this
	// ****

	//	@Test
	//	public void givenFacetsWhenMultipleSearchWithSameCacheThenFacetsOnlyCalculatedOnFirstRequest()
	//			throws Exception {
	//		//This class was created to respond to this extreme usecase
	//		queriesListener.clear();
	//
	//		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemas).sortAsc(Schemas.TITLE);
	//		query.addFieldFacet(zeSchema.numberMetadata());
	//		query.addQueryFacet("numberMetadata_d:[0 TO 10]");
	//		query.addQueryFacet("numberMetadata_d:[10 TO 20]");
	//		query.addQueryFacet("numberMetadata_d:[20 TO 30]");
	//
	//		SPEQueryResponse response = searchServices.query(query, 3);
	//		assertThat(response.getRecords()).hasSize(12);
	//		assertThat(response.getRecords().get(0)).isEqualTo(record("thirdSchema_3"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(1)).isEqualTo(record("zeSchema_4"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(2)).isEqualTo(record("zeSchema_2"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(3)).isEqualTo(record("zeSchema_1"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(4)).isEqualTo(record("anotherSchema_1"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(5)).isEqualTo(record("thirdSchema_1"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(6)).isEqualTo(record("anotherSchema_2"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(7)).isEqualTo(record("anotherSchema_3"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(8)).isEqualTo(record("thirdSchema_2"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(9)).isEqualTo(record("zeSchema_3"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(10)).isEqualTo(record("anotherSchema_4"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(11)).isEqualTo(record("thirdSchema_4"));
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		response = searchServices.query(query, 3);
	//		assertThat(response.getRecords().get(12)).isNull();
	//		assertThat(response.getFieldFacetValues()).isNotEmpty();
	//		assertThat(response.getQueryFacetsValues()).isNotEmpty();
	//
	//		assertThat(queriesListener.queries).hasSize(4);
	//		assertThat(queriesListener.queries.get(0).get("facet")).isNotNull();
	//		assertThat(queriesListener.queries.get(0).get("facet.field")).isNotNull();
	//		assertThat(queriesListener.queries.get(0).get("facet.query")).isNotNull();
	//
	//		assertThat(queriesListener.queries.get(1).get("facet")).isNotNull();
	//		assertThat(queriesListener.queries.get(1).get("facet.field")).isNotNull();
	//		assertThat(queriesListener.queries.get(1).get("facet.query")).isNotNull();
	//
	//		assertThat(queriesListener.queries.get(2).get("facet")).isNotNull();
	//		assertThat(queriesListener.queries.get(2).get("facet.field")).isNotNull();
	//		assertThat(queriesListener.queries.get(2).get("facet.query")).isNotNull();
	//
	//		assertThat(queriesListener.queries.get(3).get("facet")).isNotNull();
	//		assertThat(queriesListener.queries.get(3).get("facet.field")).isNotNull();
	//		assertThat(queriesListener.queries.get(3).get("facet.query")).isNotNull();
	//
	//	}

	// -------------------------------------------

	private void givenTestRecords() {

		Transaction transaction = new Transaction();
		//By asc_title=4
		transaction.add(new TestRecord(zeSchema, "zeSchema_1").set(Schemas.TITLE, "Canard pomme banane zeSchema_1")
				.set(zeSchema.stringMetadata(), "value A").set(zeSchema.numberMetadata(), 1));

		//By asc_title=3
		transaction.add(new TestRecord(zeSchema, "zeSchema_2").set(Schemas.TITLE, "Boeuf pomme banane zeSchema_2")
				.set(zeSchema.stringMetadata(), "value B").set(zeSchema.numberMetadata(), 1));

		//By asc_title=11
		transaction.add(new TestRecord(zeSchema, "zeSchema_3").set(Schemas.TITLE, "Ghibou pomme banane zeSchema_3")
				.set(zeSchema.stringMetadata(), "value D").set(zeSchema.numberMetadata(), 42));

		//By asc_title=2
		transaction.add(new TestRecord(zeSchema, "zeSchema_4").set(Schemas.TITLE, "Aligator pomme banane zeSchema_4")
				.set(zeSchema.stringMetadata(), "value C").set(zeSchema.numberMetadata(), 42));

		//--

		//By asc_title=5
		transaction.add(new TestRecord(anotherSchema, "anotherSchema_1").set(Schemas.TITLE, "Cane pomme banane anotherSchema_1"));

		//By asc_title=7
		transaction.add(new TestRecord(anotherSchema, "anotherSchema_2")
				.set(Schemas.TITLE, "Crocodile pomme banane anotherSchema_2"));

		//By asc_title=8
		transaction
				.add(new TestRecord(anotherSchema, "anotherSchema_3").set(Schemas.TITLE, "Dièppe pomme banane anotherSchema_3"));

		//By asc_title=10
		transaction
				.add(new TestRecord(anotherSchema, "anotherSchema_4").set(Schemas.TITLE, "Girafe pomme banane anotherSchema_4"));

		//--

		//By asc_title=6
		transaction.add(new TestRecord(thirdSchema, "thirdSchema_1").set(Schemas.TITLE, "Coq pomme banane thirdSchema_1"));

		//By asc_title=9
		transaction.add(new TestRecord(thirdSchema, "thirdSchema_2").set(Schemas.TITLE, "Faucon pomme banane thirdSchema_2"));

		//By asc_title=1
		transaction.add(new TestRecord(thirdSchema, "thirdSchema_3").set(Schemas.TITLE, "Aigle pomme banane thirdSchema_3"));

		//By asc_title=12
		transaction.add(new TestRecord(thirdSchema, "thirdSchema_4").set(Schemas.TITLE, "Renard pomme banane thirdSchema_4"));

		//--

		try {
			getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
}
