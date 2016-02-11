package com.constellio.model.services.records.populators;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordNormalizedSortFieldAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	RecordDao recordDao;
	RecordServices recordServices;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		defineSchemasManager().using(setup.withAStringMetadata(whichIsSortable));

		recordDao = getDataLayerFactory().newRecordDao();
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

	}

	@Test
	public void givenSortedStringMetadataWhenGetRecordThenSortFieldNotAvailable()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "r1").set(zeSchema.stringMetadata(), "A10"));
		transaction.add(new TestRecord(zeSchema, "r2").set(zeSchema.stringMetadata(), "A100"));
		recordServices.execute(transaction);

		assertThat(recordServices.getDocumentById("r1").get(zeSchema.stringMetadata().getSortField())).isNull();
		assertThat(recordServices.getDocumentById("r2").get(zeSchema.stringMetadata().getSortField())).isNull();

		String datastoreCode = zeSchema.stringMetadata().getSortField().getDataStoreCode();
		assertThat(recordDao.get("r1").getFields().get(datastoreCode)).isNull();
		assertThat(recordDao.get("r2").getFields().get(datastoreCode)).isNull();
		assertThat(recordDao.get("r1").getCopyFields().get(datastoreCode)).isNull();
		assertThat(recordDao.get("r2").getCopyFields().get(datastoreCode)).isNull();
	}

	@Test
	public void givenSortedStringMetadataWhenUpdatingOrDeletingFieldThenValueUpdated()
			throws Exception {

		String datastoreCode = zeSchema.stringMetadata().getSortField().getDataStoreCode();

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "r1").set(zeSchema.stringMetadata(), "A10"));
		transaction.add(new TestRecord(zeSchema, "r2").set(zeSchema.stringMetadata(), "A100"));
		recordServices.execute(transaction);

		SolrDocument r1 = recordDao.getBigVaultServer().querySingleResult(getById("r1"));
		SolrDocument r2 = recordDao.getBigVaultServer().querySingleResult(getById("r2"));
		assertThat(r1.getFieldValue(datastoreCode)).isEqualTo("a000000010");
		assertThat(r2.getFieldValue(datastoreCode)).isEqualTo("a000000100");

		transaction = new Transaction();
		transaction.add(recordServices.getDocumentById("r1").set(zeSchema.stringMetadata(), "A20"));
		transaction.add(recordServices.getDocumentById("r2").set(zeSchema.stringMetadata(), null));
		recordServices.execute(transaction);

		r1 = recordDao.getBigVaultServer().querySingleResult(getById("r1"));
		r2 = recordDao.getBigVaultServer().querySingleResult(getById("r2"));
		assertThat(r1.getFieldValue(datastoreCode)).isEqualTo("a000000020");
		assertThat(r2.getFieldValue(datastoreCode)).isNull();

		transaction = new Transaction();
		transaction.add(recordServices.getDocumentById("r1").set(zeSchema.stringMetadata(), " "));
		transaction.add(recordServices.getDocumentById("r2").set(zeSchema.stringMetadata(), " A20 "));
		recordServices.execute(transaction);

		r1 = recordDao.getBigVaultServer().querySingleResult(getById("r1"));
		r2 = recordDao.getBigVaultServer().querySingleResult(getById("r2"));
		assertThat(r1.getFieldValue(datastoreCode)).isNull();
		assertThat(r2.getFieldValue(datastoreCode)).isEqualTo("a000000020");
	}

	private SolrParams getById(String id) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", "id:" + id);
		return params;
	}

	@Test
	public void givenSortedStringMetadataWhenSearchingWithSortThenUseSortField()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "r1").set(zeSchema.stringMetadata(), "É1"));
		transaction.add(new TestRecord(zeSchema, "r2").set(zeSchema.stringMetadata(), "E2"));
		transaction.add(new TestRecord(zeSchema, "r3").set(zeSchema.stringMetadata(), "É3"));
		transaction.add(new TestRecord(zeSchema, "r4").set(zeSchema.stringMetadata(), "e4"));
		transaction.add(new TestRecord(zeSchema, "r5").set(zeSchema.stringMetadata(), "è5"));
		transaction.add(new TestRecord(zeSchema, "r6").set(zeSchema.stringMetadata(), "é6"));
		transaction.add(new TestRecord(zeSchema, "r7").set(zeSchema.stringMetadata(), "e10"));
		transaction.add(new TestRecord(zeSchema, "r8").set(zeSchema.stringMetadata(), "e100"));
		transaction.add(new TestRecord(zeSchema, "r9").set(zeSchema.stringMetadata(), "e1000"));
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortAsc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8", "r9"));

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortDesc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r9", "r8", "r7", "r6", "r5", "r4", "r3", "r2", "r1"));

	}

	//-----------------------------------------

	LogicalSearchQuery allZeSchemaRecords() {
		return new LogicalSearchQuery(from(zeSchema.type()).returnAll());
	}
}
