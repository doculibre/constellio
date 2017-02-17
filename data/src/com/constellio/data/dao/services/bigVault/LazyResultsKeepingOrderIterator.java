package com.constellio.data.dao.services.bigVault;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.BatchBuilderSearchResponseIterator;
import com.constellio.data.utils.LazyIterator;

public abstract class LazyResultsKeepingOrderIterator<T> extends LazyIterator<T> implements SearchResponseIterator<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LazyResultsIterator.class);

	private ModifiableSolrParams solrParams;
	private RecordDao recordDao;
	private List<RecordDTO> currentBatch = new ArrayList<>();

	private int currentStart;

	private int current;
	private int intervalsLength;
	private long currentNumFound = -1;
	private int skippingFirstRecords;

	public LazyResultsKeepingOrderIterator(RecordDao recordDao, SolrParams solrParams, int intervalsLength) {
		this.recordDao = recordDao;
		this.solrParams = new ModifiableSolrParams(solrParams);
		this.solrParams.set("rows", intervalsLength);
		this.intervalsLength = intervalsLength;
	}

	public LazyResultsKeepingOrderIterator(RecordDao recordDao, SolrParams solrParams, int intervalsLength, int currentStart) {
		this.recordDao = recordDao;
		this.solrParams = new ModifiableSolrParams(solrParams);
		this.solrParams.set("rows", intervalsLength);
		this.intervalsLength = intervalsLength;
		this.current = currentStart;
		this.currentStart = currentStart;
		this.skippingFirstRecords = currentStart;
	}

	@Override
	public SearchResponseIterator<List<T>> inBatches() {
		return new BatchBuilderSearchResponseIterator<T>(this, intervalsLength) {

			@Override
			public long getNumFound() {
				return LazyResultsKeepingOrderIterator.this.getNumFound();
			}
		};
	}

	@Override
	public long getNumFound() {
		if (currentNumFound == -1) {
			hasNext();
		}
		return currentNumFound;
	}

	public abstract T convert(RecordDTO recordDTO);

	@Override
	protected T getNextOrNull() {

		if (skippingFirstRecords != 0 || current - currentStart >= currentBatch.size()) {
			if (skippingFirstRecords == 0 && currentBatch.size() < intervalsLength && current != 0) {
				currentBatch = new ArrayList<>();
			} else {
				loadNextBatch();
			}

			if (currentBatch.isEmpty()) {
				return null;
			}
		}

		T returnedValue = convert(currentBatch.get(current - currentStart));
		current++;
		return returnedValue;
	}

	void loadNextBatch() {
		if (skippingFirstRecords != 0) {
			current = skippingFirstRecords;
			skippingFirstRecords = 0;
		}

		currentStart = current;
		ModifiableSolrParams params = new ModifiableSolrParams(this.solrParams);
		params.set("start", "" + current);
		QueryResponseDTO responseDTO = recordDao.query(params);
		currentBatch = responseDTO.getResults();
		currentNumFound = responseDTO.getNumFound();
	}

	public static class LazyRecordDTOResultsIterator extends LazyResultsIterator<RecordDTO> {

		public LazyRecordDTOResultsIterator(RecordDao recordDao, SolrParams solrParams, int intervalsLength) {
			super(recordDao, solrParams, intervalsLength);
		}

		@Override
		public RecordDTO convert(RecordDTO recordDTO) {
			return recordDTO;
		}
	}
}
