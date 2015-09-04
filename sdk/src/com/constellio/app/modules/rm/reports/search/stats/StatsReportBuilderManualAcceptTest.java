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
package com.constellio.app.modules.rm.reports.search.stats;

import com.constellio.app.modules.rm.reports.builders.search.stats.StatsReportBuilder;
import com.constellio.app.modules.rm.reports.model.search.stats.StatsReportModel;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class StatsReportBuilderManualAcceptTest extends ReportBuilderTestFramework {

	StatsReportModel model;

	@Before
	public void setUp()
			throws Exception {
	}

	@Test
	public void whenBuildEmptyReportThenOk() {
		model = new StatsReportModel();
		build(new StatsReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildDetailedClassificationPlanReportThenOk() {
		model = configStats();
		build(new StatsReportBuilder(model,
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
