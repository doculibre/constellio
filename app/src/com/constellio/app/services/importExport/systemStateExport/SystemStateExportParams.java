package com.constellio.app.services.importExport.systemStateExport;

import java.util.ArrayList;
import java.util.List;

public class SystemStateExportParams {

	List<String> onlyExportContentOfRecords = null;
	boolean exportPluginJars = true;

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

	public boolean isExportPluginJars() {
		return exportPluginJars;
	}

	public SystemStateExportParams setExportPluginJars(boolean exportPluginJars) {
		this.exportPluginJars = exportPluginJars;
		return this;
	}
}
