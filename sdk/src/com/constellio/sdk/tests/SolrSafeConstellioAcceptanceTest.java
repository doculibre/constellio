package com.constellio.sdk.tests;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.After;
import org.junit.Before;
import org.junit.internal.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.mockito.Mockito.spy;

// Confirm @SlowTest
public class SolrSafeConstellioAcceptanceTest extends ConstellioTest {
	private static Logger LOGGER = LoggerFactory.getLogger(SolrSafeConstellioAcceptanceTest.class);
	protected SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	protected SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	protected LogicalSearchCondition condition;
	protected SearchServices searchServices;
	protected RecordServices recordServices;
	protected RecordDao recordDao;

	protected Transaction transaction;
	protected ConditionTemplateFactory factory;

	@Before
	public void setUp()
			throws Exception {
		syncSolrConfigurationFiles(getDataLayerFactory());

		givenCollection(zeCollection, Arrays.asList(Language.French.getCode(), Language.English.getCode()));
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, getModelLayerFactory());

		transaction = new Transaction();
		factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);
	}

	@After
	public void cleanup() {
		try {
			syncSolrConfigurationFiles(getDataLayerFactory());
		} catch (AssumptionViolatedException e) {
			//OK
		}
	}

	protected Record newRecordOfZeSchema() {
		return recordServices.newRecordWithSchema(zeSchema.instance());
	}

}
