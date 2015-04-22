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
package com.constellio.data.dao.services.bigVault;

import java.util.List;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.LazyIterator;

public abstract class LazyResultsIterator<T> extends LazyIterator<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LazyResultsIterator.class);

	private ModifiableSolrParams solrParams;
	private RecordDao recordDao;
	private int size;
	private int intervalsLength;
	private int currentStart = -1;
	private int currentBatchIndex = 0;
	private List<RecordDTO> currentBatch;
	private long lastNumFound;
	private String lastId = null;

	public LazyResultsIterator(RecordDao recordDao, SolrParams solrParams, int intervalsLength) {
		this.recordDao = recordDao;
		this.solrParams = new ModifiableSolrParams(solrParams);
		this.solrParams.set("rows", intervalsLength);
		this.intervalsLength = intervalsLength;
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
		params.set("sort", "id asc");
		if (lastId != null) {
			params.add("start", "" + currentStart);
			params.add("fq", "id:{" + lastId + " TO *}");
		}
		//params.set("start", currentStart);
		QueryResponseDTO responseDTO = recordDao.query(params);
		currentBatch = responseDTO.getResults();
		lastNumFound = responseDTO.getNumFound();
		currentBatchIndex = 0;

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
