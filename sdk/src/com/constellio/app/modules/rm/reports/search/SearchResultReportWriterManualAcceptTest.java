package com.constellio.app.modules.rm.reports.search;

import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriter;
import com.constellio.app.modules.rm.reports.model.search.SearchResultReportModel;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchResultReportWriterManualAcceptTest extends ReportBuilderTestFramework {
	SearchResultReportModel model;

	@Before
	public void setUp()
			throws Exception {
	}

	@Test
	public void whenBuildEmptyReportThenOk() {
		model = new SearchResultReportModel(getAppLayerFactory().getModelLayerFactory());
		write(new SearchResultReportWriter(model,
				getModelLayerFactory().getFoldersLocator(), new Locale("fr")));
	}

	@Test
	public void whenBuildReportWithResultsThenOk() {
		model = configModel();
		write(new SearchResultReportWriter(model,
				getModelLayerFactory().getFoldersLocator(), new Locale("fr")));
	}

	private SearchResultReportModel configModel() {

		SearchResultReportModel model = new SearchResultReportModel(getAppLayerFactory().getModelLayerFactory());
		model.addTitle("title1");
		model.addTitle("title2");
		model.addTitle("number");
		model.addTitle("date");

		List<Object> line1 = new ArrayList<>();
		line1.add("cell11");
		line1.add("cell12");
		line1.add(new Integer(1));
		line1.add(null);
		model.addLine(line1);

		List<Object> line2 = new ArrayList<>();
		line2.add("cell21");
		line2.add(null);
		line2.add(new Double(2));
		line2.add(new Date());
		model.addLine(line2);

		List<Object> line3 = new ArrayList<>();
		line3.add("cell31");
		line3.add(null);
		line3.add(new Float(3));
		line3.add(new Date());
		model.addLine(line3);

		return model;
	}
}
