package com.constellio.app.services.importExport.systemStateExport;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class SystemStateExportParams {

	List<String> onlyExportContentOfRecords = null;

	@Getter
	private boolean useWeeklyExport;

	public SystemStateExportParams setUseWeeklyExport(boolean useWeeklyExport) {
		this.useWeeklyExport = useWeeklyExport;
		return this;
	}

	public boolean isExportAllContent() {
		return onlyExportContentOfRecords == null;
	}


	public SystemStateExportParams setExportAllContent() {
		this.onlyExportContentOfRecords = null;
		return this;
	}

	public SystemStateExportParams setExportNoContent() {
		this.onlyExportContentOfRecords = new ArrayList<>();
		return this;
	}

	public List<String> getOnlyExportContentOfRecords() {
		return onlyExportContentOfRecords;
	}

	public SystemStateExportParams setOnlyExportContentOfRecords(List<String> onlyExportContentOfRecords) {
		this.onlyExportContentOfRecords = onlyExportContentOfRecords;
		return this;
	}

}
