package com.constellio.app.services.importExport.systemStateExport;

import java.util.ArrayList;
import java.util.List;

public class PartialSystemStateExportParams {

	List<String> ids = null;

	int levels = 3;

	boolean exportPluginJars = true;

	public PartialSystemStateExportParams() {
	}

	public List<String> getIds() {
		return ids;
	}

	public PartialSystemStateExportParams setIds(List<String> ids) {
		this.ids = ids;
		return this;
	}

	public int getLevels() {
		return levels;
	}

	public PartialSystemStateExportParams setLevels(int levels) {
		this.levels = levels;
		return this;
	}

	public boolean isExportPluginJars() {
		return exportPluginJars;
	}

	public PartialSystemStateExportParams setExportPluginJars(boolean exportPluginJars) {
		this.exportPluginJars = exportPluginJars;
		return this;
	}
}
