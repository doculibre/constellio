package com.constellio.data.dao.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.concurrent.ConcurrencyUtils;
import com.constellio.sdk.tests.concurrent.ConcurrencyUtils.IncrementForTask;
import com.constellio.sdk.tests.concurrent.ConcurrencyUtils.WorkerContextFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@LoadTest
public class BigVaultRecordDaoLoadTest extends ConstellioTest {

	private RecordDao recordDao;

	@Before
	public void setup() {
		recordDao = getDataLayerFactory().newRecordDao();
	}

	@Test
	public void whenAddingMultipleDocumentsWithMultipleThreadsThenCanHandleLoad()
			throws Exception {

		WorkerContextFactory workerContextFactory = new WorkerContextFactory() {
			@Override
			public void setupWorkerContext(int worker, Map<String, Object> context) {
				recordDao = getDataLayerFactory().newRecordDao();
				context.put("dao", recordDao);
			}
		};

		IncrementForTask task = new IncrementForTask() {

			@Override
			public void executeTask(int i, Map<String, Object> workerContext) {
				BigVaultRecordDao dao = (BigVaultRecordDao) workerContext.get("dao");
				try {
					add(newRecordWithTitle("record #" + i));
				} catch (OptimisticLocking optimisticLocking) {
					throw new RuntimeException(optimisticLocking);
				}
			}
		};

		ConcurrencyUtils.concurrentIntegerFor(100, 0, 1000, 1, task, workerContextFactory);
		assertEquals(1000, recordDao.documentsCount());
	}

	private RecordDTO newRecordWithTitle(String title) {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("title_s", title);
		RecordDTO record = new SolrRecordDTO(UUID.randomUUID().toString(), -1, null, fields, false);
		return record;
	}

	private void add(RecordDTO recordDTO)
			throws OptimisticLocking {
		recordDao.execute(new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, Arrays.asList(recordDTO),
				new ArrayList<RecordDeltaDTO>()));
	}

}
