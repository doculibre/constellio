package com.constellio.model.services.records.populators;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_CACHE;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(recordServices.getDocumentById("r1").<String>get(zeSchema.stringMetadata().getSortField())).isNull();
		assertThat(recordServices.getDocumentById("r2").<String>get(zeSchema.stringMetadata().getSortField())).isNull();

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
	public void givenNotSortedStringMetadataWhenSearchingWithSortThenUseRawField()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setSortable(false);
			}
		});

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

		//É1  e10   e100   e1000  E2  É3  e4  è5  é6

		//E2

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().setQueryExecutionMethod(USE_SOLR).sortAsc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r1", "r7", "r8", "r9", "r2", "r3", "r4", "r5", "r6"));

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().setQueryExecutionMethod(USE_SOLR).sortDesc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r6", "r5", "r4", "r3", "r2", "r9", "r8", "r7", "r1"));

		//Cache return results in different order, but it's much better than solr!
		Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.disable();
		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().setQueryExecutionMethod(USE_SOLR).sortAsc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r1", "r7", "r8", "r9", "r2", "r3", "r4", "r5", "r6"));

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().setQueryExecutionMethod(USE_SOLR).sortDesc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r6", "r5", "r4", "r3", "r2", "r9", "r8", "r7", "r1"));


		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().setQueryExecutionMethod(USE_CACHE).sortAsc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r1", "r7", "r8", "r9", "r2", "r3", "r4", "r5", "r6"));

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().setQueryExecutionMethod(USE_CACHE).sortDesc(zeSchema.stringMetadata())))
				.isEqualTo(asList("r6", "r5", "r4", "r3", "r2", "r9", "r8", "r7", "r1"));

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

	@Test
	public void givenGlobalCodeMetadataWhenSearchingWithSortThenUseSortField()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("code").setType(MetadataValueType.STRING);
			}
		});

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "r1").set(Schemas.CODE, "É1"));
		transaction.add(new TestRecord(zeSchema, "r2").set(Schemas.CODE, "E2"));
		transaction.add(new TestRecord(zeSchema, "r3").set(Schemas.CODE, "É3"));
		transaction.add(new TestRecord(zeSchema, "r4").set(Schemas.CODE, "e4"));
		transaction.add(new TestRecord(zeSchema, "r5").set(Schemas.CODE, "è5"));
		transaction.add(new TestRecord(zeSchema, "r6").set(Schemas.CODE, "é6"));
		transaction.add(new TestRecord(zeSchema, "r7").set(Schemas.CODE, "e10"));
		transaction.add(new TestRecord(zeSchema, "r8").set(Schemas.CODE, "e100"));
		transaction.add(new TestRecord(zeSchema, "r9").set(Schemas.CODE, "e1000"));
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortAsc(Schemas.CODE)))
				.isEqualTo(asList("r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8", "r9"));

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortDesc(Schemas.CODE)))
				.isEqualTo(asList("r9", "r8", "r7", "r6", "r5", "r4", "r3", "r2", "r1"));

	}

	@Test
	public void givenGlobalTitleMetadataWhenSearchingWithSortThenUseSortField()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "r1").set(Schemas.TITLE, "É1"));
		transaction.add(new TestRecord(zeSchema, "r2").set(Schemas.TITLE, "E2"));
		transaction.add(new TestRecord(zeSchema, "r3").set(Schemas.TITLE, "É3"));
		transaction.add(new TestRecord(zeSchema, "r4").set(Schemas.TITLE, "e4"));
		transaction.add(new TestRecord(zeSchema, "r5").set(Schemas.TITLE, "è5"));
		transaction.add(new TestRecord(zeSchema, "r6").set(Schemas.TITLE, "é6"));
		transaction.add(new TestRecord(zeSchema, "r7").set(Schemas.TITLE, "e10"));
		transaction.add(new TestRecord(zeSchema, "r8").set(Schemas.TITLE, "e100"));
		transaction.add(new TestRecord(zeSchema, "r9").set(Schemas.TITLE, "e1000"));
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortAsc(Schemas.TITLE)))
				.isEqualTo(asList("r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8", "r9"));

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortDesc(Schemas.TITLE)))
				.isEqualTo(asList("r9", "r8", "r7", "r6", "r5", "r4", "r3", "r2", "r1"));

	}

	@Test
	public void givenNotSortedGlobalStringMetadataWhenSearchingWithSortThenUseRawField()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("url").setType(MetadataValueType.STRING);
			}
		});

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "r1").set(Schemas.URL, "É1"));
		transaction.add(new TestRecord(zeSchema, "r2").set(Schemas.URL, "E2"));
		transaction.add(new TestRecord(zeSchema, "r3").set(Schemas.URL, "É3"));
		transaction.add(new TestRecord(zeSchema, "r4").set(Schemas.URL, "e4"));
		transaction.add(new TestRecord(zeSchema, "r5").set(Schemas.URL, "è5"));
		transaction.add(new TestRecord(zeSchema, "r6").set(Schemas.URL, "é6"));
		transaction.add(new TestRecord(zeSchema, "r7").set(Schemas.URL, "e10"));
		transaction.add(new TestRecord(zeSchema, "r8").set(Schemas.URL, "e100"));
		transaction.add(new TestRecord(zeSchema, "r9").set(Schemas.URL, "e1000"));
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortAsc(Schemas.URL)))
				.isEqualTo(asList("r1", "r7", "r8", "r9", "r2", "r3", "r4", "r5", "r6"));

		assertThat(searchServices.searchRecordIds(allZeSchemaRecords().sortDesc(Schemas.URL)))
				.isEqualTo(asList("r6", "r5", "r4", "r3", "r2", "r9", "r8", "r7", "r1"));

	}

	//-----------------------------------------

	LogicalSearchQuery allZeSchemaRecords() {
		return new LogicalSearchQuery(from(zeSchema.type()).returnAll());
	}
}
