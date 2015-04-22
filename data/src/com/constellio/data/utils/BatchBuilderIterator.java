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
package com.constellio.data.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BatchBuilderIterator<T> extends LazyIterator<List<T>> {

	private Iterator<T> nestedIterator;

	private int batchSize;

	public BatchBuilderIterator(Iterator<T> nestedIterator, int batchSize) {
		this.nestedIterator = nestedIterator;
		this.batchSize = batchSize;
	}

	@Override
	protected List<T> getNextOrNull() {
		List<T> batch = new ArrayList<>();

		while (nestedIterator.hasNext() && batch.size() < batchSize) {
			batch.add(nestedIterator.next());
		}

		return batch.isEmpty() ? null : batch;
	}
}
