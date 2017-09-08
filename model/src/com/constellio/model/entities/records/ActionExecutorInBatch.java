package com.constellio.model.entities.records;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public abstract class ActionExecutorInBatch {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActionExecutorInBatch.class);

	SearchServices searchServices;
	String actionName;
	int batchSize;

	public ActionExecutorInBatch(SearchServices searchServices, String actionName, int batchSize) {
		this.searchServices = searchServices;
		this.actionName = actionName;
		this.batchSize = batchSize;
	}

	public abstract void doActionOnBatch(List<Record> records)
			throws Exception;

	public void execute(LogicalSearchCondition condition)
			throws Exception {
		execute(new LogicalSearchQuery(condition));
	}

	public void execute(LogicalSearchQuery query)
			throws Exception {
		SearchResponseIterator<Record> recordsIterator = searchServices.recordsIterator(query, batchSize);
		Iterator<List<Record>> recordsBatchIterator = new BatchBuilderIterator<>(recordsIterator, batchSize);
		int done = 0;
		long total = recordsIterator.getNumFound();
		while (recordsBatchIterator.hasNext()) {
			List<Record> batch = recordsBatchIterator.next();
			doActionOnBatch(batch);
			done += batch.size();
			LOGGER.info(actionName + " : " + done + " / " + total);
		}
	}

	public static class WithoutException extends ActionExecutorInBatch {

		public WithoutException(SearchServices searchServices, String actionName, int batchSize) {
			super(searchServices, actionName, batchSize);
		}

		@Override
		public void doActionOnBatch(List<Record> records) {

		}

		@Override
		public void execute(LogicalSearchQuery query) {
			try {
				super.execute(query);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void execute(LogicalSearchCondition condition) {
			try {
				super.execute(condition);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
