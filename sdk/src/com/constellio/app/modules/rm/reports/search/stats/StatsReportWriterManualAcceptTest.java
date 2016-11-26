package com.constellio.app.modules.rm.reports.search.stats;

import com.constellio.app.modules.rm.reports.builders.search.stats.StatsReportWriter;
import com.constellio.app.modules.rm.reports.model.search.stats.StatsReportModel;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class StatsReportWriterManualAcceptTest extends ReportBuilderTestFramework {

	StatsReportModel model;

	@Before
	public void setUp()
			throws Exception {
	}

	@Test
	public void whenBuildEmptyReportThenOk() {
		model = new StatsReportModel();
		build(new StatsReportWriter(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildDetailedClassificationPlanReportThenOk() {
		model = configStats();
		build(new StatsReportWriter(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	private StatsReportModel configStats() {

		StatsReportModel model = new StatsReportModel();

		Map<String, Object> stats= new HashMap<>();
		stats.put("min", 50l);
		stats.put("max", 100l);
		stats.put("sum", 150l);
		stats.put("missing", 2l);
		stats.put("count", 3l);
		model.setStats(stats);

		return model;
	}

}
