package com.constellio.data.dao.services.bigVault;

import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.BatchBuilderSearchResponseIterator;
import com.constellio.data.utils.LazyIterator;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class LazyResultsIterator<T> extends LazyIterator<T> implements SearchResponseIterator<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LazyResultsIterator.class);

	private ModifiableSolrParams solrParams;
	private RecordDao recordDao;
	private int size;
	private int intervalsLength;
	private int currentStart = -1;
	private int currentBatchIndex = 0;
	private List<RecordDTO> currentBatch;
	private long currentNumFound = -1;
	protected String lastId = null;
	private boolean ascendingSort;
	private String queryName;

	public LazyResultsIterator(RecordDao recordDao, SolrParams solrParams, int intervalsLength, boolean ascendingSort,
							   String queryName) {
		this.recordDao = recordDao;
		this.solrParams = new ModifiableSolrParams(solrParams);
		this.solrParams.set("rows", intervalsLength);
		this.intervalsLength = intervalsLength;
		this.ascendingSort = ascendingSort;
		this.queryName = queryName;
	}

	@Override
	public SearchResponseIterator<List<T>> inBatches() {
		return new BatchBuilderSearchResponseIterator<T>(this, intervalsLength) {

			@Override
			public long getNumFound() {
				return LazyResultsIterator.this.getNumFound();
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
		loadIfRequired();
		if (currentBatchIndex < currentBatch.size()) {
			RecordDTO recordDTO = currentBatch.get(currentBatchIndex++);
			lastId = recordDTO.getId();
			return convert(recordDTO);
		} else {
			return null;
		}
	}

	void loadIfRequired() {
		if (currentStart == -1) {
			currentStart = 0;
			loadNextBatch();
		} else if (currentBatchIndex == intervalsLength) {
			currentStart += intervalsLength;
			loadNextBatch();
		}
	}

	void loadNextBatch() {
		ModifiableSolrParams params = new ModifiableSolrParams(this.solrParams);
		if (ascendingSort) {
			params.set("sort", "id asc");
		} else {
			params.set("sort", "id desc");
		}
		params.set("start", 0);
		if (lastId != null) {
			//params.add("start", "" + currentStart);
			if (ascendingSort) {
				params.add("fq", "id:{" + lastId + " TO *}");
			} else {
				params.add("fq", "id:{* TO " + lastId + "}");
			}
		}
		//params.set("start", currentStart);
		QueryResponseDTO responseDTO = recordDao.query(queryName, params);
		currentBatch = responseDTO.getResults();
		currentNumFound = responseDTO.getNumFound();
		currentBatchIndex = 0;

	}

	public static class LazyRecordDTOResultsIterator extends LazyResultsIterator<RecordDTO> {

		public LazyRecordDTOResultsIterator(RecordDao recordDao, SolrParams solrParams, int intervalsLength,
											boolean ascendingSort,
											String queryName) {
			super(recordDao, solrParams, intervalsLength, ascendingSort, queryName);
		}

		@Override
		public RecordDTO convert(RecordDTO recordDTO) {
			return recordDTO;
		}
	}
}
