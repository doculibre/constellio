package com.constellio.app.modules.rm.reports.model.search.stats;

import java.util.Map;

public class StatsReportModel {
	private Map<String, Object> stats;

	public Map<String, Object> getStats() {
		return stats;
	}

	public StatsReportModel setStats(
			Map<String, Object> stats) {
		this.stats = stats;
		return this;
	}

}
