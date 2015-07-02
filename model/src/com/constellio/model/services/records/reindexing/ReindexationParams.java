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
package com.constellio.model.services.records.reindexing;

import java.util.Collections;
import java.util.List;

public class ReindexationParams {

	private ReindexationMode reindexationMode;
	private int batchSize = 100;
	private List<String> reindexedSchemaTypes = Collections.emptyList();

	public ReindexationParams(ReindexationMode reindexationMode) {
		this.reindexationMode = reindexationMode;
	}

	public ReindexationMode getReindexationMode() {
		return reindexationMode;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public ReindexationParams setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public List<String> getReindexedSchemaTypes() {
		return reindexedSchemaTypes;
	}

	public static ReindexationParams recalculateSchemaTypes(List<String> schemaTypes) {
		ReindexationParams params = new ReindexationParams(ReindexationMode.RECALCULATE);
		params.reindexedSchemaTypes = Collections.unmodifiableList(schemaTypes);
		return params;
	}

}
