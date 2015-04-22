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
package com.constellio.app.modules.rm.services.decommissioning;

import java.io.Serializable;
import java.util.List;

public class DecommissioningListParams implements Serializable {
	private String title;
	private String description;
	private List<String> selectedFolderIds;
	private String filingSpace;
	private String administrativeUnit;
	private SearchType searchType;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getSelectedFolderIds() {
		return selectedFolderIds;
	}

	public void setSelectedFolderIds(List<String> selectedFolderIds) {
		this.selectedFolderIds = selectedFolderIds;
	}

	public String getFilingSpace() {
		return filingSpace;
	}

	public void setFilingSpace(String filingSpace) {
		this.filingSpace = filingSpace;
	}

	public String getAdministrativeUnit() {
		return administrativeUnit;
	}

	public void setAdministrativeUnit(String administrativeUnit) {
		this.administrativeUnit = administrativeUnit;
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}
}
