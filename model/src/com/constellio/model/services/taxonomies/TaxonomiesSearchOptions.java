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
package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;

public class TaxonomiesSearchOptions {

	ReturnedMetadatasFilter returnedMetadatasFilter = ReturnedMetadatasFilter.idVersionSchemaTitlePath();
	private int rows = 100;
	private int startRow = 0;
	private StatusFilter includeStatus = StatusFilter.ACTIVES;
	private String requiredAccess = Role.READ;

	public TaxonomiesSearchOptions() {
		super();
	}

	public TaxonomiesSearchOptions(int rows, int startRow, StatusFilter includeStatus) {
		super();
		this.rows = rows;
		this.startRow = startRow;
		setIncludeStatus(includeStatus);
	}

	public TaxonomiesSearchOptions(TaxonomiesSearchOptions cloned) {
		super();
		this.rows = cloned.rows;
		this.startRow = cloned.startRow;
		this.includeStatus = cloned.includeStatus;
		this.returnedMetadatasFilter = cloned.returnedMetadatasFilter;
	}

	public TaxonomiesSearchOptions(StatusFilter includeLogicallyDeleted) {
		super();
		this.includeStatus = includeLogicallyDeleted;
	}

	public int getRows() {
		return rows;
	}

	public TaxonomiesSearchOptions setRows(int rows) {
		this.rows = rows;
		return this;
	}

	public int getStartRow() {
		return startRow;
	}

	public TaxonomiesSearchOptions setStartRow(int startRow) {
		this.startRow = startRow;
		return this;
	}

	public String getRequiredAccess() {
		return requiredAccess;
	}

	public TaxonomiesSearchOptions setRequiredAccess(String requiredAccess) {
		this.requiredAccess = requiredAccess;
		return this;
	}

	public StatusFilter getIncludeStatus() {
		return includeStatus;
	}

	public void setIncludeStatus(StatusFilter includeStatus) {
		if (includeStatus == null) {
			this.includeStatus = StatusFilter.ALL;
		} else {
			this.includeStatus = includeStatus;
		}
	}

	public ReturnedMetadatasFilter getReturnedMetadatasFilter() {
		return returnedMetadatasFilter;
	}

	public TaxonomiesSearchOptions setReturnedMetadatasFilter(ReturnedMetadatasFilter returnedMetadatasFilter) {
		this.returnedMetadatasFilter = returnedMetadatasFilter;
		return this;
	}

	public TaxonomiesSearchOptions cloneAddingReturnedField(Metadata metadata) {
		TaxonomiesSearchOptions clonedOptions = new TaxonomiesSearchOptions(this);
		clonedOptions.setReturnedMetadatasFilter(returnedMetadatasFilter.withIncludedField(metadata));
		clonedOptions.setIncludeStatus(includeStatus);
		clonedOptions.setRequiredAccess(requiredAccess);
		clonedOptions.setRows(rows);
		clonedOptions.setStartRow(startRow);
		return clonedOptions;

	}
}
