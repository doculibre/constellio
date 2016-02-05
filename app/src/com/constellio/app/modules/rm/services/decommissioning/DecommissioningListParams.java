package com.constellio.app.modules.rm.services.decommissioning;

import java.io.Serializable;
import java.util.List;

public class DecommissioningListParams implements Serializable {
	private String title;
	private String description;
	private List<String> selectedRecordIds;
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

	public List<String> getSelectedRecordIds() {
		return selectedRecordIds;
	}

	public void setSelectedRecordIds(List<String> selectedRecordIds) {
		this.selectedRecordIds = selectedRecordIds;
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
