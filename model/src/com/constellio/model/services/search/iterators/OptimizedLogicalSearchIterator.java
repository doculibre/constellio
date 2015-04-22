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
package com.constellio.model.services.search.iterators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.IsInCriterion;

public abstract class OptimizedLogicalSearchIterator<T> extends LazyIterator<T> {

	private Iterator<Record> currentRecordsIterator = Collections.emptyIterator();
	private Iterator<LogicalSearchQuery> queries;
	private SearchServices searchServices;
	private int batchSize;

	public OptimizedLogicalSearchIterator(LogicalSearchQuery query, SearchServices searchServices, int batchSize) {
		this.searchServices = searchServices;
		this.batchSize = batchSize;

		if (query.getCondition() instanceof DataStoreFieldLogicalSearchCondition) {
			DataStoreFieldLogicalSearchCondition metadataCondition = (DataStoreFieldLogicalSearchCondition) query.getCondition();
			if (metadataCondition.getValueCondition() instanceof IsInCriterion) {
				List<LogicalSearchQuery> queriesList = new ArrayList<>();
				IsInCriterion isInCriterion = (IsInCriterion) metadataCondition.getValueCondition();
				for (Object value : isInCriterion.getValues()) {

					LogicalSearchQuery aQuery = new LogicalSearchQuery(query);
					DataStoreFieldLogicalSearchCondition newCondition = metadataCondition
							.replacingValueConditionWith(LogicalSearchQueryOperators.equal(value));
					aQuery.setCondition(newCondition);
					queriesList.add(aQuery);
				}
				queries = queriesList.iterator();

			} else {
				queries = Arrays.asList(new LogicalSearchQuery(query)).iterator();
			}

		} else {
			queries = Arrays.asList(new LogicalSearchQuery(query)).iterator();
		}
	}

	@Override
	protected T getNextOrNull() {
		if (!currentRecordsIterator.hasNext() && queries.hasNext()) {
			currentRecordsIterator = searchServices.recordsIterator(queries.next(), batchSize);
			return getNextOrNull();
		} else if (currentRecordsIterator.hasNext()) {
			return convert(currentRecordsIterator.next());
		} else {
			return null;
		}
	}

	protected abstract T convert(Record record);

}
