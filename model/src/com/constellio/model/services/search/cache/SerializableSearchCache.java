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
package com.constellio.model.services.search.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySignature;

public class SerializableSearchCache implements Serializable {

	Map<String, Map<String, List<String>>> highlights = new HashMap<>();
	private LogicalSearchQuerySignature previousQuery;

	private List<String> resultIds = new ArrayList<>();
	private int size = -1;

	public String getCachedId(int index) {
		if (index < resultIds.size()) {
			return resultIds.get(index);
		}
		return null;
	}

	public void setRecordId(int i, String id) {
		while (resultIds.size() <= i) {
			resultIds.add(null);
		}
		resultIds.set(i, id);
	}

	public void setRecordHighLighting(String id, Map<String, List<String>> recordHighlighting) {
		highlights.put(id, recordHighlighting);
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void clear() {
		resultIds = new ArrayList<>();
		highlights.clear();
		size = -1;
	}

	public void initializeFor(LogicalSearchQuery query) {
		LogicalSearchQuerySignature querySignature = LogicalSearchQuerySignature.signature(query);

		if (previousQuery == null || !querySignature.equals(previousQuery)) {
			previousQuery = querySignature;
			clear();
		}
	}

	public Map<String, Map<String, List<String>>> getHighlightingMap() {
		return highlights;
	}
}
