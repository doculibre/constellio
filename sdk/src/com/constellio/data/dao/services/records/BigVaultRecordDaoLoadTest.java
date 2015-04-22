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
package com.constellio.data.dao.services.records;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.concurrent.ConcurrencyUtils;
import com.constellio.sdk.tests.concurrent.ConcurrencyUtils.IncrementForTask;
import com.constellio.sdk.tests.concurrent.ConcurrencyUtils.WorkerContextFactory;

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
		RecordDTO record = new RecordDTO(UUID.randomUUID().toString(), -1, null, fields);
		return record;
	}

	private void add(RecordDTO recordDTO)
			throws OptimisticLocking {
		recordDao.execute(new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, Arrays.asList(recordDTO),
				new ArrayList<RecordDeltaDTO>()));
	}

}
